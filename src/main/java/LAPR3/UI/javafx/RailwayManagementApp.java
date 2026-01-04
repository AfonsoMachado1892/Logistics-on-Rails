package LAPR3.UI.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX Application for Railway Network Logistics Management
 * Main entry point for the GUI application
 */
public class RailwayManagementApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            Scene scene = new Scene(loader.load(), 1400, 900);
            
            // Load CSS
            String css = getClass().getResource("/css/railway-theme.css").toExternalForm();
            scene.getStylesheets().add(css);
            
            primaryStage.setTitle("Railway Network Logistics Management System");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1200);
            primaryStage.setMinHeight(800);
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Error loading FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // For JavaFX 11+, we need to explicitly launch
        Application.launch(RailwayManagementApp.class, args);
    }
}

