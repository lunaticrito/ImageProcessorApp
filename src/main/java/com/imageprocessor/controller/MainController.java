package com.imageprocessor.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.input.ScrollEvent;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import javafx.collections.*;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.time.LocalDateTime;

import com.imageprocessor.model.*;
import com.imageprocessor.database.DatabaseManager;
import com.imageprocessor.service.*;
import com.imageprocessor.util.*;

public class MainController implements Initializable {

    @FXML private ListView<ImageModel> galleryListView;
    @FXML private ImageView imageView;
    @FXML private ScrollPane scrollPane;
    @FXML private StackPane imageContainer;
    @FXML private Rectangle cropOverlay;
    @FXML private Slider brightnessSlider, contrastSlider, saturationSlider, hueSlider;
    @FXML private Slider blurSlider, sharpenSlider, rotationSlider;
    @FXML private Label brightnessLabel, contrastLabel, saturationLabel, hueLabel;
    @FXML private Label blurLabel, sharpenLabel, rotationLabel;
    @FXML private CheckBox grayscaleCheck, sepiaCheck, invertCheck, edgeDetectCheck;
    @FXML private ComboBox<String> presetCombo;
    @FXML private ProgressBar progressBar;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label statusLabel, imageInfoLabel, zoomLabel;
    @FXML private HBox statusBar;

    private final DatabaseManager dbManager = DatabaseManager.getInstance();
    private final ImageProcessingService processingService = new ImageProcessingService();
    private final ObservableList<ImageModel> galleryImages = FXCollections.observableArrayList();

    private BufferedImage originalImage; 
    private BufferedImage currentImage;  
    private final Deque<BufferedImage> undoStack = new ArrayDeque<>(20);
    private final Deque<BufferedImage> redoStack = new ArrayDeque<>(20);
    private ImageModel currentImageModel; 

    private double zoomLevel = 1.0;
    private boolean isCropping = false;
    private double cropStartX, cropStartY;

