package com.tournament.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tournament.pairing.PairingStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Tournament {

    private final UUID tournamentId;
    private final String name;
    private final List<Player> players;
    private final List<Round> rounds = new ArrayList<>();
    private final TournamentType type;

    private TournamentState state = TournamentState.CREATED;

    @JsonCreator
    public Tournament(
            @JsonProperty("tournamentId") UUID tournamentId,
            @JsonProperty("name") String name,
            @JsonProperty("players") List<Player> players,
            @JsonProperty("rounds") List<Round> rounds,
            @JsonProperty("type") TournamentType type,
            @JsonProperty("state") TournamentState state) {
        if (tournamentId == null) {
            throw new IllegalArgumentException("Tournament ID cannot be null");
        }
        if (players == null) {
            throw new IllegalArgumentException("Players cannot be null");
        }
        this.tournamentId = tournamentId;
        this.name = name;
        this.players = new ArrayList<>(players);
        if (rounds != null) {
            this.rounds.addAll(rounds);
        }
        this.type = type;
        this.state = state;
    }

    public Tournament(List<Player> players, TournamentType type) {
        this("Tournament", players, type);
    }

    public Tournament(String name, List<Player> players, TournamentType type) {
        this(UUID.randomUUID(), name, players, type);
    }

    public Tournament(UUID tournamentId, String name, List<Player> players, TournamentType type) {
        this(tournamentId, name, players, new ArrayList<>(), type, TournamentState.CREATED);
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

    public UUID getTournamentId() {
        return tournamentId;
    }

    public String getName() {
        return name;
    }

    public List<Player> getPlayers() {
        return List.copyOf(players);
    }

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

    public List<Round> getRounds() {
        return List.copyOf(rounds);
    }

    public int getRoundCount() {
        return rounds.size();
    }

    public TournamentType getType() {
        return type;
    }

    public Round getCurrentRound() {
        if (rounds.isEmpty()) {
            return null;
        }
        return rounds.getLast();
    }

    public void addRound(Round round) {
        if (round == null) {
            throw new IllegalArgumentException("Round cannot be null");
        }

        rounds.add(round);
    }

    public boolean isStarted() {
        return state != TournamentState.CREATED;
    }

    public TournamentState getState() {
        return state;
    }

    public void start() {
        if (state != TournamentState.CREATED) {
            throw new IllegalStateException("Tournament already started");
        }
        state = TournamentState.STARTED;
    }

    public void finish() {
        if (state == TournamentState.CREATED) {
            throw new IllegalStateException("Tournament not started");
        }
        state = TournamentState.FINISHED;
    }
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

    @Override
    public String toString() {
        return String.format("%s (%s) [%s, %s, players: %d, rounds: %d]", 
            name, tournamentId, type, state, players.size(), rounds.size());
    }

}
