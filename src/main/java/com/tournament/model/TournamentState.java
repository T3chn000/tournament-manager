package com.tournament.model;

/**
 * Lifecycle state of a tournament.
 */
public enum TournamentState {
    /**
     * Players can still be added and no round has to exist yet.
     */
    CREATED,

    /**
     * The tournament is active and rounds or match results may be managed.
     */
    STARTED,

    /**
     * The tournament has completed all required play.
     */
    FINISHED
}
