package LAPR3.UI.javafx;

import LAPR3.DTO.DispatchPlan;
import LAPR3.model.*;
import LAPR3.repository.*;
import LAPR3.service.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.scene.control.ListCell;

/**
 * Controller for Traffic Manager
 */
public class TrafficManagerController implements Initializable {

    @FXML
    private TableView<PlannedRoute> plannedRoutesTable;
    @FXML
    private TableColumn<PlannedRoute, String> routeIdColumn;
    @FXML
    private TableColumn<PlannedRoute, String> routeTypeColumn;
    @FXML
    private TableColumn<PlannedRoute, String> assignedTrainColumn;

    @FXML
    private ComboBox<PlannedRoute> routeComboBox;
    @FXML
    private RadioButton manualPathRadio;
    @FXML
    private RadioButton automaticPathRadio;
    @FXML
    private Button calculatePathButton;
    @FXML
    private Button createScheduleButton;
    @FXML
    private TextArea pathDetailsArea;

    @FXML
    private TableView<Schedule> schedulesTable;
    @FXML
    private TableColumn<Schedule, String> scheduleIdColumn;
    @FXML
    private TableColumn<Schedule, String> trainIdColumn;
    @FXML
    private TableColumn<Schedule, String> scheduleRouteIdColumn;
    @FXML
    private TableColumn<Schedule, String> departureColumn;
    @FXML
    private TableColumn<Schedule, String> statusColumn;

    @FXML
    private TextField dispatchDateField;
    @FXML
    private Button generateDispatchPlanButton;
    @FXML
    private Button viewVisualizationsButton;
    @FXML
    private TextArea dispatchPlanArea;

    @FXML
    private ListView<Integer> manualPathList;
    @FXML
    private ComboBox<Integer> facilityComboBox;
    @FXML
    private Button addFacilityButton;
    @FXML
    private Button clearPathButton;
    @FXML
    private TabPane internalTabPane;

    private RoutePlanningService routePlanningService;
    private FacilityRepository facilityRepo;
    private LineRepository lineRepo;
    private SegmentRepository segmentRepo;
    private TrainRepository trainRepo;
    private ScheduleRepository scheduleRepo;
    private AutomaticPathPlanningService automaticPathService;
    private TrainDispatcherService dispatcherService;
    private ScheduleVisualizationService visualService;

    private ObservableList<PlannedRoute> plannedRoutes = FXCollections.observableArrayList();
    private ObservableList<Schedule> schedules = FXCollections.observableArrayList();
    private ObservableList<Integer> manualPath = FXCollections.observableArrayList();
    private Set<Integer> remainingMandatoryStops = new HashSet<>();

    private DispatchPlan currentDispatchPlan;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeRepositories();
        setupUI();
        setupEventHandlers();
        refreshData();
        
        // Refresh data when route combo box is opened
        routeComboBox.setOnShowing(e -> refreshData());
        
