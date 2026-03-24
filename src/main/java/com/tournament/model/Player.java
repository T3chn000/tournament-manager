package main.java.com.tournament.model;

import java.util.UUID;

public class Player {
//    private static int lastPlayerId;
//    private final int playerId;
//    Static działa thlko w jednej instancji aplikacji więc przy wielokrotnym włączaniu ID mogłyby się dublować więc zrezygnowano z tego rozwiązania.

    private final UUID playerId;
    private final String name;

    public Player(String name){
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        this.name = name;
        this.playerId = UUID.randomUUID();
    }
    public Player(UUID id, String name){
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        this.playerId = id;
        this.name = name;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString(){
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
