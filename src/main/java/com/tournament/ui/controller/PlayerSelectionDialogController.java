package com.tournament.ui.controller;

import com.tournament.model.Player;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Controller for selecting existing players and entering new player names.
 *
 * <p>The dialog returns existing {@link Player} instances separately from new
 * names so the application service can decide how to persist them.</p>
 */
public class PlayerSelectionDialogController {
    @FXML private TextField playerNameField;
    @FXML private ListView<Player> availablePlayersListView;
    @FXML private ListView<Player> selectedPlayersListView;
    @FXML private ListView<String> newPlayersListView;
    @FXML private Label errorLabel;

    private Stage dialogStage;
    private boolean confirmed;
    private int minimumSelectionCount = 1;
    /**
     * Player IDs hidden from selection, usually because they are already in the tournament.
     */
    private Set<UUID> excludedPlayerIds = Set.of();
    /**
     * Local copy of the player base used to rebuild available selections.
     */
    private final List<Player> availablePlayers = new ArrayList<>();

    /**
     * Configures list rendering after FXML fields are injected.
     */
    @FXML
    private void initialize() {
        configurePlayerList(availablePlayersListView);
        configurePlayerList(selectedPlayersListView);
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Sets the minimum total count of selected and newly entered players.
     *
     * @param minimumSelectionCount required count
     */
    public void setMinimumSelectionCount(int minimumSelectionCount) {
        this.minimumSelectionCount = Math.max(0, minimumSelectionCount);
    }

    /**
     * Configures selectable players and players that should be hidden.
     *
     * @param players available player base
     * @param excludedPlayerIds identifiers that should not be selectable
     */
    public void setPlayers(List<Player> players, Set<UUID> excludedPlayerIds) {
        availablePlayers.clear();
        if (players != null) {
            availablePlayers.addAll(players);
        }
        this.excludedPlayerIds = excludedPlayerIds == null ? Set.of() : Set.copyOf(excludedPlayerIds);
        refreshAvailablePlayers();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Returns existing players selected by the user.
     */
    public List<Player> getSelectedPlayers() {
        return List.copyOf(selectedPlayersListView.getItems());
    }

    /**
     * Returns new player names entered by the user.
     */
    public List<String> getNewPlayerNames() {
        return List.copyOf(newPlayersListView.getItems());
    }

    /**
     * Adds a typed name either as an existing matching player or as a new player name.
     */
    @FXML
    private void onQuickAddPlayer() {
        String playerName = playerNameField.getText() == null ? "" : playerNameField.getText().trim();
        if (playerName.isBlank()) {
            setError("Player name cannot be empty");
            return;
        }

        availablePlayers.stream()
                .filter(existing -> existing.name().equalsIgnoreCase(playerName))
                .findFirst()
                .ifPresentOrElse(
                        this::addSelectedPlayer,
                        () -> newPlayersListView.getItems().add(playerName)
                );
        playerNameField.clear();
        setError("");
    }

    /**
     * Moves the highlighted existing player to the selected list.
     */
    @FXML
    private void onAddSelectedPlayer() {
        Player selected = availablePlayersListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            addSelectedPlayer(selected);
        }
    }

    /**
     * Removes an existing player from the selected list and makes it selectable again.
     */
    @FXML
    private void onRemoveSelectedPlayer() {
        int selectedIndex = selectedPlayersListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            selectedPlayersListView.getItems().remove(selectedIndex);
            refreshAvailablePlayers();
        }
    }

    /**
     * Removes a typed new player name from the pending list.
     */
    @FXML
    private void onRemoveNewPlayer() {
        int selectedIndex = newPlayersListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            newPlayersListView.getItems().remove(selectedIndex);
        }
    }

    /**
     * Confirms the selection after checking the minimum required player count.
     */
    @FXML
    private void onConfirm() {
        int selectedCount = selectedPlayersListView.getItems().size() + newPlayersListView.getItems().size();
        if (selectedCount < minimumSelectionCount) {
            setError("Select at least " + minimumSelectionCount + " player" + (minimumSelectionCount == 1 ? "" : "s"));
            return;
        }

        confirmed = true;
        dialogStage.close();
    }

    /**
     * Closes the dialog without returning a selection.
     */
    @FXML
    private void onCancel() {
        confirmed = false;
        dialogStage.close();
    }

    /**
     * Adds one existing player to the selected list while respecting exclusions.
     */
    private void addSelectedPlayer(Player player) {
        if (excludedPlayerIds.contains(player.playerId())) {
            setError("Player already exists in tournament");
            return;
        }

        selectedPlayersListView.getItems().add(player);
        refreshAvailablePlayers();
        setError("");
    }

    /**
     * Rebuilds the available-player list after selection or exclusion changes.
     */
    private void refreshAvailablePlayers() {
        List<UUID> selectedIds = selectedPlayersListView == null
                ? List.of()
                : selectedPlayersListView.getItems().stream().map(Player::playerId).toList();
        List<Player> selectablePlayers = availablePlayers.stream()
                .filter(player -> !excludedPlayerIds.contains(player.playerId()))
                .filter(player -> !selectedIds.contains(player.playerId()))
                .toList();
        availablePlayersListView.setItems(FXCollections.observableArrayList(selectablePlayers));
    }

    /**
     * Formats player list cells with display name and short identifier.
     */
    private void configurePlayerList(ListView<Player> listView) {
        listView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Player item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : "%s (%s)".formatted(item.name(), item.playerId().toString().substring(0, 8)));
            }
        });
    }

    /**
     * Shows validation feedback inside the dialog.
     */
    private void setError(String message) {
        errorLabel.setText(message);
    }
}
