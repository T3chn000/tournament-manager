package com.tournament.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlayerDirectoryTest {

    @Test
    void shouldCreateDirectoryWithValidPlayers() {
        Player alice = new Player("Alice");
        Player bob = new Player("Bob");

        PlayerDirectory directory = new PlayerDirectory(List.of(alice, bob));

        assertEquals(List.of(alice, bob), directory.getPlayers());
        assertTrue(directory.findById(alice.playerId()).isPresent());
        assertTrue(directory.findByName("alice").isPresent());
    }

    @Test
    void shouldCreateEmptyDirectory() {
        PlayerDirectory directory = new PlayerDirectory();

        assertTrue(directory.getPlayers().isEmpty());
    }

    @Test
    void shouldResolveMissingPlayerByCreatingIt() {
        PlayerDirectory directory = new PlayerDirectory();

        Player player = directory.resolveOrCreate(" Alice ");

        assertNotNull(player.playerId());
        assertEquals("Alice", player.name());
        assertEquals(List.of(player), directory.getPlayers());
    }

    @Test
    void shouldResolveExistingPlayerByNormalizedName() {
        PlayerDirectory directory = new PlayerDirectory();
        Player alice = directory.resolveOrCreate("Alice");

        Player resolved = directory.resolveOrCreate(" alice ");

        assertEquals(alice, resolved);
        assertEquals(1, directory.getPlayers().size());
    }

    @Test
    void shouldRejectDuplicatePlayerId() {
        UUID id = UUID.randomUUID();
        Player alice = new Player(id, "Alice");
        Player renamed = new Player(id, "Renamed Alice");

        assertThrows(IllegalArgumentException.class, () -> new PlayerDirectory(List.of(alice, renamed)));
    }

    @Test
    void shouldRejectDuplicateNormalizedName() {
        Player alice = new Player("Alice");
        Player duplicate = new Player(" alice ");

        assertThrows(IllegalArgumentException.class, () -> new PlayerDirectory(List.of(alice, duplicate)));
    }

    @Test
    void shouldRejectByePlayer() {
        assertThrows(IllegalArgumentException.class, () -> new PlayerDirectory(List.of(Player.BYE)));
    }

    @Test
    void shouldReturnUnmodifiablePlayersList() {
        PlayerDirectory directory = new PlayerDirectory(List.of(new Player("Alice")));

        assertThrows(UnsupportedOperationException.class, () -> directory.getPlayers().add(new Player("Bob")));
    }

    @Test
    void shouldRenameExistingPlayer() {
        PlayerDirectory directory = new PlayerDirectory();
        Player alice = directory.resolveOrCreate("Alice");

        Player renamed = directory.renamePlayer(alice.playerId(), "Alice Cooper");

        assertEquals(alice.playerId(), renamed.playerId());
        assertEquals("Alice Cooper", renamed.name());
        assertTrue(directory.findByName("Alice Cooper").isPresent());
        assertTrue(directory.findByName("Alice").isEmpty());
    }

    @Test
    void shouldRejectRenameToExistingName() {
        PlayerDirectory directory = new PlayerDirectory(List.of(new Player("Alice"), new Player("Bob")));
        Player alice = directory.findByName("Alice").orElseThrow();

        assertThrows(IllegalArgumentException.class, () -> directory.renamePlayer(alice.playerId(), "bob"));
    }
}
