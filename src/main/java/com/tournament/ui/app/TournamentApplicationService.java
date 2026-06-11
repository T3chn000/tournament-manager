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
import java.util.UUID;

/**
 * Application-level facade used by JavaFX controllers.
 *
 * <p>It coordinates domain services, repositories, player directory management
 * and conversion to view models. User-facing validation errors are exposed as
 * {@link UiActionException}.</p>
 */
public class TournamentApplicationService {
    private final TournamentService tournamentService;
    private final RankingCalculator rankingCalculator;
    private final TournamentRepository repository;
    private final PlayerDirectoryRepository playerDirectoryRepository;
    private final List<Tournament> tournaments = new ArrayList<>();
    private PlayerDirectory playerDirectory;

    /**
     * Creates the service with the default domain and persistence collaborators.
     */
    public TournamentApplicationService() {
        this(new TournamentService(), new RankingCalculator(), new TournamentRepository(), new PlayerDirectoryRepository(), new PlayerDirectory());
    }

    /**
     * Creates the service with all domain and persistence collaborators supplied.
     *
     * @param tournamentService domain tournament service
     * @param rankingCalculator ranking calculator
     * @param repository tournament repository
     * @param playerDirectoryRepository player directory repository
     * @param playerDirectory initial player directory
     */
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

    /**
     * Loads tournaments and the player directory from disk.
     */
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

    /**
     * Saves one tournament through the tournament repository.
     *
     * @param tournament tournament managed by this service
     */
    public void saveTournament(Tournament tournament) {
        tournament = requireTournament(tournament);
        try {
            repository.save(withCurrentPlayerNames(tournament));
        } catch (IOException e) {
            throw new UiActionException("Failed to save tournament: " + e.getMessage());
        }
    }

    /**
     * Returns summary rows for the tournament list.
     *
     * @return tournament summaries in the current session
     */
    public List<TournamentSummary> getTournaments() {
        return tournaments.stream()
                .map(this::toSummary)
                .toList();
    }

    /**
     * Returns sorted player rows for the global player list.
     *
     * @return player rows sorted by name
     */
    public List<PlayerRow> getPlayers() {
        return getPlayerBase().stream()
                .sorted(Comparator.comparing(Player::name, String.CASE_INSENSITIVE_ORDER))
                .map(player -> new PlayerRow(player.playerId(), player.name()))
                .toList();
    }

