package com.tournament.ui.controller;

import com.tournament.model.TournamentType;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;
import java.util.Locale;

public class CreateTournamentDialogController {
    @FXML private TextField tournamentNameField;
    @FXML private ComboBox<TournamentType> tournamentTypeComboBox;
    @FXML private TextField playerNameField;
    @FXML private ListView<String> playersListView;
    @FXML private Label errorLabel;

    private Stage dialogStage;
    private boolean confirmed;

    @FXML
    private void initialize() {
        tournamentTypeComboBox.setItems(FXCollections.observableArrayList(TournamentType.values()));
        tournamentTypeComboBox.getSelectionModel().select(TournamentType.SWISS);
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getTournamentName() {
        return tournamentNameField.getText();
    }

    public TournamentType getTournamentType() {
        return tournamentTypeComboBox.getValue();
    }

    public List<String> getPlayerNames() {
        return List.copyOf(playersListView.getItems());
    }

    @FXML
    private void onAddPlayer() {
        String playerName = playerNameField.getText() == null ? "" : playerNameField.getText().trim();
        if (playerName.isBlank()) {
            setError("Player name cannot be empty");
            return;
        }

        boolean duplicate = playersListView.getItems().stream()
                .map(name -> name.toLowerCase(Locale.ROOT))
                .anyMatch(playerName.toLowerCase(Locale.ROOT)::equals);
        if (duplicate) {
            setError("Player names must be unique");
            return;
        }

        playersListView.getItems().add(playerName);
        playerNameField.clear();
        setError("");
    }

    @FXML
    private void onRemovePlayer() {
        int selectedIndex = playersListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            playersListView.getItems().remove(selectedIndex);
        }
    }

    @FXML
    private void onCreate() {
        if (tournamentNameField.getText() == null || tournamentNameField.getText().isBlank()) {
            setError("Tournament name cannot be empty");
            return;
        }
        if (playersListView.getItems().size() < 2) {
            setError("Need at least 2 players");
            return;
        }

        confirmed = true;
        dialogStage.close();
    }

    @FXML
    private void onCancel() {
        confirmed = false;
        dialogStage.close();
    }

    private void setError(String message) {
        errorLabel.setText(message);
    }
}
