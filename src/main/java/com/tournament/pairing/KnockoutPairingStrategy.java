package com.tournament.pairing;

import com.tournament.model.Player;
import com.tournament.model.Match;
import com.tournament.model.Tournament;
import com.tournament.model.Round;

import java.util.ArrayList;
import java.util.List;

public class KnockoutPairingStrategy implements PairingStrategy {
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
}
