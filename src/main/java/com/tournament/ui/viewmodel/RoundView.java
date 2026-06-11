package com.tournament.ui.viewmodel;

import java.util.List;

/**
 * Round data prepared for display in the UI.
 *
 * @param roundNumber one-based round number
 * @param finished whether every match has a recorded result
 * @param hasDraws whether the round contains at least one drawn match
 * @param matches match rows for this round
 */
public record RoundView(
        int roundNumber,
        boolean finished,
        boolean hasDraws,
        List<MatchView> matches
) {
}
