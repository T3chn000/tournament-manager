package com.tournament.model;

import com.tournament.pairing.PairingStrategy;

import java.util.ArrayList;
import java.util.List;

public class Tournament {

    private final String name;
    private final List<Player> players;
    private final List<Round> rounds = new ArrayList<>();
    private final TournamentType type;

    private TournamentState state = TournamentState.CREATED;

    public Tournament(List<Player> players, TournamentType type) {
        this("Tournament", players, type);
    }

    public Tournament(String name, List<Player> players, TournamentType type) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tournament name cannot be empty");
        }
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("Players cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Tournament type cannot be null");
        }

        this.name = name;
        this.players = new ArrayList<>(players);
        this.type = type;
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
        return "%s [%s, %s, players: %d, rounds: %d]"
                .formatted(name, type, state, players.size(), rounds.size());
    }

}
