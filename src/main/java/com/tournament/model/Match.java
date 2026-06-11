package com.tournament.model;

/**
 * A single pairing between two players, including score and optional tie-break data.
 *
 * <p>Matches against {@link Player#BYE} are resolved automatically and cannot
 * receive manual point values.</p>
 */
public class Match {
    private final Player player1;
    private final Player player2;
    private Integer player1Points;
    private Integer player2Points;
    private MatchResult result;
    private Player tieBreakWinner;

    /**
     * Creates a match using given data.
     *
     * @param player1 first player
     * @param player2 second player
     * @param player1Points optional points for the first player
     * @param player2Points optional points for the second player
     * @param tieBreakWinner optional winner used to resolve a draw
     */
    public Match(
            Player player1,
            Player player2,
            Integer player1Points,
            Integer player2Points,
            Player tieBreakWinner) {
        validatePlayers(player1, player2);
        validateScore(player1, player2, player1Points, player2Points, tieBreakWinner);
        this.player1 = player1;
        this.player2 = player2;
        this.player1Points = player1Points;
        this.player2Points = player2Points;
        this.result = deriveResult(player1, player2, player1Points, player2Points);
        this.tieBreakWinner = tieBreakWinner;
    }

    /**
     * Creates an unplayed match for two different players.
     *
     * @param player1 first player
     * @param player2 second player
     */
    public Match(Player player1, Player player2) {
        validatePlayers(player1, player2);
        this.player1 = player1;
        this.player2 = player2;
        if (isByeMatch()) {
            this.result = player1.equals(Player.BYE)
                    ? MatchResult.PLAYER2_WIN
                    : MatchResult.PLAYER1_WIN;
        }
    }

    /**
     * Creates a match and immediately records its score.
     *
     * @param player1 first player
     * @param player2 second player
     * @param player1Points points for the first player
     * @param player2Points points for the second player
     */
    public Match(Player player1, Player player2, int player1Points, int player2Points) {
        this(player1, player2);
        setPoints(player1Points, player2Points);
    }

    /**
     * Returns the first player in the pairing.
     *
     * @return first player
     */
    public Player getPlayer1() {return this.player1;}

    /**
     * Returns the second player in the pairing.
     *
     * @return second player
     */
    public Player getPlayer2() {return this.player2;}

    /**
     * Returns points recorded for the first player.
     *
     * @return first player points, or {@code null} when no score is recorded
     */
    public Integer getPlayer1Points() { return player1Points; }

    /**
     * Returns points recorded for the second player.
     *
     * @return second player points, or {@code null} when no score is recorded
     */
    public Integer getPlayer2Points() { return player2Points; }

    /**
     * Returns the derived match result.
     *
     * @return match result, or {@code null} when the match is not played
     */
    public MatchResult getResult() { return result; }

    /**
     * Returns the player selected to resolve a drawn match.
     *
     * @return tie-break winner, or {@code null} when no tie-break is set
     */
    public Player getTieBreakWinner() { return tieBreakWinner; }

    /**
     * Returns the winner, including a tie-break winner for resolved draws.
     *
     * @return winning player, or {@code null} when the match has no winner yet
     */
    public Player getWinner() {
        if (result == null) {
            return null;
        }

        if (result == MatchResult.DRAW) {
            return tieBreakWinner;
        }

        return result == MatchResult.PLAYER1_WIN ? player1 : player2;
    }

    /**
     * Returns the losing player when the match has a winner.
     *
     * @return losing player, or {@code null} when no winner is known
     */
    public Player getLoser() {
        Player winner = getWinner();
        if (winner == null) {
            return null;
        }
        return winner.equals(player1) ? player2 : player1;
    }

    /**
     * Returns a display-friendly score.
     *
     * @return score in {@code "x : y"} form, or {@code "-"} for unplayed matches
     */
    public String getScore() {
        if (player1Points == null || player2Points == null) {
            return "-";
        }
        return player1Points + " : " + player2Points;
    }

    /**
     * Checks whether a result is available for this match.
     *
     * @return {@code true} when the match has a result
     */
    public boolean isPlayed() {
        return result != null;
    }

    /**
     * Checks whether the recorded score is a draw.
     *
     * @return {@code true} when the match result is a draw
     */
    public boolean isDraw() {
        return result == MatchResult.DRAW;
    }

    /**
     * Checks whether this match contains the BYE placeholder.
     *
     * @return {@code true} when either side is {@link Player#BYE}
     */
    public boolean isByeMatch() {
        return hasPlayer(Player.BYE);
    }

    /**
     * Checks whether the given player participates in this match.
     *
     * @param player player to look for
     * @return {@code true} when the player is either side of the match
     */
    public boolean hasPlayer(Player player) {
        return player1.equals(player) || player2.equals(player);
    }

