package com.tournament.persistence;

import com.tournament.model.Player;

import java.util.List;

/**
 * Versioned JSON wrapper used to persist the player directory.
 *
 * @param version schema version
 * @param players stored players
 */
record PlayerDirectoryData(int version, List<Player> players) {
    PlayerDirectoryData {
        if (players == null) {
            players = List.of();
        }
        players = List.copyOf(players);
    }
}
