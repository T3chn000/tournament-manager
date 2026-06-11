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

/**
 * JSON repository for the global player directory.
 *
 * <p>The repository stores players in a small wrapper file and writes
 * updates through a temporary file before replacing the original.</p>
 */
public class PlayerDirectoryRepository {
    private static final String DATA_FILE = "data/players/players.json";

    private final ObjectMapper objectMapper;
    private final Path filePath;

    /**
     * Creates a repository that stores the player directory in the default file.
     */
    public PlayerDirectoryRepository() {
        this(Paths.get(DATA_FILE));
    }

    /**
     * Creates a repository that stores data at the provided path.
     *
     * @param filePath JSON file path
     */
    public PlayerDirectoryRepository(Path filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException("Player directory file path cannot be null");
        }
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.filePath = filePath;
        ensureDirectoryExists();
    }

    /**
     * Loads the player directory.
     *
     * @return loaded directory, or an empty directory when the file does not exist
     * @throws IOException when the JSON file cannot be read
     */
    public PlayerDirectory load() throws IOException {
        ensureDirectoryExists();
        if (!Files.exists(filePath)) {
            return new PlayerDirectory();
        }

        PlayerDirectoryData data = objectMapper.readValue(filePath.toFile(), PlayerDirectoryData.class);
        return new PlayerDirectory(data.players());
    }

    /**
     * Saves the player directory to disk.
     *
     * @param directory directory to save
     * @throws IOException when the file cannot be written
     */
    public void save(PlayerDirectory directory) throws IOException {
        if (directory == null) {
            throw new IllegalArgumentException("Player directory cannot be null");
        }

        ensureDirectoryExists();
        Path tempPath = filePath.resolveSibling(filePath.getFileName() + ".tmp");

        try {
            objectMapper.writeValue(tempPath.toFile(), new PlayerDirectoryData(directory.getPlayers()));
            moveReplacingExisting(tempPath, filePath);
        } finally {
            Files.deleteIfExists(tempPath);
        }
    }

    /**
     * Creates the parent directory for the player directory file when needed.
     */
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

    /**
     * Replaces the saved player file, using an atomic move when available.
     */
    private void moveReplacingExisting(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
