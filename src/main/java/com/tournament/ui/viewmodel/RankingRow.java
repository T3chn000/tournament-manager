package com.tournament.ui.viewmodel;

import java.util.UUID;

public record RankingRow(
        int place,
        UUID playerId,
        String playerName,
        int points,
        int wins,
        int draws,
        int losses,
        int playedMatches,
        int byeCount
) {
}
