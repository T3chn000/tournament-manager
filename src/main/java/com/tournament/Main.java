package com.tournament;

import com.tournament.controller.TournamentController;
import com.tournament.model.*;
import com.tournament.service.TournamentService;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        TournamentService service = new TournamentService();
        TournamentController controller = new TournamentController(service);

        // przykładowi gracze
        List<Player> players = List.of(
                new Player("A"),
                new Player("B"),
                new Player("C"),
                new Player("D"),
                new Player("E")
        );

        Tournament tournament = controller.createTournament(players, TournamentType.KNOCKOUT);

        controller.startTournament(tournament);

        while (!controller.isFinished(tournament)) {
            Round round = controller.nextRound(tournament);

            System.out.println("=== Round " + round.getRoundNumber() + " ===");

            for (Match match : round.getMatches()) {
                System.out.println(match.getPlayer1() + " vs " + match.getPlayer2());

                if (!match.isPlayed() && !match.isByeMatch()) {
                    //prosta symulacja: player1 zawsze wygrywa
                    match.setPoints(1, 0);
                }

                System.out.println("Score: " + match.getScore() +
                        " Winner: " + match.getWinner());
            }
        }

        System.out.println("Tournament finished!");
    }
}