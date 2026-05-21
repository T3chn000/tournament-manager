package com.tournament.controller;

import com.tournament.model.*;
import com.tournament.persistence.TournamentRepository;
import com.tournament.service.TournamentService;

import java.util.*;

public class TournamentController {

    private final Scanner scanner = new Scanner(System.in);
    private final TournamentService service = new TournamentService();
    private final TournamentRepository repository = new TournamentRepository();
    private final List<Tournament> tournaments = new ArrayList<>();

    public void start() {
        System.out.println("Loading saved tournaments...");
        loadTournaments();

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
                    case SAVE -> saveTournament();
                    case LOAD -> loadTournaments();
                    case EXIT -> running = false;
                }

            } catch (Exception e) {
                System.out.println("Something went wrong. Please try again.");
            }
        }
    }

    private void saveTournament() {
        if (tournaments.isEmpty()) {
            System.out.println("No tournaments to save.");
            return;
        }

        listTournaments();
        System.out.print("Choose tournament to save (index): ");
        int index = readInt();

        if (!isValidIndex(index)) return;

        Tournament t = tournaments.get(index);
        saveManagedTournament(t);
    }

    private void saveManagedTournament(Tournament t) {
        try {
            repository.save(t);
            System.out.println("Tournament saved.");
        } catch (Exception e) {
            System.out.println("Error saving: " + e.getMessage());
        }
    }

    private void loadTournaments() {
        try {
            List<Tournament> loaded = repository.load();
            tournaments.clear(); // czy konieczne?
            tournaments.addAll(loaded);
            System.out.println("Loaded " + tournaments.size() + " tournaments.");
        } catch (Exception e) {
            System.out.println("Error loading: " + e.getMessage());
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
        if (name.isBlank()) {
            System.out.println("Tournament name cannot be empty.");
            return;
        }

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

        Tournament t;
        try {
            t = service.createTournament(name, players, type);
            tournaments.add(t);
        } catch (IllegalArgumentException e) {
            System.out.println("Cannot create tournament: " + e.getMessage());
            return;
        }

        System.out.println("Tournament created:");
        printTournamentDetails(t);
    }

    private TournamentType chooseType() {
        System.out.println("Choose type:");
        System.out.println("1. SWISS");
        System.out.println("2. KNOCKOUT");

        while (true) {
            int t = readInt();

            switch (t) {
                case 1:
                    return TournamentType.SWISS;
                case 2:
                    return TournamentType.KNOCKOUT;
                default:
                    System.out.print("Invalid option. Choose 1 or 2: ");
            }
        }
    }

    private void listTournaments() {
        if (tournaments.isEmpty()) {
            System.out.println("No tournaments.");
            return;
        }

        for (int i = 0; i < tournaments.size(); i++) {
            System.out.println(i + ": " + tournaments.get(i));
        }
    }

    private void deleteTournament() {
        if (tournaments.isEmpty()) {
            System.out.println("No tournaments.");
            return;
        }

        listTournaments();

        System.out.print("Index to delete: ");
        int index = readInt();

        if (!isValidIndex(index)) return;

        tournaments.remove(index);
        System.out.println("Deleted.");
    }

    private void manageTournament() {
        if (tournaments.isEmpty()) {
            System.out.println("No tournaments.");
            return;
        }

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
                case 6 -> showPlayers(t);
                case 7 -> saveManagedTournament(t);
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
        System.out.println("6. Show players");
        System.out.println("7. Save tournament");
        System.out.println("0. Back");
    }

    private void addPlayer(Tournament t) {
        if (t.isStarted()) {
            System.out.println("Cannot add players after start!");
            return;
        }

        System.out.print("Player name: ");
        String name = scanner.nextLine();

        try {
            service.addPlayer(t, new Player(name));
            System.out.println("Added.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Cannot add player: " + e.getMessage());
        }
    }

    private void startTournament(Tournament t) {
        if (t.getPlayers().size() < 2) {
            System.out.println("Not enough players!");
            return;
        }

        try {
            service.startTournament(t);
            System.out.println("Started.");
        } catch (IllegalStateException e) {
            System.out.println("Cannot start tournament: " + e.getMessage());
        }
    }

    private void nextRound(Tournament t) {
        try {
            Round round = service.generateNextRound(t);

            service.simulateRound(t, round);

            System.out.println("Round generated and simulated:");
            System.out.println(round);
        } catch (Exception e) {
            System.out.println("Cannot generate round: " + e.getMessage());
        }
    }

    private void simulateTournament(Tournament t) {
        if (t.getState() == TournamentState.CREATED) {
            System.out.println("Start tournament first!");
            return;
        }
        if (t.getState() == TournamentState.FINISHED) {
            System.out.println("Tournament already finished.");
            printTournamentDetails(t);
            return;
        }

        try {
            while (!service.isFinished(t)) {
                Round round = service.generateNextRound(t);
                service.simulateRound(t, round);
                System.out.println(round);
            }

            System.out.println("Tournament finished!");
            printTournamentDetails(t);
        } catch (Exception e) {
            System.out.println("Cannot simulate tournament: " + e.getMessage());
        }
    }

    private void showRounds(Tournament t) {
        if (t.getRounds().isEmpty()) {
            System.out.println("No rounds yet.");
            return;
        }

        t.getRounds().forEach(System.out::println);
    }

    private void showPlayers(Tournament t) {
        System.out.println("Players:");
        List<Player> players = t.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            System.out.println((i + 1) + ". " + player.name() + " (" + player.playerId() + ")");
        }
    }

    private void printTournamentDetails(Tournament t) {
        System.out.println(t);
        showPlayers(t);
        if (t.getCurrentRound() != null) {
            System.out.println("Current round:");
            System.out.println(t.getCurrentRound());
        }
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
