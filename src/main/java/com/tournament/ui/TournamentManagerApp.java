package com.tournament.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * JavaFX entry point for the desktop tournament manager.
 */
public class TournamentManagerApp extends Application {
    private static final String APP_ICON_PATH = "/images/app-icon.png";

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(TournamentManagerApp.class.getResource("/fxml/MainLayout.fxml"));
        Scene scene = new Scene(loader.load(), 1000, 700);
        scene.getStylesheets().add(TournamentManagerApp.class.getResource("/css/app.css").toExternalForm());

        stage.setTitle("Tournament Manager");
        applyApplicationIcon(stage);
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Applies the shared application icon to a stage.
     *
     * @param stage stage to decorate
     */
    public static void applyApplicationIcon(Stage stage) {
        Objects.requireNonNull(stage, "stage");
        stage.getIcons().add(loadApplicationIcon());
    }

    /**
     * Applies the shared application icon to a JavaFX dialog once it is shown.
     *
     * @param dialog dialog to decorate
     */
    public static void applyApplicationIcon(Dialog<?> dialog) {
        Objects.requireNonNull(dialog, "dialog");
        dialog.setOnShown(event -> {
            if (dialog.getDialogPane().getScene().getWindow() instanceof Stage stage) {
                applyApplicationIcon(stage);
            }
        });
    }

    /**
     * Loads the application icon from resources and fails fast when the asset is missing.
     */
    private static Image loadApplicationIcon() {
        return new Image(Objects.requireNonNull(
                TournamentManagerApp.class.getResourceAsStream(APP_ICON_PATH),
                "Missing application icon: " + APP_ICON_PATH
        ));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
