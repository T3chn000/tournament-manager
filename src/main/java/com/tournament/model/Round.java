package com.tournament.model;

import java.util.List;
import java.util.Objects;

/**
 * A numbered group of matches generated for a tournament.
 *
 * <p>Rounds are immutable with respect to their match list, but individual
 * matches may still receive results.</p>
 */
public class Round {
    private final int roundNumber;
    private final List<Match> matches;

    /**
     * Creates a round with at least one match.
     *
     * @param roundNumber one-based round number
     * @param matches matches scheduled in this round
     */
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

    /**
     * Returns the number of matches in the round.
     *
     * @return match count
     */
    public int size() {
        return matches.size();
    }

    /**
     * Returns the one-based round number.
     *
     * @return round number
     */
    public int getRoundNumber() {
        return roundNumber;
    }

    /**
     * Returns the matches scheduled in this round.
     *
     * @return immutable match list
     */
    public List<Match> getMatches() {
        return matches;
    }

    /**
     * Returns matches that still need a winner or a recorded result.
     *
     * @return unresolved matches in their original order
     */
    public List<Match> getUnresolvedMatches() {
        return matches.stream()
                .filter(m -> !m.isPlayed() || m.getWinner() == null)
                .toList();
    }

    /**
     * Returns all winners once the round is finished.
     *
     * @return winners for resolved matches
     */
    public List<Player> getWinners() {
        if (!isFinished()) {
            throw new IllegalStateException("Round is not finished");
        }

        return matches.stream()
                .map(Match::getWinner)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Checks whether the round contains any drawn match.
     *
     * @return {@code true} when at least one match is a draw
     */
    public boolean hasDraws() {
        return matches.stream().anyMatch(Match::isDraw);
    }

    /**
     * Checks whether at least one match has no result.
     *
     * @return {@code true} when any match is unplayed
     */
    public boolean hasUnplayedMatches() {
        return matches.stream().anyMatch(m -> !m.isPlayed());
    }

    /**
     * Checks whether a player participates in any match in the round.
     *
     * @param player player to look for
     * @return {@code true} when the player appears in the round
     */
    public boolean containsPlayer(Player player) {
        return matches.stream().anyMatch(m -> m.hasPlayer(player));
    }

    /**
     * Checks whether every match has a recorded result.
     *
     * @return {@code true} when all matches are played
     */
    public boolean isFinished() {
        return matches.stream().allMatch(Match::isPlayed);
    }

    /**
     * Returns a compact textual representation of the round and its matches.
     *
     * @return round summary with match lines
     */
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
