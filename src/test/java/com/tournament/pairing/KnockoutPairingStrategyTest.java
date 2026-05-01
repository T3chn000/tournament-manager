package com.tournament.pairing;

import com.tournament.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class KnockoutPairingStrategyTest {

    @Test
    void shouldThrowExceptionWhenTournamentIsNull() {
        PairingStrategy strategy = new KnockoutPairingStrategy(new Random(1));

        assertThrows(IllegalArgumentException.class, () -> strategy.generateNextRound(null));
    }

    @Test
    void shouldGenerateFirstRoundForEvenNumberOfPlayers() {
        Player p1 = new Player("A");
        Player p2 = new Player("B");
        Player p3 = new Player("C");
        Player p4 = new Player("D");

        Tournament tournament = new Tournament(List.of(p1, p2, p3, p4), TournamentType.KNOCKOUT);

        PairingStrategy strategy = new KnockoutPairingStrategy(new Random(1));
        Round round = strategy.generateNextRound(tournament);

        assertEquals(1, round.getRoundNumber());
        assertEquals(2, round.size());

        assertTrue(round.containsPlayer(p1));
        assertTrue(round.containsPlayer(p2));
        assertTrue(round.containsPlayer(p3));
        assertTrue(round.containsPlayer(p4));
    }

    @Test
    void shouldGenerateFirstRoundForOddNumberOfPlayers() {
        Player p1 = new Player("A");
        Player p2 = new Player("B");
        Player p3 = new Player("C");
        Player p4 = new Player("D");
        Player p5 = new Player("E");

        Tournament tournament = new Tournament(List.of(p1, p2, p3, p4, p5), TournamentType.KNOCKOUT);

        PairingStrategy strategy = new KnockoutPairingStrategy(new Random(1));
        Round round = strategy.generateNextRound(tournament);

        assertEquals(1, round.getRoundNumber());

        boolean hasByeMatch = round.getMatches().stream().anyMatch(match -> match.hasPlayer(Player.BYE));

        assertTrue(hasByeMatch);
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

        PairingStrategy strategy = new KnockoutPairingStrategy(new Random(1));
        Round secondRound = strategy.generateNextRound(tournament);

        assertEquals(2, secondRound.getRoundNumber());
        assertEquals(1, secondRound.size());

        Match finalMatch = secondRound.getMatches().getFirst();

        assertTrue(finalMatch.hasPlayer(p1));
        assertTrue(finalMatch.hasPlayer(p4));
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

        PairingStrategy strategy = new KnockoutPairingStrategy(new Random(1));

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

        PairingStrategy strategy = new KnockoutPairingStrategy(new Random(1));

        assertThrows(IllegalStateException.class, () -> strategy.generateNextRound(tournament));
    }

    @Test
    void shouldThrowExceptionWhenTournamentIsAlreadyFinished() {
        Player p1 = new Player("A");
        Player p2 = new Player("B");

        Tournament tournament = new Tournament(List.of(p1, p2), TournamentType.KNOCKOUT);

        Match finalMatch = new Match(p1, p2, 1, 0);

        Round finalRound = new Round(1, List.of(finalMatch));
        tournament.addRound(finalRound);

        PairingStrategy strategy = new KnockoutPairingStrategy(new Random(1));

        assertThrows(IllegalStateException.class, () -> strategy.generateNextRound(tournament));
    }
}