package com.tournament.pairing;

import com.tournament.model.Player;
import com.tournament.model.Match;
import com.tournament.model.Tournament;
import com.tournament.model.Round;

import java.util.*;

/**
 * Pairing strategy for single-elimination knockout tournaments.
 *
 * <p>The first round is shuffled, BYE matches are added when needed, and later
 * rounds are generated from the winners of the previous round.</p>
 */
public class KnockoutPairingStrategy implements PairingStrategy {
    private final Random random;

    public KnockoutPairingStrategy() {
        this(new Random());
    }

    public KnockoutPairingStrategy(Random random) {
        this.random = random;
    }

    /**
     * Generates the next knockout round.
     *
     * @param tournament tournament to pair
     * @return next round
     */
    @Override
    public Round generateNextRound(Tournament tournament) {

        if (tournament == null)
            throw new IllegalArgumentException("Tournament cannot be null");

        List<Player> players;

        if (tournament.getCurrentRound() == null) {
            players = new ArrayList<>(tournament.getPlayers());
            validateNoDuplicates(players);
            Collections.shuffle(players, random);
        } else {
            Round last = tournament.getCurrentRound();
            if (!last.isFinished()) {
                throw new IllegalStateException("Previous round not finished");
            }

            players = getPlayersForNextRound(tournament);
        }

        int n = players.size();

        if (n == 1) {
            throw new IllegalStateException("Tournament finished");
        }

        int nextPower = getNextPowerOf2(n);
        int byeCount = nextPower - n;

        List<Match> matches = new ArrayList<>();

        for (int i = 0; i < byeCount; i++) {
            matches.add(new Match(players.get(i), Player.BYE));
        }

        for (int i = byeCount; i < players.size(); i += 2) {
            matches.add(new Match(players.get(i), players.get(i + 1)));
        }

        return new Round(tournament.getRoundCount() + 1, matches);
    }

    /**
     * Returns initial players for the first round or winners for later rounds.
     */
    private List<Player> getPlayersForNextRound(Tournament tournament) {
        Round lastRound = tournament.getCurrentRound();

        if (lastRound == null)
            return new ArrayList<>(tournament.getPlayers());

        if (!lastRound.isFinished())
            throw new IllegalStateException("Previous round is not finished");

        return getWinnersFromRound(lastRound);
    }

    /**
     * Extracts winners from a completed knockout round.
     *
     * <p>A draw without a tie-break winner is rejected because knockout rounds
     * cannot advance unresolved matches.</p>
     */
    private List<Player> getWinnersFromRound(Round round) {
        List<Player> winners = new ArrayList<>();

        for (Match match : round.getMatches()) {
            Player winner = match.getWinner();
            if (winner == null)
                throw new IllegalStateException("Knockout round cannot end with a draw");
            winners.add(winner);
        }

        return winners;
    }
    
    /**
     * Guards against duplicate players before the first knockout bracket is built.
     */
    private void validateNoDuplicates(List<Player> players) {
        Set<Player> set = new HashSet<>(players);
        if (set.size() != players.size()) {
            throw new IllegalArgumentException("Duplicate players detected");
        }
    }

    /**
     * Finds the bracket size needed to calculate BYE matches.
     */
    private int getNextPowerOf2(int n) {
        int power = 1;
        while (power < n) {
            power *= 2;
        }
        return power;
    }
}
