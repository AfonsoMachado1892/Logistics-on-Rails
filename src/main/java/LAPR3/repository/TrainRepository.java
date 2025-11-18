package LAPR3.repository;

import LAPR3.model.Locomotive;
import LAPR3.model.Train;
import LAPR3.model.Wagon;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Repository for managing Train compositions
 * Loads from CSV and provides query methods
 * Part of USLP07 - Subtask #62
 */
public class TrainRepository {
    private final Map<String, Train> trains;
    private final LocomotiveRepository locomotiveRepository;
    private final WagonRepository wagonRepository;

    public TrainRepository(LocomotiveRepository locomotiveRepository,
                           WagonRepository wagonRepository) {
        this.trains = new HashMap<>();
        this.locomotiveRepository = locomotiveRepository;
        this.wagonRepository = wagonRepository;
    }

    /**
     * Loads trains from CSV file
     *
     * @param filePath path to trains.csv
     * @throws IOException if file cannot be read
     */
    public void loadFromCSV(String filePath) throws IOException {
        trains.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                try {
                    Train train = parseTrainFromCSV(line);
                    trains.put(train.getTrainId(), train);
                } catch (Exception e) {
                    System.err.println("Error parsing train line: " + line);
                    System.err.println("Error: " + e.getMessage());
                }
            }
        }

        System.out.println("Loaded " + trains.size() + " trains");
    }

    /**
     * Parses a train from CSV line
     * Format: trainId,operator,locomotives,wagons,totalPayloadTons
     * locomotives: "5601" or "5601,5602"
     * wagons: "W001,W002,W003"
     */
    private Train parseTrainFromCSV(String line) {
        // Use regex to handle quoted fields properly
        String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

        if (parts.length != 5) {
            throw new IllegalArgumentException("Invalid CSV format: expected 5 fields");
        }

        String trainId = parts[0].trim();
        String operator = parts[1].trim();
        String locomotivesStr = parts[2].trim().replace("\"", "");
        String wagonsStr = parts[3].trim().replace("\"", "");
        double totalPayloadTons = Double.parseDouble(parts[4].trim());

        // Parse locomotive numbers
        List<Locomotive> locomotives = new ArrayList<>();
        if (!locomotivesStr.isEmpty()) {
            String[] locoNumbers = locomotivesStr.split(",");
            for (String locoNumStr : locoNumbers) {
                int locoNum = Integer.parseInt(locoNumStr.trim());
                Locomotive loco = locomotiveRepository.findByNumber(locoNum)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Locomotive not found: " + locoNum));
                locomotives.add(loco);
            }
        }

        // Parse wagon IDs
        List<Wagon> wagons = new ArrayList<>();
        if (!wagonsStr.isEmpty()) {
            String[] wagonIds = wagonsStr.split(",");
            for (String wagonId : wagonIds) {
                wagonId = wagonId.trim();
                String finalWagonId = wagonId;
                Wagon wagon = wagonRepository.findById(wagonId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Wagon not found: " + finalWagonId));
                wagons.add(wagon);
            }
        }

        return new Train(trainId, operator, locomotives, wagons, totalPayloadTons);
    }

    /**
     * Finds a train by ID
     */
    public Optional<Train> findById(String trainId) {
        return Optional.ofNullable(trains.get(trainId));
    }

    /**
     * Gets all trains
     */
    public List<Train> findAll() {
        return new ArrayList<>(trains.values());
    }

    /**
     * Finds trains by operator
     */
    public List<Train> findByOperator(String operator) {
        List<Train> result = new ArrayList<>();
        for (Train train : trains.values()) {
            if (train.getOperator().equalsIgnoreCase(operator)) {
                result.add(train);
            }
        }
        return result;
    }

    /**
     * Finds trains compatible with a specific gauge
     */
    public List<Train> findByGauge(int gauge) {
        List<Train> result = new ArrayList<>();
        for (Train train : trains.values()) {
            if (train.isCompatibleWithGauge(gauge)) {
                result.add(train);
            }
        }
        return result;
    }

    /**
     * Finds trains that require electrification
     */
    public List<Train> findElectricTrains() {
        List<Train> result = new ArrayList<>();
        for (Train train : trains.values()) {
            if (train.requiresElectrification()) {
                result.add(train);
            }
        }
        return result;
    }

    /**
     * Gets the number of trains in the repository
     */
    public int count() {
        return trains.size();
    }

    /**
     * Checks if repository contains a train with given ID
     */
    public boolean exists(String trainId) {
        return trains.containsKey(trainId);
    }

    /**
     * Validates all trains in the repository
     * @return list of validation errors
     */
    public List<String> validateAll() {
        List<String> errors = new ArrayList<>();
        for (Train train : trains.values()) {
            String error = train.validate();
            if (!error.isEmpty()) {
                errors.add(train.getTrainId() + ": " + error);
            }
        }
        return errors;
    }
}