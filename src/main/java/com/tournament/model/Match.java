package com.tournament.model;

public class Match {
    private final Player player1;
    private final Player player2;
    private Integer player1Points;
    private Integer player2Points;
    private MatchResult result;

    public Match(Player player1, Player player2) {
        if (player1 == null || player2 == null) {
            throw new IllegalArgumentException("Players cannot be null");
        }
        if (player1.equals(player2)) {
            throw new IllegalArgumentException("Players must be different");
        }
        this.player1 = player1;
        this.player2 = player2;
        if (isByeMatch()) {
            this.result = player1.equals(Player.BYE)
                    ? MatchResult.PLAYER2_WIN
                    : MatchResult.PLAYER1_WIN;
        }
    }
    public Match(Player player1, Player player2, int player1Points, int player2Points) {
        this(player1, player2);
        setPoints(player1Points, player2Points);
    }

    public Player getPlayer1() {return this.player1;}
    public Player getPlayer2() {return this.player2;}
    public MatchResult getMatchResult() {return this.result;}
    public Player getWinner() {
        if (result == null || result == MatchResult.DRAW) {
            return null;
        }
        return result == MatchResult.PLAYER1_WIN ? player1 : player2;
    }
    public Player getLoser() {
        if (result == null || result == MatchResult.DRAW) {
            return null;
        }
        return result == MatchResult.PLAYER1_WIN ? player2 : player1;
    }
    public String getScore() {
        if (player1Points == null || player2Points == null) {
            return "-";
        }
        return player1Points + " : " + player2Points;
    }
    public boolean isPlayed() {
        return result != null;
    }
    public boolean isDraw() {
        return result == MatchResult.DRAW;
    }
    public boolean isByeMatch() {
        return hasPlayer(Player.BYE);
    }
    public boolean hasPlayer(Player player) {
        return player1.equals(player) || player2.equals(player);
    }

    private void updateMatchResult() {
        if (player1Points > player2Points) {
            this.result = MatchResult.PLAYER1_WIN;
        } else if (player2Points > player1Points) {
            this.result = MatchResult.PLAYER2_WIN;
        } else {
            this.result = MatchResult.DRAW;
        }
    }

    public void setPoints(int player1Points, int player2Points) {
        if (isByeMatch()) {
            throw new IllegalStateException("Cannot set points for BYE match");
        }

        if (player1Points < 0 || player2Points < 0) {
            throw new IllegalArgumentException("Points cannot be negative");
        }

        this.player1Points = player1Points;
        this.player2Points = player2Points;
        updateMatchResult();
    }
    @Override
    public String toString() {
        String winner = getWinner() == null ? "-" : getWinner().name();
        return "%s vs %s | score: %s | result: %s | winner: %s"
                .formatted(player1.name(), player2.name(), getScore(), result == null ? "not played" : result, winner);
    }
}
