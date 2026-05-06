package com.tournament.model;

import java.util.List;
import java.util.Objects;

public class Round {
    private final int roundNumber;
    private final List<Match> matches;

    public Round(int roundNumber, List<Match> matches) {
        if (roundNumber <= 0) {
            throw new IllegalArgumentException("Round number must be positive");
        }
        if (matches == null || matches.isEmpty()) {
            throw new IllegalArgumentException("Matches cannot be null or empty");
        }

        this.roundNumber = roundNumber;
        this.matches = List.copyOf(matches); // immutability
    }

    public int size() {
        return matches.size();
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public List<Match> getUnresolvedMatches() {
        return matches.stream()
                .filter(m -> !m.isPlayed() || m.getWinner() == null)
                .toList();
    }

    public List<Player> getWinners() {
        if (!isFinished()) {
            throw new IllegalStateException("Round is not finished");
        }

        return matches.stream()
                .map(Match::getWinner)
                .filter(Objects::nonNull)
                .toList();
    }

    public boolean hasDraws() {
        return matches.stream().anyMatch(Match::isDraw);
    }

    public boolean hasUnplayedMatches() {
        return matches.stream().anyMatch(m -> !m.isPlayed());
    }

    public boolean containsPlayer(Player player) {
        return matches.stream().anyMatch(m -> m.hasPlayer(player));
    }

    public boolean isFinished() {
        return matches.stream().allMatch(Match::isPlayed);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Round ")
                .append(roundNumber)
                .append(isFinished() ? " [finished]" : " [in progress]");

        for (int i = 0; i < matches.size(); i++) {
            builder.append(System.lineSeparator())
                    .append("  ")
                    .append(i + 1)
                    .append(". ")
                    .append(matches.get(i));
        }

        return builder.toString();
    }
}
