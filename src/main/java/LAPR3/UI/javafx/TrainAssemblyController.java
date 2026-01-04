package LAPR3.UI.javafx;

import LAPR3.model.*;
import LAPR3.repository.*;
import LAPR3.service.RoutePlanningService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for Train Assembly
 */
public class TrainAssemblyController implements Initializable {

    @FXML
    private ComboBox<PlannedRoute> routeComboBox;
    @FXML
    private ComboBox<String> operatorComboBox;
    @FXML
    private ListView<Locomotive> locomotivesList;
    @FXML
    private ListView<Locomotive> selectedLocomotivesList;
    @FXML
    private ListView<Wagon> wagonsList;
    @FXML
    private ListView<Wagon> selectedWagonsList;
    @FXML
    private TextField departureDateField;
    @FXML
    private TextField departureTimeField;
    @FXML
    private Button addLocomotiveButton;
    @FXML
    private Button removeLocomotiveButton;
    @FXML
    private Button addWagonButton;
    @FXML
    private Button removeWagonButton;
    @FXML
    private Button assembleTrainButton;
    @FXML
    private Button refreshButton;
    @FXML
    private TextArea trainDetailsArea;
    @FXML
    private TableView<Train> assembledTrainsTable;
    @FXML
    private TableColumn<Train, Integer> trainIdColumn;
    @FXML
    private TableColumn<Train, String> operatorColumn;
    @FXML
    private TableColumn<Train, Integer> locosColumn;
    @FXML
    private TableColumn<Train, Integer> wagonsColumn;
    @FXML
    private TableColumn<Train, String> powerColumn;
    @FXML
    private Label validationLabel;

    private RoutePlanningService routePlanningService;
    private LocomotiveRepository locomotiveRepo;
    private WagonRepository wagonRepo;
    private FacilityRepository facilityRepo;
    private TrainRepository trainRepo;
    private ScheduleRepository scheduleRepo;
    private LineRepository lineRepo;

    private ObservableList<Locomotive> availableLocos = FXCollections.observableArrayList();
    private ObservableList<Locomotive> selectedLocos = FXCollections.observableArrayList();
    private ObservableList<Wagon> availableWagons = FXCollections.observableArrayList();
    private ObservableList<Wagon> selectedWagons = FXCollections.observableArrayList();
    private ObservableList<Train> assembledTrains = FXCollections.observableArrayList();

