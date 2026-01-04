package LAPR3.UI.javafx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;
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

    private FreightAssignmentController freightAssignmentController;
    private TrainAssemblyController trainAssemblyController;
    private TrafficManagerController trafficManagerController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize ServiceManager once
        ServiceManager.getInstance(); 

        loadTabContent(freightAssignmentTab, "/fxml/FreightAssignmentView.fxml");
        loadTabContent(trainAssemblyTab, "/fxml/TrainAssemblyView.fxml");
        loadTabContent(trafficManagerTab, "/fxml/TrafficManagerView.fxml");

        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                if (newTab == freightAssignmentTab && freightAssignmentController != null) {
                    freightAssignmentController.refreshData();
                } else if (newTab == trainAssemblyTab && trainAssemblyController != null) {
                    trainAssemblyController.refreshData();
                } else if (newTab == trafficManagerTab && trafficManagerController != null) {
                    trafficManagerController.refreshData();
                }
            }
        });
    }

    private void loadTabContent(Tab tab, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            tab.setContent(content);

            // Store controller instances
            if (fxmlPath.equals("/fxml/FreightAssignmentView.fxml")) {
                freightAssignmentController = loader.getController();
            } else if (fxmlPath.equals("/fxml/TrainAssemblyView.fxml")) {
                trainAssemblyController = loader.getController();
            } else if (fxmlPath.equals("/fxml/TrafficManagerView.fxml")) {
                trafficManagerController = loader.getController();
            }

        } catch (IOException e) {
            System.err.println("Failed to load FXML for tab " + tab.getText() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}

