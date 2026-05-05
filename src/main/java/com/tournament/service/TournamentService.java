package com.tournament.service;

import com.tournament.model.*;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class TournamentService {

    private final Random random = new Random();

    public Tournament createTournament(List<Player> players, TournamentType type) {
        return new Tournament(players, type);
    }

    public Tournament createTournament(String name, List<Player> players, TournamentType type) {
        return new Tournament(name, players, type);
    }

    public void startTournament(Tournament tournament) {
        if (tournament == null) {
            throw new IllegalArgumentException("Tournament cannot be null");
        }

        tournament.start();
    }

    public void addPlayer(Tournament tournament, Player player) {
        if (tournament == null) {
            throw new IllegalArgumentException("Tournament cannot be null");
        }

        tournament.addPlayer(player);
    }

    public Round generateNextRound(Tournament tournament) {
        if (tournament == null) {
            throw new IllegalArgumentException("Tournament cannot be null");
        }

        return tournament.generateNextRound();
    }

    public void simulateRound(Round round) {
        if (round == null) {
            throw new IllegalArgumentException("Round cannot be null");
        }

        for (Match match : round.getMatches()) {
            if (match.isPlayed()) {
                continue;
            }

            int player1Points = random.nextInt(2);
            int player2Points = 1 - player1Points;
            match.setPoints(player1Points, player2Points);
        }
    }

    public boolean isFinished(Tournament tournament) {
        if (tournament == null) {
            throw new IllegalArgumentException("Tournament cannot be null");
        }

        if (tournament.getState() == TournamentState.FINISHED) {
            return true;
        }

        Round current = tournament.getCurrentRound();

        if (current == null) return false;

        if (!current.isFinished()) return false;

        // knockout: jeden zwycięzca
        long winners = current.getMatches().stream()
                .map(Match::getWinner)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        boolean finished = winners == 1;
        if (finished) {
            tournament.finish();
        }

        return finished;
    }
}
