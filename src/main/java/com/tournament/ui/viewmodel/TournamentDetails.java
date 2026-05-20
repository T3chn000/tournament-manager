package com.tournament.ui.viewmodel;

import com.tournament.model.TournamentState;
import com.tournament.model.TournamentType;

import java.util.List;

public record TournamentDetails(
        String name,
        TournamentType type,
        TournamentState state,
        List<PlayerRow> players,
        List<RoundView> rounds,
        List<RankingRow> ranking
) {
    public int playerCount() {
        return players.size();
    }

    public int roundCount() {
        return rounds.size();
    }
}
