package com.tournament.controller;

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

    public String getLabel() {
        return label;
    }

    public static MenuOption fromInt(int i) {
        return values()[i];
    }
}