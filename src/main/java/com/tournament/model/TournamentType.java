package com.tournament.model;

import com.tournament.pairing.*;

/**
 * Supported tournament formats.
 *
 * <p>Each type knows how to create the pairing strategy used for the next round.</p>
 */
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

    /**
     * Creates a fresh pairing strategy for this tournament type.
     *
     * @return pairing strategy suitable for the type
     */
    public abstract PairingStrategy createStrategy();
}
