package com.tournament.service;

import com.tournament.model.*;

import java.util.List;

public class TournamentService {

    public Tournament createTournament(List<Player> players, TournamentType type) {
        return new Tournament(players, type);
    }

    public void startTournament(Tournament tournament) {
        if (tournament == null) {
            throw new IllegalArgumentException("Tournament cannot be null");
        }

        tournament.start();
    }

    public Round generateNextRound(Tournament tournament) {
        if (tournament == null) {
            throw new IllegalArgumentException("Tournament cannot be null");
        }

        return tournament.generateNextRound();
    }

    public boolean isFinished(Tournament tournament) {
        Round current = tournament.getCurrentRound();

        if (current == null) return false;

        if (!current.isFinished()) return false;

        // knockout: jeden zwycięzca
        long winners = current.getMatches().stream()
                .map(Match::getWinner)
                .distinct()
                .count();

        return winners == 1;
    }
}