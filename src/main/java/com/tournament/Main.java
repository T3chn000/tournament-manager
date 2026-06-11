package com.tournament;

import com.tournament.controller.TournamentController;

/**
 * Console entry point for the legacy text-based tournament manager.
 */
public class Main {

    public static void main(String[] args) {
        TournamentController controller = new TournamentController();
        controller.start();
    }
}
