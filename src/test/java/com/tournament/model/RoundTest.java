package com.tournament.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoundTest {

    private final Player p1 = new Player("A");
    private final Player p2 = new Player("B");
    private final Player p3 = new Player("C");
    private final Player p4 = new Player("D");

    @Test
    void shouldCreateRoundWithValidData() {
        Match match1 = new Match(p1, p2);
        Match match2 = new Match(p3, p4);

        Round round = new Round(1, List.of(match1, match2));

        assertEquals(1, round.getRoundNumber());
        assertEquals(2, round.size());
        assertEquals(2, round.getMatches().size());
    }

    @Test
    void shouldThrowExceptionWhenRoundNumberIsZero() {
        Match match = new Match(p1, p2);

        assertThrows(IllegalArgumentException.class, () -> new Round(0, List.of(match)));
    }

    @Test
    void shouldThrowExceptionWhenMatchesAreNull() {
        assertThrows(IllegalArgumentException.class, () -> new Round(1, null));
    }

    @Test
    void shouldThrowExceptionWhenMatchesAreEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new Round(1, List.of()));
    }

    @Test
    void matchesListShouldBeImmutable() {
        Match match = new Match(p1, p2);

        Round round = new Round(1, List.of(match));

        assertThrows(UnsupportedOperationException.class, () -> round.getMatches().add(new Match(p3, p4)));
    }

    @Test
    void shouldReturnFalseWhenMatchNotPlayed() {
        Match match1 = new Match(p1, p2);
        Match match2 = new Match(p3, p4);
        Round round = new Round(1, List.of(match1, match2));

        assertFalse(round.isFinished());
        assertTrue(round.hasUnplayedMatches());
    }

    @Test
    void shouldReturnTrueWhenAllMatchesPlayed() {
        Match match1 = new Match(p1, p2);
        Match match2 = new Match(p3, p4);
        match1.setPoints(2, 1);
        match2.setPoints(1, 2);

        Round round = new Round(1, List.of(match1, match2));

        assertTrue(round.isFinished());
        assertFalse(round.hasUnplayedMatches());
    }

    @Test
    void shouldDetectDraw() {
        Match match = new Match(p1, p2);
        match.setPoints(1, 1);

        Round round = new Round(1, List.of(match));

        assertTrue(round.hasDraws());
    }

    @Test
    void shouldReturnFalseWhenNoDraws() {
        Match match = new Match(p1, p2);
        match.setPoints(1, 0);

        Round round = new Round(1, List.of(match));

        assertFalse(round.hasDraws());
    }

    @Test
    void shouldReturnUnplayedMatches() {
        Match match1 = new Match(p1, p2);
        Match match2 = new Match(p3, p4);

        match1.setPoints(1, 0);

        Round round = new Round(1, List.of(match1, match2));

        List<Match> unresolved = round.getUnresolvedMatches();

        assertEquals(1, unresolved.size());
        assertTrue(unresolved.contains(match2));
    }

    @Test
    void shouldTreatDrawAsUnresolved() {
        Match match = new Match(p1, p2);
        match.setPoints(1, 1);

        Round round = new Round(1, List.of(match));

        List<Match> unresolved = round.getUnresolvedMatches();

        assertEquals(1, unresolved.size());
    }

    @Test
    void shouldReturnWinnersWhenFinished() {
        Match match1 = new Match(p1, p2);
        Match match2 = new Match(p3, p4);

        match1.setPoints(1, 0);
        match2.setPoints(0, 1);

        Round round = new Round(1, List.of(match1, match2));

        List<Player> winners = round.getWinners();

        assertEquals(2, winners.size());
        assertTrue(winners.contains(p1));
        assertTrue(winners.contains(p4));
        assertFalse(winners.contains(p2));
        assertFalse(winners.contains(p3));
    }

    @Test
    void shouldThrowExceptionWhenGettingWinnersFromUnfinishedRound() {
        Match match = new Match(p1, p2);
        Round round = new Round(1, List.of(match));

        assertThrows(IllegalStateException.class, round::getWinners);
    }

    @Test
    void shouldNotIncludeNullWinnerForDraw() {
        Match match = new Match(p1, p2);
        match.setPoints(1, 1);

        Round round = new Round(1, List.of(match));

        List<Player> winners = round.getWinners();

        assertTrue(winners.isEmpty());
    }

    @Test
    void shouldReturnTrueIfPlayerIsInRound() {
        Match match = new Match(p1, p2);
        Round round = new Round(1, List.of(match));

        assertTrue(round.containsPlayer(p1));
        assertTrue(round.containsPlayer(p2));
    }

    @Test
    void shouldReturnFalseIfPlayerIsNotInRound() {
        Match match = new Match(p1, p2);
        Round round = new Round(1, List.of(match));

        assertFalse(round.containsPlayer(p3));
    }

    @Test
    void toStringShouldShowRoundNumberAndMatches() {
        Match match = new Match(p1, p2);
        Round round = new Round(1, List.of(match));

        String result = round.toString();

        assertTrue(result.contains("Round 1"));
        assertTrue(result.contains("A"));
        assertTrue(result.contains("B"));
    }
}
