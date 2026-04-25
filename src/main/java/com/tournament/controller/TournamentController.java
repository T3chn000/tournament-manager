package com.tournament.controller;

import com.tournament.model.*;
import com.tournament.service.TournamentService;

import java.util.List;

// klasa do zrobienia JavaFX
public class TournamentController {

    private final TournamentService service;

    public TournamentController(TournamentService service) {
        this.service = service;
    }

    public Tournament createTournament(List<Player> players, TournamentType type) {
        return service.createTournament(players, type);
    }

    public void startTournament(Tournament tournament) {
        service.startTournament(tournament);
    }

    public Round nextRound(Tournament tournament) {
        return service.generateNextRound(tournament);
    }

    public boolean isFinished(Tournament tournament) {
        return service.isFinished(tournament);
    }
}