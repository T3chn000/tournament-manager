package com.tournament.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * In-memory registry of known players.
 *
 * <p>The directory keeps player names unique in a case-insensitive way and can
 * reuse an existing player when a tournament is created from names.</p>
 */
public class PlayerDirectory {
    /**
     * Mutable backing store kept private so callers cannot bypass validation.
     */
    private final List<Player> players = new ArrayList<>();

    public PlayerDirectory() {
        this(List.of());
    }

    /**
     * Creates a directory from existing players, validating each entry and rejecting duplicates.
     *
     * @param players players to preload into the directory
     */
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

    /**
     * Adds a new player to the directory.
     *
     * @param player player to add
     */
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

    /**
     * Finds a player by name or creates and stores a new one.
     *
     * @param name player display name
     * @return existing or newly created player
     */
    public Player resolveOrCreate(String name) {
        return findByName(name)
                .orElseGet(() -> {
                    Player player = new Player(normalizeDisplayName(name));
                    addPlayer(player);
                    return player;
                });
    }

    /**
     * Finds a player by identifier.
     *
     * @param playerId player identifier
     * @return matching player, if present
     */
    public Optional<Player> findById(UUID playerId) {
        if (playerId == null) {
            return Optional.empty();
        }
        return players.stream()
                .filter(player -> player.playerId().equals(playerId))
                .findFirst();
    }

    /**
     * Finds a player by display name using case-insensitive comparison.
     *
     * @param name player display name
     * @return matching player, if present
     */
    public Optional<Player> findByName(String name) {
        String normalizedName = normalizeName(name);
        if (normalizedName.isBlank()) {
            return Optional.empty();
        }
        return findByNormalizedName(normalizedName);
    }

    /**
     * Renames an existing player while keeping the same identifier.
     *
     * @param playerId player to rename
     * @param newName new display name
     * @return renamed player instance
     */
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

    /**
     * Finds a player using the already-normalized display name.
     */
    private Optional<Player> findByNormalizedName(String normalizedName) {
        return players.stream()
                .filter(player -> normalizeName(player.name()).equals(normalizedName))
                .findFirst();
    }

    /**
     * Returns the list index for a player ID, or {@code -1} when absent.
     */
    private int findIndexById(UUID playerId) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).playerId().equals(playerId)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Rejects invalid directory entries before they can be stored.
     */
    private static void validatePlayer(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        if (player.equals(Player.BYE)) {
            throw new IllegalArgumentException("BYE player cannot be stored in player directory");
        }
    }

    /**
     * Trims a name for display while still rejecting empty input.
     */
    private static String normalizeDisplayName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Player name cannot be empty");
        }
        return name.trim();
    }

    /**
     * Produces the comparison key used for case-insensitive name uniqueness.
     */
    private static String normalizeName(String name) {
        if (name == null) {
            return "";
        }
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
