package com.tournament.persistence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tournament.model.Match;
import com.tournament.model.Player;

/**
 * JSON representation of a match.
 *
 * <p>The result is deliberately not persisted because it is derived from the
 * saved players, points and optional tie-break winner.</p>
 *
 * @param player1 first player
 * @param player2 second player
 * @param player1Points points for the first player, when recorded
 * @param player2Points points for the second player, when recorded
 * @param tieBreakWinner optional winner used to resolve a knockout draw
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record MatchData(
        Player player1,
        Player player2,
        Integer player1Points,
        Integer player2Points,
        Player tieBreakWinner
) {
    /**
     * Captures the persistent state of a domain match.
     */
    static MatchData fromDomain(Match match) {
        return new MatchData(
                match.getPlayer1(),
                match.getPlayer2(),
                match.getPlayer1Points(),
                match.getPlayer2Points(),
                match.getTieBreakWinner()
        );
    }

    /**
     * Rebuilds a domain match from persisted data.
     */
    Match toDomain() {
        return new Match(player1, player2, player1Points, player2Points, tieBreakWinner);
    }
}
