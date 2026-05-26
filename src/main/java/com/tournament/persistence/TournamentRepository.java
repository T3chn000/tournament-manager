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

public class TournamentRepository {
    private final ObjectMapper objectMapper;
    private final Path dataDirectory;
    private static final String DATA_DIR = "data/tournaments";

    public TournamentRepository() {
        this(Paths.get(DATA_DIR));
    }

    public TournamentRepository(Path dataDirectory) {
        if (dataDirectory == null) {
            throw new IllegalArgumentException("Data directory cannot be null");
        }
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.dataDirectory = dataDirectory;
        ensureDirectoryExists();
    }

    private void ensureDirectoryExists() {
        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create data directory: " + e.getMessage());
        }
    }

    public void save(Tournament tournament) throws IOException {
        if (tournament == null) {
            throw new IllegalArgumentException("Tournament cannot be null");
        }

        ensureDirectoryExists();
        Path path = getTournamentPath(tournament.getTournamentId());
        Path tempPath = dataDirectory.resolve(path.getFileName() + ".tmp");

        try {
            objectMapper.writeValue(tempPath.toFile(), tournament);
            moveReplacingExisting(tempPath, path);
        } finally {
            Files.deleteIfExists(tempPath);
        }
    }

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
                        tournaments.add(objectMapper.readValue(path.toFile(), Tournament.class));
                    } catch (IOException e) {
                        System.err.println("Error loading tournament from " + path + ": " + e.getMessage());
                    }
                }
            }
        }

        return tournaments;
    }

    public void delete(UUID tournamentId) throws IOException {
        if (tournamentId == null) {
            throw new IllegalArgumentException("Tournament ID cannot be null");
        }

        Files.deleteIfExists(getTournamentPath(tournamentId));
    }

    private Path getTournamentPath(UUID tournamentId) {
        return dataDirectory.resolve("tournament_" + tournamentId + ".json");
    }

    private void moveReplacingExisting(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
