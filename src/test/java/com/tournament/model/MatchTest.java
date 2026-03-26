package test.java.com.tournament.model;

import main.java.com.tournament.model.Match;
import main.java.com.tournament.model.MatchResult;
import main.java.com.tournament.model.Player;
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
        assertNull(match.getMatchResult());
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
        assertEquals(MatchResult.PLAYER1_WIN, match.getMatchResult());
        assertEquals(p1, match.getWinner());
    }

    @Test
    void shouldAutoWinAgainstBye_player2Wins() {
        Match match = new Match(Player.BYE, p2);

        assertTrue(match.isByeMatch());
        assertEquals(MatchResult.PLAYER2_WIN, match.getMatchResult());
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
        assertEquals(MatchResult.PLAYER1_WIN, match.getMatchResult());
        assertEquals(p1, match.getWinner());
        assertEquals(p2, match.getLoser());
        assertTrue(match.isPlayed());
    }

    @Test
    void shouldSetPointsAndDetermineWinner_player2Wins() {
        Match match = new Match(p1, p2);

        match.setPoints(1, 3);

        assertEquals(MatchResult.PLAYER2_WIN, match.getMatchResult());
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
        assertEquals(MatchResult.PLAYER1_WIN, match.getMatchResult());
        assertTrue(match.isPlayed());
    }

    @Test
    void shouldDetectPlayerPresence() {
        Match match = new Match(p1, p2);

        assertTrue(match.hasPlayer(p1));
        assertTrue(match.hasPlayer(p2));
        assertFalse(match.hasPlayer(new Player("Other")));
    }
}