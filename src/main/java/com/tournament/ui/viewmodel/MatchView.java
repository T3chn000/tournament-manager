package com.tournament.ui.viewmodel;

public record MatchView(
        int matchIndex,
        String player1Name,
        String player2Name,
        String score,
        String result,
        String winnerName,
        boolean played,
        boolean draw,
        boolean byeMatch,
        boolean editable
) {
}
