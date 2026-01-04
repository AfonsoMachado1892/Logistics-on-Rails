package LAPR3.UI.javafx;

import LAPR3.model.Freight;
import LAPR3.model.PlannedRoute;
import LAPR3.repository.FacilityRepository;
import LAPR3.repository.FreightRepository;
import LAPR3.service.RoutePlanningService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for Freight Assignment (USLP08)
 */
public class FreightAssignmentController implements Initializable {

    @FXML
    private TableView<Freight> freightTable;
    @FXML
    private TableColumn<Freight, Integer> freightIdColumn;
    @FXML
    private TableColumn<Freight, String> originColumn;
    @FXML
    private TableColumn<Freight, String> destinationColumn;
    @FXML
    private TableColumn<Freight, Integer> wagonsColumn;
    @FXML
    private TableColumn<Freight, String> dateColumn;

    @FXML
    private TableView<PlannedRoute> routesTable;
    @FXML
    private TableColumn<PlannedRoute, String> routeIdColumn;
    @FXML
    private TableColumn<PlannedRoute, String> routeTypeColumn;
    @FXML
    private TableColumn<PlannedRoute, Integer> freightCountColumn;
    @FXML
    private TableColumn<PlannedRoute, String> statusColumn;

    @FXML
    private Button createSimpleRouteButton;
    @FXML
    private Button createCompositeRouteButton;
    @FXML
    private Button refreshButton;
    @FXML
    private TextArea routeDetailsArea;

    @FXML
    private ListView<Integer> selectedFreightsList;
    @FXML
    private Button addFreightButton;
    @FXML
    private Button removeFreightButton;
    @FXML
    private Button clearSelectionButton;

    private FreightRepository freightRepo;
    private FacilityRepository facilityRepo;
    private RoutePlanningService planningService;

    private ObservableList<Freight> freightList = FXCollections.observableArrayList();
    private ObservableList<PlannedRoute> routeList = FXCollections.observableArrayList();
    private ObservableList<Integer> selectedFreightIds = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeRepositories();
        setupTableColumns();
        setupEventHandlers();
        refreshData();
    }

    private void initializeRepositories() {
        ServiceManager serviceManager = ServiceManager.getInstance();
        freightRepo = serviceManager.getFreightRepo();
        facilityRepo = serviceManager.getFacilityRepo();
        planningService = serviceManager.getRoutePlanningService();
    }

    private void setupTableColumns() {
        // Freight table
        freightIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        originColumn.setCellValueFactory(new PropertyValueFactory<>("originName"));
        destinationColumn.setCellValueFactory(new PropertyValueFactory<>("destinationName"));
        wagonsColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(
                cellData.getValue().getAssignedWagons().size()).asObject());
        dateColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDate().toString()));

        // Route table
        routeIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        routeTypeColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getType().toString()));
        freightCountColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(
                cellData.getValue().getFreightIds().size()).asObject());
        statusColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().isAssigned() ? 
                    "Assigned to Train " + cellData.getValue().getAssignedTrainId() : 
                    "Unassigned"));

        freightTable.setItems(freightList);
        routesTable.setItems(routeList);
        selectedFreightsList.setItems(selectedFreightIds);
    }

    private void setupEventHandlers() {
        createSimpleRouteButton.setOnAction(e -> createSimpleRoute());
        createCompositeRouteButton.setOnAction(e -> createCompositeRoute());
        refreshButton.setOnAction(e -> refreshData());
        addFreightButton.setOnAction(e -> addSelectedFreight());
        removeFreightButton.setOnAction(e -> removeSelectedFreight());
        clearSelectionButton.setOnAction(e -> clearSelection());

        freightTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateRouteDetails(null);
            }
        });

        routesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateRouteDetails(newVal);
            }
        });
    }

    private void refreshData() {
        freightList.clear();
        freightList.addAll(freightRepo.findAll().stream()
            .filter(f -> f.getAssignedWagons().size() > 0)
            .collect(Collectors.toList()));

        routeList.clear();
        routeList.addAll(planningService.getAllRoutes());
    }

    private void createSimpleRoute() {
        Freight selected = freightTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a freight to create a simple route.");
            return;
        }

        try {
            PlannedRoute route = planningService.createSimpleRoute(selected.getId());
            showSuccess("Simple route created: " + route.getId());
            refreshData();
            routesTable.getSelectionModel().select(route);
        } catch (Exception e) {
            showError("Error creating route: " + e.getMessage());
        }
    }

    private void createCompositeRoute() {
        if (selectedFreightIds.size() < 2) {
            showError("Please select at least 2 freights for a composite route.");
            return;
        }

        try {
            List<Integer> freightIds = new ArrayList<>(selectedFreightIds);
            PlannedRoute route = planningService.createCompositeRoute(freightIds);
            showSuccess("Composite route created: " + route.getId());
            refreshData();
            clearSelection();
            routesTable.getSelectionModel().select(route);
        } catch (Exception e) {
            showError("Error creating composite route: " + e.getMessage());
        }
    }

    private void addSelectedFreight() {
        Freight selected = freightTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a freight from the table.");
            return;
        }

        if (selectedFreightIds.contains(selected.getId())) {
            showError("Freight already selected.");
            return;
        }

        // Validate chain constraint
        if (!selectedFreightIds.isEmpty()) {
            int lastId = selectedFreightIds.get(selectedFreightIds.size() - 1);
            Freight lastFreight = freightRepo.findById(lastId).orElse(null);
            if (lastFreight != null && lastFreight.getDestinationId() != selected.getOriginId()) {
                showError("Invalid chain: Next freight must start at " + 
                    facilityRepo.findById(lastFreight.getDestinationId())
                        .map(f -> f.getName()).orElse("Unknown"));
                return;
            }
        }

        selectedFreightIds.add(selected.getId());
    }

    private void removeSelectedFreight() {
        Integer selected = selectedFreightsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedFreightIds.remove(selected);
        }
    }

    private void clearSelection() {
        selectedFreightIds.clear();
    }

    private void updateRouteDetails(PlannedRoute route) {
        if (route == null) {
            routeDetailsArea.clear();
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("Route ID: ").append(route.getId()).append("\n");
        details.append("Type: ").append(route.getType()).append("\n");
        details.append("Status: ").append(route.isAssigned() ? 
            "Assigned to Train " + route.getAssignedTrainId() : "Unassigned").append("\n\n");
        
        details.append("Freights: ");
        details.append(route.getFreightIds().stream()
            .map(String::valueOf)
            .collect(Collectors.joining(", ")));
        details.append("\n\n");

        details.append("Actions:\n");
        for (var action : route.getActions()) {
            String facilityName = facilityRepo.findById(action.getFacilityId())
                .map(f -> f.getName()).orElse("Unknown");
            details.append(String.format("  %s at %s: Freight %d (%d wagons)\n",
                action.getType(), facilityName, action.getFreightId(), action.getWagonCount()));
        }

        if (route.hasPath()) {
            details.append("\nPath: ");
            details.append(route.getPath().stream()
                .map(id -> facilityRepo.findById(id).map(f -> f.getName()).orElse(String.valueOf(id)))
                .collect(Collectors.joining(" → ")));
        }

        routeDetailsArea.setText(details.toString());
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

