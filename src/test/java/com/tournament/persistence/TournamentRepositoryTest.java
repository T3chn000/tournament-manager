package com.tournament.persistence;

import com.tournament.model.Match;
import com.tournament.model.MatchResult;
import com.tournament.model.Player;
import com.tournament.model.Round;
import com.tournament.model.Tournament;
import com.tournament.model.TournamentState;
import com.tournament.model.TournamentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TournamentRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void shouldSaveTournamentWithoutDerivedResult() throws IOException {
        Player alice = new Player("Alice");
        Player bob = new Player("Bob");
        Match match = new Match(alice, bob, 2, 1);
        Tournament tournament = new Tournament("Cup", List.of(alice, bob), TournamentType.KNOCKOUT);
        tournament.start();
        tournament.addRound(new Round(1, List.of(match)));
        TournamentRepository repository = new TournamentRepository(tempDir);

        repository.save(tournament);

        String json = Files.readString(tempDir.resolve("tournament_" + tournament.getTournamentId() + ".json"));
        assertTrue(json.contains("\"tournamentId\""));
        assertTrue(json.contains("\"matches\""));
        assertTrue(json.contains("\"tieBreakWinner\""));
        assertFalse(json.contains("\"result\""));
    }

    @Test
    void shouldLoadTournamentAndRebuildDerivedMatchResult() throws IOException {
        Player alice = new Player("Alice");
        Player bob = new Player("Bob");
        Match match = new Match(alice, bob, 3, 1);
        Tournament tournament = new Tournament("Cup", List.of(alice, bob), TournamentType.SWISS);
        tournament.start();
        tournament.addRound(new Round(1, List.of(match)));
        TournamentRepository repository = new TournamentRepository(tempDir);

        repository.save(tournament);
        Tournament loaded = repository.load().getFirst();
        Match loadedMatch = loaded.getRounds().getFirst().getMatches().getFirst();

        assertEquals(tournament.getTournamentId(), loaded.getTournamentId());
        assertEquals(TournamentState.STARTED, loaded.getState());
        assertEquals(MatchResult.PLAYER1_WIN, loadedMatch.getResult());
        assertEquals(alice, loadedMatch.getWinner());
    }

    @Test
    void shouldLoadResolvedDrawAndKeepTieBreakWinner() throws IOException {
        Player alice = new Player("Alice");
        Player bob = new Player("Bob");
        Match match = new Match(alice, bob, 2, 2);
        match.resolveDraw(bob);
        Tournament tournament = new Tournament("Cup", List.of(alice, bob), TournamentType.KNOCKOUT);
        tournament.start();
        tournament.addRound(new Round(1, List.of(match)));
        TournamentRepository repository = new TournamentRepository(tempDir);

        repository.save(tournament);
        Tournament loaded = repository.load().getFirst();
        Match loadedMatch = loaded.getRounds().getFirst().getMatches().getFirst();

        assertEquals(MatchResult.DRAW, loadedMatch.getResult());
        assertEquals(bob, loadedMatch.getTieBreakWinner());
        assertEquals(bob, loadedMatch.getWinner());
    }
}
