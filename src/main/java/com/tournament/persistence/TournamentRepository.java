package com.tournament.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tournament.model.Tournament;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TournamentRepository {
    private final ObjectMapper objectMapper;
    private static final String DATA_DIR = "data/tournaments";

    public TournamentRepository() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        ensureDirectoryExists();
    }

    private void ensureDirectoryExists() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            System.err.println("Could not create data directory: " + e.getMessage());
        }
    }

    public void save(Tournament tournament) throws IOException {
        Path path = Paths.get(DATA_DIR, "tournament_" + tournament.getTournamentId() + ".json");
        objectMapper.writeValue(path.toFile(), tournament);
    }

    public List<Tournament> load() throws IOException {
        List<Tournament> tournaments = new ArrayList<>();
        
        Path dirPath = Paths.get(DATA_DIR);
        if (Files.exists(dirPath)) {
            try (Stream<Path> paths = Files.list(dirPath)) {
                List<Path> files = paths
                        .filter(p -> p.getFileName().toString().startsWith("tournament_") && p.getFileName().toString().endsWith(".json"))
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
}
