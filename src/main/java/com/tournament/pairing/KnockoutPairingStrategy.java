package com.tournament.pairing;

import com.tournament.model.Player;
import com.tournament.model.Match;
import com.tournament.model.Tournament;
import com.tournament.model.Round;

import java.util.*;

public class KnockoutPairingStrategy implements PairingStrategy {
    private final Random random;

    public KnockoutPairingStrategy() {
        this(new Random());
    }

    public KnockoutPairingStrategy(Random random) {
        this.random = random;
    }

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

    /*  Stara wersja bez wstępnej rundy.
    @Override
    public Round generateNextRound(Tournament tournament) {
        if (tournament == null)
            throw new IllegalArgumentException("Tournament cannot be null");

        List<Player> playersForRound = getPlayersForNextRound(tournament);

        if (playersForRound.isEmpty())
            throw new IllegalStateException("Not enough players to generate next round");

        if (playersForRound.size() == 1)
            throw new IllegalStateException("Tournament is already finished");

        int nextRoundNumber = tournament.getRoundCount() + 1;
        List<Match> matches = createMatches(playersForRound);

        return new Round(nextRoundNumber, matches);
    }
    */

    private List<Player> getPlayersForNextRound(Tournament tournament) {
        Round lastRound = tournament.getCurrentRound();

        if (lastRound == null)
            return new ArrayList<>(tournament.getPlayers());

        if (!lastRound.isFinished())
            throw new IllegalStateException("Previous round is not finished");

        return getWinnersFromRound(lastRound);
    }

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

    private List<Match> createMatches(List<Player> players) {
        List<Match> matches = new ArrayList<>();

        for (int i = 0; i < players.size(); i += 2) {
            Player player1 = players.get(i);

            if (i + 1 < players.size()) {
                Player player2 = players.get(i + 1);
                matches.add(new Match(player1, player2));
            } else {
                Match byeMatch = new Match(player1, Player.BYE);
                matches.add(byeMatch);
            }
        }

        return matches;
    }
    private void validateNoDuplicates(List<Player> players) {
        Set<Player> set = new HashSet<>(players);
        if (set.size() != players.size()) {
            throw new IllegalArgumentException("Duplicate players detected");
        }
    }

    private int getNextPowerOf2(int n) {
        int power = 1;
        while (power < n) {
            power *= 2;
        }
        return power;
    }
}
