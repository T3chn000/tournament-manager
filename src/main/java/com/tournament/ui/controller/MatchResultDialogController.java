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

/**
 * Controller for editing one match result.
 *
 * <p>For knockout draws the dialog also asks for a tie-break winner.</p>
 */
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

    /**
     * Configures editable score spinners and tie-break visibility updates.
     */
    @FXML
    private void initialize() {
        player1PointsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999, 0));
        player2PointsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999, 0));
        player1PointsSpinner.setEditable(true);
        player2PointsSpinner.setEditable(true);

        player1PointsSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateTieBreakVisibility());
        player2PointsSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateTieBreakVisibility());
    }

    /**
     * Sets the stage owned by this dialog controller.
     *
     * @param dialogStage dialog stage to close after user action
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Populates the dialog with match data and tournament-specific rules.
     *
     * @param match match view shown in the dialog
     * @param tournamentType tournament format used for tie-break rules
     */
    public void setMatch(MatchView match, TournamentType tournamentType) {
        this.tournamentType = tournamentType;
        player1Label.setText(match.player1Name());
        player2Label.setText(match.player2Name());

        tieBreakWinnerComboBox.getItems().clear();
        tieBreakWinnerComboBox.getItems().addAll(match.player1Name(), match.player2Name());

        applyExistingPoints(match);

        if (match.tieBreakWinnerIndex() != null) {
            tieBreakWinnerComboBox.getSelectionModel().select(match.tieBreakWinnerIndex() - 1);
        }

        updateTieBreakVisibility();
    }

    /**
     * Shows the tie-break selector only when a knockout match is currently drawn.
     */
    private void updateTieBreakVisibility() {
        boolean isKnockoutDraw = (tournamentType == TournamentType.KNOCKOUT) && (getPlayer1Points() == getPlayer2Points());
        tieBreakContainer.setVisible(isKnockoutDraw);
        tieBreakContainer.setManaged(isKnockoutDraw);
    }

    /**
     * Indicates whether the user accepted the edited result.
     *
     * @return {@code true} when the user confirmed the dialog
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Returns points entered for player one.
     *
     * @return points assigned to player one
     */
    public int getPlayer1Points() {
        return player1PointsSpinner.getValue();
    }

    /**
     * Returns points entered for player two.
     *
     * @return points assigned to player two
     */
    public int getPlayer2Points() {
        return player2PointsSpinner.getValue();
    }

    /**
     * Returns selected tie-break winner as player index.
     *
     * @return {@code 1}, {@code 2}, or {@code null} when no tie-break applies
     */
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

    /**
     * Validates the result and closes the dialog when the score can be applied.
     */
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

    /**
     * Closes the dialog without applying the edited score.
     */
    @FXML
    private void onCancel() {
        confirmed = false;
        dialogStage.close();
    }

    /**
     * Pre-fills score inputs when an already played match is edited.
     */
    private void applyExistingPoints(MatchView match) {
        player1PointsSpinner.getValueFactory().setValue(match.player1Points() == null ? 0 : match.player1Points());
        player2PointsSpinner.getValueFactory().setValue(match.player2Points() == null ? 0 : match.player2Points());
    }
}
