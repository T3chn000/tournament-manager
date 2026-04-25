package com.tournament.model;

import com.tournament.pairing.*;

public enum TournamentType {

    KNOCKOUT {
        @Override
        public PairingStrategy createStrategy() {
            return new KnockoutPairingStrategy();
        }
    },

    SWISS {
        @Override
        public PairingStrategy createStrategy() {
            return new SwissPairingStrategy();
        }
    };

    public abstract PairingStrategy createStrategy();
}