    private final PauseTransition sliderDebounce = new PauseTransition(Duration.millis(300));
    private boolean suppressFilterUpdate = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        galleryListView.setItems(galleryImages);
        galleryListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(ImageModel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox box = new VBox();
                    Label nameLbl = new Label(item.getFileName());
                    Label dimLbl = new Label(item.getWidth() + "x" + item.getHeight());
                    dimLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #a6adc8;");
                    box.getChildren().addAll(nameLbl, dimLbl);
                    setGraphic(box);
                }
            }
        });
        
        galleryListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getFile() != null) {
                loadImageIntoView(newVal.getFile());
                currentImageModel = newVal;
            }
        });

        loadGalleryFromDatabase();

        setupSlider(brightnessSlider, brightnessLabel, true);
        setupSlider(contrastSlider, contrastLabel, true);
        setupSlider(saturationSlider, saturationLabel, true);
        setupSlider(hueSlider, hueLabel, true);
        setupSlider(rotationSlider, rotationLabel, true);
        setupSlider(blurSlider, blurLabel, false);
        setupSlider(sharpenSlider, sharpenLabel, false);

        grayscaleCheck.selectedProperty().addListener((o, old, val) -> applyCurrentFilters());
        sepiaCheck.selectedProperty().addListener((o, old, val) -> applyCurrentFilters());
        invertCheck.selectedProperty().addListener((o, old, val) -> applyCurrentFilters());
        edgeDetectCheck.selectedProperty().addListener((o, old, val) -> applyCurrentFilters());

        progressBar.visibleProperty().bind(processingService.runningProperty());
        progressIndicator.visibleProperty().bind(processingService.runningProperty());
        progressBar.progressProperty().bind(processingService.progressProperty());
        statusLabel.textProperty().bind(processingService.statusMessageProperty());

        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.isControlDown() || true) {
                if (event.getDeltaY() > 0) {
                    onZoomIn();
                } else {
                    onZoomOut();
                }
                event.consume();
            }
        });

        imageContainer.setOnMousePressed(e -> {
            if (isCropping) {
                cropStartX = e.getX();
                cropStartY = e.getY();
                cropOverlay.setX(cropStartX);
                cropOverlay.setY(cropStartY);
                cropOverlay.setWidth(0);
                cropOverlay.setHeight(0);
                cropOverlay.setVisible(true);
            }
        });
        
        imageContainer.setOnMouseDragged(e -> {
            if (isCropping) {
                double currentX = e.getX();
                double currentY = e.getY();
                cropOverlay.setX(Math.min(cropStartX, currentX));
                cropOverlay.setY(Math.min(cropStartY, currentY));
                cropOverlay.setWidth(Math.abs(currentX - cropStartX));
                cropOverlay.setHeight(Math.abs(currentY - cropStartY));
            }
        });
        
        imageContainer.setOnMouseReleased(e -> {
            if (isCropping) {
                // crop overlay finalized
            }
        });

        sliderDebounce.setOnFinished(e -> applyCurrentFilters());

        loadPresetsIntoComboBox();
        presetCombo.getSelectionModel().selectedItemProperty().addListener((o, old, val) -> {
            if (val != null) {
                try {
                    FilterPreset preset = dbManager.getPreset(val);
                    if (preset != null) {
                        applyPresetToControls(preset);
                    }
                } catch (Exception ex) {
                    System.err.println("Failed to load preset: " + ex.getMessage());
                }
            }
        });
    }

    private void setupSlider(Slider slider, Label label, boolean integer) {
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (integer) {
                label.setText(String.valueOf(newVal.intValue()));
            } else {
                label.setText(String.format(Locale.US, "%.1f", newVal.doubleValue()));
            }
            if (!suppressFilterUpdate) {
                sliderDebounce.playFromStart();
            }
        });
    }

    @FXML private void onOpenImage() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp"));
        File file = fc.showOpenDialog(imageView.getScene().getWindow());
        if (file != null) {
            try {
                BufferedImage img = ImageUtils.loadFromFile(file);
                ImageModel model = createImageModelFromFile(file, img);
                dbManager.saveImageMetadata(model);
                galleryImages.add(model);
                galleryListView.getSelectionModel().select(model);
            } catch (Exception e) {
                DialogUtils.showError("Open Error", "Failed to open image.", e);
            }
        }
    }

    @FXML private void onOpenFolder() {
        DirectoryChooser dc = new DirectoryChooser();
        File dir = dc.showDialog(imageView.getScene().getWindow());
        if (dir != null) {
            File[] files = dir.listFiles((d, name) -> name.matches("(?i).*\\.(png|jpg|jpeg|bmp)"));
            if (files != null) {
                for (File f : files) {
                    try {
                        BufferedImage img = ImageUtils.loadFromFile(f);
                        ImageModel model = createImageModelFromFile(f, img);
                        dbManager.saveImageMetadata(model);
                        galleryImages.add(model);
                    } catch (Exception e) {
                        System.err.println("Failed to load: " + f.getName());
                    }
                }
            }
        }
    }

    @FXML private void onSaveImage() {
        if (currentImageModel != null && currentImage != null && currentImageModel.getFile() != null) {
            try {
                ImageUtils.saveToFile(currentImage, currentImageModel.getFile());
                dbManager.logEdit(currentImageModel.getId(), "Saved Image");
                statusLabel.setText("Saved successfully.");
            } catch (Exception e) {
                DialogUtils.showError("Save Error", "Failed to save image.", e);
            }
        }
    }

    @FXML private void onSaveImageAs() {
        if (currentImage == null) return;
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPEG", "*.jpg"));
        File file = fc.showSaveDialog(imageView.getScene().getWindow());
        if (file != null) {
            try {
                ImageUtils.saveToFile(currentImage, file);
                ImageModel model = createImageModelFromFile(file, currentImage);
                dbManager.saveImageMetadata(model);
                galleryImages.add(model);
                galleryListView.getSelectionModel().select(model);
                statusLabel.setText("Saved successfully.");
            } catch (Exception e) {
                DialogUtils.showError("Save Error", "Failed to save image.", e);
            }
        }
    }

    @FXML private void onExit() {
        Platform.exit();
    }

    @FXML private void onDeleteImage() {
        ImageModel selected = galleryListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (DialogUtils.showConfirm("Delete Image", "Remove image from gallery?")) {
                try {
                    dbManager.deleteImage(selected.getId());
                    galleryImages.remove(selected);
                    if (currentImageModel == selected) {
                        currentImage = null;
                        originalImage = null;
                        imageView.setImage(null);
                        imageInfoLabel.setText("No image loaded");
                    }
                } catch (Exception e) {
                    DialogUtils.showError("Delete Error", "Failed to delete image.", e);
                }
            }
        }
    }

    @FXML private void onRotateLeft() {
        if (currentImage == null) return;
        pushUndo();
        processingService.processImage(currentImage, ImageOperations::rotate90CCW, this::onProcessingComplete, this::onProcessingError);
    }

    @FXML private void onRotateRight() {
        if (currentImage == null) return;
        pushUndo();
        processingService.processImage(currentImage, ImageOperations::rotate90CW, this::onProcessingComplete, this::onProcessingError);
    }

    @FXML private void onApplyRotation() {
        if (currentImage == null) return;
        double degrees = rotationSlider.getValue();
        if (degrees != 0) {
            pushUndo();
            processingService.processImage(currentImage, img -> ImageOperations.rotate(img, degrees), this::onProcessingComplete, this::onProcessingError);
            rotationSlider.setValue(0);
        }
    }

    @FXML private void onFlipHorizontal() {
        if (currentImage == null) return;
        pushUndo();
        processingService.processImage(currentImage, ImageOperations::flipHorizontal, this::onProcessingComplete, this::onProcessingError);
    }

    @FXML private void onFlipVertical() {
        if (currentImage == null) return;
        pushUndo();
        processingService.processImage(currentImage, ImageOperations::flipVertical, this::onProcessingComplete, this::onProcessingError);
    }

    @FXML private void onCrop() {
        if (currentImage == null) return;
        if (!isCropping) {
            isCropping = true;
            imageContainer.setCursor(Cursor.CROSSHAIR);
            statusLabel.setText("Draw a rectangle to crop. Click Crop again to confirm.");
        } else {
            if (cropOverlay.isVisible() && cropOverlay.getWidth() > 0 && cropOverlay.getHeight() > 0) {
                pushUndo();
                double scaleX = currentImage.getWidth() / imageView.getBoundsInLocal().getWidth();
                double scaleY = currentImage.getHeight() / imageView.getBoundsInLocal().getHeight();
                int x = (int) (cropOverlay.getX() * scaleX);
                int y = (int) (cropOverlay.getY() * scaleY);
                int w = (int) (cropOverlay.getWidth() * scaleX);
                int h = (int) (cropOverlay.getHeight() * scaleY);
                processingService.processImage(currentImage, img -> ImageOperations.crop(img, x, y, w, h), this::onProcessingComplete, this::onProcessingError);
            }
            isCropping = false;
            cropOverlay.setVisible(false);
            imageContainer.setCursor(Cursor.DEFAULT);
            statusLabel.setText("Ready");
        }
    }

    @FXML private void onResize() {
        if (currentImage == null) return;
        TextInputDialog dialog = new TextInputDialog(currentImage.getWidth() + "x" + currentImage.getHeight());
        dialog.setTitle("Resize Image");
        dialog.setHeaderText("Enter new dimensions (WxH):");
        dialog.showAndWait().ifPresent(val -> {
            try {
                String[] parts = val.split("x");
                int w = Integer.parseInt(parts[0].trim());
                int h = Integer.parseInt(parts[1].trim());
                pushUndo();
                processingService.processImage(currentImage, img -> ImageOperations.resize(img, w, h), this::onProcessingComplete, this::onProcessingError);
            } catch (Exception e) {
                DialogUtils.showError("Invalid Input", "Please enter dimensions in format WxH", e);
            }
        });
    }

    @FXML private void onUndo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(ImageUtils.deepCopy(currentImage));
            currentImage = undoStack.pop();
            displayImage(currentImage);
        }
    }

    @FXML private void onRedo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(ImageUtils.deepCopy(currentImage));
            currentImage = redoStack.pop();
            displayImage(currentImage);
        }
    }

    @FXML private void onResetFilters() {
        suppressFilterUpdate = true;
        brightnessSlider.setValue(0);
        contrastSlider.setValue(0);
        saturationSlider.setValue(0);
        hueSlider.setValue(0);
        blurSlider.setValue(0);
        sharpenSlider.setValue(0);
        rotationSlider.setValue(0);
        grayscaleCheck.setSelected(false);
        sepiaCheck.setSelected(false);
        invertCheck.setSelected(false);
        edgeDetectCheck.setSelected(false);
        suppressFilterUpdate = false;
        applyCurrentFilters();
    }

    @FXML private void onSavePreset() {
        TextInputDialog dialog = new TextInputDialog("My Preset");
        dialog.setTitle("Save Preset");
        dialog.setHeaderText("Enter preset name:");
        dialog.showAndWait().ifPresent(name -> {
            try {
                FilterPreset preset = buildPresetFromControls();
                preset.setName(name);
                dbManager.savePreset(preset);
                if (!presetCombo.getItems().contains(name)) {
                    presetCombo.getItems().add(name);
                }
                presetCombo.getSelectionModel().select(name);
            } catch (Exception e) {
                DialogUtils.showError("Save Error", "Failed to save preset.", e);
            }
        });
    }

    @FXML private void onDeletePreset() {
        String selected = presetCombo.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (DialogUtils.showConfirm("Delete Preset", "Delete preset '" + selected + "'?")) {
                try {
                    dbManager.deletePreset(selected);
                    presetCombo.getItems().remove(selected);
                } catch (Exception e) {
                    DialogUtils.showError("Delete Error", "Failed to delete preset.", e);
                }
            }
        }
    }

    @FXML private void onExportPreset() {
        FilterPreset preset = buildPresetFromControls();
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fc.showSaveDialog(imageView.getScene().getWindow());
        if (file != null) {
            try {
                JsonUtil.exportPreset(preset, file);
                statusLabel.setText("Preset exported.");
            } catch (Exception e) {
                DialogUtils.showError("Export Error", "Failed to export preset.", e);
            }
        }
    }

    @FXML private void onImportPreset() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fc.showOpenDialog(imageView.getScene().getWindow());
        if (file != null) {
            try {
                FilterPreset preset = JsonUtil.importPreset(file);
                applyPresetToControls(preset);
                statusLabel.setText("Preset imported.");
            } catch (Exception e) {
                DialogUtils.showError("Import Error", "Failed to import preset.", e);
            }
        }
    }

    @FXML private void onViewHistory() {
        if (currentImageModel != null) {
            try {
                List<String> logs = dbManager.getEditHistory(currentImageModel.getId());
                TextArea ta = new TextArea(String.join("\n", logs));
                ta.setEditable(false);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Edit History");
                alert.setHeaderText("Edit history for " + currentImageModel.getFileName());
                alert.getDialogPane().setContent(ta);
                alert.showAndWait();
            } catch (Exception e) {
                DialogUtils.showError("History Error", "Failed to load history.", e);
            }
        }
    }

    @FXML private void onClearHistory() {
        if (currentImageModel != null) {
            if (DialogUtils.showConfirm("Clear History", "Clear edit history for this image?")) {
                try {
                    dbManager.clearHistory(currentImageModel.getId());
                    statusLabel.setText("History cleared.");
                } catch (Exception e) {
                    DialogUtils.showError("Clear Error", "Failed to clear history.", e);
                }
            }
        }
    }

    @FXML private void onZoomIn() {
        zoomLevel = Math.min(5.0, zoomLevel + 0.1);
        updateZoom();
    }

    @FXML private void onZoomOut() {
        zoomLevel = Math.max(0.1, zoomLevel - 0.1);
        updateZoom();
    }

    @FXML private void onFitToWindow() {
        if (currentImage != null) {
            double rw = scrollPane.getViewportBounds().getWidth() / currentImage.getWidth();
            double rh = scrollPane.getViewportBounds().getHeight() / currentImage.getHeight();
            zoomLevel = Math.min(rw, rh);
            zoomLevel = Math.max(0.1, Math.min(5.0, zoomLevel));
            updateZoom();
        }
    }

    @FXML private void onAbout() {
        DialogUtils.showInfo("About", "JavaFX Image Processor\nVersion 1.0\nA comprehensive image processing tool.");
    }

    private void loadImageIntoView(File file) {
        try {
            BufferedImage img = ImageUtils.loadFromFile(file);
            originalImage = img;
            currentImage = ImageUtils.deepCopy(img);
            undoStack.clear();
            redoStack.clear();
            onResetFilters();
            displayImage(currentImage);
            updateImageInfo();
            zoomLevel = 1.0;
            updateZoom();
        } catch (Exception e) {
            DialogUtils.showError("Load Error", "Failed to load image into view.", e);
        }
    }

    private void displayImage(BufferedImage img) {
        if (img != null) {
            Platform.runLater(() -> {
                Image fxImg = ImageUtils.convertToFXImage(img);
                imageView.setImage(fxImg);
                updateImageInfo();
            });
        }
    }

    private FilterPreset buildPresetFromControls() {
        FilterPreset preset = new FilterPreset();
        preset.setBrightness((int) brightnessSlider.getValue());
        preset.setContrast((int) contrastSlider.getValue());
        preset.setSaturation((int) saturationSlider.getValue());
        preset.setHue((int) hueSlider.getValue());
        preset.setBlur(blurSlider.getValue());
        preset.setSharpen(sharpenSlider.getValue());
        preset.setGrayscale(grayscaleCheck.isSelected());
        preset.setSepia(sepiaCheck.isSelected());
        preset.setInvert(invertCheck.isSelected());
        preset.setEdgeDetect(edgeDetectCheck.isSelected());
        return preset;
    }

    private void applyCurrentFilters() {
        if (originalImage == null || suppressFilterUpdate) return;
        FilterPreset preset = buildPresetFromControls();
        if (preset.isDefault()) {
            currentImage = ImageUtils.deepCopy(originalImage);
            displayImage(currentImage);
        } else {
            processingService.processImage(originalImage, preset, this::onProcessingComplete, this::onProcessingError);
        }
    }

    private void onProcessingComplete(BufferedImage result) {
        currentImage = result;
        displayImage(result);
    }

    private void onProcessingError(Throwable error) {
        Platform.runLater(() -> DialogUtils.showError("Processing Error", "An error occurred during processing.", error));
    }

    private void pushUndo() {
        if (currentImage != null) {
            undoStack.push(ImageUtils.deepCopy(currentImage));
            if (undoStack.size() > 20) {
                undoStack.removeLast();
            }
            redoStack.clear();
        }
    }

    private void updateZoom() {
        if (currentImage != null) {
            imageView.setFitWidth(currentImage.getWidth() * zoomLevel);
            imageView.setFitHeight(currentImage.getHeight() * zoomLevel);
            zoomLabel.setText(String.format(Locale.US, "%d%%", (int)(zoomLevel * 100)));
        }
    }

    private void updateImageInfo() {
        if (currentImage != null && currentImageModel != null) {
            imageInfoLabel.setText(currentImage.getWidth() + "x" + currentImage.getHeight() + " - " + currentImageModel.getFileName());
        } else {
            imageInfoLabel.setText("No image loaded");
        }
    }

    private void applyPresetToControls(FilterPreset preset) {
        suppressFilterUpdate = true;
        brightnessSlider.setValue(preset.getBrightness());
        contrastSlider.setValue(preset.getContrast());
        saturationSlider.setValue(preset.getSaturation());
        hueSlider.setValue(preset.getHue());
        blurSlider.setValue(preset.getBlur());
        sharpenSlider.setValue(preset.getSharpen());
        grayscaleCheck.setSelected(preset.isGrayscale());
        sepiaCheck.setSelected(preset.isSepia());
        invertCheck.setSelected(preset.isInvert());
        edgeDetectCheck.setSelected(preset.isEdgeDetect());
        suppressFilterUpdate = false;
        applyCurrentFilters();
    }

    private void loadGalleryFromDatabase() {
        try {
            galleryImages.setAll(dbManager.getAllImages());
        } catch (Exception e) {
            System.err.println("Failed to load gallery from database: " + e.getMessage());
        }
    }

    private void loadPresetsIntoComboBox() {
        try {
            presetCombo.getItems().setAll(dbManager.getAllPresetNames());
        } catch (Exception e) {
            System.err.println("Failed to load presets: " + e.getMessage());
        }
    }

    private ImageModel createImageModelFromFile(File file, BufferedImage img) {
        ImageModel model = new ImageModel();
        model.setFilePath(file.getAbsolutePath());
        model.setFileName(file.getName());
        model.setWidth(img.getWidth());
        model.setHeight(img.getHeight());
        model.setFormat(ImageUtils.getFormatFromFileName(file.getName()));
        model.setFileSizeBytes(file.length());
        model.setCreatedAt(LocalDateTime.now());
        model.setUpdatedAt(LocalDateTime.now());
        return model;
    }
}
