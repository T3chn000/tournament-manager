package com.tournament.ui.viewmodel;

import java.util.UUID;

/**
 * One row in the tournament ranking table.
 *
 * @param place ranking position
 * @param playerId player identifier
 * @param playerName player display name
 * @param points total ranking points
 * @param wins number of wins
 * @param draws number of draws
 * @param losses number of losses
 * @param playedMatches number of non-BYE matches played
 * @param byeCount number of BYE wins
 */
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