        // Refresh planned routes when the Planned Routes tab is selected
        if (internalTabPane != null) {
            internalTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                if (newTab != null && newTab.getText().equals("Planned Routes")) {
                    refreshData();
                }
            });
        }
    }

    private void initializeRepositories() {
        ServiceManager serviceManager = ServiceManager.getInstance();
        facilityRepo = serviceManager.getFacilityRepo();
        lineRepo = serviceManager.getLineRepo();
        segmentRepo = serviceManager.getSegmentRepo();
        trainRepo = serviceManager.getTrainRepo();
        scheduleRepo = serviceManager.getScheduleRepo();
        routePlanningService = serviceManager.getRoutePlanningService();
        automaticPathService = serviceManager.getAutomaticPathService();
        dispatcherService = serviceManager.getDispatcherService();
        visualService = serviceManager.getVisualService();
    }

    private void setupUI() {
        ToggleGroup pathGroup = new ToggleGroup();
        manualPathRadio.setToggleGroup(pathGroup);
        automaticPathRadio.setToggleGroup(pathGroup);
        automaticPathRadio.setSelected(true);

        plannedRoutesTable.setItems(plannedRoutes);
        schedulesTable.setItems(schedules);
        manualPathList.setItems(manualPath);
        
        // Configure manual path list to show facility names
        manualPathList.setCellFactory(listView -> new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer facilityId, boolean empty) {
                super.updateItem(facilityId, empty);
                if (empty || facilityId == null) {
                    setText(null);
                } else {
                    String name = facilityRepo.findById(facilityId)
                        .map(Facility::getName)
                        .orElse("Unknown");
                    setText(name + " (ID: " + facilityId + ")");
                }
            }
        });

        routeIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        routeTypeColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType().toString()));
        assignedTrainColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().isAssigned() ? 
                    cellData.getValue().getAssignedTrainId() : "Unassigned"));

        scheduleIdColumn.setCellValueFactory(new PropertyValueFactory<>("scheduleId"));
        trainIdColumn.setCellValueFactory(new PropertyValueFactory<>("trainId"));
        scheduleRouteIdColumn.setCellValueFactory(new PropertyValueFactory<>("routeId"));
        departureColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDepartureDateTime().toString()));
        statusColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStatus().toString()));

        // Configure facility combo box to show names with mandatory indicator
        // We'll update this dynamically when the path changes
        updateFacilityComboBoxDisplay();
        
        // Listen to manual path changes to update combo box display
        manualPath.addListener((javafx.collections.ListChangeListener.Change<? extends Integer> c) -> {
            updateFacilityComboBoxDisplay();
        });
    }

    private void setupEventHandlers() {
        calculatePathButton.setOnAction(e -> calculatePath());
        createScheduleButton.setOnAction(e -> createSchedule());
        generateDispatchPlanButton.setOnAction(e -> generateDispatchPlan());
        viewVisualizationsButton.setOnAction(e -> viewVisualizations());
        addFacilityButton.setOnAction(e -> addFacilityToPath());
        clearPathButton.setOnAction(e -> clearPath());

        routeComboBox.setOnAction(e -> updatePathOptions());
        
        // When manual path radio is selected, initialize path if route is selected
        manualPathRadio.setOnAction(e -> {
            if (manualPathRadio.isSelected() && routeComboBox.getValue() != null) {
                updatePathOptions();
            }
        });
    }

    public void refreshData() {
        // Refresh planned routes
        plannedRoutes.clear();
        List<PlannedRoute> allRoutes = routePlanningService.getAllRoutes();
        plannedRoutes.addAll(allRoutes);

        // Refresh route combo box with assigned routes
        routeComboBox.getItems().clear();
        List<PlannedRoute> assignedRoutes = routePlanningService.getAssignedRoutes();
        routeComboBox.getItems().addAll(assignedRoutes);

        // Refresh schedules
        schedules.clear();
        schedules.addAll(scheduleRepo.findAll());
    }

    private void updatePathOptions() {
        PlannedRoute route = routeComboBox.getValue();
        if (route == null) return;

        List<Integer> mandatory = route.getMandatoryFacilities();
        if (mandatory.isEmpty()) {
            showError("Route has no mandatory facilities.");
            return;
        }

        // Initialize path with first mandatory facility
        int startId = mandatory.get(0);
        manualPath.clear();
        manualPath.add(startId);
        
        // Initialize remaining mandatory stops (all except the first)
        remainingMandatoryStops.clear();
        remainingMandatoryStops.addAll(mandatory);
        remainingMandatoryStops.remove(startId);
        
        // Update available facilities and path details
        updateAvailableFacilities(startId, route);
        updatePathDetailsDisplay(route);
    }

    private void updateAvailableFacilities(int currentId, PlannedRoute route) {
        facilityComboBox.getItems().clear();
        Map<Integer, Line> connections = lineRepo.findDirectlyConnectedFacilities(currentId);
        
        if (connections.isEmpty()) {
            showError("No connections available from current facility.");
            facilityComboBox.setPromptText("No connections available");
            return;
        }
        
        // Add connected facilities to combo box (sorted for better UX)
        List<Integer> connectedIds = new ArrayList<>(connections.keySet());
        connectedIds.sort((a, b) -> {
            // Sort mandatory stops first, then by name
            boolean aMandatory = remainingMandatoryStops.contains(a);
            boolean bMandatory = remainingMandatoryStops.contains(b);
            if (aMandatory != bMandatory) {
                return aMandatory ? -1 : 1;
            }
            String nameA = facilityRepo.findById(a).map(Facility::getName).orElse("");
            String nameB = facilityRepo.findById(b).map(Facility::getName).orElse("");
            return nameA.compareToIgnoreCase(nameB);
        });
        
        facilityComboBox.getItems().addAll(connectedIds);
        facilityComboBox.setPromptText("Select next facility...");
        
        // Update path details to show current status
        updatePathDetailsDisplay(route);
    }

    private void addFacilityToPath() {
        Integer facilityId = facilityComboBox.getValue();
        if (facilityId == null) {
            showError("Please select a facility from the list.");
            return;
        }

        PlannedRoute route = routeComboBox.getValue();
        if (route == null) {
            showError("Please select a route first.");
            return;
        }

        if (manualPath.isEmpty()) {
            showError("Path is empty. Please select a route first.");
            return;
        }

        int currentId = manualPath.get(manualPath.size() - 1);

        // Verify facility is directly connected
        Map<Integer, Line> connections = lineRepo.findDirectlyConnectedFacilities(currentId);
        if (!connections.containsKey(facilityId)) {
            String currentName = facilityRepo.findById(currentId)
                .map(Facility::getName)
                .orElse(String.valueOf(currentId));
            String selectedName = facilityRepo.findById(facilityId)
                .map(Facility::getName)
                .orElse(String.valueOf(facilityId));
            showError("Facility " + selectedName + " is not directly connected to " + currentName + ".\nPlease choose a directly connected facility.");
            return;
        }

        // Add facility to path
        manualPath.add(facilityId);
        
        // Check if this is a mandatory stop
        if (remainingMandatoryStops.contains(facilityId)) {
            String facilityName = facilityRepo.findById(facilityId)
                .map(Facility::getName)
                .orElse(String.valueOf(facilityId));
            remainingMandatoryStops.remove(facilityId);
            showSuccess("Mandatory stop reached: " + facilityName);
        }
        
        // Clear selection in combo box
        facilityComboBox.getSelectionModel().clearSelection();
        
        // Update available facilities for next step
        updateAvailableFacilities(facilityId, route);
    }

    private void updateFacilityComboBoxDisplay() {
        facilityComboBox.setCellFactory(listView -> new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer facilityId, boolean empty) {
                super.updateItem(facilityId, empty);
                if (empty || facilityId == null) {
                    setText(null);
                } else {
                    String name = facilityRepo.findById(facilityId)
                        .map(Facility::getName)
                        .orElse("Unknown");
                    boolean isMandatory = remainingMandatoryStops.contains(facilityId);
                    setText(String.format("[%d] %s%s", facilityId, name, isMandatory ? " ⚠ MANDATORY" : ""));
                }
            }
        });
        
        facilityComboBox.setButtonCell(new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer facilityId, boolean empty) {
                super.updateItem(facilityId, empty);
                if (empty || facilityId == null) {
                    setText("Select next facility...");
                } else {
                    String name = facilityRepo.findById(facilityId)
                        .map(Facility::getName)
                        .orElse("Unknown");
                    boolean isMandatory = remainingMandatoryStops.contains(facilityId);
                    setText(String.format("[%d] %s%s", facilityId, name, isMandatory ? " ⚠ MANDATORY" : ""));
                }
            }
        });
    }
    
    private void clearPath() {
        manualPath.clear();
        remainingMandatoryStops.clear();
        
        PlannedRoute route = routeComboBox.getValue();
        if (route != null) {
            List<Integer> mandatory = route.getMandatoryFacilities();
            if (!mandatory.isEmpty()) {
                int startId = mandatory.get(0);
                manualPath.add(startId);
                remainingMandatoryStops.addAll(mandatory);
                remainingMandatoryStops.remove(startId);
                updateAvailableFacilities(startId, route);
            }
        }
        
        facilityComboBox.getItems().clear();
        pathDetailsArea.clear();
        updateFacilityComboBoxDisplay();
    }
    
    private void updatePathDetailsDisplay(PlannedRoute route) {
        if (route == null) return;
        
        StringBuilder details = new StringBuilder();
        details.append("Route: ").append(route.getId()).append("\n");
        details.append("Type: ").append(route.getType()).append("\n\n");
        
        // Show mandatory facilities
        List<Integer> mandatory = route.getMandatoryFacilities();
        details.append("Mandatory Stops (must visit in order):\n");
        for (Integer facilityId : mandatory) {
            String name = facilityRepo.findById(facilityId)
                .map(Facility::getName)
                .orElse("Unknown");
            boolean visited = manualPath.contains(facilityId);
            details.append(String.format("  %s %s (ID: %d)\n", 
                visited ? "✓" : "○", name, facilityId));
        }
        
        details.append("\n");
        
        // Show current position
        if (!manualPath.isEmpty()) {
            int currentId = manualPath.get(manualPath.size() - 1);
            String currentName = facilityRepo.findById(currentId)
                .map(Facility::getName)
                .orElse("Unknown");
            details.append("Current Position: ").append(currentName).append(" (ID: ").append(currentId).append(")\n");
        }
        
        // Show remaining mandatory stops
        if (!remainingMandatoryStops.isEmpty()) {
            details.append("\nRemaining Mandatory Stops:\n");
            for (Integer facilityId : remainingMandatoryStops) {
                String name = facilityRepo.findById(facilityId)
                    .map(Facility::getName)
                    .orElse(String.valueOf(facilityId));
                details.append("  - ").append(name).append(" (ID: ").append(facilityId).append(")\n");
            }
        } else if (!manualPath.isEmpty()) {
            details.append("\n✓ All mandatory stops visited!\n");
        }
        
        // Show current path
        if (!manualPath.isEmpty()) {
            details.append("\nCurrent Path:\n");
            details.append(manualPath.stream()
                .map(id -> {
                    String name = facilityRepo.findById(id)
                        .map(Facility::getName)
                        .orElse(String.valueOf(id));
                    return name + " (" + id + ")";
                })
                .collect(Collectors.joining(" → ")));
        }
        
        // Show available connections (same as shown in ComboBox)
        if (!manualPath.isEmpty()) {
            int currentId = manualPath.get(manualPath.size() - 1);
            Map<Integer, Line> connections = lineRepo.findDirectlyConnectedFacilities(currentId);
            if (!connections.isEmpty()) {
                details.append("\n\nDirectly Connected Facilities (select from dropdown):\n");
                List<Integer> sortedIds = new ArrayList<>(connections.keySet());
                sortedIds.sort((a, b) -> {
                    boolean aMandatory = remainingMandatoryStops.contains(a);
                    boolean bMandatory = remainingMandatoryStops.contains(b);
                    if (aMandatory != bMandatory) {
                        return aMandatory ? -1 : 1;
                    }
                    String nameA = facilityRepo.findById(a).map(Facility::getName).orElse("");
                    String nameB = facilityRepo.findById(b).map(Facility::getName).orElse("");
                    return nameA.compareToIgnoreCase(nameB);
                });
                
                for (Integer neighborId : sortedIds) {
                    String name = facilityRepo.findById(neighborId)
                        .map(Facility::getName)
                        .orElse("Unknown");
                    boolean isMandatory = remainingMandatoryStops.contains(neighborId);
                    details.append(String.format("  [%d] %s%s\n", 
                        neighborId, name, isMandatory ? " ⚠ MANDATORY" : ""));
                }
            } else {
                details.append("\n\n⚠ No connections available from current position.\n");
            }
        }
        
        pathDetailsArea.setText(details.toString());
    }

    private void calculatePath() {
        PlannedRoute route = routeComboBox.getValue();
        if (route == null) {
            showError("Please select a route.");
            return;
        }

        // Debug: Check route has mandatory facilities
        List<Integer> mandatory = route.getMandatoryFacilities();
        System.out.println("[DEBUG] Route " + route.getId() + " has " + mandatory.size() + " mandatory facilities: " + mandatory);

        List<Integer> path;
        if (manualPathRadio.isSelected()) {
            if (manualPath.size() < 2) {
                showError("Path must have at least 2 facilities.");
                return;
            }
            
            // Verify all mandatory stops are visited
            if (!remainingMandatoryStops.isEmpty()) {
                String missing = remainingMandatoryStops.stream()
                    .map(id -> facilityRepo.findById(id)
                        .map(Facility::getName)
                        .orElse(String.valueOf(id)))
                    .collect(Collectors.joining(", "));
                showError("Not all mandatory stops have been visited.\n" +
                    "Missing: " + missing);
                return;
            }
            
            path = new ArrayList<>(manualPath);
        } else {
            path = calculatePathAutomatically(route);
            if (path == null || path.isEmpty()) {
                // Get facility names for better error message
                if (mandatory.size() >= 2) {
                    String fromName = facilityRepo.findById(mandatory.get(0))
                        .map(Facility::getName)
                        .orElse(String.valueOf(mandatory.get(0)));
                    String toName = facilityRepo.findById(mandatory.get(mandatory.size() - 1))
                        .map(Facility::getName)
                        .orElse(String.valueOf(mandatory.get(mandatory.size() - 1)));
                    showError("Automatic path calculation failed.\n" +
                        "Could not find a path from " + fromName + " (" + mandatory.get(0) + ") to " + 
                        toName + " (" + mandatory.get(mandatory.size() - 1) + ").\n" +
                        "Please check if the facilities are connected in the network.\n\n" +
                        "Mandatory facilities: " + mandatory);
                } else {
                    showError("Automatic path calculation failed.\n" +
                        "Route does not have enough facilities to calculate a path.\n" +
                        "Route has " + mandatory.size() + " mandatory facilities.");
                }
                return;
            }
        }

        route.setPath(path);
        Train train = trainRepo.findById(route.getAssignedTrainId()).orElse(null);
        if (train != null) {
            train.setPath(path);
        }

        updatePathDetails(route, path);
        showSuccess("Path calculated successfully.");
    }

    private List<Integer> calculatePathAutomatically(PlannedRoute route) {
        List<Integer> mandatoryStops = route.getMandatoryFacilities();
        
        if (mandatoryStops.size() < 2) {
            System.err.println("Route has less than 2 mandatory facilities");
            return null;
        }
        
        List<Integer> completePath = new ArrayList<>();

        for (int i = 0; i < mandatoryStops.size() - 1; i++) {
            int from = mandatoryStops.get(i);
            int to = mandatoryStops.get(i + 1);

            // Verify facilities exist
            if (facilityRepo.findById(from).isEmpty()) {
                System.err.println("Facility not found: " + from);
                return null;
            }
            if (facilityRepo.findById(to).isEmpty()) {
                System.err.println("Facility not found: " + to);
                return null;
            }

            AutomaticPathPlanningService.PathCalculationResult result = 
                automaticPathService.calculateOptimalPath(from, to);

            if (!result.isSuccess()) {
                String fromName = facilityRepo.findById(from)
                    .map(Facility::getName)
                    .orElse(String.valueOf(from));
                String toName = facilityRepo.findById(to)
                    .map(Facility::getName)
                    .orElse(String.valueOf(to));
                
                if (result.hasNegativeCycle()) {
                    System.err.println("Negative cycle detected between " + fromName + " and " + toName);
                } else {
                    System.err.println("No path found from " + fromName + " (" + from + ") to " + toName + " (" + to + ")");
                }
                return null;
            }

            List<Integer> segment = result.getPath();
            if (segment == null || segment.isEmpty()) {
                System.err.println("Path segment is empty from " + from + " to " + to);
                return null;
            }
            
            if (i > 0 && !segment.isEmpty()) {
                segment.remove(0); // Remove duplicate facility
            }
            completePath.addAll(segment);
        }

        return completePath;
    }

    private void updatePathDetails(PlannedRoute route, List<Integer> path) {
        StringBuilder details = new StringBuilder();
        details.append("Route: ").append(route.getId()).append("\n");
        details.append("Path: ");
        details.append(path.stream()
            .map(id -> facilityRepo.findById(id).map(Facility::getName).orElse(String.valueOf(id)))
            .collect(Collectors.joining(" → ")));
        pathDetailsArea.setText(details.toString());
    }

    private void createSchedule() {
        PlannedRoute route = routeComboBox.getValue();
        if (route == null || !route.hasPath()) {
            showError("Please calculate a path first.");
            return;
        }

        try {
            String trainId = route.getAssignedTrainId();
            Train train = trainRepo.findById(trainId).orElse(null);
            if (train == null) {
                showError("Assigned train not found.");
                return;
            }

            // Validate train has departure date and time
            if (train.getDepartureDate() == null || train.getDepartureTime() == null) {
                showError("Train does not have a valid departure date and time.");
                return;
            }

            Optional<Schedule> existing = scheduleRepo.findAll().stream()
                .filter(s -> s.getTrainId().equals(trainId) && s.getRouteId().equals(route.getId()))
                .findFirst();

            if (existing.isPresent()) {
                showSuccess("Schedule already exists: " + existing.get().getScheduleId());
            } else {
                String scheduleId = "SCH-" + trainId;
                // Use the train's departure date and time
                LocalDateTime departure = LocalDateTime.of(
                    train.getDepartureDate(),
                    train.getDepartureTime()
                );
                Schedule schedule = new Schedule(scheduleId, trainId, route.getId(), 
                    departure, Schedule.ScheduleStatus.SCHEDULED);
                scheduleRepo.save(schedule);
                showSuccess("Schedule created: " + scheduleId);
            }

            refreshData();
        } catch (Exception e) {
            showError("Error creating schedule: " + e.getMessage());
        }
    }

    private void generateDispatchPlan() {
        String dateStr = dispatchDateField.getText().trim();
        if (dateStr.isEmpty()) {
            showError("Please enter a date (yyyy-MM-dd).");
            return;
        }

        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
            
            // Check if there are schedules for this date
            List<Schedule> schedulesForDate = scheduleRepo.findAll().stream()
                .filter(s -> s.getDepartureDateTime().toLocalDate().equals(date))
                .filter(s -> s.getStatus() == Schedule.ScheduleStatus.SCHEDULED)
                .collect(Collectors.toList());
            
            if (schedulesForDate.isEmpty()) {
                showError("No scheduled trains found for date " + dateStr + ".\nPlease create schedules first.");
                return;
            }
            
            currentDispatchPlan = dispatcherService.createDailyDispatchPlan(date.atStartOfDay());
            
            if (currentDispatchPlan == null || currentDispatchPlan.getResolvedSchedules().isEmpty()) {
                showError("Dispatch plan generated but contains no schedules.");
                return;
            }

            String summary = dispatcherService.getDispatchSummary(currentDispatchPlan);
            dispatchPlanArea.setText(summary);
            showSuccess("Dispatch plan generated successfully with " + 
                currentDispatchPlan.getResolvedSchedules().size() + " schedules.");
        } catch (Exception e) {
            showError("Error generating dispatch plan: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void viewVisualizations() {
        if (currentDispatchPlan == null) {
            showError("Please generate a dispatch plan first.");
            return;
        }

        if (currentDispatchPlan.getResolvedSchedules().isEmpty()) {
            showError("Dispatch plan is empty. No schedules to visualize.");
            return;
        }

        // Create a dialog to select visualization type
        ChoiceDialog<String> dialog = new ChoiceDialog<>(
            "Timeline View (ASCII)",
            "Timeline View (ASCII)",
            "Train Diagram (Bildfahrplan)",
            "Crossing Operations List",
            "Conflict Heatmap",
            "Delay Statistics",
            "All Visualizations"
        );
        dialog.setTitle("Dispatch Visualizations");
        dialog.setHeaderText("Select visualization type:");
        dialog.setContentText("Choose a visualization:");

        String result = dialog.showAndWait().orElse(null);
        if (result == null) return;

        StringBuilder content = new StringBuilder();
        content.append("═══════════════════════════════════════════════════════\n");
        content.append("  DISPATCH PLAN VISUALIZATIONS\n");
        content.append("═══════════════════════════════════════════════════════\n\n");

        try {
            switch (result) {
                case "Timeline View (ASCII)":
                    content.append("1. TIMELINE VIEW (ASCII)\n");
                    content.append("─".repeat(60)).append("\n");
                    String timeline = visualService.visualizeTimeline(currentDispatchPlan);
                    content.append(timeline != null ? timeline : "No timeline data available.");
                    break;
                case "Train Diagram (Bildfahrplan)":
                    content.append("2. TRAIN DIAGRAM (Bildfahrplan)\n");
                    content.append("─".repeat(60)).append("\n");
                    String diagram = visualService.visualizeTrainDiagram(currentDispatchPlan);
                    content.append(diagram != null ? diagram : "No train diagram data available.");
                    break;
                case "Crossing Operations List":
                    content.append("3. CROSSING OPERATIONS LIST\n");
                    content.append("─".repeat(60)).append("\n");
                    String crossings = visualService.visualizeCrossingOperations(currentDispatchPlan);
                    content.append(crossings != null ? crossings : "No crossing operations data available.");
                    break;
                case "Conflict Heatmap":
                    content.append("4. CONFLICT HEATMAP\n");
                    content.append("─".repeat(60)).append("\n");
                    String heatmap = visualService.visualizeConflictHeatmap(currentDispatchPlan);
                    content.append(heatmap != null ? heatmap : "No conflict heatmap data available.");
                    break;
                case "Delay Statistics":
                    content.append("5. DELAY STATISTICS\n");
                    content.append("─".repeat(60)).append("\n");
                    String stats = visualService.visualizeDelayStatistics(currentDispatchPlan);
                    content.append(stats != null ? stats : "No delay statistics available.");
                    break;
                case "All Visualizations":
                    content.append("ALL VISUALIZATIONS\n");
                    content.append("═".repeat(60)).append("\n\n");
                    
                    content.append("1. TIMELINE VIEW (ASCII)\n");
                    content.append("─".repeat(60)).append("\n");
                    String t1 = visualService.visualizeTimeline(currentDispatchPlan);
                    content.append(t1 != null ? t1 : "No timeline data available.");
                    content.append("\n\n");
                    
                    content.append("2. TRAIN DIAGRAM (Bildfahrplan)\n");
                    content.append("─".repeat(60)).append("\n");
                    String t2 = visualService.visualizeTrainDiagram(currentDispatchPlan);
                    content.append(t2 != null ? t2 : "No train diagram data available.");
                    content.append("\n\n");
                    
                    content.append("3. CROSSING OPERATIONS LIST\n");
                    content.append("─".repeat(60)).append("\n");
                    String t3 = visualService.visualizeCrossingOperations(currentDispatchPlan);
                    content.append(t3 != null ? t3 : "No crossing operations data available.");
                    content.append("\n\n");
                    
                    content.append("4. CONFLICT HEATMAP\n");
                    content.append("─".repeat(60)).append("\n");
                    String t4 = visualService.visualizeConflictHeatmap(currentDispatchPlan);
                    content.append(t4 != null ? t4 : "No conflict heatmap data available.");
                    content.append("\n\n");
                    
                    content.append("5. DELAY STATISTICS\n");
                    content.append("─".repeat(60)).append("\n");
                    String t5 = visualService.visualizeDelayStatistics(currentDispatchPlan);
                    content.append(t5 != null ? t5 : "No delay statistics available.");
                    break;
            }
        } catch (Exception e) {
            content.append("Error generating visualization: ").append(e.getMessage());
            e.printStackTrace();
        }

        // Show in a scrollable dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Dispatch Visualizations");
        alert.setHeaderText(result);
        
        TextArea textArea = new TextArea(content.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(25);
        textArea.setPrefColumnCount(80);
        
        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setPrefSize(900, 700);
        alert.setResizable(true);
        alert.showAndWait();
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

