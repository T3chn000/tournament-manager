package com.tournament.persistence;

import com.tournament.model.Player;

import java.util.List;

/**
 * JSON wrapper used to persist the player directory.
 *
 * @param players stored players
 */
record PlayerDirectoryData(List<Player> players) {
    PlayerDirectoryData {
        players = players == null ? List.of() : List.copyOf(players);
    }
}
