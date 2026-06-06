package com.tournament.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class PlayerDirectory {
    private final List<Player> players = new ArrayList<>();

    public PlayerDirectory() {
        this(List.of());
    }

    public PlayerDirectory(List<Player> players) {
        if (players == null) {
            throw new IllegalArgumentException("Players cannot be null");
        }

        for (Player player : players) {
            addPlayer(player);
        }
    }

    public List<Player> getPlayers() {
        return List.copyOf(players);
    }

    public void addPlayer(Player player) {
        validatePlayer(player);
        String normalizedName = normalizeName(player.name());

        if (findById(player.playerId()).isPresent()) {
            throw new IllegalArgumentException("Duplicate player ID detected");
        }
        if (findByNormalizedName(normalizedName).isPresent()) {
            throw new IllegalArgumentException("Duplicate player name detected");
        }

        players.add(player);
    }

    public Player resolveOrCreate(String name) {
        return findByName(name)
                .orElseGet(() -> {
                    Player player = new Player(normalizeDisplayName(name));
                    addPlayer(player);
                    return player;
                });
    }

    public Optional<Player> findById(UUID playerId) {
        if (playerId == null) {
            return Optional.empty();
        }
        return players.stream()
                .filter(player -> player.playerId().equals(playerId))
                .findFirst();
    }

    public Optional<Player> findByName(String name) {
        String normalizedName = normalizeName(name);
        if (normalizedName.isBlank()) {
            return Optional.empty();
        }
        return findByNormalizedName(normalizedName);
    }

    public Player renamePlayer(UUID playerId, String newName) {
        if (playerId == null) {
            throw new IllegalArgumentException("Player ID cannot be null");
        }

        int playerIndex = findIndexById(playerId);
        if (playerIndex < 0) {
            throw new IllegalArgumentException("Player not found");
        }

        String normalizedNewName = normalizeName(newName);
        Optional<Player> duplicate = findByNormalizedName(normalizedNewName);
        if (duplicate.isPresent() && !duplicate.get().playerId().equals(playerId)) {
            throw new IllegalArgumentException("Duplicate player name detected");
        }

        Player renamed = new Player(playerId, normalizeDisplayName(newName));
        players.set(playerIndex, renamed);
        return renamed;
    }

    private Optional<Player> findByNormalizedName(String normalizedName) {
        return players.stream()
                .filter(player -> normalizeName(player.name()).equals(normalizedName))
                .findFirst();
    }

    private int findIndexById(UUID playerId) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).playerId().equals(playerId)) {
                return i;
            }
        }
        return -1;
    }

    private static void validatePlayer(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        if (player.equals(Player.BYE)) {
            throw new IllegalArgumentException("BYE player cannot be stored in player directory");
        }
    }

    private static String normalizeDisplayName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Player name cannot be empty");
        }
        return name.trim();
    }

    private static String normalizeName(String name) {
        if (name == null) {
            return "";
        }
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
