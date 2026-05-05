package com.tournament.pairing;

import com.tournament.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SwissPairingStrategyTest {

    @Test
    void shouldThrowExceptionWhenTournamentIsNull() {
        PairingStrategy strategy = new SwissPairingStrategy();

        assertThrows(IllegalArgumentException.class, () -> strategy.generateNextRound(null));
    }

    @Test
    void shouldGenerateFirstRoundForEvenNumberOfPlayers() {
        Player p1 = new Player("A");
        Player p2 = new Player("B");
        Player p3 = new Player("C");
        Player p4 = new Player("D");

        Tournament tournament = new Tournament(List.of(p1, p2, p3, p4), TournamentType.SWISS);

        PairingStrategy strategy = new SwissPairingStrategy();
        Round round = strategy.generateNextRound(tournament);

        assertEquals(1, round.getRoundNumber());
        assertEquals(2, round.size());

        assertTrue(round.containsPlayer(p1));
        assertTrue(round.containsPlayer(p2));
        assertTrue(round.containsPlayer(p3));
        assertTrue(round.containsPlayer(p4));
    }

    @Test
    void shouldGenerateByeForOddNumberOfPlayers() {
        Player p1 = new Player("A");
        Player p2 = new Player("B");
        Player p3 = new Player("C");

        Tournament tournament = new Tournament(List.of(p1, p2, p3), TournamentType.SWISS);

        PairingStrategy strategy = new SwissPairingStrategy();
        Round round = strategy.generateNextRound(tournament);

        assertEquals(1, round.getRoundNumber());
        assertEquals(2, round.size());

        long byeMatches = round.getMatches().stream()
                .filter(Match::isByeMatch)
                .count();

        assertEquals(1, byeMatches);
    }

    @Test
    void shouldThrowExceptionWhenPreviousRoundIsNotFinished() {
        Player p1 = new Player("A");
        Player p2 = new Player("B");
        Player p3 = new Player("C");
        Player p4 = new Player("D");

        Tournament tournament = new Tournament(List.of(p1, p2, p3, p4), TournamentType.SWISS);

        Match match1 = new Match(p1, p2, 2, 0);
        Match match2 = new Match(p3, p4);
        // match2 bez wyniku

        Round firstRound = new Round(1, List.of(match1, match2));
        tournament.addRound(firstRound);

        PairingStrategy strategy = new SwissPairingStrategy();

        assertThrows(IllegalStateException.class, () -> strategy.generateNextRound(tournament));
    }

    @Test
    void shouldGenerateSecondRoundAfterFinishedFirstRound() {
        Player p1 = new Player("A");
        Player p2 = new Player("B");
        Player p3 = new Player("C");
        Player p4 = new Player("D");

        Tournament tournament = new Tournament(List.of(p1, p2, p3, p4), TournamentType.SWISS);

        Match match1 = new Match(p1, p2, 2, 0);
        Match match2 = new Match(p3, p4, 0, 2);


        tournament.addRound(new Round(1, List.of(match1, match2)));

        PairingStrategy strategy = new SwissPairingStrategy();
        Round secondRound = strategy.generateNextRound(tournament);

        assertEquals(2, secondRound.getRoundNumber());
        assertEquals(2, secondRound.size());
    }

    @Test
    void shouldAvoidRepeatedPairingIfPossible() {
        Player p1 = new Player("A");
        Player p2 = new Player("B");
        Player p3 = new Player("C");
        Player p4 = new Player("D");

        Tournament tournament = new Tournament(List.of(p1, p2, p3, p4), TournamentType.SWISS);

        Match match1 = new Match(p1, p2, 2, 0);
        Match match2 = new Match(p3, p4, 2, 0);

        tournament.addRound(new Round(1, List.of(match1, match2)));

        PairingStrategy strategy = new SwissPairingStrategy();
        Round secondRound = strategy.generateNextRound(tournament);

        for (Match match : secondRound.getMatches()) {
            assertFalse(match.hasPlayer(p1) && match.hasPlayer(p2));
            assertFalse(match.hasPlayer(p3) && match.hasPlayer(p4));
        }
    }

    @Test
    void shouldPairPlayersWithSimilarScores() {
        Player p1 = new Player("A");
        Player p2 = new Player("B");
        Player p3 = new Player("C");
        Player p4 = new Player("D");

        Tournament tournament = new Tournament(List.of(p1, p2, p3, p4), TournamentType.SWISS);

        Match match1 = new Match(p1, p2, 2, 0);
        Match match2 = new Match(p3, p4, 2 ,0);

        tournament.addRound(new Round(1, List.of(match1, match2)));

        PairingStrategy strategy = new SwissPairingStrategy();
        Round secondRound = strategy.generateNextRound(tournament);

        boolean winnersPlayTogether = secondRound.getMatches().stream()
                .anyMatch(match -> match.hasPlayer(p1) && match.hasPlayer(p3));

        boolean losersPlayTogether = secondRound.getMatches().stream()
                .anyMatch(match -> match.hasPlayer(p2) && match.hasPlayer(p4));

        assertTrue(winnersPlayTogether);
        assertTrue(losersPlayTogether);
    }

    @Test
    void shouldNotGiveByeAgainIfAnotherPlayerWithoutByeExists() {
        Player p1 = new Player("A");
        Player p2 = new Player("B");
        Player p3 = new Player("C");
        Player p4 = new Player("D");
        Player p5 = new Player("E");

        Tournament tournament = new Tournament(List.of(p1, p2, p3, p4, p5), TournamentType.SWISS);

        Match match1 = new Match(p1, p2, 2, 0);
        Match match2 = new Match(p3, p4, 2, 0);
        Match byeMatch = new Match(p5, Player.BYE);

        tournament.addRound(new Round(1, List.of(match1, match2, byeMatch)));

        PairingStrategy strategy = new SwissPairingStrategy();
        Round secondRound = strategy.generateNextRound(tournament);

        Match newByeMatch = secondRound.getMatches().stream()
                .filter(Match::isByeMatch)
                .findFirst()
                .orElseThrow();

        assertFalse(newByeMatch.hasPlayer(p5));
    }

    @Test
    void shouldAllowDrawsAsFinishedMatchesInSwiss() {
        Player p1 = new Player("A");
        Player p2 = new Player("B");
        Player p3 = new Player("C");
        Player p4 = new Player("D");

        Tournament tournament = new Tournament(List.of(p1, p2, p3, p4), TournamentType.SWISS);

        Match match1 = new Match(p1, p2, 1, 1);
        Match match2 = new Match(p3, p4, 2, 0);

        tournament.addRound(new Round(1, List.of(match1, match2)));

        PairingStrategy strategy = new SwissPairingStrategy();

        assertDoesNotThrow(() -> strategy.generateNextRound(tournament));
    }
}