package com.imageprocessor;

import com.imageprocessor.database.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.net.URL;

public class App extends Application {

    @Override
    public void init() throws Exception {
        try {
            DatabaseManager.getInstance().initialize();
        } catch (Exception e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            URL fxmlUrl = getClass().getResource("/com/imageprocessor/main-view.fxml");
            if (fxmlUrl == null) {
                throw new IllegalStateException("Cannot find /com/imageprocessor/main-view.fxml in resources");
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            
            URL cssUrl = getClass().getResource("/com/imageprocessor/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            primaryStage.setTitle("Image Processor");
            primaryStage.setMinWidth(1200);
            primaryStage.setMinHeight(800);
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (Exception e) {
            System.err.println("Failed to load application: " + e.getMessage());
            e.printStackTrace();
            
            // Note: If JavaFX fails to init, Alert might throw an exception too, but this catches FXML load errors
            try {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Fatal Error");
                alert.setHeaderText("Application Failed to Start");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            } catch (Exception ex) {
                System.err.println("Could not display error dialog: " + ex.getMessage());
            }
            
            System.exit(1);
        }
    }

    @Override
    public void stop() throws Exception {
        try {
            DatabaseManager.getInstance().close();
        } catch (Exception e) {
            System.err.println("Error while closing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
