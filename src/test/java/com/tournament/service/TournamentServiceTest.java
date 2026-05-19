package com.tournament.service;

import com.tournament.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

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
    void shouldCreateNamedTournament() {
        TournamentService service = new TournamentService();

        Player p1 = new Player("A");
        Player p2 = new Player("B");

        Tournament tournament = service.createTournament("Spring Cup", List.of(p1, p2), TournamentType.KNOCKOUT);

        assertEquals("Spring Cup", tournament.getName());
        assertEquals(TournamentType.KNOCKOUT, tournament.getType());
    }

    @Test
    void shouldCreateTournamentWithProvidedId() {
        TournamentService service = new TournamentService();

        Player p1 = new Player("A");
        Player p2 = new Player("B");
        UUID id = UUID.randomUUID();

        Tournament tournament = service.createTournament(id, "Spring Cup", List.of(p1, p2), TournamentType.KNOCKOUT);

        assertEquals(id, tournament.getTournamentId());
        assertEquals("Spring Cup", tournament.getName());
    }

    @Test
    void shouldStartTournament() {
        TournamentService service = new TournamentService();

        Player p1 = new Player("A");
        Player p2 = new Player("B");

        Tournament tournament = service.createTournament(List.of(p1, p2), TournamentType.KNOCKOUT);

        service.startTournament(tournament);

        assertEquals(TournamentState.STARTED, tournament.getState());
        assertTrue(tournament.isStarted());
    }

    @Test
    void shouldAddPlayerToTournament() {
        TournamentService service = new TournamentService();

        Player p1 = new Player("A");
        Player p2 = new Player("B");
        Player p3 = new Player("C");

        Tournament tournament = service.createTournament(List.of(p1, p2), TournamentType.KNOCKOUT);

        service.addPlayer(tournament, p3);

        assertEquals(3, tournament.getPlayers().size());
        assertTrue(tournament.getPlayers().contains(p3));
    }

    @Test
    void shouldThrowExceptionWhenAddingPlayerToNullTournament() {
        TournamentService service = new TournamentService();

        assertThrows(IllegalArgumentException.class, () -> service.addPlayer(null, new Player("A")));
    }

    @Test
    void shouldThrowExceptionWhenAddingNullPlayer() {
        TournamentService service = new TournamentService();

        Player p1 = new Player("A");
        Player p2 = new Player("B");

        Tournament tournament = service.createTournament(List.of(p1, p2), TournamentType.KNOCKOUT);

        assertThrows(IllegalArgumentException.class, () -> service.addPlayer(tournament, null));
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
        Match match = round.getMatches().get(0);
        match.setPoints(1, 0);

        assertTrue(service.isFinished(tournament));
        assertEquals(TournamentState.FINISHED, tournament.getState());
    }

    @Test
    void shouldReturnTrueWhenFinalRoundDrawIsResolved() {
        TournamentService service = new TournamentService();

        Player p1 = new Player("A");
        Player p2 = new Player("B");

        Tournament tournament = service.createTournament(List.of(p1, p2), TournamentType.KNOCKOUT);

        service.startTournament(tournament);

        Round round = service.generateNextRound(tournament);
        Match match = round.getMatches().get(0);
        match.setPoints(1, 1);
        match.resolveDraw(p2);

        assertTrue(service.isFinished(tournament));
        assertEquals(TournamentState.FINISHED, tournament.getState());
    }

    @Test
    void shouldReturnFalseWhenFinalRoundHasDraw() {
        TournamentService service = new TournamentService();

        Player p1 = new Player("A");
        Player p2 = new Player("B");

        Tournament tournament = service.createTournament(List.of(p1, p2), TournamentType.KNOCKOUT);

        service.startTournament(tournament);

        Round round = service.generateNextRound(tournament);
        Match match = round.getMatches().get(0);
        match.setPoints(1, 1);

        assertFalse(service.isFinished(tournament));
        assertEquals(TournamentState.STARTED, tournament.getState());
    }

    @Test
    void shouldNotFinishSwissTournamentAutomaticallyAfterRound() {
        TournamentService service = new TournamentService();

        Player p1 = new Player("A");
        Player p2 = new Player("B");

        Tournament tournament = service.createTournament(List.of(p1, p2), TournamentType.SWISS);

        service.startTournament(tournament);

        Round round = service.generateNextRound(tournament);
        Match match = round.getMatches().get(0);
        match.setPoints(1, 0);

        assertFalse(service.isFinished(tournament));
        assertEquals(TournamentState.STARTED, tournament.getState());
    }

    @Test
    void shouldReturnTrueWhenTournamentIsAlreadyMarkedFinished() {
        TournamentService service = new TournamentService();

        Player p1 = new Player("A");
        Player p2 = new Player("B");

        Tournament tournament = service.createTournament(List.of(p1, p2), TournamentType.KNOCKOUT);

        service.startTournament(tournament);
        tournament.finish();

        assertTrue(service.isFinished(tournament));
    }

    @Test
    void shouldThrowExceptionWhenCheckingIfNullTournamentIsFinished() {
        TournamentService service = new TournamentService();

        assertThrows(IllegalArgumentException.class, () -> service.isFinished(null));
    }

    @Test
    void shouldSimulateRoundWithoutChangingByeMatch() {
        TournamentService service = new TournamentService();

        Player p1 = new Player("A");
        Player p2 = new Player("B");
        Player p3 = new Player("C");

        Tournament tournament = service.createTournament(List.of(p1, p2, p3), TournamentType.KNOCKOUT);
        service.startTournament(tournament);
        Round round = service.generateNextRound(tournament);

        Match byeMatch = round.getMatches().stream()
                .filter(Match::isByeMatch)
                .findFirst()
                .orElseThrow();

        service.simulateRound(tournament, round);

        assertTrue(byeMatch.isPlayed());
        assertTrue(byeMatch.isByeMatch());
        assertEquals("-", byeMatch.getScore());
    }

    @Test
    void shouldAlwaysProduceWinnerWhenSimulatingKnockoutRound() {
        TournamentService service = new TournamentService();

        Player p1 = new Player("A");
        Player p2 = new Player("B");

        Tournament tournament = service.createTournament(List.of(p1, p2), TournamentType.KNOCKOUT);
        service.startTournament(tournament);
        Round round = service.generateNextRound(tournament);
        Match match = round.getMatches().get(0);

        service.simulateRound(tournament, round);

        assertTrue(match.isPlayed());
        assertNotNull(match.getWinner());
    }
}
