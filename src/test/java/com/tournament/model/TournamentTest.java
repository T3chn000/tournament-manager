package com.tournament.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TournamentTest {

    @Test
    void shouldCreateTournamentWithValidData() {
        Player p1 = new Player("A");
        Player p2 = new Player("B");

        Tournament tournament = new Tournament(List.of(p1, p2), TournamentType.KNOCKOUT);

        assertEquals(2, tournament.getPlayers().size());
        assertEquals(TournamentType.KNOCKOUT, tournament.getType());
        assertEquals(0, tournament.getRoundCount());
        assertNull(tournament.getCurrentRound());
        assertFalse(tournament.isStarted());
    }

    @Test
    void shouldThrowExceptionWhenPlayersAreNull() {
        assertThrows(IllegalArgumentException.class, () -> new Tournament(null, TournamentType.KNOCKOUT));
    }

    @Test
    void shouldThrowExceptionWhenPlayersAreEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new Tournament(List.of(), TournamentType.KNOCKOUT));
    }

    @Test
    void shouldThrowExceptionWhenTournamentTypeIsNull() {
        Player p1 = new Player("A");

        assertThrows(IllegalArgumentException.class, () -> new Tournament(List.of(p1), null));
    }

    @Test
    void shouldReturnUnmodifiablePlayersList() {
        Player p1 = new Player("A");
        Player p2 = new Player("B");

        Tournament tournament = new Tournament(List.of(p1, p2), TournamentType.KNOCKOUT);

        assertThrows(UnsupportedOperationException.class, () -> tournament.getPlayers().add(new Player("C")));
    }

    @Test
    void shouldReturnUnmodifiableRoundsList() {
        Player p1 = new Player("A");
        Player p2 = new Player("B");

        Tournament tournament = new Tournament(List.of(p1, p2), TournamentType.KNOCKOUT);

        Match match = new Match(p1, p2);
        Round round = new Round(1, List.of(match));

        assertThrows(UnsupportedOperationException.class, () -> tournament.getRounds().add(round));
    }

    @Test
    void shouldReturnNullWhenThereIsNoCurrentRound() {
        Player p1 = new Player("A");
        Player p2 = new Player("B");

        Tournament tournament = new Tournament(List.of(p1, p2), TournamentType.KNOCKOUT);

        assertNull(tournament.getCurrentRound());
    }

    @Test
    void shouldAddRound() {
        Player p1 = new Player("A");
        Player p2 = new Player("B");

        Tournament tournament = new Tournament(List.of(p1, p2), TournamentType.KNOCKOUT);

        Match match = new Match(p1, p2);
        Round round = new Round(1, List.of(match));

        tournament.addRound(round);

        assertEquals(1, tournament.getRoundCount());
        assertEquals(round, tournament.getCurrentRound());
        assertEquals(1, tournament.getRounds().size());
    }

    @Test
    void shouldThrowExceptionWhenAddingNullRound() {
        Player p1 = new Player("A");
        Player p2 = new Player("B");

        Tournament tournament = new Tournament(List.of(p1, p2), TournamentType.KNOCKOUT);

        assertThrows(IllegalArgumentException.class, () -> tournament.addRound(null));
    }

    @Test
    void shouldReturnLastAddedRoundAsCurrentRound() {
        Player p1 = new Player("A");
        Player p2 = new Player("B");
        Player p3 = new Player("C");
        Player p4 = new Player("D");

        Tournament tournament = new Tournament(List.of(p1, p2, p3, p4), TournamentType.KNOCKOUT);

        Round round1 = new Round(1, List.of(new Match(p1, p2)));
        Round round2 = new Round(2, List.of(new Match(p3, p4)));

        tournament.addRound(round1);
        tournament.addRound(round2);

        assertEquals(2, tournament.getRoundCount());
        assertEquals(round2, tournament.getCurrentRound());
    }

    @Test
    void shouldStartTournament() {
        Player p1 = new Player("A");
        Player p2 = new Player("B");

        Tournament tournament = new Tournament(List.of(p1, p2), TournamentType.KNOCKOUT);

        tournament.start();

        assertTrue(tournament.isStarted());
    }

    @Test
    void shouldThrowExceptionWhenStartingAlreadyStartedTournament() {
        Player p1 = new Player("A");
        Player p2 = new Player("B");

        Tournament tournament = new Tournament(List.of(p1, p2), TournamentType.KNOCKOUT);

        tournament.start();

        assertThrows(IllegalStateException.class, tournament::start);
    }
}