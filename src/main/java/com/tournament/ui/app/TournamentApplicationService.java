package com.tournament.ui.app;

import com.tournament.model.Match;
import com.tournament.model.Player;
import com.tournament.model.PlayerDirectory;
import com.tournament.model.Round;
import com.tournament.model.Tournament;
import com.tournament.model.TournamentState;
import com.tournament.model.TournamentType;
import com.tournament.persistence.PlayerDirectoryRepository;
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
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TournamentApplicationService {
    private final TournamentService tournamentService;
    private final RankingCalculator rankingCalculator;
    private final TournamentRepository repository;
    private final PlayerDirectoryRepository playerDirectoryRepository;
    private final List<Tournament> tournaments = new ArrayList<>();
    private PlayerDirectory playerDirectory;

    public TournamentApplicationService() {
        this(new TournamentService(), new RankingCalculator(), new TournamentRepository(), new PlayerDirectoryRepository(), new PlayerDirectory());
    }

    public TournamentApplicationService(TournamentService tournamentService, RankingCalculator rankingCalculator, TournamentRepository repository) {
        this(tournamentService, rankingCalculator, repository, new PlayerDirectoryRepository(), new PlayerDirectory());
    }

    public TournamentApplicationService(
            TournamentService tournamentService,
            RankingCalculator rankingCalculator,
            TournamentRepository repository,
            PlayerDirectoryRepository playerDirectoryRepository,
            PlayerDirectory playerDirectory) {
        this.tournamentService = tournamentService;
        this.rankingCalculator = rankingCalculator;
        this.repository = repository;
        this.playerDirectoryRepository = playerDirectoryRepository;
        this.playerDirectory = playerDirectory;
    }

    public void loadSavedTournaments() {
        loadSavedData();
    }

    public void loadSavedData() {
        try {
            List<Tournament> loaded = repository.load();
            tournaments.clear();
            tournaments.addAll(loaded);

            playerDirectory = playerDirectoryRepository.load();
            boolean changed = importTournamentPlayers();
            if (changed) {
                playerDirectoryRepository.save(playerDirectory);
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new UiActionException("Failed to load saved data: " + e.getMessage());
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

    public List<PlayerRow> getPlayers() {
        return getPlayerBase().stream()
                .sorted(Comparator.comparing(Player::name, String.CASE_INSENSITIVE_ORDER))
                .map(player -> new PlayerRow(player.playerId(), player.name()))
                .toList();
    }

    public List<Player> getPlayerBase() {
        return playerDirectory.getPlayers().stream()
                .sorted(Comparator.comparing(Player::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public TournamentDetails getDetails(Tournament tournament) {
        return toDetails(requireTournament(tournament));
    }

    public Player createPlayer(String name) {
        try {
            Player player = new Player(name);
            playerDirectory.addPlayer(player);
            savePlayerDirectory();
            return player;
        } catch (IllegalArgumentException e) {
            throw new UiActionException(e.getMessage());
        }
    }

    public Player renamePlayer(Player player, String newName) {
        if (player == null) {
            throw new UiActionException("Player cannot be null");
        }
        try {
            Player renamed = playerDirectory.renamePlayer(player.playerId(), newName);
            savePlayerDirectory();
            return renamed;
        } catch (IllegalArgumentException e) {
            throw new UiActionException(e.getMessage());
        }
    }

    public Tournament createTournament(String name, TournamentType type, List<String> playerNames) {
        try {
            validateTournamentInput(name, type, playerNames);
            List<Player> players = playerNames.stream()
                    .map(String::trim)
                    .map(playerDirectory::resolveOrCreate)
                    .toList();
            Tournament tournament = tournamentService.createTournament(name.trim(), players, type);
            tournaments.add(tournament);
            savePlayerDirectory();
            return tournament;
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new UiActionException(e.getMessage());
        }
    }

    public Tournament createTournamentWithPlayers(String name, TournamentType type, List<Player> players) {
        try {
            validateTournamentPlayersInput(name, type, players);
            boolean changed = false;
            for (Player player : players) {
                changed |= addPlayerToDirectoryIfMissing(player);
            }
            if (changed) {
                savePlayerDirectory();
            }
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
            Player player = playerDirectory.resolveOrCreate(playerName.trim());
            tournamentService.addPlayer(tournament, player);
            savePlayerDirectory();
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new UiActionException(e.getMessage());
        }
    }

    public void addPlayer(Tournament tournament, Player player) {
        addPlayers(tournament, List.of(player));
    }

    public void addPlayers(Tournament tournament, List<Player> players) {
        tournament = requireTournament(tournament);
        try {
            validatePlayersForTournament(tournament, players);
            boolean changed = false;
            for (Player player : players) {
                changed |= addPlayerToDirectoryIfMissing(player);
                tournamentService.addPlayer(tournament, player);
            }
            if (changed) {
                savePlayerDirectory();
            }
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

    private void validateTournamentPlayersInput(String name, TournamentType type, List<Player> players) {
        if (name == null || name.isBlank()) {
            throw new UiActionException("Tournament name cannot be empty");
        }
        if (type == null) {
            throw new UiActionException("Tournament type cannot be null");
        }
        if (players == null || players.size() < 2) {
            throw new UiActionException("Need at least 2 players");
        }

        Set<Player> uniquePlayers = new LinkedHashSet<>();
        Set<String> normalizedNames = new LinkedHashSet<>();
        for (Player player : players) {
            if (player == null) {
                throw new UiActionException("Player cannot be null");
            }
            if (!uniquePlayers.add(player)) {
                throw new UiActionException("Player already exists in tournament");
            }
            String normalizedName = player.name().trim().toLowerCase(Locale.ROOT);
            if (!normalizedNames.add(normalizedName)) {
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

    private void validatePlayersForTournament(Tournament tournament, List<Player> playersToAdd) {
        if (playersToAdd == null || playersToAdd.isEmpty()) {
            throw new UiActionException("Select at least one player");
        }

        Set<Player> uniquePlayers = new LinkedHashSet<>(tournament.getPlayers());
        Set<String> normalizedNames = tournament.getPlayers().stream()
                .map(Player::name)
                .map(name -> name.toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        for (Player player : playersToAdd) {
            if (player == null) {
                throw new UiActionException("Player cannot be null");
            }
            if (!uniquePlayers.add(player)) {
                throw new UiActionException("Player already exists in tournament");
            }
            String normalizedName = player.name().trim().toLowerCase(Locale.ROOT);
            if (!normalizedNames.add(normalizedName)) {
                throw new UiActionException("Player names must be unique");
            }
        }
    }

    private boolean importTournamentPlayers() {
        boolean changed = false;
        for (Tournament tournament : tournaments) {
            for (Player player : tournament.getPlayers()) {
                if (!player.equals(Player.BYE)) {
                    changed |= addPlayerToDirectoryIfMissing(player);
                }
            }
        }
        return changed;
    }

    private boolean addPlayerToDirectoryIfMissing(Player player) {
        if (playerDirectory.findById(player.playerId()).isPresent()) {
            return false;
        }
        if (playerDirectory.findByName(player.name()).isPresent()) {
            return false;
        }
        playerDirectory.addPlayer(player);
        return true;
    }

    private void savePlayerDirectory() {
        try {
            playerDirectoryRepository.save(playerDirectory);
        } catch (IOException e) {
            throw new UiActionException("Failed to save player directory: " + e.getMessage());
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