    private static final double MAX_TRAIN_LENGTH_M = 750.0;
    private static final double MIN_POWER_TO_WEIGHT_RATIO = 3.0;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeRepositories();
        setupUI();
        setupEventHandlers();
        refreshData();
    }

    private void initializeRepositories() {
        ServiceManager serviceManager = ServiceManager.getInstance();
        facilityRepo = serviceManager.getFacilityRepo();
        locomotiveRepo = serviceManager.getLocomotiveRepo();
        wagonRepo = serviceManager.getWagonRepo();
        trainRepo = serviceManager.getTrainRepo();
        scheduleRepo = serviceManager.getScheduleRepo();
        lineRepo = serviceManager.getLineRepo();
        routePlanningService = serviceManager.getRoutePlanningService();
    }

    private void setupUI() {
        operatorComboBox.getItems().addAll("CP", "MEDWAY", "TAKARGO");
        operatorComboBox.setValue("CP");

        locomotivesList.setItems(availableLocos);
        selectedLocomotivesList.setItems(selectedLocos);
        wagonsList.setItems(availableWagons);
        selectedWagonsList.setItems(selectedWagons);
        assembledTrainsTable.setItems(assembledTrains);

        trainIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        operatorColumn.setCellValueFactory(new PropertyValueFactory<>("operatorVat"));
        locosColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(
                cellData.getValue().getLocomotives().size()).asObject());
        wagonsColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(
                cellData.getValue().getWagons().size()).asObject());
        powerColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                String.format("%.0f kW", cellData.getValue().getTotalPowerKw())));

        departureDateField.setPromptText("yyyy-MM-dd");
        departureTimeField.setPromptText("HH:mm");
    }

    private void setupEventHandlers() {
        addLocomotiveButton.setOnAction(e -> addLocomotive());
        removeLocomotiveButton.setOnAction(e -> removeLocomotive());
        addWagonButton.setOnAction(e -> addWagon());
        removeWagonButton.setOnAction(e -> removeWagon());
        assembleTrainButton.setOnAction(e -> assembleTrain());
        refreshButton.setOnAction(e -> refreshData());

        routeComboBox.setOnAction(e -> updateWagonsForRoute());
        // Refresh routes when combo box is opened
        routeComboBox.setOnShowing(e -> refreshData());
        selectedLocos.addListener((javafx.collections.ListChangeListener.Change<? extends Locomotive> c) -> validateTrain());
        selectedWagons.addListener((javafx.collections.ListChangeListener.Change<? extends Wagon> c) -> validateTrain());
    }

    public void refreshData() {
        availableLocos.clear();
        availableLocos.addAll(locomotiveRepo.findAll());

        availableWagons.clear();
        availableWagons.addAll(wagonRepo.findAll());

        // Always refresh routes from the shared service
        routeComboBox.getItems().clear();
        routeComboBox.getItems().addAll(routePlanningService.getUnassignedRoutes());

        assembledTrains.clear();
        assembledTrains.addAll(trainRepo.findAll());
    }

    private void updateWagonsForRoute() {
        PlannedRoute route = routeComboBox.getValue();
        if (route == null) return;

        availableWagons.clear();
        List<Integer> freightIds = route.getFreightIds();
        List<Wagon> routeWagons = new ArrayList<>();

        FreightRepository freightRepo = ServiceManager.getInstance().getFreightRepo();
        for (int fid : freightIds) {
            Freight f = freightRepo.findById(fid).orElse(null);
            if (f != null) {
                routeWagons.addAll(f.getAssignedWagons());
            }
        }

        if (!routeWagons.isEmpty()) {
            availableWagons.addAll(routeWagons);
        } else {
            int departureFacilityId = route.getMandatoryFacilities().get(0);
            availableWagons.addAll(wagonRepo.findAll().stream()
                .filter(w -> w.getCurrentFacilityId() != null && 
                    w.getCurrentFacilityId() == departureFacilityId)
                .collect(Collectors.toList()));
        }
    }

    private void addLocomotive() {
        Locomotive selected = locomotivesList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Check if already added by comparing loco number
            boolean alreadyAdded = selectedLocos.stream()
                .anyMatch(l -> l.getLocoNumber().equals(selected.getLocoNumber()));
            if (!alreadyAdded) {
                selectedLocos.add(selected);
                validateTrain();
            }
        }
    }

    private void removeLocomotive() {
        Locomotive selected = selectedLocomotivesList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedLocos.remove(selected);
            validateTrain();
        }
    }

    private void addWagon() {
        Wagon selected = wagonsList.getSelectionModel().getSelectedItem();
        if (selected != null && !selectedWagons.contains(selected)) {
            selectedWagons.add(selected);
            validateTrain();
        }
    }

    private void removeWagon() {
        Wagon selected = selectedWagonsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedWagons.remove(selected);
            validateTrain();
        }
    }

    private void validateTrain() {
        if (selectedLocos.isEmpty() || selectedWagons.isEmpty()) {
            validationLabel.setText("Please select at least one locomotive and one wagon.");
            validationLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        PlannedRoute route = routeComboBox.getValue();
        if (route == null) {
            validationLabel.setText("Please select a route.");
            validationLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        // Calculate metrics
        double totalWeight = selectedLocos.stream().mapToDouble(Locomotive::getWeightTons).sum() +
            selectedWagons.stream().mapToDouble(Wagon::getTotalWeightTons).sum();
        double totalLength = selectedLocos.stream().mapToDouble(Locomotive::getLengthM).sum() +
            selectedWagons.stream().mapToDouble(w -> w.getLengthMm() / 1000.0).sum();
        double totalPower = selectedLocos.stream().mapToDouble(Locomotive::getPowerKw).sum();
        double powerToWeight = totalPower / totalWeight;

        // Validate gauge
        try {
            validateGaugeCompatibility(route);
        } catch (IllegalArgumentException e) {
            validationLabel.setText("Gauge Error: " + e.getMessage());
            validationLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        // Validate constraints
        List<String> errors = new ArrayList<>();
        if (totalLength > MAX_TRAIN_LENGTH_M) {
            errors.add(String.format("Length %.1fm exceeds max %.0fm", totalLength, MAX_TRAIN_LENGTH_M));
        }
        if (powerToWeight < MIN_POWER_TO_WEIGHT_RATIO) {
            errors.add(String.format("Power/Weight %.2f kW/t too low (min %.1f)", 
                powerToWeight, MIN_POWER_TO_WEIGHT_RATIO));
        }

        if (errors.isEmpty()) {
            validationLabel.setText("✓ Train configuration is valid.");
            validationLabel.setStyle("-fx-text-fill: #27ae60;");
        } else {
            validationLabel.setText("Errors: " + String.join(", ", errors));
            validationLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    private void validateGaugeCompatibility(PlannedRoute route) {
        List<Integer> facilities = route.getMandatoryFacilities();
        if (facilities.size() < 2) {
            // If route has less than 2 facilities, skip gauge validation
            return;
        }

        int startId = facilities.get(0);
        int nextId = facilities.get(1);

        Optional<Line> lineOpt = lineRepo.findLineConnecting(startId, nextId);
        if (lineOpt.isEmpty()) {
            // If no direct line found, try to find any line in the route
            // or skip validation (path will be calculated later)
            System.out.println("Warning: No direct line found between facilities " + startId + " and " + nextId + ". Gauge validation skipped.");
            return;
        }

        Line line = lineOpt.get();
        double routeGauge = line.getGauge();

        for (Locomotive l : selectedLocos) {
            if (Math.abs(l.getGauge() - routeGauge) > 0.01) {
                throw new IllegalArgumentException(String.format(
                    "Locomotive %s gauge (%.0f) does not match route (%.0f)",
                    l.getLocoNumber(), l.getGauge(), routeGauge));
            }
        }

        for (Wagon w : selectedWagons) {
            if (Math.abs(w.getGauge() - routeGauge) > 0.01) {
                throw new IllegalArgumentException(String.format(
                    "Wagon %s gauge (%.0f) does not match route (%.0f)",
                    w.getWagonNumber(), w.getGauge(), routeGauge));
            }
        }
    }

    private void assembleTrain() {
        PlannedRoute route = routeComboBox.getValue();
        if (route == null) {
            showError("Please select a route.");
            return;
        }

        if (selectedLocos.isEmpty() || selectedWagons.isEmpty()) {
            showError("Please select at least one locomotive and one wagon.");
            return;
        }

        String operator = operatorComboBox.getValue();
        if (operator == null) {
            showError("Please select an operator.");
            return;
        }

        LocalDateTime departureDateTime;
        try {
            String dateStr = departureDateField.getText().trim();
            String timeStr = departureTimeField.getText().trim();
            if (dateStr.isEmpty() || timeStr.isEmpty()) {
                showError("Please enter departure date and time.");
                return;
            }
            departureDateTime = LocalDateTime.parse(dateStr + " " + timeStr, DATE_TIME_FORMATTER);
        } catch (Exception e) {
            showError("Invalid date/time format. Use yyyy-MM-dd and HH:mm");
            return;
        }

        try {
            // Validate gauge
            validateGaugeCompatibility(route);

            // Create train
            Train train = new Train();
            int trainId = 1000 + trainRepo.count() + 1;
            train.setId(trainId);
            train.setOperatorVat(operator);
            train.setStartFacilityId(route.getMandatoryFacilities().get(0));
            train.setEndFacilityId(route.getMandatoryFacilities().get(route.getMandatoryFacilities().size() - 1));
            train.setDepartureDate(departureDateTime.toLocalDate());
            train.setDepartureTime(departureDateTime.toLocalTime());
            train.setLocomotives(new ArrayList<>(selectedLocos));
            train.setWagons(new ArrayList<>(selectedWagons));

            trainRepo.save(train);

            // Create schedule
            String scheduleId = "SCH-" + trainId;
            Schedule schedule = new Schedule(
                scheduleId,
                String.valueOf(trainId),
                route.getId(),
                departureDateTime,
                Schedule.ScheduleStatus.SCHEDULED
            );
            scheduleRepo.save(schedule);

            // Mark route as assigned
            route.setAssignedTrainId(String.valueOf(trainId));

            showSuccess("Train " + trainId + " assembled and assigned to route " + route.getId());
            refreshData();
            clearSelection();
        } catch (Exception e) {
            showError("Error assembling train: " + e.getMessage());
        }
    }

    private void clearSelection() {
        selectedLocos.clear();
        selectedWagons.clear();
        validationLabel.setText("");
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

