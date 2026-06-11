package com.tournament.ui.app;

import com.tournament.model.Match;
import com.tournament.model.MatchResult;
import com.tournament.model.Player;
import com.tournament.model.Round;
import com.tournament.model.Tournament;
import com.tournament.ui.viewmodel.RankingRow;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates ranking rows from played tournament matches.
 *
 * <p>The current scoring model gives two points for a win or BYE and one point
 * for a draw. Tie-break winners still score the match as a draw.</p>
 */
public class RankingCalculator {

    /**
     * Builds a ranking table for all tournament players.
     *
     * @param tournament tournament to rank
     * @return sorted ranking rows
     */
    public List<RankingRow> calculate(Tournament tournament) {
        if (tournament == null) {
            throw new IllegalArgumentException("Tournament cannot be null");
        }

        Map<Player, Stats> statsByPlayer = new HashMap<>();
        for (Player player : tournament.getPlayers()) {
            statsByPlayer.put(player, new Stats(player));
        }

        for (Round round : tournament.getRounds()) {
            for (Match match : round.getMatches()) {
                applyMatch(statsByPlayer, match);
            }
        }

        List<Stats> sortedStats = new ArrayList<>(statsByPlayer.values());
        sortedStats.sort(Comparator
                .comparingInt(Stats::points).reversed()
                .thenComparing(Comparator.comparingInt(Stats::wins).reversed())
                .thenComparing(stats -> stats.player().name()));

        List<RankingRow> ranking = getRankingRows(sortedStats);
        return List.copyOf(ranking);
    }

    /**
     * Converts sorted internal stats into table rows with one-based places.
     */
    private static List<RankingRow> getRankingRows(List<Stats> sortedStats) {
        List<RankingRow> ranking = new ArrayList<>();
        for (int i = 0; i < sortedStats.size(); i++) {
            Stats stats = sortedStats.get(i);
            ranking.add(new RankingRow(
                    i + 1,
                    stats.player().playerId(),
                    stats.player().name(),
                    stats.points(),
                    stats.wins(),
                    stats.draws(),
                    stats.losses(),
                    stats.playedMatches(),
                    stats.byeCount()
            ));
        }
        return ranking;
    }

    /**
     * Applies one played match to both players' accumulated statistics.
     */
    private void applyMatch(Map<Player, Stats> statsByPlayer, Match match) {
        if (!match.isPlayed()) {
            return;
        }

        Player player1 = match.getPlayer1();
        Player player2 = match.getPlayer2();

        if (match.isByeMatch()) {
            Player winner = match.getWinner();
            if (winner != null && !winner.equals(Player.BYE)) {
                statsByPlayer.get(winner).addByeWin();
            }
            return;
        }

        Stats player1Stats = statsByPlayer.get(player1);
        Stats player2Stats = statsByPlayer.get(player2);
        MatchResult result = match.getResult();

        if (result == MatchResult.DRAW) {
            player1Stats.addDraw();
            player2Stats.addDraw();
        } else if (result == MatchResult.PLAYER1_WIN) {
            player1Stats.addWin();
            player2Stats.addLoss();
        } else if (result == MatchResult.PLAYER2_WIN) {
            player2Stats.addWin();
            player1Stats.addLoss();
        }
    }

    private static final class Stats {
        private final Player player;
        private int points;
        private int wins;
        private int draws;
        private int losses;
        private int playedMatches;
        private int byeCount;

        /**
         * Creates an empty accumulator for one player.
         */
        private Stats(Player player) {
            this.player = player;
        }

        /**
         * Records a regular win.
         */
        private void addWin() {
            points += 2;
            wins++;
            playedMatches++;
        }

        /**
         * Records a draw as one ranking point for the player.
         */
        private void addDraw() {
            points++;
            draws++;
            playedMatches++;
        }

        /**
         * Records a regular loss.
         */
        private void addLoss() {
            losses++;
            playedMatches++;
        }

        /**
         * Records an automatic BYE win without increasing played match count.
         */
        private void addByeWin() {
            points += 2;
            byeCount++;
        }

        /**
         * Returns the player represented by this accumulator.
         */
        private Player player() {
            return player;
        }

        /**
         * Returns accumulated ranking points.
         */
        private int points() {
            return points;
        }

        /**
         * Returns accumulated wins.
         */
        private int wins() {
            return wins;
        }

        /**
         * Returns accumulated draws.
         */
        private int draws() {
            return draws;
        }

        /**
         * Returns accumulated losses.
         */
        private int losses() {
            return losses;
        }

        /**
         * Returns the number of played non-BYE matches.
         */
        private int playedMatches() {
            return playedMatches;
        }

        /**
         * Returns the number of automatic BYE wins.
         */
        private int byeCount() {
            return byeCount;
        }
    }
}
