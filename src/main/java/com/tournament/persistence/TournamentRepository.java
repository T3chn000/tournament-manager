package com.tournament.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tournament.model.Tournament;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TournamentRepository {
    private final ObjectMapper objectMapper;
    private static final String FILE_PATH = "data/tournaments.json";

    public TournamentRepository() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void save(List<Tournament> tournaments) throws IOException {
        File file = new File(FILE_PATH);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        objectMapper.writeValue(file, tournaments);
    }

    public List<Tournament> load() throws IOException {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        return objectMapper.readValue(file, new TypeReference<List<Tournament>>() {});
    }
}
