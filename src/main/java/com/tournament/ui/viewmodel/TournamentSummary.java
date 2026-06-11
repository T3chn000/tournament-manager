package com.tournament.ui.viewmodel;

import com.tournament.model.Tournament;
import com.tournament.model.TournamentState;
import com.tournament.model.TournamentType;

/**
 * Compact tournament data shown in the tournament list.
 *
 * @param tournament underlying domain object
 * @param name tournament name
 * @param type tournament format
 * @param state tournament lifecycle state
 * @param playerCount number of registered players
 * @param roundCount number of generated rounds
 */
public record TournamentSummary(
        Tournament tournament,
        String name,
        TournamentType type,
        TournamentState state,
        int playerCount,
        int roundCount
) {
}
