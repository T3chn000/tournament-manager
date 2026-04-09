package com.tournament.pairing;

import com.tournament.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KnockoutPairingStrategyTest {

    @Test
    void shouldThrowExceptionWhenTournamentIsNull() {
        PairingStrategy strategy = new KnockoutPairingStrategy();

        assertThrows(IllegalArgumentException.class, () -> strategy.generateNextRound(null));
    }

    @Test
    void shouldGenerateFirstRoundForEvenNumberOfPlayers() {
        Player p1 = new Player("A");
        Player p2 = new Player("B");
        Player p3 = new Player("C");
        Player p4 = new Player("D");

        Tournament tournament = new Tournament(List.of(p1, p2, p3, p4), TournamentType.KNOCKOUT);

        PairingStrategy strategy = new KnockoutPairingStrategy();
        Round round = strategy.generateNextRound(tournament);

        assertEquals(1, round.getRoundNumber());
        assertEquals(2, round.size());

        Match match1 = round.getMatches().get(0);
        Match match2 = round.getMatches().get(1);

        assertEquals(p1, match1.getPlayer1());
        assertEquals(p2, match1.getPlayer2());
        assertEquals(p3, match2.getPlayer1());
        assertEquals(p4, match2.getPlayer2());
    }

    @Test
    void shouldGenerateFirstRoundForOddNumberOfPlayers() {
        Player p1 = new Player("A");
        Player p2 = new Player("B");
        Player p3 = new Player("C");
        Player p4 = new Player("D");
        Player p5 = new Player("E");

        Tournament tournament = new Tournament(List.of(p1, p2, p3, p4, p5), TournamentType.KNOCKOUT);

        PairingStrategy strategy = new KnockoutPairingStrategy();
        Round round = strategy.generateNextRound(tournament);

        assertEquals(1, round.getRoundNumber());
        assertEquals(3, round.size());

        Match match1 = round.getMatches().get(0);
        Match match2 = round.getMatches().get(1);
        Match match3 = round.getMatches().get(2);

        assertEquals(p1, match1.getPlayer1());
        assertEquals(p2, match1.getPlayer2());
        assertEquals(p3, match2.getPlayer1());
        assertEquals(p4, match2.getPlayer2());
        assertEquals(p5, match3.getPlayer1());
        assertEquals(Player.BYE, match3.getPlayer2());
    }

    @Test
    void shouldGenerateSecondRoundFromWinnersOfPreviousRound() {
        Player p1 = new Player("A");
        Player p2 = new Player("B");
        Player p3 = new Player("C");
        Player p4 = new Player("D");

        Tournament tournament = new Tournament(List.of(p1, p2, p3, p4), TournamentType.KNOCKOUT);

        Match match1 = new Match(p1, p2, 1, 0);
        Match match2 = new Match(p3, p4, 0, 1);

        Round firstRound = new Round(1, List.of(match1, match2));
        tournament.addRound(firstRound);

        PairingStrategy strategy = new KnockoutPairingStrategy();
        Round secondRound = strategy.generateNextRound(tournament);

        assertEquals(2, secondRound.getRoundNumber());
        assertEquals(1, secondRound.size());

        Match finalMatch = secondRound.getMatches().getFirst();
        assertEquals(p1, finalMatch.getPlayer1());
        assertEquals(p4, finalMatch.getPlayer2());
    }

    @Test
    void shouldThrowExceptionWhenPreviousRoundIsNotFinished() {
        Player p1 = new Player("A");
        Player p2 = new Player("B");
        Player p3 = new Player("C");
        Player p4 = new Player("D");

        Tournament tournament = new Tournament(List.of(p1, p2, p3, p4), TournamentType.KNOCKOUT);

        Match match1 = new Match(p1, p2, 1, 0);
        Match match2 = new Match(p3, p4); // match2 bez wyniku

        Round firstRound = new Round(1, List.of(match1, match2));
        tournament.addRound(firstRound);

        PairingStrategy strategy = new KnockoutPairingStrategy();

        assertThrows(IllegalStateException.class, () -> strategy.generateNextRound(tournament));
    }

    @Test
    void shouldThrowExceptionWhenPreviousRoundContainsDraw() {
        Player p1 = new Player("A");
        Player p2 = new Player("B");

        Tournament tournament = new Tournament(List.of(p1, p2), TournamentType.KNOCKOUT);

        Match match = new Match(p1, p2, 0, 0);

        Round firstRound = new Round(1, List.of(match));
        tournament.addRound(firstRound);

        PairingStrategy strategy = new KnockoutPairingStrategy();

        assertThrows(IllegalStateException.class, () -> strategy.generateNextRound(tournament));
    }

    @Test
    void shouldGenerateRoundNumberBasedOnExistingRounds() {
        Player p1 = new Player("A");
        Player p2 = new Player("B");
        Player p3 = new Player("C");
        Player p4 = new Player("D");

        Tournament tournament = new Tournament(List.of(p1, p2, p3, p4), TournamentType.KNOCKOUT);

        Match match1 = new Match(p1, p2, 0, 1);
        Match match2 = new Match(p3, p4, 1, 0);

        Round round1 = new Round(1, List.of(match1, match2));
        tournament.addRound(round1);

        PairingStrategy strategy = new KnockoutPairingStrategy();
        Round round2 = strategy.generateNextRound(tournament);

        assertEquals(2, round2.getRoundNumber());
    }

    @Test
    void shouldUseAllPlayersInFirstRound() {
        Player p1 = new Player("A");
        Player p2 = new Player("B");
        Player p3 = new Player("C");
        Player p4 = new Player("D");

        Tournament tournament = new Tournament(List.of(p1, p2, p3, p4), TournamentType.KNOCKOUT);

        PairingStrategy strategy = new KnockoutPairingStrategy();
        Round round = strategy.generateNextRound(tournament);

        assertTrue(round.containsPlayer(p1));
        assertTrue(round.containsPlayer(p2));
        assertTrue(round.containsPlayer(p3));
        assertTrue(round.containsPlayer(p4));
    }
}