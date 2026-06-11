package com.tournament.ui.app;

/**
 * Runtime exception used for user-facing UI action failures.
 */
public class UiActionException extends RuntimeException {
    /**
     * Creates an exception with a message suitable for displaying in the UI.
     *
     * @param message user-facing error message
     */
    public UiActionException(String message) {
        super(message);
    }
}
