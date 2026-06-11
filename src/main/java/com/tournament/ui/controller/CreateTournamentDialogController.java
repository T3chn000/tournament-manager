package com.tournament.ui.controller;

import com.tournament.model.TournamentType;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the dialog that collects the tournament name and type.
 */
public class CreateTournamentDialogController {
    @FXML private TextField tournamentNameField;
    @FXML private ComboBox<TournamentType> tournamentTypeComboBox;
    @FXML private Label errorLabel;

    private Stage dialogStage;
    private boolean confirmed;

    /**
     * Initializes available tournament types and selects the default Swiss format.
     */
    @FXML
    private void initialize() {
        tournamentTypeComboBox.setItems(FXCollections.observableArrayList(TournamentType.values()));
        tournamentTypeComboBox.getSelectionModel().select(TournamentType.SWISS);
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Indicates whether the user accepted the dialog.
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Returns the entered tournament name.
     */
    public String getTournamentName() {
        return tournamentNameField.getText();
    }

    /**
     * Returns the selected tournament type.
     */
    public TournamentType getTournamentType() {
        return tournamentTypeComboBox.getValue();
    }

    /**
     * Validates the entered tournament metadata and closes the dialog on success.
     */
    @FXML
    private void onCreate() {
        if (tournamentNameField.getText() == null || tournamentNameField.getText().isBlank()) {
            setError("Tournament name cannot be empty");
            return;
        }

        confirmed = true;
        dialogStage.close();
    }

    /**
     * Closes the dialog without applying changes.
     */
    @FXML
    private void onCancel() {
        confirmed = false;
        dialogStage.close();
    }

    /**
     * Shows validation feedback inside the dialog.
     */
    private void setError(String message) {
        errorLabel.setText(message);
    }
}
