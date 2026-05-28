package com.tournament.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Match {
    private final Player player1;
    private final Player player2;
    private Integer player1Points;
    private Integer player2Points;
    private MatchResult result;
    private Player tieBreakWinner;

    @JsonCreator
    public Match(
            @JsonProperty("player1") Player player1,
            @JsonProperty("player2") Player player2,
            @JsonProperty("player1Points") Integer player1Points,
            @JsonProperty("player2Points") Integer player2Points,
            @JsonProperty("result") MatchResult result,
            @JsonProperty("tieBreakWinner") Player tieBreakWinner) {
        validatePlayers(player1, player2);
        validateSerializedScore(player1, player2, player1Points, player2Points, result, tieBreakWinner);
        this.player1 = player1;
        this.player2 = player2;
        this.player1Points = player1Points;
        this.player2Points = player2Points;
        this.result = deserializeResult(player1, player2, player1Points, player2Points, result);
        this.tieBreakWinner = tieBreakWinner;
    }

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
    public Match(Player player1, Player player2, int player1Points, int player2Points) {
        this(player1, player2);
        setPoints(player1Points, player2Points);
    }

    public Player getPlayer1() {return this.player1;}
    public Player getPlayer2() {return this.player2;}
    public Integer getPlayer1Points() { return player1Points; }
    public Integer getPlayer2Points() { return player2Points; }
    public MatchResult getResult() { return result; }
    public Player getTieBreakWinner() { return tieBreakWinner; }
    @JsonIgnore
    public Player getWinner() {
        if (result == null) {
            return null;
        }

        if (result == MatchResult.DRAW) {
            return tieBreakWinner;
        }

        return result == MatchResult.PLAYER1_WIN ? player1 : player2;
    }
    @JsonIgnore
    public Player getLoser() {
        Player winner = getWinner();
        if (winner == null) {
            return null;
        }
        return winner.equals(player1) ? player2 : player1;
    }
    @JsonIgnore
    public String getScore() {
        if (player1Points == null || player2Points == null) {
            return "-";
        }
        return player1Points + " : " + player2Points;
    }
    @JsonIgnore
    public boolean isPlayed() {
        return result != null;
    }
    @JsonIgnore
    public boolean isDraw() {
        return result == MatchResult.DRAW;
    }
    @JsonIgnore
    public boolean isByeMatch() {
        return hasPlayer(Player.BYE);
    }
    public boolean hasPlayer(Player player) {
        return player1.equals(player) || player2.equals(player);
    }

    public void resolveDraw(Player winner) {
        if (result != MatchResult.DRAW) {
            throw new IllegalStateException("Only draw matches can be resolved");
        }

        if (!hasPlayer(winner) || winner.equals(Player.BYE)) {
            throw new IllegalArgumentException("Tie-break winner must be one of match players");
        }

        this.tieBreakWinner = winner;
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
        this.tieBreakWinner = null;
        updateMatchResult();
    }

    private static void validatePlayers(Player player1, Player player2) {
        if (player1 == null || player2 == null) {
            throw new IllegalArgumentException("Players cannot be null");
        }
        if (player1.equals(player2)) {
            throw new IllegalArgumentException("Players must be different");
        }
    }

    private static void validateSerializedScore(Player player1, Player player2, Integer player1Points, Integer player2Points, MatchResult result, Player tieBreakWinner) {
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
            validateSerializedByeMatch(player1, player1Points, player2Points, result, tieBreakWinner);
            return;
        }

        MatchResult resolvedResult = result != null ? result : resultFromPoints(player1Points, player2Points);
        if (resolvedResult != null && player1Points != null && resolvedResult != resultFromPoints(player1Points, player2Points)) {
            throw new IllegalArgumentException("Match result does not match points");
        }
        if (tieBreakWinner != null) {
            if (resolvedResult != MatchResult.DRAW) {
                throw new IllegalArgumentException("Tie-break winner can only be set for draws");
            }
            if (!tieBreakWinner.equals(player1) && !tieBreakWinner.equals(player2)) {
                throw new IllegalArgumentException("Tie-break winner must be one of match players");
            }
        }
    }

    private static void validateSerializedByeMatch(Player player1, Integer player1Points, Integer player2Points, MatchResult result, Player tieBreakWinner) {
        if (player1Points != null || player2Points != null) {
            throw new IllegalArgumentException("BYE match cannot have points");
        }
        MatchResult expectedResult = player1.equals(Player.BYE) ? MatchResult.PLAYER2_WIN : MatchResult.PLAYER1_WIN;
        if (result != null && result != expectedResult) {
            throw new IllegalArgumentException("BYE match result is invalid");
        }
        if (tieBreakWinner != null) {
            throw new IllegalArgumentException("BYE match cannot have tie-break winner");
        }
    }

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

    private static MatchResult deserializeResult(Player player1, Player player2, Integer player1Points, Integer player2Points, MatchResult result) {
        if (result != null) {
            return result;
        }
        if (player1.equals(Player.BYE)) {
            return MatchResult.PLAYER2_WIN;
        }
        if (player2.equals(Player.BYE)) {
            return MatchResult.PLAYER1_WIN;
        }
        return resultFromPoints(player1Points, player2Points);
    }

    @Override
    public String toString() {
        String winnerName = getWinner() == null ? "-" : getWinner().name();
        return String.format("%s vs %s | score: %s | result: %s | winner: %s",
                player1.name(), player2.name(), getScore(), result == null ? "not played" : result, winnerName);
    }
}
