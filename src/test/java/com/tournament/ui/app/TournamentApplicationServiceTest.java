package com.tournament.ui.app;

import com.tournament.model.Player;
import com.tournament.model.PlayerDirectory;
import com.tournament.model.Tournament;
import com.tournament.model.TournamentType;
import com.tournament.persistence.PlayerDirectoryRepository;
import com.tournament.persistence.TournamentRepository;
import com.tournament.service.TournamentService;
import com.tournament.ui.viewmodel.PlayerRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TournamentApplicationServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void shouldImportPlayersFromSavedTournamentsIntoDirectoryOnLoad() throws IOException {
        Player alice = new Player("Alice");
        Player bob = new Player("Bob");
        Tournament saved = new Tournament("Old Cup", List.of(alice, bob), TournamentType.SWISS);
        TournamentRepository tournamentRepository = new TournamentRepository(tempDir.resolve("tournaments"));
        tournamentRepository.save(saved);
        PlayerDirectoryRepository playerRepository = new PlayerDirectoryRepository(tempDir.resolve("players").resolve("players.json"));
        TournamentApplicationService service = createService(tournamentRepository, playerRepository);

        service.loadSavedData();

        List<PlayerRow> players = service.getPlayers();
        assertEquals(2, players.size());
        assertTrue(players.stream().anyMatch(player -> player.playerId().equals(alice.playerId())));
        assertTrue(players.stream().anyMatch(player -> player.playerId().equals(bob.playerId())));
        assertEquals(2, playerRepository.load().getPlayers().size());
    }

    @Test
    void shouldReuseSamePlayerIdWhenCreatingTournamentsFromSameName() {
        TournamentApplicationService service = createService();

        Tournament first = service.createTournament("First", TournamentType.SWISS, List.of("Alice", "Bob"));
        Tournament second = service.createTournament("Second", TournamentType.KNOCKOUT, List.of(" alice ", "Charlie"));

        Player firstAlice = first.getPlayers().stream()
                .filter(player -> player.name().equals("Alice"))
                .findFirst()
                .orElseThrow();
        Player secondAlice = second.getPlayers().stream()
                .filter(player -> player.name().equals("Alice"))
                .findFirst()
                .orElseThrow();
        assertEquals(firstAlice.playerId(), secondAlice.playerId());
        assertEquals(3, service.getPlayers().size());
    }

    @Test
    void shouldAddExistingPlayerToMultipleCreatedTournaments() {
        TournamentApplicationService service = createService();
        Player alice = service.createPlayer("Alice");
        Player bob = service.createPlayer("Bob");
        Player charlie = service.createPlayer("Charlie");
        Tournament first = service.createTournamentWithPlayers("First", TournamentType.SWISS, List.of(alice, bob));
        Tournament second = service.createTournamentWithPlayers("Second", TournamentType.SWISS, List.of(bob, charlie));

        service.addPlayer(first, charlie);
        service.addPlayer(second, alice);

        assertTrue(first.getPlayers().contains(charlie));
        assertTrue(second.getPlayers().contains(alice));
    }

    @Test
    void shouldRejectAddingSameExistingPlayerTwiceToTournament() {
        TournamentApplicationService service = createService();
        Player alice = service.createPlayer("Alice");
        Player bob = service.createPlayer("Bob");
        Tournament tournament = service.createTournamentWithPlayers("Cup", TournamentType.SWISS, List.of(alice, bob));

        UiActionException exception = assertThrows(UiActionException.class, () -> service.addPlayer(tournament, alice));

        assertEquals("Player already exists in tournament", exception.getMessage());
    }

    @Test
    void shouldRejectCreatingTournamentWithDuplicatePlayerNames() {
        TournamentApplicationService service = createService();
        Player alice = new Player("Alice");
        Player duplicateAlice = new Player(" alice ");

        UiActionException exception = assertThrows(
                UiActionException.class,
                () -> service.createTournamentWithPlayers("Cup", TournamentType.SWISS, List.of(alice, duplicateAlice))
        );

        assertEquals("Player names must be unique", exception.getMessage());
    }

    @Test
    void shouldRejectAddingPlayerWithDuplicateNameToTournament() {
        TournamentApplicationService service = createService();
        Player alice = service.createPlayer("Alice");
        Player bob = service.createPlayer("Bob");
        Player duplicateAlice = new Player(" alice ");
        Tournament tournament = service.createTournamentWithPlayers("Cup", TournamentType.SWISS, List.of(alice, bob));

        UiActionException exception = assertThrows(UiActionException.class, () -> service.addPlayer(tournament, duplicateAlice));

        assertEquals("Player names must be unique", exception.getMessage());
    }

    @Test
    void shouldRejectAddingBatchWithDuplicateNamesWithoutChangingTournament() {
        TournamentApplicationService service = createService();
        Player alice = service.createPlayer("Alice");
        Player bob = service.createPlayer("Bob");
        Player charlie = new Player("Charlie");
        Player duplicateCharlie = new Player(" charlie ");
        Tournament tournament = service.createTournamentWithPlayers("Cup", TournamentType.SWISS, List.of(alice, bob));

        UiActionException exception = assertThrows(
                UiActionException.class,
                () -> service.addPlayers(tournament, List.of(charlie, duplicateCharlie))
        );

        assertEquals("Player names must be unique", exception.getMessage());
        assertEquals(List.of(alice, bob), tournament.getPlayers());
    }

    private TournamentApplicationService createService() {
        return createService(
                new TournamentRepository(tempDir.resolve("tournaments")),
                new PlayerDirectoryRepository(tempDir.resolve("players").resolve("players.json"))
        );
    }

    private TournamentApplicationService createService(TournamentRepository tournamentRepository, PlayerDirectoryRepository playerRepository) {
        return new TournamentApplicationService(
                new TournamentService(),
                new RankingCalculator(),
                tournamentRepository,
                playerRepository,
                new PlayerDirectory()
        );
    }
}
