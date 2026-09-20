package com.imageprocessor.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * Utility class for displaying JavaFX dialogs and choosers.
 */
public class DialogUtils {

    /**
     * Shows an information alert dialog.
     */
    public static void showInfo(String title, String message) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> showInfo(title, message));
            return;
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(null);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows a warning alert dialog.
     */
    public static void showWarning(String title, String message) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> showWarning(title, message));
            return;
        }
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initOwner(null);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows an error alert dialog.
     */
    public static void showError(String title, String message) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> showError(title, message));
            return;
        }
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(null);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows an error alert dialog with throwable exception details.
     */
    public static void showError(String title, String message, Throwable e) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> showError(title, message, e));
            return;
        }
        String detailedMessage = message;
        if (e != null) {
            String exMsg = e.getMessage();
            detailedMessage += "\n\nDetails: " + (exMsg != null && !exMsg.isEmpty() ? exMsg : e.toString());
        }
        showError(title, detailedMessage);
    }

    /**
     * Shows a confirmation alert dialog returning true if confirmed.
     */
    public static boolean showConfirm(String title, String message) {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("showConfirm must be called on the JavaFX Application Thread");
        }
        Optional<ButtonType> result = showConfirmation(title, message);
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Shows a confirmation alert dialog.
     */
    public static Optional<ButtonType> showConfirmation(String title, String message) {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("showConfirmation must be called on the JavaFX Application Thread");
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(null);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait();
    }

    /**
     * Shows an open file dialog for images.
     */
    public static File showOpenImageDialog(Window owner) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Image");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.bmp"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        return chooser.showOpenDialog(owner);
    }

    /**
     * Shows an open file dialog for multiple images.
     */
    public static List<File> showOpenMultipleImagesDialog(Window owner) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Multiple Images");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.bmp"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        return chooser.showOpenMultipleDialog(owner);
    }

    /**
     * Shows a save file dialog for images.
     */
    public static File showSaveImageDialog(Window owner) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Image As");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("JPEG", "*.jpeg", "*.jpg"),
                new FileChooser.ExtensionFilter("BMP", "*.bmp")
        );
        return chooser.showSaveDialog(owner);
    }

    /**
     * Shows a save file dialog for exporting presets.
     */
    public static File showExportPresetDialog(Window owner) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Preset");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        return chooser.showSaveDialog(owner);
    }

    /**
     * Shows an open file dialog for importing presets.
     */
    public static File showImportPresetDialog(Window owner) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Preset");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        return chooser.showOpenDialog(owner);
    }

    /**
     * Creates and configures a directory chooser.
     */
    public static DirectoryChooser createDirectoryChooser(String title) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(title);
        return chooser;
    }
}
