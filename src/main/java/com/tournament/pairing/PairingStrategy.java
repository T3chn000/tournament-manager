package com.tournament.pairing;

import com.tournament.model.Round;
import com.tournament.model.Tournament;

public interface PairingStrategy {
   Round generateNextRound(Tournament tournament);
}
