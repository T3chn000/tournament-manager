package com.tournament.ui.viewmodel;

import java.util.UUID;

public record PlayerRow(UUID playerId, String name) {
    public String shortId() {
        return playerId.toString().substring(0, 8);
    }
}
