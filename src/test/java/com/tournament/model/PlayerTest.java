package com.tournament.model;

import com.tournament.model.Player;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    void shouldCreatePlayerWithValidData() {
        UUID id = UUID.randomUUID();
        Player player = new Player(id, "Osoba");

        assertEquals(id, player.playerId());
        assertEquals("Osoba", player.name());
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        UUID id = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class,
                () -> new Player(id, null));
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        UUID id = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class,
                () -> new Player(id, "   "));
    }

    @Test
    void shouldThrowExceptionWhenIdIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new Player(null, "Osoba"));
    }

    @Test
    void shouldCreatePlayerWithGeneratedId() {
        Player player = new Player("Osoba");

        assertNotNull(player.playerId());
        assertEquals("Osoba", player.name());
    }

    @Test
    void playersWithSameIdShouldBeEqual() {
        UUID id = UUID.randomUUID();

        Player p1 = new Player(id, "A");
        Player p2 = new Player(id, "B");

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void playersWithDifferentIdsShouldNotBeEqual() {
        Player p1 = new Player(UUID.randomUUID(), "A");
        Player p2 = new Player(UUID.randomUUID(), "A");

        assertNotEquals(p1, p2);
    }

    @Test
    void toStringShouldContainNameAndId() {
        UUID id = UUID.randomUUID();
        Player player = new Player(id, "Osoba");

        String result = player.toString();

        assertTrue(result.contains("Osoba"));
        assertTrue(result.contains(id.toString()));
    }

    @Test
    void byePlayerShouldHaveFixedValues() {
        Player bye = Player.BYE;

        assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000000"), bye.playerId());
        assertEquals("BYE", bye.name());
    }

    @Test
    void byePlayerShouldBeEqualToAnotherWithSameId() {
        Player other = new Player(
                UUID.fromString("00000000-0000-0000-0000-000000000000"),
                "Whatever"
        );

        assertEquals(Player.BYE, other);
    }
}