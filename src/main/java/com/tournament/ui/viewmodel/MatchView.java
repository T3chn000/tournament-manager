package com.tournament.ui.viewmodel;

public record MatchView(
        int matchIndex,
        String player1Name,
        String player2Name,
        String score,
        Integer player1Points,
        Integer player2Points,
        String result,
        String winnerName,
        Integer tieBreakWinnerIndex,
        boolean played,
        boolean draw,
        boolean byeMatch,
        boolean editable
) {
}
