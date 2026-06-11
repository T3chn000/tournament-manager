package com.tournament;

import com.tournament.controller.TournamentController;

/**
 * Console entry point for the legacy text-based tournament manager.
 */
public class Main {

    /**
     * Starts the legacy console application.
     *
     * @param args command-line arguments, currently unused
     */
    public static void main(String[] args) {
        TournamentController controller = new TournamentController();
        controller.start();
    }
}
