package com.tournament.ui.controller;

import com.tournament.model.TournamentType;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CreateTournamentDialogController {
    @FXML private TextField tournamentNameField;
    @FXML private ComboBox<TournamentType> tournamentTypeComboBox;
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

    @FXML
    private void onCreate() {
        if (tournamentNameField.getText() == null || tournamentNameField.getText().isBlank()) {
            setError("Tournament name cannot be empty");
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
