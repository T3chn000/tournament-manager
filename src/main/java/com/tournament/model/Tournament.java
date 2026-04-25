package com.tournament.model;

import java.util.ArrayList;
import java.util.List;

public class Tournament {

    private final List<Player> players;
    private final List<Round> rounds = new ArrayList<>();
    private final TournamentType type;

    private boolean started = false;

    public Tournament(List<Player> players, TournamentType type) {
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("Players cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Tournament type cannot be null");
        }

        this.players = new ArrayList<>(players);
        this.type = type;
    }

    public List<Player> getPlayers() {
        return List.copyOf(players);
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
        return started;
    }

    public void start() {
        if (started) {
            throw new IllegalStateException("Tournament already started");
        }
        started = true;
    }


}