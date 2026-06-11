package com.tournament.controller;

/**
 * Menu commands supported by the console controller.
 */
public enum MenuOption {
    CREATE("Create tournament"),
    LIST("List tournaments"),
    MANAGE("Manage tournament"),
    DELETE("Delete tournament"),
    SAVE("Save tournament"),
    LOAD("Load tournaments"),
    EXIT("Exit");

    private final String label;

    MenuOption(String label) {
        this.label = label;
    }

    /**
     * Returns text displayed in the console menu.
     *
     * @return menu label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Converts a menu index to an option.
     *
     * @param i ordinal menu index
     * @return matching menu option
     */
    public static MenuOption fromInt(int i) {
        return values()[i];
    }
}