    /**
     * Returns the sorted player directory as domain objects.
     *
     * @return players sorted by name
     */
    public List<Player> getPlayerBase() {
        return playerDirectory.getPlayers().stream()
                .sorted(Comparator.comparing(Player::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /**
     * Returns all details needed to render a tournament.
     *
     * @param tournament tournament managed by this service
     * @return full tournament details
     */
    public TournamentDetails getDetails(Tournament tournament) {
        return toDetails(requireTournament(tournament));
    }

    /**
     * Creates and persists a player in the global player directory.
     *
     * @param name player display name
     * @return created player
     */
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

    /**
     * Renames a player in the global player directory.
     *
     * @param player player to rename
     * @param newName new display name
     * @return renamed player
     */
    public Player renamePlayer(Player player, String newName) {
        if (player == null) {
            throw new UiActionException("Player cannot be null");
        }
        try {
            Player renamed = playerDirectory.renamePlayer(player.playerId(), newName);
            savePlayerDirectory();
            saveTournamentsContaining(renamed.playerId());
            return renamed;
        } catch (IllegalArgumentException e) {
            throw new UiActionException(e.getMessage());
        }
    }

    /**
     * Creates a tournament from already selected players.
     *
     * @param name tournament name
     * @param type tournament format
     * @param players selected players
     * @return created tournament
     */
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

    /**
     * Deletes a tournament and removes its saved file.
     *
     * @param tournament tournament to delete
     */
    public void deleteTournament(Tournament tournament) {
        requireTournament(tournament);
        tournaments.remove(tournament);
        try {
            repository.delete(tournament.getTournamentId());
        } catch (IOException e) {
            throw new UiActionException("Failed to delete tournament file: " + e.getMessage());
        }
    }

    /**
     * Adds existing players to a tournament before it starts.
     *
     * @param tournament target tournament
     * @param players players to add
     */
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

    /**
     * Starts a tournament.
     *
     * @param tournament tournament to start
     */
    public void startTournament(Tournament tournament) {
        tournament = requireTournament(tournament);
        try {
            tournamentService.startTournament(tournament);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new UiActionException(e.getMessage());
        }
    }

    /**
     * Generates the next round and converts it to a view model.
     *
     * @param tournament tournament to advance
     * @return generated round view
     */
    public RoundView generateNextRound(Tournament tournament) {
        tournament = requireTournament(tournament);
        try {
            return toRoundView(tournamentService.generateNextRound(tournament), tournament);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new UiActionException(e.getMessage());
        }
    }

    /**
     * Simulates the current round of a started tournament.
     *
     * @param tournament tournament whose current round should be simulated
     */
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

    /**
     * Simulates rounds until the tournament finishes.
     *
     * @param tournament tournament to simulate
     */
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

    /**
     * Updates one match score and resolves knockout draws when required.
     *
     * @param tournament tournament containing the match
     * @param roundNumber one-based round number
     * @param matchIndex zero-based match index inside the round
     * @param player1Points points for the first player
     * @param player2Points points for the second player
     * @param tieBreakWinnerIndex one-based winner index for knockout draws, or {@code null}
     */
    public void updateMatchScore(
            Tournament tournament,
            int roundNumber,
            int matchIndex,
            int player1Points,
            int player2Points,
            Integer tieBreakWinnerIndex
    ) {
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

    /**
     * Validates tournament creation from selected player domain objects.
     */
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

    /**
     * Validates a batch of selected players before adding any of them.
     *
     * <p>The method checks the whole batch up front so a failed add does not
     * leave the tournament partially modified.</p>
     */
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

    /**
     * Adds players found in saved tournaments to the global directory when missing.
     */
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

    /**
     * Stores a player in the global directory only when neither ID nor name already exists.
     */
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

    /**
     * Persists the player directory and translates IO errors into UI-friendly errors.
     */
    private void savePlayerDirectory() {
        try {
            playerDirectoryRepository.save(playerDirectory);
        } catch (IOException e) {
            throw new UiActionException("Failed to save player directory: " + e.getMessage());
        }
    }

    /**
     * Persists tournaments that reference a renamed player using current display names.
     */
    private void saveTournamentsContaining(UUID playerId) {
        try {
            for (Tournament tournament : tournaments) {
                if (containsPlayer(tournament, playerId)) {
                    repository.save(withCurrentPlayerNames(tournament));
                }
            }
        } catch (IOException e) {
            throw new UiActionException("Failed to save renamed player in tournaments: " + e.getMessage());
        }
    }

    /**
     * Checks whether a tournament stores the given player identifier.
     */
    private boolean containsPlayer(Tournament tournament, UUID playerId) {
        return tournament.getPlayers().stream()
                .anyMatch(player -> player.playerId().equals(playerId));
    }

    /**
     * Creates a save-only copy with player names synchronized from the player directory.
     */
    private Tournament withCurrentPlayerNames(Tournament tournament) {
        List<Player> players = tournament.getPlayers().stream()
                .map(this::withCurrentPlayerName)
                .toList();
        List<Round> rounds = tournament.getRounds().stream()
                .map(this::withCurrentPlayerNames)
                .toList();
        return new Tournament(
                tournament.getTournamentId(),
                tournament.getName(),
                players,
                rounds,
                tournament.getType(),
                tournament.getState()
        );
    }

    /**
     * Creates a round copy whose matches use current player names.
     */
    private Round withCurrentPlayerNames(Round round) {
        List<Match> matches = round.getMatches().stream()
                .map(this::withCurrentPlayerNames)
                .toList();
        return new Round(round.getRoundNumber(), matches);
    }

    /**
     * Creates a match copy whose participants and tie-break winner use current player names.
     */
    private Match withCurrentPlayerNames(Match match) {
        Player tieBreakWinner = match.getTieBreakWinner() == null
                ? null
                : withCurrentPlayerName(match.getTieBreakWinner());
        return new Match(
                withCurrentPlayerName(match.getPlayer1()),
                withCurrentPlayerName(match.getPlayer2()),
                match.getPlayer1Points(),
                match.getPlayer2Points(),
                tieBreakWinner
        );
    }

    /**
     * Returns the current player-directory object for the same identifier when available.
     */
    private Player withCurrentPlayerName(Player player) {
        if (player.equals(Player.BYE)) {
            return Player.BYE;
        }
        return playerDirectory.findById(player.playerId()).orElse(player);
    }

    /**
     * Converts a tournament aggregate into the full details view model.
     */
    private TournamentDetails toDetails(Tournament tournament) {
        return new TournamentDetails(
                tournament.getName(),
                tournament.getType(),
                tournament.getState(),
                tournament.getPlayers().stream()
                        .map(player -> new PlayerRow(player.playerId(), getDisplayName(player)))
                        .toList(),
                tournament.getRounds().stream()
                        .map(round -> toRoundView(round, tournament))
                        .toList(),
                rankingCalculator.calculate(tournament).stream()
                        .map(this::withCurrentPlayerName)
                        .toList()
        );
    }

    /**
     * Converts a tournament aggregate into the compact list item view model.
     */
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

    /**
     * Converts a domain round into table-friendly match rows.
     */
    private RoundView toRoundView(Round round, Tournament tournament) {
        List<Match> matches = round.getMatches();
        List<MatchView> matchViews = new ArrayList<>();
        for (int i = 0; i < matches.size(); i++) {
            Match match = matches.get(i);
            String result = match.getResult() == null ? "not played" : match.getResult().name();
            String winner = match.getWinner() == null ? "-" : getDisplayName(match.getWinner());
            boolean editable = tournament.getState() == TournamentState.STARTED && !match.isByeMatch();
            String score = match.getScore();
            if (match.isDraw() && match.getTieBreakWinner() != null) {
                score += " (OT)";
            }
            Integer tieBreakWinnerIndex = getTieBreakWinnerIndex(match);
            matchViews.add(new MatchView(
                    i,
                    getDisplayName(match.getPlayer1()),
                    getDisplayName(match.getPlayer2()),
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

    /**
     * Replaces stale tournament player names with the current player-directory name.
     */
    private RankingRow withCurrentPlayerName(RankingRow row) {
        return new RankingRow(
                row.place(),
                row.playerId(),
                getDisplayName(row.playerId(), row.playerName()),
                row.points(),
                row.wins(),
                row.draws(),
                row.losses(),
                row.playedMatches(),
                row.byeCount()
        );
    }

    /**
     * Resolves the current display name for a player while preserving BYE labels.
     */
    private String getDisplayName(Player player) {
        if (player.equals(Player.BYE)) {
            return Player.BYE.name();
        }
        return getDisplayName(player.playerId(), player.name());
    }

    /**
     * Looks up a current player name by identifier and falls back to stored tournament data.
     */
    private String getDisplayName(UUID playerId, String fallbackName) {
        return playerDirectory.findById(playerId)
                .map(Player::name)
                .orElse(fallbackName);
    }

    /**
     * Maps a tie-break winner to the one-based selector index used by the result dialog.
     */
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

    /**
     * Ensures that UI actions operate only on tournaments managed by this service instance.
     */
    private Tournament requireTournament(Tournament tournament) {
        if (tournament == null || !tournaments.contains(tournament)) {
            throw new UiActionException("Tournament not found");
        }
        return tournament;
    }

    /**
     * Finds a round by its one-based round number.
     */
    private Round findRound(Tournament tournament, int roundNumber) {
        return tournament.getRounds().stream()
                .filter(round -> round.getRoundNumber() == roundNumber)
                .findFirst()
                .orElseThrow(() -> new UiActionException("Round not found"));
    }

    /**
     * Finds a match by its zero-based index inside a round.
     */
    private Match findMatch(Round round, int matchIndex) {
        if (matchIndex < 0 || matchIndex >= round.getMatches().size()) {
            throw new UiActionException("Match not found");
        }
        return round.getMatches().get(matchIndex);
    }
}
