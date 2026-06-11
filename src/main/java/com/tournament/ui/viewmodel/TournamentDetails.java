package com.tournament.ui.viewmodel;

import com.tournament.model.TournamentState;
import com.tournament.model.TournamentType;

import java.util.List;

/**
 * Full tournament data needed by the details panel.
 *
 * @param name tournament name
 * @param type tournament format
 * @param state tournament lifecycle state
 * @param players player rows
 * @param rounds round rows
 * @param ranking calculated ranking rows
 */
public record TournamentDetails(
        String name,
        TournamentType type,
        TournamentState state,
        List<PlayerRow> players,
        List<RoundView> rounds,
        List<RankingRow> ranking
) {
    /**
     * Returns the number of players in the tournament.
     */
    public int playerCount() {
        return players.size();
    }

    /**
     * Returns the number of generated rounds.
     */
    public int roundCount() {
        return rounds.size();
    }
}
