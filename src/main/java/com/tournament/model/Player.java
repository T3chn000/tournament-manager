package com.tournament.model;

import java.util.UUID;

public record Player(UUID playerId, String name) {
//    private static int lastPlayerId;
//    private final int playerId;
//    Static działa thlko w jednej instancji aplikacji więc przy wielokrotnym włączaniu ID mogłyby się dublować więc zrezygnowano z tego rozwiązania.
    public static final Player BYE = new Player(UUID.fromString("00000000-0000-0000-0000-000000000000"), "BYE");
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
