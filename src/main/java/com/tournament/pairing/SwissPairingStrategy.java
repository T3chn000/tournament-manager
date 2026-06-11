package com.tournament.pairing;

import com.tournament.model.*;

import java.util.*;

/**
 * Pairing strategy for Swiss tournaments.
 *
 * <p>Players are ordered by current score, paired with similar scorers, and
 * repeat pairings are avoided when possible. A BYE is assigned when the player
 * count is odd.</p>
 */
public class SwissPairingStrategy implements PairingStrategy {

    /**
     * Generates the next Swiss round for the given tournament.
     *
     * @param tournament tournament to pair
     * @return next round
     */
    @Override
    public Round generateNextRound(Tournament tournament) {
        if (tournament == null) {
            throw new IllegalArgumentException("Tournament cannot be null");
        }

        int maxRounds = (int) Math.ceil(Math.log(tournament.getPlayers().size()) / Math.log(2));

        if (tournament.getRoundCount() >= maxRounds) {
            throw new IllegalStateException("Tournament finished");
        }

        Round lastRound = tournament.getCurrentRound();

        if (lastRound != null && !lastRound.isFinished()) {
            throw new IllegalStateException("Previous round not finished");
        }

        List<Player> players = collectPlayers(tournament);
        List<Match> matches = createMatches(players, tournament);

        return new Round(tournament.getRoundCount() + 1, matches);
    }

    /**
     * Orders players by current Swiss score and then by name for deterministic output.
     */
    private List<Player> collectPlayers(Tournament tournament) {
        return tournament.getPlayers().stream()
                .sorted(Comparator
                        .comparingInt((Player p) -> calculatePoints(p, tournament))
                        .reversed()
                        .thenComparing(Player::name))
                .toList();
    }

    /**
     * Creates all matches for the round, including a BYE match when needed.
     */
    private List<Match> createMatches(List<Player> players, Tournament tournament) {
        List<Player> available = new ArrayList<>(players);
        List<Match> matches = new ArrayList<>();

        if (available.size() % 2 == 1) {
            Player byePlayer = selectByePlayer(available, tournament);
            available.remove(byePlayer);
            matches.add(new Match(byePlayer, Player.BYE));
        }

        while (available.size() > 1) {
            Player player1 = available.removeFirst();
            int opponentIndex = findOpponentIndex(player1, available, tournament);

            Player player2 = available.remove(opponentIndex);
            matches.add(new Match(player1, player2));
        }

        return matches;
    }

    /**
     * Selects the lowest-ranked player who has not already received a BYE.
     */
    private Player selectByePlayer(List<Player> players, Tournament tournament) {
        for (int i = players.size() - 1; i >= 0; i--) {
            Player player = players.get(i);

            if (!havePlayedTogether(player, Player.BYE, tournament)) {
                return player;
            }
        }

        return players.getLast();
    }

    /**
     * Finds the first candidate who has not already played against the given player.
     *
     * <p>When all candidates are repeats, index {@code 0} is returned as a
     * practical fallback so the round can still be generated.</p>
     */
    private int findOpponentIndex(Player player, List<Player> candidates, Tournament tournament) {
        for (int i = 0; i < candidates.size(); i++) {
            Player candidate = candidates.get(i);

            if (!havePlayedTogether(player, candidate, tournament)) {
                return i;
            }
        }

        return 0;
    }

    /**
     * Checks previous rounds for an existing pairing between two players.
     */
    private boolean havePlayedTogether(Player p1, Player p2, Tournament tournament) {
        return tournament.getRounds().stream()
                .flatMap(round -> round.getMatches().stream())
                .anyMatch(match -> match.hasPlayer(p1) && match.hasPlayer(p2));
    }

    /**
     * Calculates Swiss ranking points used only for pairing order.
     */
    private int calculatePoints(Player player, Tournament tournament) {
        int points = 0;

        for (Round round : tournament.getRounds()) {
            for (Match match : round.getMatches()) {
                if (!match.hasPlayer(player)) {
                    continue;
                }

                if (match.isByeMatch()) {
                    points += 2;
                    continue;
                }

                if (match.isDraw()) {
                    points += 1;
                } else if (match.getWinner() != null && match.getWinner().equals(player)) {
                    points += 2;
                }
            }
        }

        return points;
    }
}
