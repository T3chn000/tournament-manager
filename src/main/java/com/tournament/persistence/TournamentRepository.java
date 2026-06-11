package com.tournament.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tournament.model.Tournament;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * JSON repository for tournament files.
 *
 * <p>Each tournament is stored in a separate file named after its UUID. Loading
 * skips unreadable tournament files and returns the tournaments that were read
 * successfully.</p>
 */
public class TournamentRepository {
    private final ObjectMapper objectMapper;
    private final Path dataDirectory;
    private static final String DATA_DIR = "data/tournaments";

    public TournamentRepository() {
        this(Paths.get(DATA_DIR));
    }

    /**
     * Creates a repository using the given data directory.
     *
     * @param dataDirectory directory containing tournament JSON files
     */
    public TournamentRepository(Path dataDirectory) {
        if (dataDirectory == null) {
            throw new IllegalArgumentException("Data directory cannot be null");
        }
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.dataDirectory = dataDirectory;
        ensureDirectoryExists();
    }

    /**
     * Creates the tournament storage directory before any file operation.
     */
    private void ensureDirectoryExists() {
        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create data directory: " + e.getMessage());
        }
    }

    /**
     * Saves one tournament to its JSON file.
     *
     * @param tournament tournament to save
     * @throws IOException when the file cannot be written
     */
    public void save(Tournament tournament) throws IOException {
        if (tournament == null) {
            throw new IllegalArgumentException("Tournament cannot be null");
        }

        ensureDirectoryExists();
        Path path = getTournamentPath(tournament.getTournamentId());
        Path tempPath = dataDirectory.resolve(path.getFileName() + ".tmp");

        try {
            objectMapper.writeValue(tempPath.toFile(), TournamentData.fromDomain(tournament));
            moveReplacingExisting(tempPath, path);
        } finally {
            Files.deleteIfExists(tempPath);
        }
    }

    /**
     * Loads all tournament files from the repository directory.
     *
     * @return loaded tournaments sorted by filename
     * @throws IOException when the directory cannot be listed
     */
    public List<Tournament> load() throws IOException {
        List<Tournament> tournaments = new ArrayList<>();
        
        if (Files.exists(dataDirectory)) {
            try (Stream<Path> paths = Files.list(dataDirectory)) {
                List<Path> files = paths
                        .filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().startsWith("tournament_") && p.getFileName().toString().endsWith(".json"))
                        .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                        .toList();
                
                for (Path path : files) {
                    try {
                        TournamentData data = objectMapper.readValue(path.toFile(), TournamentData.class);
                        tournaments.add(data.toDomain());
                    } catch (IOException e) {
                        System.err.println("Error loading tournament from " + path + ": " + e.getMessage());
                    }
                }
            }
        }

        return tournaments;
    }

    /**
     * Deletes the JSON file for a tournament.
     *
     * @param tournamentId tournament identifier
     * @throws IOException when the file cannot be deleted
     */
    public void delete(UUID tournamentId) throws IOException {
        if (tournamentId == null) {
            throw new IllegalArgumentException("Tournament ID cannot be null");
        }

        Files.deleteIfExists(getTournamentPath(tournamentId));
    }

    /**
     * Builds the canonical filename for a tournament UUID.
     */
    private Path getTournamentPath(UUID tournamentId) {
        return dataDirectory.resolve("tournament_" + tournamentId + ".json");
    }

    /**
     * Replaces the target file, preferring an atomic move when the filesystem supports it.
     */
    private void moveReplacingExisting(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