    /**
     * Resolves a drawn match by selecting one of its players as the winner.
     *
     * @param winner player who won the tie-break
     */
    public void resolveDraw(Player winner) {
        if (result != MatchResult.DRAW) {
            throw new IllegalStateException("Only draw matches can be resolved");
        }

        if (!hasPlayer(winner) || winner.equals(Player.BYE)) {
            throw new IllegalArgumentException("Tie-break winner must be one of match players");
        }

        this.tieBreakWinner = winner;
    }

    /**
     * Records points and derives the match result from them.
     *
     * @param player1Points points for the first player
     * @param player2Points points for the second player
     */
    public void setPoints(int player1Points, int player2Points) {
        if (isByeMatch()) {
            throw new IllegalStateException("Cannot set points for BYE match");
        }

        if (player1Points < 0 || player2Points < 0) {
            throw new IllegalArgumentException("Points cannot be negative");
        }

        this.player1Points = player1Points;
        this.player2Points = player2Points;
        this.tieBreakWinner = null;
        this.result = resultFromPoints(this.player1Points, this.player2Points);
    }

    /**
     * Returns a compact textual representation of the match.
     *
     * @return player names, score, result and winner
     */
    @Override
    public String toString() {
        String winnerName = getWinner() == null ? "-" : getWinner().name();
        return String.format("%s vs %s | score: %s | result: %s | winner: %s",
                player1.name(), player2.name(), getScore(), result == null ? "not played" : result, winnerName);
    }

    /**
     * Converts a pair of point values into a result.
     */
    private static MatchResult resultFromPoints(Integer player1Points, Integer player2Points) {
        if (player1Points == null || player2Points == null) {
            return null;
        }
        if (player1Points > player2Points) {
            return MatchResult.PLAYER1_WIN;
        }
        if (player2Points > player1Points) {
            return MatchResult.PLAYER2_WIN;
        }
        return MatchResult.DRAW;
    }

    /**
     * Ensures that a match has two non-null, different participants.
     */
    private static void validatePlayers(Player player1, Player player2) {
        if (player1 == null || player2 == null) {
            throw new IllegalArgumentException("Players cannot be null");
        }
        if (player1.equals(player2)) {
            throw new IllegalArgumentException("Players must be different");
        }
    }

    /**
     * Checks consistency of points and given tie-break winner.
     *
     * <p>This protects the domain model from accepting impossible states,
     * such as a non-draw with a tie-break winner.</p>
     */
    private static void validateScore(Player player1, Player player2, Integer player1Points, Integer player2Points, Player tieBreakWinner) {
        boolean hasPlayer1Points = player1Points != null;
        boolean hasPlayer2Points = player2Points != null;
        if (hasPlayer1Points != hasPlayer2Points) {
            throw new IllegalArgumentException("Both players must have points or neither does");
        }
        if (hasPlayer1Points && (player1Points < 0 || player2Points < 0)) {
            throw new IllegalArgumentException("Points cannot be negative");
        }

        boolean byeMatch = player1.equals(Player.BYE) || player2.equals(Player.BYE);
        if (byeMatch) {
            validateByeMatch(player1Points, player2Points, tieBreakWinner);
            return;
        }

        MatchResult resolvedResult = resultFromPoints(player1Points, player2Points);
        if (tieBreakWinner != null) {
            if (resolvedResult != MatchResult.DRAW) {
                throw new IllegalArgumentException("Tie-break winner can only be set for draws");
            }
            if (!tieBreakWinner.equals(player1) && !tieBreakWinner.equals(player2)) {
                throw new IllegalArgumentException("Tie-break winner must be one of match players");
            }
        }
    }

    /**
     * Applies the special rules for BYE matches.
     *
     * <p>BYE matches are automatic wins and therefore must not contain manual
     * points or tie-break data.</p>
     */
    private static void validateByeMatch(Integer player1Points, Integer player2Points, Player tieBreakWinner) {
        if (player1Points != null || player2Points != null) {
            throw new IllegalArgumentException("BYE match cannot have points");
        }
        if (tieBreakWinner != null) {
            throw new IllegalArgumentException("BYE match cannot have tie-break winner");
        }
    }

    /**
     * Rebuilds the in-memory result from match participants and points.
     */
    private static MatchResult deriveResult(Player player1, Player player2, Integer player1Points, Integer player2Points) {
        if (player1.equals(Player.BYE)) {
            return MatchResult.PLAYER2_WIN;
        }
        if (player2.equals(Player.BYE)) {
            return MatchResult.PLAYER1_WIN;
        }
        return resultFromPoints(player1Points, player2Points);
    }
}
