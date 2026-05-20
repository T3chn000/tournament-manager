package com.tournament.ui.viewmodel;

import com.tournament.model.Tournament;
import com.tournament.model.TournamentState;
import com.tournament.model.TournamentType;

public record TournamentSummary(
        Tournament tournament,
        String name,
        TournamentType type,
        TournamentState state,
        int playerCount,
        int roundCount
) {
}
