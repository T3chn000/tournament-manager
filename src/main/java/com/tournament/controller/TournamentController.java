package com.tournament.controller;

import com.tournament.model.*;
import com.tournament.service.TournamentService;

import java.util.*;

public class TournamentController {

    private final Scanner scanner = new Scanner(System.in);
    private final TournamentService service = new TournamentService();
    private final List<Tournament> tournaments = new ArrayList<>();

    public void start() {
        boolean running = true;

        while (running) {
            printMenu();
            int choice = readInt();

            try {
                MenuOption option = MenuOption.fromInt(choice);

                switch (option) {
                    case CREATE -> createTournament();
                    case LIST -> listTournaments();
                    case MANAGE -> manageTournament();
                    case DELETE -> deleteTournament();
                    case EXIT -> running = false;
                }

            } catch (Exception e) {
                System.out.println("Something went wrong. Please try again.");
            }
        }
    }

    private void printMenu() {
        System.out.println("\n=== TOURNAMENT MANAGER ===");

        MenuOption[] options = MenuOption.values();
        for (int i = 0; i < options.length; i++) {
            System.out.println(i + ". " + options[i].getLabel());
        }

        System.out.print("Choose: ");
    }

    private void createTournament() {
        System.out.print("Tournament name: ");
        String name = scanner.nextLine();

        TournamentType type = chooseType();

        List<Player> players = new ArrayList<>();
        System.out.println("Add players (type 'end'):");

        while (true) {
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("end")) break;

            if (input.isBlank()) continue;

            players.add(new Player(input));
        }

        if (players.size() < 2) {
            System.out.println("Need at least 2 players!");
            return;
        }

        Tournament t = service.createTournament(players, type);
        tournaments.add(t);

        System.out.println("Tournament created!");
    }

    private TournamentType chooseType() {
        System.out.println("Choose type:");
        System.out.println("1. SWISS");
        System.out.println("2. KNOCKOUT");

        int t = readInt();

        return switch (t) {
            case 1 -> TournamentType.SWISS;
            case 2 -> TournamentType.KNOCKOUT;
            default -> {
                System.out.println("Invalid option!");
                yield null;
            }
        };
    }

    private void listTournaments() {
        if (tournaments.isEmpty()) {
            System.out.println("No tournaments.");
            return;
        }

        for (int i = 0; i < tournaments.size(); i++) {
            System.out.println(i + ": " + tournaments.get(i).toString());
        }
    }

    private void deleteTournament() {
        listTournaments();

        System.out.print("Index to delete: ");
        int index = readInt();

        if (!isValidIndex(index)) return;

        tournaments.remove(index);
        System.out.println("Deleted.");
    }

    private void manageTournament() {
        listTournaments();

        System.out.print("Choose tournament: ");
        int index = readInt();

        if (!isValidIndex(index)) return;

        Tournament t = tournaments.get(index);

        boolean managing = true;

        while (managing) {
            printManageMenu();
            int choice = readInt();

            switch (choice) {
                case 1 -> addPlayer(t);
                case 2 -> startTournament(t);
                case 3 -> nextRound(t);
                case 4 -> simulateTournament(t);
                case 5 -> showRounds(t);
                case 0 -> managing = false;
                default -> System.out.println("Invalid");
            }
        }
    }

    private void printManageMenu() {
        System.out.println("\n=== MANAGE ===");
        System.out.println("1. Add player");
        System.out.println("2. Start tournament");
        System.out.println("3. Next round");
        System.out.println("4. Simulate tournament");
        System.out.println("5. Show rounds");
        System.out.println("0. Back");
    }

    private void addPlayer(Tournament t) {
        if (t.isStarted()) {
            System.out.println("Cannot add players after start!");
            return;
        }

        System.out.print("Player name: ");
        String name = scanner.nextLine();

        t.getPlayers().add(new Player(name));
        System.out.println("Added.");
    }

    private void startTournament(Tournament t) {
        if (t.getPlayers().size() < 2) {
            System.out.println("Not enough players!");
            return;
        }

        service.startTournament(t);
        System.out.println("Started.");
    }

    private void nextRound(Tournament t) {
        try {
            Round round = service.generateNextRound(t);

            simulateRound(round);

            System.out.println("Round generated + simulated.");
        } catch (Exception e) {
            System.out.println("Cannot generate round: " + e.getMessage());
        }
    }

    private void simulateTournament(Tournament t) {
        if (!t.isStarted()) {
            System.out.println("Start tournament first!");
            return;
        }

        while (!service.isFinished(t)) {
            Round round = service.generateNextRound(t);
            simulateRound(round);
        }

        System.out.println("Tournament finished!");
    }

    private void simulateRound(Round round) {
        Random rand = new Random();

        for (Match m : round.getMatches()) {
            int p1 = rand.nextInt(2);
            int p2 = 1 - p1;

            m.setPoints(p1, p2);
        }
    }

    private void showRounds(Tournament t) {
        t.getRounds().forEach(System.out::println);
    }

    private boolean isValidIndex(int i) {
        if (i < 0 || i >= tournaments.size()) {
            System.out.println("Invalid index");
            return false;
        }
        return true;
    }

    private int readInt() {
        while (!scanner.hasNextInt()) {
            System.out.println("Enter number!");
            scanner.next();
        }
        int val = scanner.nextInt();
        scanner.nextLine();
        return val;
    }
}