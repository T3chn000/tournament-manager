package com.tournament.ui.controller;

import com.tournament.model.TournamentType;
import com.tournament.ui.viewmodel.MatchView;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MatchResultDialogController {
    @FXML private Label player1Label;
    @FXML private Label player2Label;
    @FXML private Spinner<Integer> player1PointsSpinner;
    @FXML private Spinner<Integer> player2PointsSpinner;
    @FXML private Label errorLabel;
    @FXML private VBox tieBreakContainer;
    @FXML private ComboBox<String> tieBreakWinnerComboBox;

    private Stage dialogStage;
    private TournamentType tournamentType;
    private boolean confirmed;

    @FXML
    private void initialize() {
        player1PointsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999, 0));
        player2PointsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999, 0));
        player1PointsSpinner.setEditable(true);
        player2PointsSpinner.setEditable(true);

        player1PointsSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateTieBreakVisibility());
        player2PointsSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateTieBreakVisibility());
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setMatch(MatchView match, TournamentType tournamentType) {
        this.tournamentType = tournamentType;
        player1Label.setText(match.player1Name());
        player2Label.setText(match.player2Name());

        tieBreakWinnerComboBox.getItems().clear();
        tieBreakWinnerComboBox.getItems().addAll(match.player1Name(), match.player2Name());

        applyExistingScore(match.score());

        if (match.played() && match.draw()) {
            tieBreakWinnerComboBox.setValue(match.winnerName());
        }

        updateTieBreakVisibility();
    }

    private void updateTieBreakVisibility() {
        boolean isKnockoutDraw = (tournamentType == TournamentType.KNOCKOUT) && (getPlayer1Points() == getPlayer2Points());
        tieBreakContainer.setVisible(isKnockoutDraw);
        tieBreakContainer.setManaged(isKnockoutDraw);
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

    public Integer getTieBreakWinnerIndex() {
        if (tournamentType == TournamentType.KNOCKOUT && getPlayer1Points() == getPlayer2Points()) {
            String selected = tieBreakWinnerComboBox.getValue();
            if (selected == null) {
                return null;
            }
            if (selected.equals(player1Label.getText())) {
                return 1;
            }
            if (selected.equals(player2Label.getText())) {
                return 2;
            }
        }
        return null;
    }

    @FXML
    private void onSave() {
        if (tournamentType == TournamentType.KNOCKOUT && getPlayer1Points() == getPlayer2Points()) {
            if (tieBreakWinnerComboBox.getValue() == null) {
                errorLabel.setText("Please select a tie-break winner");
                return;
            }
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
            int p1 = Integer.parseInt(parts[0].trim());
            int p2 = Integer.parseInt(parts[1].trim().split(" ")[0]);
            player1PointsSpinner.getValueFactory().setValue(p1);
            player2PointsSpinner.getValueFactory().setValue(p2);
        } catch (NumberFormatException ignored) {
            player1PointsSpinner.getValueFactory().setValue(0);
            player2PointsSpinner.getValueFactory().setValue(0);
        }
    }
}
