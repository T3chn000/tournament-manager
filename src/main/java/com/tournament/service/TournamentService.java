package com.tournament.service;

import com.tournament.model.*;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

/**
 * Domain service for common tournament operations.
 *
 * <p>The service validates null inputs, delegates round generation to the
 * tournament aggregate, and provides simple random simulation helpers.</p>
 */
public class TournamentService {
    private final Random random = new Random(); //used for simulation

    /**
     * Creates a tournament with the default name.
     *
     * @param players initial players
     * @param type tournament format
     * @return created tournament
     */
    public Tournament createTournament(List<Player> players, TournamentType type) {
        return new Tournament(players, type);
    }

    /**
     * Creates a named tournament.
     *
     * @param name tournament name
     * @param players initial players
     * @param type tournament format
     * @return created tournament
     */
    public Tournament createTournament(String name, List<Player> players, TournamentType type) {
        return new Tournament(name, players, type);
    }

    /**
     * Creates a tournament with an explicit identifier.
     *
     * @param tournamentId stable tournament identifier
     * @param name tournament name
     * @param players initial players
     * @param type tournament format
     * @return created tournament
     */
    public Tournament createTournament(UUID tournamentId, String name, List<Player> players, TournamentType type) {
        return new Tournament(tournamentId, name, players, type);
    }

    /**
     * Starts a tournament so rounds can be generated.
     *
     * @param tournament tournament to start
     */
    public void startTournament(Tournament tournament) {
        if (tournament == null) {
            throw new IllegalArgumentException("Tournament cannot be null");
        }

        tournament.start();
    }

    /**
     * Adds a player before the tournament starts.
     *
     * @param tournament target tournament
     * @param player player to add
     */
    public void addPlayer(Tournament tournament, Player player) {
        if (tournament == null) {
            throw new IllegalArgumentException("Tournament cannot be null");
        }

        tournament.addPlayer(player);
    }

    /**
     * Generates and stores the next round.
     *
     * @param tournament tournament to advance
     * @return generated round
     */
    public Round generateNextRound(Tournament tournament) {
        if (tournament == null) {
            throw new IllegalArgumentException("Tournament cannot be null");
        }

        return tournament.generateNextRound();
    }

    /**
     * Fills all unplayed matches in a round with random scores.
     *
     * <p>Knockout draws are resolved by a random tie-break winner.</p>
     *
     * @param tournament tournament owning the round
     * @param round round to simulate
     */
    public void simulateRound(Tournament tournament, Round round) {
        if (tournament == null) {
            throw new IllegalArgumentException("Tournament cannot be null");
        }
        if (round == null) {
            throw new IllegalArgumentException("Round cannot be null");
        }

        boolean resolveDraws = tournament.getType() == TournamentType.KNOCKOUT;

        for (Match match : round.getMatches()) {
            if (match.isPlayed()) {
                continue;
            }

            int player1Points = random.nextInt(3);
            int player2Points = random.nextInt(3);
            match.setPoints(player1Points, player2Points);

            if (resolveDraws && match.isDraw()) {
                Player tieBreakWinner = random.nextBoolean() ? match.getPlayer1() : match.getPlayer2();
                match.resolveDraw(tieBreakWinner);
            }
        }
    }

    /**
     * Checks whether a tournament is finished and marks it finished when appropriate.
     *
     * @param tournament tournament to inspect
     * @return {@code true} when the tournament is finished
     */
    public boolean isFinished(Tournament tournament) {
        if (tournament == null) {
            throw new IllegalArgumentException("Tournament cannot be null");
        }

        if (tournament.getState() == TournamentState.FINISHED) {
            return true;
        }

        Round current = tournament.getCurrentRound();

        if (current == null) return false;

        if (!current.isFinished()) return false;

        if (tournament.getType() == TournamentType.KNOCKOUT) {
            // knockout: one winner
            long winners = current.getMatches().stream()
                    .map(Match::getWinner)
                    .filter(Objects::nonNull)
                    .distinct()
                    .count();

            boolean finished = winners == 1;
            if (finished) {
                tournament.finish();
            }

            return finished;
        } else if (tournament.getType() == TournamentType.SWISS) {
            int maxRounds = (int) Math.ceil(Math.log(tournament.getPlayers().size()) / Math.log(2));

            if (tournament.getRoundCount() >= maxRounds) {
                tournament.finish();
                return true;
            }
        }

        return false;
    }
}
