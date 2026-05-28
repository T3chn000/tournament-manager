package com.tournament.ui.controller;

import com.tournament.model.Tournament;
import com.tournament.model.TournamentState;
import com.tournament.model.TournamentType;
import com.tournament.ui.app.TournamentApplicationService;
import com.tournament.ui.app.UiActionException;
import com.tournament.ui.viewmodel.MatchView;
import com.tournament.ui.viewmodel.PlayerRow;
import com.tournament.ui.viewmodel.RankingRow;
import com.tournament.ui.viewmodel.RoundView;
import com.tournament.ui.viewmodel.TournamentDetails;
import com.tournament.ui.viewmodel.TournamentSummary;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class MainController {
    private final TournamentApplicationService applicationService = new TournamentApplicationService();

    @FXML private ListView<TournamentSummary> tournamentListView;
    @FXML private VBox emptyState;
    @FXML private VBox detailsPane;
    @FXML private Label titleLabel;
    @FXML private Label metaLabel;
    @FXML private Label statusLabel;
    @FXML private Button deleteButton;
    @FXML private Button addPlayerButton;
    @FXML private Button startButton;
    @FXML private Button nextRoundButton;
    @FXML private Button simulateRoundButton;
    @FXML private Button simulateTournamentButton;
    @FXML private TableView<PlayerRow> playersTable;
    @FXML private TableColumn<PlayerRow, String> playerNumberColumn;
    @FXML private TableColumn<PlayerRow, String> playerNameColumn;
    @FXML private TableColumn<PlayerRow, String> playerIdColumn;
    @FXML private VBox roundsContainer;
    @FXML private TableView<RankingRow> rankingTable;
    @FXML private TableColumn<RankingRow, String> placeColumn;
    @FXML private TableColumn<RankingRow, String> rankingPlayerColumn;
    @FXML private TableColumn<RankingRow, String> pointsColumn;
    @FXML private TableColumn<RankingRow, String> winsColumn;
    @FXML private TableColumn<RankingRow, String> drawsColumn;
    @FXML private TableColumn<RankingRow, String> lossesColumn;
    @FXML private TableColumn<RankingRow, String> playedColumn;
    @FXML private TableColumn<RankingRow, String> byeColumn;

    private Tournament selectedTournament;
    private TournamentDetails selectedDetails;

    @FXML
    private void initialize() {
        configureTournamentList();
        configurePlayersTable();
        configureRankingTable();
        try {
            applicationService.loadSavedTournaments();
        } catch (UiActionException e) {
            showError("Failed to load saved tournaments: " + e.getMessage());
        }
        refreshTournamentList(null);
    }

    @FXML
    private void onNewTournament() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreateTournamentDialog.fxml"));
            Parent root = loader.load();
            CreateTournamentDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            controller.setDialogStage(dialogStage);
            dialogStage.setTitle("New tournament");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(tournamentListView.getScene().getWindow());
            dialogStage.setScene(createStyledScene(root));
            dialogStage.setMinWidth(420);
            dialogStage.setMinHeight(420);
            dialogStage.showAndWait();

            if (controller.isConfirmed()) {
                Tournament created = applicationService.createTournament(
                        controller.getTournamentName(),
                        controller.getTournamentType(),
                        controller.getPlayerNames()
                );
                applicationService.saveTournament(created);
                refreshTournamentList(created);
                setStatus("Tournament created");
            }
        } catch (IOException e) {
            showError("Cannot open tournament dialog");
        } catch (UiActionException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onDeleteTournament() {
        if (selectedDetails == null) {
            return;
        }

        ButtonType deleteType = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Delete tournament \"%s\"?".formatted(selectedDetails.name()),
                deleteType,
                ButtonType.CANCEL
        );
        confirmation.setTitle("Delete tournament");
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == deleteType) {
            try {
                applicationService.deleteTournament(selectedTournament);
                refreshTournamentList(null);
                setStatus("Tournament deleted");
            } catch (UiActionException e) {
                showError(e.getMessage());
            }
        }
    }

    @FXML
    private void onAddPlayer() {
        if (selectedDetails == null) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add player");
        dialog.setHeaderText("Add player to " + selectedDetails.name());
        dialog.setContentText("Player name:");
        dialog.showAndWait()
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .ifPresent(name -> handleAction(
                        () -> applicationService.addPlayer(selectedTournament, name),
                        "Player added"
                ));
    }

    @FXML
    private void onStartTournament() {
        runSelectedAction(() -> applicationService.startTournament(selectedTournament), "Tournament started");
    }

    @FXML
    private void onNextRound() {
        runSelectedAction(() -> applicationService.generateNextRound(selectedTournament), "Next round generated");
    }

    @FXML
    private void onSimulateRound() {
        runSelectedAction(() -> applicationService.simulateCurrentRound(selectedTournament), "Current round simulated");
    }

    @FXML
    private void onSimulateTournament() {
        runSelectedAction(() -> applicationService.simulateTournament(selectedTournament), "Tournament simulated");
    }

    private void configureTournamentList() {
        tournamentListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(TournamentSummary item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText("%s%n%s / %s / %d players".formatted(
                        item.name(),
                        item.type(),
                        item.state(),
                        item.playerCount()
                ));
            }
        });

        tournamentListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                showEmptyState();
            } else {
                showTournament(newValue.tournament());
            }
        });
    }

    private void configurePlayersTable() {
        playerNumberColumn.setCellValueFactory(cell -> new SimpleStringProperty(
                String.valueOf(playersTable.getItems().indexOf(cell.getValue()) + 1)
        ));
        playerNameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().name()));
        playerIdColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().shortId()));
        playersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private void configureRankingTable() {
        placeColumn.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().place())));
        rankingPlayerColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().playerName()));
        pointsColumn.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().points())));
        winsColumn.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().wins())));
        drawsColumn.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().draws())));
        lossesColumn.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().losses())));
        playedColumn.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().playedMatches())));
        byeColumn.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().byeCount())));
        rankingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private void refreshTournamentList(Tournament tournamentToSelect) {
        tournamentListView.setItems(FXCollections.observableArrayList(applicationService.getTournaments()));
        if (tournamentToSelect != null) {
            tournamentListView.getItems().stream()
                    .filter(summary -> summary.tournament().getTournamentId().equals(tournamentToSelect.getTournamentId()))
                    .findFirst()
                    .ifPresentOrElse(
                            summary -> tournamentListView.getSelectionModel().select(summary),
                            this::selectDefaultOrEmpty
                    );
        } else {
            selectDefaultOrEmpty();
        }
    }

    private void selectDefaultOrEmpty() {
        if (!tournamentListView.getItems().isEmpty()) {
            tournamentListView.getSelectionModel().select(0);
        } else {
            showEmptyState();
        }
    }

    private void showTournament(Tournament tournament) {
        selectedTournament = tournament;
        selectedDetails = applicationService.getDetails(tournament);
        titleLabel.setText(selectedDetails.name());
        metaLabel.setText("%s / %s / %d players / %d rounds".formatted(
                selectedDetails.type(),
                selectedDetails.state(),
                selectedDetails.playerCount(),
                selectedDetails.roundCount()
        ));

        playersTable.setItems(FXCollections.observableArrayList(selectedDetails.players()));
        rankingTable.setItems(FXCollections.observableArrayList(selectedDetails.ranking()));
        renderRounds();
        updateActionStates();

        emptyState.setVisible(false);
        emptyState.setManaged(false);
        detailsPane.setVisible(true);
        detailsPane.setManaged(true);
    }

    private void showEmptyState() {
        selectedDetails = null;
        selectedTournament = null;
        emptyState.setVisible(true);
        emptyState.setManaged(true);
        detailsPane.setVisible(false);
        detailsPane.setManaged(false);
        deleteButton.setDisable(true);
    }

    private void renderRounds() {
        roundsContainer.getChildren().clear();
        if (selectedDetails.rounds().isEmpty()) {
            roundsContainer.getChildren().add(new Label("No rounds yet."));
            return;
        }

        for (RoundView round : selectedDetails.rounds()) {
            TableView<MatchView> table = createMatchesTable(round);
            String status = round.finished() ? "finished" : "in progress";
            TitledPane pane = new TitledPane("Round %d (%s)".formatted(round.roundNumber(), status), table);
            pane.setExpanded(round.roundNumber() == selectedDetails.roundCount());
            roundsContainer.getChildren().add(pane);
        }
    }

    private TableView<MatchView> createMatchesTable(RoundView round) {
        TableView<MatchView> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(round.matches()));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setMinHeight(160);

        TableColumn<MatchView, String> number = textColumn("#", match -> String.valueOf(match.matchIndex() + 1));
        TableColumn<MatchView, String> player1 = textColumn("Player 1", MatchView::player1Name);
        TableColumn<MatchView, String> player2 = textColumn("Player 2", MatchView::player2Name);
        TableColumn<MatchView, String> score = textColumn("Score", MatchView::score);
        TableColumn<MatchView, String> result = textColumn("Result", MatchView::result);
        TableColumn<MatchView, String> winner = textColumn("Winner", MatchView::winnerName);
        TableColumn<MatchView, Void> action = new TableColumn<>("Action");
        action.setCellFactory(column -> new TableCell<>() {
            private final Button button = new Button("Set result");

            {
                button.getStyleClass().add("table-action-button");
                button.setOnAction(event -> {
                    MatchView match = getTableView().getItems().get(getIndex());
                    openMatchResultDialog(round.roundNumber(), match);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                MatchView match = getTableView().getItems().get(getIndex());
                button.setDisable(!match.editable());
                button.setText(match.played() ? "Edit result" : "Set result");
                setGraphic(button);
            }
        });

        table.getColumns().add(number);
        table.getColumns().add(player1);
        table.getColumns().add(player2);
        table.getColumns().add(score);
        table.getColumns().add(result);
        table.getColumns().add(winner);
        table.getColumns().add(action);
        return table;
    }

    private TableColumn<MatchView, String> textColumn(String title, TextExtractor extractor) {
        TableColumn<MatchView, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new SimpleStringProperty(extractor.text(cell.getValue())));
        return column;
    }

    private void openMatchResultDialog(int roundNumber, MatchView match) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MatchResultDialog.fxml"));
            Parent root = loader.load();
            MatchResultDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            controller.setDialogStage(dialogStage);
            controller.setMatch(match, selectedDetails.type());
            dialogStage.setTitle("Match result");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(tournamentListView.getScene().getWindow());
            dialogStage.setScene(createStyledScene(root));
            dialogStage.showAndWait();

            if (controller.isConfirmed()) {
                Integer tieBreakWinnerIndex = controller.getTieBreakWinnerIndex();
                handleAction(
                        () -> applicationService.updateMatchScore(
                                selectedTournament,
                                roundNumber,
                                match.matchIndex(),
                                controller.getPlayer1Points(),
                                controller.getPlayer2Points(),
                                tieBreakWinnerIndex
                        ),
                        "Match result updated"
                );
            }
        } catch (IOException e) {
            showError("Cannot open match dialog");
        }
    }

    private void updateActionStates() {
        boolean hasSelection = selectedDetails != null;
        boolean created = hasSelection && selectedDetails.state() == TournamentState.CREATED;
        boolean started = hasSelection && selectedDetails.state() == TournamentState.STARTED;
        boolean hasCurrentRound = hasSelection && !selectedDetails.rounds().isEmpty();
        boolean currentRoundFinished = hasCurrentRound && selectedDetails.rounds().getLast().finished();

        deleteButton.setDisable(!hasSelection);
        addPlayerButton.setDisable(!created);
        startButton.setDisable(!created || selectedDetails.playerCount() < 2);
        nextRoundButton.setDisable(!started || (hasCurrentRound && !currentRoundFinished));
        simulateRoundButton.setDisable(!started || !hasCurrentRound || currentRoundFinished);
        simulateTournamentButton.setDisable(!started);
    }

    private Scene createStyledScene(Parent root) {
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        return scene;
    }

    private void runSelectedAction(Runnable action, String successMessage) {
        if (selectedDetails == null) {
            return;
        }
        handleAction(action, successMessage);
    }

    private void handleAction(Runnable action, String successMessage) {
        try {
            Tournament tournamentToSelect = selectedTournament;
            action.run();
            if (tournamentToSelect != null) {
                applicationService.saveTournament(tournamentToSelect);
            }
            refreshTournamentList(tournamentToSelect);
            if (tournamentToSelect != null) {
                showTournament(tournamentToSelect);
            }
            setStatus(successMessage);
        } catch (UiActionException e) {
            showError(e.getMessage());
        }
    }

    private void setStatus(String message) {
        statusLabel.getStyleClass().remove("error-text");
        statusLabel.setText(message);
    }

    private void showError(String message) {
        if (!statusLabel.getStyleClass().contains("error-text")) {
            statusLabel.getStyleClass().add("error-text");
        }
        statusLabel.setText(message);
    }

    @FunctionalInterface
    private interface TextExtractor {
        String text(MatchView match);
    }
}
