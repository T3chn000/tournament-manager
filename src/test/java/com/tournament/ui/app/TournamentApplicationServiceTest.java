package com.tournament.ui.app;

import com.tournament.model.Player;
import com.tournament.model.PlayerDirectory;
import com.tournament.model.Tournament;
import com.tournament.model.TournamentType;
import com.tournament.persistence.PlayerDirectoryRepository;
import com.tournament.persistence.TournamentRepository;
import com.tournament.service.TournamentService;
import com.tournament.ui.viewmodel.PlayerRow;
import com.tournament.ui.viewmodel.TournamentDetails;
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
    void shouldReuseSelectedPlayerInMultipleCreatedTournaments() {
        TournamentApplicationService service = createService();
        Player alice = service.createPlayer("Alice");
        Player bob = service.createPlayer("Bob");
        Player charlie = service.createPlayer("Charlie");

        Tournament first = service.createTournamentWithPlayers("First", TournamentType.SWISS, List.of(alice, bob));
        Tournament second = service.createTournamentWithPlayers("Second", TournamentType.KNOCKOUT, List.of(alice, charlie));

        assertTrue(first.getPlayers().contains(alice));
        assertTrue(second.getPlayers().contains(alice));
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

        service.addPlayers(first, List.of(charlie));
        service.addPlayers(second, List.of(alice));

        assertTrue(first.getPlayers().contains(charlie));
        assertTrue(second.getPlayers().contains(alice));
    }

    @Test
    void shouldRejectAddingSameExistingPlayerTwiceToTournament() {
        TournamentApplicationService service = createService();
        Player alice = service.createPlayer("Alice");
        Player bob = service.createPlayer("Bob");
        Tournament tournament = service.createTournamentWithPlayers("Cup", TournamentType.SWISS, List.of(alice, bob));

        UiActionException exception = assertThrows(UiActionException.class, () -> service.addPlayers(tournament, List.of(alice)));

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

        UiActionException exception = assertThrows(UiActionException.class, () -> service.addPlayers(tournament, List.of(duplicateAlice)));

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

    @Test
    void shouldShowRenamedPlayerInTournamentDetails() {
        TournamentApplicationService service = createService();
        Player alice = service.createPlayer("Alice");
        Player bob = service.createPlayer("Bob");
        Tournament tournament = service.createTournamentWithPlayers("Cup", TournamentType.SWISS, List.of(alice, bob));

        service.startTournament(tournament);
        service.generateNextRound(tournament);
        service.renamePlayer(alice, "Alicia");

        TournamentDetails details = service.getDetails(tournament);

        assertTrue(details.players().stream().anyMatch(player -> player.name().equals("Alicia")));
        assertTrue(details.rounds().getFirst().matches().stream()
                .anyMatch(match -> match.player1Name().equals("Alicia") || match.player2Name().equals("Alicia")));
    }

    @Test
    void shouldPersistCurrentPlayerNameInTournamentJsonAfterRename() throws IOException {
        TournamentRepository tournamentRepository = new TournamentRepository(tempDir.resolve("tournaments"));
        TournamentApplicationService service = createService(
                tournamentRepository,
                new PlayerDirectoryRepository(tempDir.resolve("players").resolve("players.json"))
        );
        Player alice = service.createPlayer("Alice");
        Player bob = service.createPlayer("Bob");
        Tournament tournament = service.createTournamentWithPlayers("Cup", TournamentType.SWISS, List.of(alice, bob));

        service.startTournament(tournament);
        service.generateNextRound(tournament);
        service.saveTournament(tournament);
        service.renamePlayer(alice, "Alicia");

        Tournament loaded = tournamentRepository.load().getFirst();

        assertTrue(loaded.getPlayers().stream().anyMatch(player -> player.name().equals("Alicia")));
        assertTrue(loaded.getRounds().getFirst().getMatches().stream()
                .anyMatch(match -> match.getPlayer1().name().equals("Alicia") || match.getPlayer2().name().equals("Alicia")));
        assertFalse(loaded.getPlayers().stream().anyMatch(player -> player.name().equals("Alice")));
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
