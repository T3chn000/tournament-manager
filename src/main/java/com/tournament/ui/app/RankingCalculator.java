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

public class RankingCalculator {

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
        MatchResult result = match.getMatchResult();

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

        private Stats(Player player) {
            this.player = player;
        }

        private void addWin() {
            points += 2;
            wins++;
            playedMatches++;
        }

        private void addDraw() {
            points++;
            draws++;
            playedMatches++;
        }

        private void addLoss() {
            losses++;
            playedMatches++;
        }

        private void addByeWin() {
            points += 2;
            byeCount++;
        }

        private Player player() {
            return player;
        }

        private int points() {
            return points;
        }

        private int wins() {
            return wins;
        }

        private int draws() {
            return draws;
        }

        private int losses() {
            return losses;
        }

        private int playedMatches() {
            return playedMatches;
        }

        private int byeCount() {
            return byeCount;
        }
    }
}
