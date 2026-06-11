package com.tournament.ui.viewmodel;

/**
 * Read-only match data prepared for tables and result editing dialogs.
 *
 * @param matchIndex zero-based match index within a round
 * @param player1Name first player display name
 * @param player2Name second player display name
 * @param score formatted score for display
 * @param player1Points raw points for player one, if available
 * @param player2Points raw points for player two, if available
 * @param result result label
 * @param winnerName winner display name or placeholder
 * @param tieBreakWinnerIndex one-based tie-break winner index, or {@code null}
 * @param played whether the match has a result
 * @param draw whether the score is a draw
 * @param byeMatch whether this is an automatic BYE match
 * @param editable whether the UI should allow editing this match
 */
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
