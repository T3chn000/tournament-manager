package com.tournament.model;

import com.tournament.pairing.PairingStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate root for a tournament, including players, rounds, type and lifecycle state.
 *
 * <p>The tournament delegates pairing generation to its {@link TournamentType}
 * and stores generated rounds in order.</p>
 */
public class Tournament {
    private final UUID tournamentId;
    private final String name;
    private final List<Player> players;
    private final List<Round> rounds = new ArrayList<>();
    private final TournamentType type;

    private TournamentState state = TournamentState.CREATED;

    /**
     * Creates a tournament from stored data.
     *
     * @param tournamentId stable tournament identifier
     * @param name display name
     * @param players players registered in the tournament
     * @param rounds previously generated rounds
     * @param type tournament format
     * @param state current lifecycle state
     */
    public Tournament(
            UUID tournamentId,
            String name,
            List<Player> players,
            List<Round> rounds,
            TournamentType type,
            TournamentState state) {
        validateTournament(tournamentId, name, players, type);

        this.tournamentId = tournamentId;
        this.name = name;
        this.players = new ArrayList<>(players);
        if (rounds != null) {
            this.rounds.addAll(rounds);
        }
        this.type = type;
        this.state = state == null ? TournamentState.CREATED : state;
    }

    /**
     * Creates a tournament with the default name.
     *
     * @param players initial players
     * @param type tournament format
     */
    public Tournament(List<Player> players, TournamentType type) {
        this("Tournament", players, type);
    }

    /**
     * Creates a named tournament with a generated identifier.
     *
     * @param name tournament name
     * @param players initial players
     * @param type tournament format
     */
    public Tournament(String name, List<Player> players, TournamentType type) {
        this(UUID.randomUUID(), name, players, type);
    }

    /**
     * Creates a tournament with an explicit identifier.
     *
     * @param tournamentId stable tournament identifier
     * @param name tournament name
     * @param players initial players
     * @param type tournament format
     */
    public Tournament(UUID tournamentId, String name, List<Player> players, TournamentType type) {
        validateTournament(tournamentId, name, players, type);

        this.tournamentId = tournamentId;
        this.name = name;
        this.players = new ArrayList<>(players);
        this.type = type;
    }

    /**
     * Returns the stable tournament identifier.
     *
     * @return tournament identifier
     */
    public UUID getTournamentId() {
        return tournamentId;
    }

    /**
     * Returns the tournament display name.
     *
     * @return tournament name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns registered players.
     *
     * @return immutable player list
     */
    public List<Player> getPlayers() {
        return List.copyOf(players);
    }

    /**
     * Adds a player before the tournament starts.
     *
     * @param player player to register
     */
    public void addPlayer(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        if (state != TournamentState.CREATED) {
            throw new IllegalStateException("Cannot add players after tournament start");
        }
        if (players.contains(player)) {
            throw new IllegalArgumentException("Duplicate player detected");
        }

        players.add(player);
    }

    /**
     * Returns generated rounds.
     *
     * @return immutable round list
     */
    public List<Round> getRounds() {
        return List.copyOf(rounds);
    }

    /**
     * Returns the number of generated rounds.
     *
     * @return round count
     */
    public int getRoundCount() {
        return rounds.size();
    }

    /**
     * Returns the tournament format.
     *
     * @return tournament type
     */
    public TournamentType getType() {
        return type;
    }

    /**
     * Returns the latest generated round.
     *
     * @return current round, or {@code null} when no round has been generated
     */
    public Round getCurrentRound() {
        if (rounds.isEmpty()) {
            return null;
        }
        return rounds.getLast();
    }

    /**
     * Adds a round directly to the tournament.
     *
     * <p>This is mainly used for persistence and tests; normal application
     * flow should call {@link #generateNextRound()}.</p>
     *
     * @param round round to append
     */
    public void addRound(Round round) {
        if (round == null) {
            throw new IllegalArgumentException("Round cannot be null");
        }

        rounds.add(round);
    }

    /**
     * Checks whether the tournament has moved past creation.
     *
     * @return {@code true} when the tournament is started or finished
     */
    public boolean isStarted() {
        return state != TournamentState.CREATED;
    }

    /**
     * Returns the current lifecycle state.
     *
     * @return tournament state
     */
    public TournamentState getState() {
        return state;
    }

    /**
     * Moves the tournament from created to started state.
     */
    public void start() {
        if (state != TournamentState.CREATED) {
            throw new IllegalStateException("Tournament already started");
        }
        state = TournamentState.STARTED;
    }

    /**
     * Marks a started tournament as finished.
     */
    public void finish() {
        if (state == TournamentState.CREATED) {
            throw new IllegalStateException("Tournament not started");
        }
        state = TournamentState.FINISHED;
    }

    /**
     * Generates and stores the next round according to the tournament type.
     *
     * @return generated round
     */
    public Round generateNextRound() {
        if (state == TournamentState.CREATED) {
            throw new IllegalStateException("Tournament not started");
        }
        if (state == TournamentState.FINISHED) {
            throw new IllegalStateException("Tournament finished");
        }

        PairingStrategy strategy = type.createStrategy();
        Round round = strategy.generateNextRound(this);

        rounds.add(round);
        return round;
    }

    /**
     * Returns a compact textual representation of the tournament.
     *
     * @return tournament summary
     */
    @Override
    public String toString() {
        return String.format("%s (%s) [%s, %s, players: %d, rounds: %d]", 
            name, tournamentId, type, state, players.size(), rounds.size());
    }

    /**
     * Validates the minimum data needed for a usable tournament aggregate.
     */
    private static void validateTournament(UUID tournamentId, String name, List<Player> players, TournamentType type) {
        if (tournamentId == null) {
            throw new IllegalArgumentException("Tournament ID cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tournament name cannot be empty");
        }
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("Players cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Tournament type cannot be null");
        }
    }
}
