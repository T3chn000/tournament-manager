package com.tournament.ui.viewmodel;

import java.util.UUID;

/**
 * Player data shown in table rows.
 *
 * @param playerId player identifier
 * @param name player display name
 */
public record PlayerRow(UUID playerId, String name) {
    /**
     * Returns a compact identifier for table display.
     *
     * @return first eight characters of the player identifier
     */
    public String shortId() {
        return playerId.toString().substring(0, 8);
    }
}
