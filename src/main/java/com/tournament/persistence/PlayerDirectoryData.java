package com.tournament.persistence;

import com.tournament.model.Player;

import java.util.List;

record PlayerDirectoryData(int version, List<Player> players) {
    PlayerDirectoryData {
        if (players == null) {
            players = List.of();
        }
        players = List.copyOf(players);
    }
}
