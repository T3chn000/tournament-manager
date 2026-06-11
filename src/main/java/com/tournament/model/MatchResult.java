package com.tournament.model;

/**
 * Result of a played match.
 *
 * <p>A {@code null} match result means that the match has not been played yet.</p>
 */
public enum MatchResult {
    PLAYER1_WIN,
    PLAYER2_WIN,
    DRAW
}
