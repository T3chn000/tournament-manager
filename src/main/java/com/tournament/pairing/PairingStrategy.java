package com.tournament.pairing;

import com.tournament.model.Round;
import com.tournament.model.Tournament;

/**
 * Generates the next round for a tournament format.
 */
public interface PairingStrategy {
   /**
    * Builds the next round without directly changing the tournament.
    *
    * @param tournament tournament to pair
    * @return generated round
    */
   Round generateNextRound(Tournament tournament);
}
