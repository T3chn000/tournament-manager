package main.java.com.tournament.model;

public class Match {
    public enum Result {
        PLAYER1_WIN,
        PLAYER2_WIN,
        DRAW
    }
    private final Player player1;
    private final Player player2;
    private Result result;

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
                    ? Result.PLAYER2_WIN
                    : Result.PLAYER1_WIN;
        }
    }

    public Player getPlayer1() {return this.player1;}
    public Player getPlayer2() {return this.player2;}
    public Result getResult() {return this.result;}
    public Player getWinner() {
        if (result == null || result == Result.DRAW) {
            return null;
        }
        return result == Result.PLAYER1_WIN ? player1 : player2;
    }
    public Player getLoser() {
        if (result == null || result == Result.DRAW) {
            return null;
        }
        return result == Result.PLAYER1_WIN ? player2 : player1;
    }
    public boolean isPlayed() {
        return result != null;
    }
    public boolean isDraw() {
        return result == Result.DRAW;
    }
    public boolean isByeMatch() {
        return hasPlayer(Player.BYE);
    }
    public boolean hasPlayer(Player player) {
        return player1.equals(player) || player2.equals(player);
    }

    public void setResult(Result result) {
        if (result == null) {
            throw new IllegalArgumentException("Result cannot be null");
        }
        if (this.result != null) {
            throw new IllegalStateException("Match already has result");
        }
        this.result = result;
    }
}
