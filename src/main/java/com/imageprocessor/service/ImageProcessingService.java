package com.imageprocessor.service;

import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.beans.property.*;

import java.util.concurrent.*;
import java.util.function.*;
import java.awt.image.BufferedImage;
import com.imageprocessor.model.FilterPreset;

/**
 * Service class managing asynchronous image processing.
 */
public class ImageProcessingService {

    private final ExecutorService executor;
    private final ReadOnlyBooleanWrapper running;
    private final ReadOnlyStringWrapper statusMessage;
    private final ReadOnlyDoubleWrapper progress;

    public ImageProcessingService() {
        int threads = Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool(threads, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        running = new ReadOnlyBooleanWrapper(false);
        statusMessage = new ReadOnlyStringWrapper("");
        progress = new ReadOnlyDoubleWrapper(0.0);
    }

    public ReadOnlyBooleanProperty runningProperty() {
        return running.getReadOnlyProperty();
    }

    public ReadOnlyStringProperty statusMessageProperty() {
        return statusMessage.getReadOnlyProperty();
    }

    public ReadOnlyDoubleProperty progressProperty() {
        return progress.getReadOnlyProperty();
    }

    public boolean isRunning() {
        return running.get();
    }

    public void processImage(BufferedImage source, FilterPreset preset, Consumer<BufferedImage> onSuccess, Consumer<Throwable> onFailure) {
        Task<BufferedImage> task = new Task<>() {
            @Override
            protected BufferedImage call() throws Exception {
                updateMessage("Processing image...");
                BufferedImage current = source;
                int totalSteps = 12;
                int step = 0;

                // Step 1: Rotation
                if (isCancelled()) return null;
                if (preset.getRotation() != 0) {
                    current = ImageOperations.rotate(current, preset.getRotation());
                }
                updateProgress(++step, totalSteps);

                // Step 2: Flip Horizontal
                if (isCancelled()) return null;
                if (preset.isFlipHorizontal()) {
                    current = ImageOperations.flipHorizontal(current);
                }
                updateProgress(++step, totalSteps);

                // Step 3: Flip Vertical
                if (isCancelled()) return null;
                if (preset.isFlipVertical()) {
                    current = ImageOperations.flipVertical(current);
                }
                updateProgress(++step, totalSteps);

                // Step 4: Brightness
                if (isCancelled()) return null;
                if (preset.getBrightness() != 0) {
                    current = ImageOperations.adjustBrightness(current, preset.getBrightness());
                }
                updateProgress(++step, totalSteps);

                // Step 5: Contrast
                if (isCancelled()) return null;
                if (preset.getContrast() != 0) {
                    current = ImageOperations.adjustContrast(current, preset.getContrast());
                }
                updateProgress(++step, totalSteps);

                // Step 6: Saturation
                if (isCancelled()) return null;
                if (preset.getSaturation() != 0) {
                    current = ImageOperations.adjustSaturation(current, preset.getSaturation());
                }
                updateProgress(++step, totalSteps);

                // Step 7: Hue
                if (isCancelled()) return null;
                if (preset.getHue() != 0) {
                    current = ImageOperations.adjustHue(current, preset.getHue());
                }
                updateProgress(++step, totalSteps);

                // Step 8: Grayscale
                if (isCancelled()) return null;
                if (preset.isGrayscale()) {
                    current = ImageOperations.applyGrayscale(current);
                }
                updateProgress(++step, totalSteps);

                // Step 9: Sepia
                if (isCancelled()) return null;
                if (preset.isSepia()) {
                    current = ImageOperations.applySepia(current);
                }
                updateProgress(++step, totalSteps);

                // Step 10: Invert
                if (isCancelled()) return null;
                if (preset.isInvert()) {
                    current = ImageOperations.applyInvert(current);
                }
                updateProgress(++step, totalSteps);

                // Step 11: Blur
                if (isCancelled()) return null;
                if (preset.getBlurRadius() > 0) {
                    current = ImageOperations.applyGaussianBlur(current, preset.getBlurRadius());
                }
                updateProgress(++step, totalSteps);

                // Step 12: Sharpen
                if (isCancelled()) return null;
                if (preset.getSharpenStrength() > 0) {
                    current = ImageOperations.applySharpen(current, preset.getSharpenStrength());
                }
                updateProgress(++step, totalSteps);

                // Edge Detection (Extra step if needed, substituting one of the flips or adding 13th)
                if (isCancelled()) return null;
                if (preset.isEdgeDetection()) {
                    current = ImageOperations.applyEdgeDetection(current);
                }

                updateMessage("Done processing.");
                return current;
            }
        };

        setupTaskHandlers(task, onSuccess, onFailure);
        executor.submit(task);
    }

    public void processImage(BufferedImage source, Function<BufferedImage, BufferedImage> operation, Consumer<BufferedImage> onSuccess, Consumer<Throwable> onFailure) {
        applySingleOperation(source, "Operation", operation, onSuccess, onFailure);
    }

    public void applySingleOperation(BufferedImage source, String operationName, Function<BufferedImage, BufferedImage> operation, Consumer<BufferedImage> onSuccess, Consumer<Throwable> onFailure) {
        Task<BufferedImage> task = new Task<>() {
            @Override
            protected BufferedImage call() throws Exception {
                updateMessage("Applying " + operationName + "...");
                updateProgress(0, 1);
                BufferedImage result = operation.apply(source);
                updateProgress(1, 1);
                updateMessage("Done.");
                return result;
            }
        };

        setupTaskHandlers(task, onSuccess, onFailure);
        executor.submit(task);
    }

    private void setupTaskHandlers(Task<BufferedImage> task, Consumer<BufferedImage> onSuccess, Consumer<Throwable> onFailure) {
        task.setOnRunning(e -> {
            Platform.runLater(() -> {
                running.set(true);
                statusMessage.bind(task.messageProperty());
                progress.bind(task.progressProperty());
            });
        });

        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                unbindProperties();
                onSuccess.accept(task.getValue());
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                unbindProperties();
                onFailure.accept(task.getException());
            });
        });

        task.setOnCancelled(e -> {
            Platform.runLater(this::unbindProperties);
        });
    }

    private void unbindProperties() {
        running.set(false);
        statusMessage.unbind();
        statusMessage.set("");
        progress.unbind();
        progress.set(0);
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
