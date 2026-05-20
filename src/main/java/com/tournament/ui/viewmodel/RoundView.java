package com.tournament.ui.viewmodel;

import java.util.List;

public record RoundView(
        int roundNumber,
        boolean finished,
        boolean hasDraws,
        List<MatchView> matches
) {
}
