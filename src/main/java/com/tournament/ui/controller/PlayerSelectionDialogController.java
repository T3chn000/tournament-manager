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

public class PlayerSelectionDialogController {
    @FXML private TextField playerNameField;
    @FXML private ListView<Player> availablePlayersListView;
    @FXML private ListView<Player> selectedPlayersListView;
    @FXML private ListView<String> newPlayersListView;
    @FXML private Label errorLabel;

    private Stage dialogStage;
    private boolean confirmed;
    private int minimumSelectionCount = 1;
    private Set<UUID> excludedPlayerIds = Set.of();
    private final List<Player> availablePlayers = new ArrayList<>();

    @FXML
    private void initialize() {
        configurePlayerList(availablePlayersListView);
        configurePlayerList(selectedPlayersListView);
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setMinimumSelectionCount(int minimumSelectionCount) {
        this.minimumSelectionCount = Math.max(0, minimumSelectionCount);
    }

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

    public List<Player> getSelectedPlayers() {
        return List.copyOf(selectedPlayersListView.getItems());
    }

    public List<String> getNewPlayerNames() {
        return List.copyOf(newPlayersListView.getItems());
    }

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

    @FXML
    private void onAddSelectedPlayer() {
        Player selected = availablePlayersListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            addSelectedPlayer(selected);
        }
    }

    @FXML
    private void onRemoveSelectedPlayer() {
        int selectedIndex = selectedPlayersListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            selectedPlayersListView.getItems().remove(selectedIndex);
            refreshAvailablePlayers();
        }
    }

    @FXML
    private void onRemoveNewPlayer() {
        int selectedIndex = newPlayersListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            newPlayersListView.getItems().remove(selectedIndex);
        }
    }

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

    @FXML
    private void onCancel() {
        confirmed = false;
        dialogStage.close();
    }

    private void addSelectedPlayer(Player player) {
        if (excludedPlayerIds.contains(player.playerId())) {
            setError("Player already exists in tournament");
            return;
        }

        selectedPlayersListView.getItems().add(player);
        refreshAvailablePlayers();
        setError("");
    }

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

    private void configurePlayerList(ListView<Player> listView) {
        listView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Player item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : "%s (%s)".formatted(item.name(), item.playerId().toString().substring(0, 8)));
            }
        });
    }

    private void setError(String message) {
        errorLabel.setText(message);
    }
}
