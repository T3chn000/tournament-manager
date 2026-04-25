package com.tournament.pairing;

import com.tournament.model.*;

import java.util.*;

public class SwissPairingStrategy implements PairingStrategy {

    @Override
    public Round generateNextRound(Tournament tournament) {

        List<Player> players;

        if (tournament.getCurrentRound() == null) {
            players = new ArrayList<>(tournament.getPlayers());
        } else {
            Round last = tournament.getCurrentRound();
            if (!last.isFinished()) {
                throw new IllegalStateException("Previous round not finished");
            }

            // TODO: sort by score
            players = collectPlayers(tournament);
        }

        List<Match> matches = new ArrayList<>();

        for (int i = 0; i < players.size(); i += 2) {
            if (i + 1 < players.size()) {
                matches.add(new Match(players.get(i), players.get(i + 1)));
            } else {
                matches.add(new Match(players.get(i), Player.BYE));
            }
        }

        return new Round(tournament.getRoundCount() + 1, matches);
    }

    private List<Player> collectPlayers(Tournament tournament) {
        // TODO: ranking / points
        return new ArrayList<>(tournament.getPlayers());
    }
}