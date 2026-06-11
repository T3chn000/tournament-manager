package com.tournament.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tournament.model.Match;
import com.tournament.model.MatchResult;
import com.tournament.model.Player;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MatchTest {

    private Player p1 = new Player(UUID.randomUUID(), "A");
    private Player p2 = new Player(UUID.randomUUID(), "B");

    @Test
    void shouldCreateMatchWithValidPlayers() {
        Match match = new Match(p1, p2);

        assertEquals(p1, match.getPlayer1());
        assertEquals(p2, match.getPlayer2());
        assertNull(match.getResult());
        assertFalse(match.isPlayed());
    }

    @Test
    void shouldThrowWhenPlayerIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new Match(null, p2));

        assertThrows(IllegalArgumentException.class,
                () -> new Match(p1, null));
    }

    @Test
    void shouldThrowWhenPlayersAreSame() {
        assertThrows(IllegalArgumentException.class,
                () -> new Match(p1, p1));
    }

    @Test
    void shouldAutoWinAgainstBye_player1Wins() {
        Match match = new Match(p1, Player.BYE);

        assertTrue(match.isByeMatch());
        assertEquals(MatchResult.PLAYER1_WIN, match.getResult());
        assertEquals(p1, match.getWinner());
    }

    @Test
    void shouldAutoWinAgainstBye_player2Wins() {
        Match match = new Match(Player.BYE, p2);

        assertTrue(match.isByeMatch());
        assertEquals(MatchResult.PLAYER2_WIN, match.getResult());
        assertEquals(p2, match.getWinner());
    }

    @Test
    void shouldNotAllowSettingPointsForByeMatch() {
        Match match = new Match(p1, Player.BYE);

        assertThrows(IllegalStateException.class,
                () -> match.setPoints(1, 0));
    }

    @Test
    void shouldSetPointsAndDetermineWinner_player1Wins() {
        Match match = new Match(p1, p2);

        match.setPoints(3, 1);

        assertEquals("3 : 1", match.getScore());
        assertEquals(MatchResult.PLAYER1_WIN, match.getResult());
        assertEquals(p1, match.getWinner());
        assertEquals(p2, match.getLoser());
        assertTrue(match.isPlayed());
    }

    @Test
    void shouldSetPointsAndDetermineWinner_player2Wins() {
        Match match = new Match(p1, p2);

        match.setPoints(1, 3);

        assertEquals(MatchResult.PLAYER2_WIN, match.getResult());
        assertEquals(p2, match.getWinner());
        assertEquals(p1, match.getLoser());
    }

    @Test
    void shouldDetectDraw() {
        Match match = new Match(p1, p2);

        match.setPoints(2, 2);

        assertTrue(match.isDraw());
        assertNull(match.getWinner());
        assertNull(match.getLoser());
    }

    @Test
    void shouldResolveDrawWithTieBreakWinner() {
        Match match = new Match(p1, p2);

        match.setPoints(1, 1);
        match.resolveDraw(p1);

        assertTrue(match.isDraw());
        assertEquals(p1, match.getWinner());
        assertEquals(p2, match.getLoser());
    }

    @Test
    void shouldSerializeMatchWithoutDerivedResult() throws Exception {
        Match match = new Match(p1, p2, 3, 1);

        String json = new ObjectMapper().writeValueAsString(match);

        assertFalse(json.contains("\"result\""));
    }

    @Test
    void shouldDeserializeResultFromPoints() throws Exception {
        Match match = new Match(p1, p2, 3, 1);
        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writeValueAsString(match);
        Match loadedMatch = mapper.readValue(json, Match.class);

        assertEquals(MatchResult.PLAYER1_WIN, loadedMatch.getResult());
        assertEquals(p1, loadedMatch.getWinner());
        assertTrue(loadedMatch.isPlayed());
    }

    @Test
    void shouldDeserializeResolvedDrawWithoutResultField() throws Exception {
        Match match = new Match(p1, p2, 2, 2);
        match.resolveDraw(p2);
        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writeValueAsString(match);
        Match loadedMatch = mapper.readValue(json, Match.class);

        assertFalse(json.contains("\"result\""));
        assertEquals(MatchResult.DRAW, loadedMatch.getResult());
        assertEquals(p2, loadedMatch.getWinner());
    }

    @Test
    void shouldThrowWhenResolvingMatchThatIsNotDraw() {
        Match match = new Match(p1, p2);
        match.setPoints(1, 0);

        assertThrows(IllegalStateException.class, () -> match.resolveDraw(p1));
    }

    @Test
    void shouldThrowWhenTieBreakWinnerIsNotInMatch() {
        Match match = new Match(p1, p2);
        match.setPoints(1, 1);

        assertThrows(IllegalArgumentException.class, () -> match.resolveDraw(new Player("Other")));
    }

    @Test
    void shouldThrowWhenPointsNegative() {
        Match match = new Match(p1, p2);

        assertThrows(IllegalArgumentException.class,
                () -> match.setPoints(-1, 2));

        assertThrows(IllegalArgumentException.class,
                () -> match.setPoints(2, -1));
    }

    @Test
    void shouldReturnDashWhenNotPlayed() {
        Match match = new Match(p1, p2);

        assertEquals("-", match.getScore());
    }

    @Test
    void shouldCreateMatchWithPoints() {
        Match match = new Match(p1, p2, 5, 3);

        assertEquals("5 : 3", match.getScore());
        assertEquals(MatchResult.PLAYER1_WIN, match.getResult());
        assertTrue(match.isPlayed());
    }

    @Test
    void shouldDetectPlayerPresence() {
        Match match = new Match(p1, p2);

        assertTrue(match.hasPlayer(p1));
        assertTrue(match.hasPlayer(p2));
        assertFalse(match.hasPlayer(new Player("Other")));
    }

    @Test
    void toStringShouldShowPlayersScoreAndWinner() {
        Match match = new Match(p1, p2);
        match.setPoints(2, 0);

        String result = match.toString();

        assertTrue(result.contains("A"));
        assertTrue(result.contains("B"));
        assertTrue(result.contains("2 : 0"));
        assertTrue(result.contains("winner: A"));
    }
}
