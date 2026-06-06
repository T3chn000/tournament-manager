package com.tournament.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tournament.model.PlayerDirectory;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class PlayerDirectoryRepository {
    private static final int DATA_VERSION = 1;
    private static final String DATA_FILE = "data/players/players.json";

    private final ObjectMapper objectMapper;
    private final Path filePath;

    public PlayerDirectoryRepository() {
        this(Paths.get(DATA_FILE));
    }

    public PlayerDirectoryRepository(Path filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException("Player directory file path cannot be null");
        }
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.filePath = filePath;
        ensureDirectoryExists();
    }

    public PlayerDirectory load() throws IOException {
        ensureDirectoryExists();
        if (!Files.exists(filePath)) {
            return new PlayerDirectory();
        }

        PlayerDirectoryData data = objectMapper.readValue(filePath.toFile(), PlayerDirectoryData.class);
        return new PlayerDirectory(data.players());
    }

    public void save(PlayerDirectory directory) throws IOException {
        if (directory == null) {
            throw new IllegalArgumentException("Player directory cannot be null");
        }

        ensureDirectoryExists();
        Path tempPath = filePath.resolveSibling(filePath.getFileName() + ".tmp");

        try {
            objectMapper.writeValue(tempPath.toFile(), new PlayerDirectoryData(DATA_VERSION, directory.getPlayers()));
            moveReplacingExisting(tempPath, filePath);
        } finally {
            Files.deleteIfExists(tempPath);
        }
    }

    private void ensureDirectoryExists() {
        Path parent = filePath.getParent();
        if (parent == null) {
            return;
        }

        try {
            Files.createDirectories(parent);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create player data directory: " + e.getMessage());
        }
    }

    private void moveReplacingExisting(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
