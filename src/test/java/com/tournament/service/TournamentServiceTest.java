package com.tournament.service;

import com.tournament.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TournamentServiceTest {

    @Test
    void shouldCreateTournament() {
        TournamentService service = new TournamentService();

        Player p1 = new Player("A");
        Player p2 = new Player("B");

        Tournament tournament = service.createTournament(List.of(p1, p2), TournamentType.KNOCKOUT);

        assertNotNull(tournament);
        assertEquals(TournamentType.KNOCKOUT, tournament.getType());
        assertEquals(2, tournament.getPlayers().size());
        assertFalse(tournament.isStarted());
    }

    @Test
    void shouldStartTournament() {
        TournamentService service = new TournamentService();

        Player p1 = new Player("A");
        Player p2 = new Player("B");

        Tournament tournament = service.createTournament(List.of(p1, p2), TournamentType.KNOCKOUT);

        service.startTournament(tournament);

        assertTrue(tournament.isStarted());
    }

    @Test
    void shouldThrowExceptionWhenStartingNullTournament() {
        TournamentService service = new TournamentService();

        assertThrows(IllegalArgumentException.class, () -> service.startTournament(null));
    }

    @Test
    void shouldGenerateNextRoundForStartedTournament() {
        TournamentService service = new TournamentService();

        Player p1 = new Player("A");
        Player p2 = new Player("B");

        Tournament tournament = service.createTournament(List.of(p1, p2), TournamentType.KNOCKOUT);

        service.startTournament(tournament);

        Round round = service.generateNextRound(tournament);

        assertNotNull(round);
        assertEquals(1, tournament.getRoundCount());
        assertEquals(round, tournament.getCurrentRound());
    }

    @Test
    void shouldThrowExceptionWhenGeneratingRoundForNullTournament() {
        TournamentService service = new TournamentService();

        assertThrows(IllegalArgumentException.class, () -> service.generateNextRound(null));
    }

    @Test
    void shouldThrowExceptionWhenGeneratingRoundBeforeTournamentStart() {
        TournamentService service = new TournamentService();

        Player p1 = new Player("A");
        Player p2 = new Player("B");

        Tournament tournament = service.createTournament(List.of(p1, p2), TournamentType.KNOCKOUT);

        assertThrows(IllegalStateException.class, () -> service.generateNextRound(tournament));
    }

    @Test
    void shouldReturnFalseWhenTournamentHasNoRounds() {
        TournamentService service = new TournamentService();

        Player p1 = new Player("A");
        Player p2 = new Player("B");

        Tournament tournament = service.createTournament(List.of(p1, p2), TournamentType.KNOCKOUT);

        assertFalse(service.isFinished(tournament));
    }

    @Test
    void shouldReturnFalseWhenCurrentRoundIsNotFinished() {
        TournamentService service = new TournamentService();

        Player p1 = new Player("A");
        Player p2 = new Player("B");

        Tournament tournament = service.createTournament(List.of(p1, p2), TournamentType.KNOCKOUT);

        service.startTournament(tournament);
        service.generateNextRound(tournament);

        assertFalse(service.isFinished(tournament));
    }

    @Test
    void shouldReturnTrueWhenFinalRoundHasOneWinner() {
        TournamentService service = new TournamentService();

        Player p1 = new Player("A");
        Player p2 = new Player("B");

        Tournament tournament = service.createTournament(List.of(p1, p2), TournamentType.KNOCKOUT);

        service.startTournament(tournament);

        Round round = service.generateNextRound(tournament);
        Match match = round.getMatches().getFirst();
        match.setPoints(1, 0);

        assertTrue(service.isFinished(tournament));
    }

    @Test
    void shouldThrowExceptionWhenCheckingIfNullTournamentIsFinished() {
        TournamentService service = new TournamentService();

        assertThrows(IllegalArgumentException.class, () -> service.isFinished(null));
    }
}