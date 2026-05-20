package com.tournament.ui.controller;

import com.tournament.model.TournamentType;
import com.tournament.ui.viewmodel.MatchView;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;

public class MatchResultDialogController {
    @FXML private Label player1Label;
    @FXML private Label player2Label;
    @FXML private Spinner<Integer> player1PointsSpinner;
    @FXML private Spinner<Integer> player2PointsSpinner;
    @FXML private Label errorLabel;

    private Stage dialogStage;
    private TournamentType tournamentType;
    private boolean confirmed;

    @FXML
    private void initialize() {
        player1PointsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999, 0));
        player2PointsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999, 0));
        player1PointsSpinner.setEditable(true);
        player2PointsSpinner.setEditable(true);
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setMatch(MatchView match, TournamentType tournamentType) {
        this.tournamentType = tournamentType;
        player1Label.setText(match.player1Name());
        player2Label.setText(match.player2Name());
        applyExistingScore(match.score());
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public int getPlayer1Points() {
        return player1PointsSpinner.getValue();
    }

    public int getPlayer2Points() {
        return player2PointsSpinner.getValue();
    }

    @FXML
    private void onSave() {
        if (tournamentType == TournamentType.KNOCKOUT && getPlayer1Points() == getPlayer2Points()) {
            errorLabel.setText("Knockout match cannot end with a draw");
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

    private void applyExistingScore(String score) {
        if (score == null || !score.contains(":")) {
            return;
        }

        String[] parts = score.split(":");
        if (parts.length != 2) {
            return;
        }

        try {
            player1PointsSpinner.getValueFactory().setValue(Integer.parseInt(parts[0].trim()));
            player2PointsSpinner.getValueFactory().setValue(Integer.parseInt(parts[1].trim()));
        } catch (NumberFormatException ignored) {
            player1PointsSpinner.getValueFactory().setValue(0);
            player2PointsSpinner.getValueFactory().setValue(0);
        }
    }
}
