package LAPR3.UI.javafx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Main controller for the JavaFX application
 * Manages the tabbed interface with three main sections
 */
public class MainController implements Initializable {

    @FXML
    private TabPane mainTabPane;

    @FXML
    private Tab freightAssignmentTab;

    @FXML
    private Tab trainAssemblyTab;

    @FXML
    private Tab trafficManagerTab;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Add listener to refresh data when switching tabs
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == null) return;
            
            // Try to get the controller from the tab content
            Node tabContent = newTab.getContent();
            if (tabContent != null) {
                // Find controller in the content
                Object userData = tabContent.getUserData();
                if (userData instanceof TrainAssemblyController) {
                    ((TrainAssemblyController) userData).refreshData();
                } else if (userData instanceof TrafficManagerController) {
                    ((TrafficManagerController) userData).refreshData();
                }
            }
            
            // Alternative: Use FXMLLoader to get controller
            // This is a workaround since fx:include doesn't expose controllers directly
            if (newTab == trainAssemblyTab || newTab == trafficManagerTab) {
                // Force refresh by accessing ServiceManager
                // The controllers will refresh when their UI elements are accessed
                ServiceManager.getInstance(); // Ensure service manager is initialized
            }
        });
    }
}

