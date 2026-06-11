package com.tournament.model;

import java.util.UUID;

/**
 * A tournament participant identified by a stable UUID.
 *
 * <p>Players are equal when their identifiers are equal, so a player name can
 * change without breaking tournament references.</p>
 *
 * @param playerId unique player identifier
 * @param name display name shown in the UI and rankings
 */
public record Player(UUID playerId, String name) {
    /**
     * Synthetic player used to represent a free win.
     */
    public static final Player BYE = new Player(UUID.fromString("00000000-0000-0000-0000-000000000000"), "BYE");

    /**
     * Creates a player with a generated identifier.
     *
     * @param name display name of the player
     */
    public Player(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        this(UUID.randomUUID(), name);
    }

    public Player {
        if (playerId == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
    }

    @Override
    public String toString() {
        return String.format("Gracz %s (%s)", name, playerId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return playerId.equals(player.playerId);
    }

    @Override
    public int hashCode() {
        return playerId.hashCode();
    }
}
