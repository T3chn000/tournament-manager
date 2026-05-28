package com.tournament.ui.app;

import com.tournament.model.Match;
import com.tournament.model.Player;
import com.tournament.model.Round;
import com.tournament.model.Tournament;
import com.tournament.model.TournamentState;
import com.tournament.model.TournamentType;
import com.tournament.persistence.TournamentRepository;
import com.tournament.service.TournamentService;
import com.tournament.ui.viewmodel.MatchView;
import com.tournament.ui.viewmodel.PlayerRow;
import com.tournament.ui.viewmodel.RankingRow;
import com.tournament.ui.viewmodel.RoundView;
import com.tournament.ui.viewmodel.TournamentDetails;
import com.tournament.ui.viewmodel.TournamentSummary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TournamentApplicationService {
    private final TournamentService tournamentService;
    private final RankingCalculator rankingCalculator;
    private final TournamentRepository repository;
    private final List<Tournament> tournaments = new ArrayList<>();

    public TournamentApplicationService() {
        this(new TournamentService(), new RankingCalculator(), new TournamentRepository());
    }

    public TournamentApplicationService(TournamentService tournamentService, RankingCalculator rankingCalculator, TournamentRepository repository) {
        this.tournamentService = tournamentService;
        this.rankingCalculator = rankingCalculator;
        this.repository = repository;
    }

    public void loadSavedTournaments() {
        try {
            List<Tournament> loaded = repository.load();
            tournaments.clear();
            tournaments.addAll(loaded);
        } catch (IOException e) {
            throw new UiActionException("Failed to load saved tournaments: " + e.getMessage());
        }
    }

    public void saveTournament(Tournament tournament) {
        tournament = requireTournament(tournament);
        try {
            repository.save(tournament);
        } catch (IOException e) {
            throw new UiActionException("Failed to save tournament: " + e.getMessage());
        }
    }

    public List<TournamentSummary> getTournaments() {
        return tournaments.stream()
                .map(this::toSummary)
                .toList();
    }

    public TournamentDetails getDetails(Tournament tournament) {
        return toDetails(requireTournament(tournament));
    }

    public Tournament createTournament(String name, TournamentType type, List<String> playerNames) {
        try {
            validateTournamentInput(name, type, playerNames);
            List<Player> players = playerNames.stream()
                    .map(String::trim)
                    .map(Player::new)
                    .toList();
            Tournament tournament = tournamentService.createTournament(name.trim(), players, type);
            tournaments.add(tournament);
            return tournament;
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new UiActionException(e.getMessage());
        }
    }

    public void deleteTournament(Tournament tournament) {
        requireTournament(tournament);
        tournaments.remove(tournament);
        try {
            repository.delete(tournament.getTournamentId());
        } catch (IOException e) {
            throw new UiActionException("Failed to delete tournament file: " + e.getMessage());
        }
    }

    public void addPlayer(Tournament tournament, String playerName) {
        tournament = requireTournament(tournament);
        try {
            validatePlayerName(tournament, playerName);
            tournamentService.addPlayer(tournament, new Player(playerName.trim()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new UiActionException(e.getMessage());
        }
    }

    public void startTournament(Tournament tournament) {
        tournament = requireTournament(tournament);
        try {
            tournamentService.startTournament(tournament);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new UiActionException(e.getMessage());
        }
    }

    public RoundView generateNextRound(Tournament tournament) {
        tournament = requireTournament(tournament);
        try {
            return toRoundView(tournamentService.generateNextRound(tournament), tournament);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new UiActionException(e.getMessage());
        }
    }

    public void simulateCurrentRound(Tournament tournament) {
        tournament = requireTournament(tournament);
        Round currentRound = tournament.getCurrentRound();
        if (currentRound == null) {
            throw new UiActionException("No round to simulate");
        }

        try {
            tournamentService.simulateRound(tournament, currentRound);
            tournamentService.isFinished(tournament);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new UiActionException(e.getMessage());
        }
    }

    public void simulateTournament(Tournament tournament) {
        tournament = requireTournament(tournament);
        if (tournament.getState() == TournamentState.CREATED) {
            throw new UiActionException("Start tournament first");
        }

        try {
            while (!tournamentService.isFinished(tournament)) {
                Round currentRound = tournament.getCurrentRound();
                if (currentRound != null && !currentRound.isFinished()) {
                    tournamentService.simulateRound(tournament, currentRound);
                } else {
                    Round round = tournamentService.generateNextRound(tournament);
                    tournamentService.simulateRound(tournament, round);
                }
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new UiActionException(e.getMessage());
        }
    }

    public void updateMatchScore(Tournament tournament, int roundNumber, int matchIndex, int player1Points, int player2Points, Integer tieBreakWinnerIndex) {
        tournament = requireTournament(tournament);
        Round round = findRound(tournament, roundNumber);
        Match match = findMatch(round, matchIndex);

        if (match.isByeMatch()) {
            throw new UiActionException("Cannot set points for BYE match");
        }

        try {
            match.setPoints(player1Points, player2Points);
            if (tournament.getType() == TournamentType.KNOCKOUT && match.isDraw()) {
                if (tieBreakWinnerIndex == null) {
                    throw new UiActionException("Tie-break winner must be specified for draw in knockout tournament");
                }
                if (tieBreakWinnerIndex == 1) {
                    match.resolveDraw(match.getPlayer1());
                } else if (tieBreakWinnerIndex == 2) {
                    match.resolveDraw(match.getPlayer2());
                } else {
                    throw new UiActionException("Invalid tie-break winner index: " + tieBreakWinnerIndex);
                }
            }
            tournamentService.isFinished(tournament);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new UiActionException(e.getMessage());
        }
    }

    public List<RankingRow> getRanking(Tournament tournament) {
        return rankingCalculator.calculate(requireTournament(tournament));
    }

    private void validateTournamentInput(String name, TournamentType type, List<String> playerNames) {
        if (name == null || name.isBlank()) {
            throw new UiActionException("Tournament name cannot be empty");
        }
        if (type == null) {
            throw new UiActionException("Tournament type cannot be null");
        }
        if (playerNames == null || playerNames.size() < 2) {
            throw new UiActionException("Need at least 2 players");
        }

        Set<String> normalizedNames = new LinkedHashSet<>();
        for (String playerName : playerNames) {
            if (playerName == null || playerName.isBlank()) {
                throw new UiActionException("Player name cannot be empty");
            }
            String normalized = playerName.trim().toLowerCase(Locale.ROOT);
            if (!normalizedNames.add(normalized)) {
                throw new UiActionException("Player names must be unique");
            }
        }
    }

    private void validatePlayerName(Tournament tournament, String playerName) {
        if (playerName == null || playerName.isBlank()) {
            throw new UiActionException("Player name cannot be empty");
        }
        String normalizedName = playerName.trim().toLowerCase(Locale.ROOT);
        boolean duplicate = tournament.getPlayers().stream()
                .map(Player::name)
                .map(name -> name.toLowerCase(Locale.ROOT))
                .anyMatch(normalizedName::equals);
        if (duplicate) {
            throw new UiActionException("Player names must be unique");
        }
    }

    private TournamentDetails toDetails(Tournament tournament) {
        return new TournamentDetails(
                tournament.getName(),
                tournament.getType(),
                tournament.getState(),
                tournament.getPlayers().stream()
                        .map(player -> new PlayerRow(player.playerId(), player.name()))
                        .toList(),
                tournament.getRounds().stream()
                        .map(round -> toRoundView(round, tournament))
                        .toList(),
                rankingCalculator.calculate(tournament)
        );
    }

    private TournamentSummary toSummary(Tournament tournament) {
        return new TournamentSummary(
                tournament,
                tournament.getName(),
                tournament.getType(),
                tournament.getState(),
                tournament.getPlayers().size(),
                tournament.getRoundCount()
        );
    }

    private RoundView toRoundView(Round round, Tournament tournament) {
        List<Match> matches = round.getMatches();
        List<MatchView> matchViews = new ArrayList<>();
        for (int i = 0; i < matches.size(); i++) {
            Match match = matches.get(i);
            String result = match.getResult() == null ? "not played" : match.getResult().name();
            String winner = match.getWinner() == null ? "-" : match.getWinner().name();
            boolean editable = tournament.getState() == TournamentState.STARTED && !match.isByeMatch();
            String score = match.getScore();
            if (match.isDraw() && match.getTieBreakWinner() != null) {
                score += " (OT)";
            }
            Integer tieBreakWinnerIndex = getTieBreakWinnerIndex(match);
            matchViews.add(new MatchView(
                    i,
                    match.getPlayer1().name(),
                    match.getPlayer2().name(),
                    score,
                    match.getPlayer1Points(),
                    match.getPlayer2Points(),
                    result,
                    winner,
                    tieBreakWinnerIndex,
                    match.isPlayed(),
                    match.isDraw(),
                    match.isByeMatch(),
                    editable
            ));
        }
        return new RoundView(round.getRoundNumber(), round.isFinished(), round.hasDraws(), List.copyOf(matchViews));
    }

    private Integer getTieBreakWinnerIndex(Match match) {
        Player tieBreakWinner = match.getTieBreakWinner();
        if (tieBreakWinner == null) {
            return null;
        }
        if (tieBreakWinner.equals(match.getPlayer1())) {
            return 1;
        }
        if (tieBreakWinner.equals(match.getPlayer2())) {
            return 2;
        }
        return null;
    }

    private Tournament requireTournament(Tournament tournament) {
        if (tournament == null || !tournaments.contains(tournament)) {
            throw new UiActionException("Tournament not found");
        }
        return tournament;
    }

    private Round findRound(Tournament tournament, int roundNumber) {
        return tournament.getRounds().stream()
                .filter(round -> round.getRoundNumber() == roundNumber)
                .findFirst()
                .orElseThrow(() -> new UiActionException("Round not found"));
    }

    private Match findMatch(Round round, int matchIndex) {
        if (matchIndex < 0 || matchIndex >= round.getMatches().size()) {
            throw new UiActionException("Match not found");
        }
        return round.getMatches().get(matchIndex);
    }
}
