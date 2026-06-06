package com.tournament.persistence;

import com.tournament.model.Player;
import com.tournament.model.PlayerDirectory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlayerDirectoryRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void shouldLoadEmptyDirectoryWhenFileDoesNotExist() throws IOException {
        Path filePath = tempDir.resolve("players").resolve("players.json");
        PlayerDirectoryRepository repository = new PlayerDirectoryRepository(filePath);

        PlayerDirectory directory = repository.load();

        assertTrue(directory.getPlayers().isEmpty());
        assertTrue(Files.exists(filePath.getParent()));
    }

    @Test
    void shouldSaveAndLoadPlayerDirectory() throws IOException {
        Player alice = new Player(UUID.randomUUID(), "Alice");
        Player bob = new Player(UUID.randomUUID(), "Bob");
        Path filePath = tempDir.resolve("players").resolve("players.json");
        PlayerDirectoryRepository repository = new PlayerDirectoryRepository(filePath);

        repository.save(new PlayerDirectory(List.of(alice, bob)));
        PlayerDirectory loaded = repository.load();

        assertEquals(List.of(alice, bob), loaded.getPlayers());
    }

    @Test
    void shouldUseWrappedJsonFormat() throws IOException {
        Player alice = new Player(UUID.randomUUID(), "Alice");
        Path filePath = tempDir.resolve("players.json");
        PlayerDirectoryRepository repository = new PlayerDirectoryRepository(filePath);

        repository.save(new PlayerDirectory(List.of(alice)));

        String json = Files.readString(filePath);
        assertTrue(json.contains("\"version\" : 1"));
        assertTrue(json.contains("\"players\""));
        assertTrue(json.contains(alice.playerId().toString()));
        assertTrue(json.contains("\"name\" : \"Alice\""));
    }

    @Test
    void shouldRejectDuplicatePlayersWhenLoading() throws IOException {
        UUID id = UUID.randomUUID();
        Path filePath = tempDir.resolve("players.json");
        Files.writeString(filePath, """
                {
                  "version": 1,
                  "players": [
                    { "playerId": "%s", "name": "Alice" },
                    { "playerId": "%s", "name": "Renamed Alice" }
                  ]
                }
                """.formatted(id, id));
        PlayerDirectoryRepository repository = new PlayerDirectoryRepository(filePath);

        assertThrows(IllegalArgumentException.class, repository::load);
    }

    @Test
    void shouldRejectByePlayerWhenLoading() throws IOException {
        Path filePath = tempDir.resolve("players.json");
        Files.writeString(filePath, """
                {
                  "version": 1,
                  "players": [
                    { "playerId": "00000000-0000-0000-0000-000000000000", "name": "BYE" }
                  ]
                }
                """);
        PlayerDirectoryRepository repository = new PlayerDirectoryRepository(filePath);

        assertThrows(IllegalArgumentException.class, repository::load);
    }
}
