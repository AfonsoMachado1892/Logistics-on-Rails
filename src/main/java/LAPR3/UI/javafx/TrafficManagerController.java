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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for Traffic Manager (USLP10)
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

        facilityComboBox.getItems().addAll(facilityRepo.findAll().stream()
            .map(Facility::getId)
            .collect(Collectors.toList()));
    }

    private void setupEventHandlers() {
        calculatePathButton.setOnAction(e -> calculatePath());
        createScheduleButton.setOnAction(e -> createSchedule());
        generateDispatchPlanButton.setOnAction(e -> generateDispatchPlan());
        viewVisualizationsButton.setOnAction(e -> viewVisualizations());
        addFacilityButton.setOnAction(e -> addFacilityToPath());
        clearPathButton.setOnAction(e -> clearPath());

        routeComboBox.setOnAction(e -> updatePathOptions());
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

        facilityComboBox.getItems().clear();
        List<Integer> mandatory = route.getMandatoryFacilities();
        if (!mandatory.isEmpty()) {
            int currentId = mandatory.get(0);
            manualPath.clear();
            manualPath.add(currentId);
            updateAvailableFacilities(currentId, mandatory);
        }
    }

    private void updateAvailableFacilities(int currentId, List<Integer> mandatory) {
        facilityComboBox.getItems().clear();
        Map<Integer, Line> connections = lineRepo.findDirectlyConnectedFacilities(currentId);
        facilityComboBox.getItems().addAll(connections.keySet());
    }

    private void addFacilityToPath() {
        Integer facilityId = facilityComboBox.getValue();
        if (facilityId == null) return;

        PlannedRoute route = routeComboBox.getValue();
        if (route == null) return;

        int currentId = manualPath.isEmpty() ? 
            route.getMandatoryFacilities().get(0) : 
            manualPath.get(manualPath.size() - 1);

        Map<Integer, Line> connections = lineRepo.findDirectlyConnectedFacilities(currentId);
        if (!connections.containsKey(facilityId)) {
            showError("Facility not directly connected.");
            return;
        }

        manualPath.add(facilityId);
        updateAvailableFacilities(facilityId, route.getMandatoryFacilities());
    }

    private void clearPath() {
        manualPath.clear();
        PlannedRoute route = routeComboBox.getValue();
        if (route != null && !route.getMandatoryFacilities().isEmpty()) {
            manualPath.add(route.getMandatoryFacilities().get(0));
        }
    }

    private void calculatePath() {
        PlannedRoute route = routeComboBox.getValue();
        if (route == null) {
            showError("Please select a route.");
            return;
        }

        List<Integer> path;
        if (manualPathRadio.isSelected()) {
            if (manualPath.size() < 2) {
                showError("Path must have at least 2 facilities.");
                return;
            }
            path = new ArrayList<>(manualPath);
        } else {
            path = calculatePathAutomatically(route);
            if (path == null || path.isEmpty()) {
                showError("Automatic path calculation failed.");
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
        List<Integer> completePath = new ArrayList<>();

        for (int i = 0; i < mandatoryStops.size() - 1; i++) {
            int from = mandatoryStops.get(i);
            int to = mandatoryStops.get(i + 1);

            AutomaticPathPlanningService.PathCalculationResult result = 
                automaticPathService.calculateOptimalPath(from, to);

            if (!result.isSuccess()) {
                return null;
            }

            List<Integer> segment = result.getPath();
            if (i > 0) {
                segment.remove(0);
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

            Optional<Schedule> existing = scheduleRepo.findAll().stream()
                .filter(s -> s.getTrainId().equals(trainId))
                .findFirst();

            if (existing.isPresent()) {
                showSuccess("Schedule already exists: " + existing.get().getScheduleId());
            } else {
                String scheduleId = "SCH-" + trainId;
                LocalDateTime departure = LocalDateTime.now();
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

