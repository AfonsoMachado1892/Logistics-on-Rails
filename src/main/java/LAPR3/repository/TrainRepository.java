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
            int lineNumber = 1;

            while ((line = br.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) continue;

                try {
                    Train train = parseTrainFromCSV(line);
                    trains.put(train.getTrainId(), train);
                } catch (Exception e) {
                    System.err.println("Error parsing train at line " + lineNumber + ": " + line);
                    System.err.println("Error: " + e.getMessage());
                    // Continue loading other trains
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
        // Manual parsing to handle quoted fields correctly
        List<String> fields = parseCSVLine(line);

        if (fields.size() != 5) {
            throw new IllegalArgumentException("Invalid CSV format: expected 5 fields, got " + fields.size());
        }

        String trainId = fields.get(0).trim();
        String operator = fields.get(1).trim();
        String locomotivesStr = fields.get(2).trim();
        String wagonsStr = fields.get(3).trim();
        double totalPayloadTons;

        try {
            totalPayloadTons = Double.parseDouble(fields.get(4).trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid payload value: " + fields.get(4));
        }

        // Parse locomotive numbers
        List<Locomotive> locomotives = new ArrayList<>();
        if (!locomotivesStr.isEmpty()) {
            String[] locoNumbers = locomotivesStr.split(",");
            for (String locoNumStr : locoNumbers) {
                locoNumStr = locoNumStr.trim();
                if (locoNumStr.isEmpty()) continue;

                try {
                    int locoNum = Integer.parseInt(locoNumStr);
                    Optional<Locomotive> locoOpt = locomotiveRepository.findByNumber(locoNum);

                    if (locoOpt.isPresent()) {
                        locomotives.add(locoOpt.get());
                    } else {
                        System.err.println("Warning: Locomotive not found: " + locoNum + " (skipping)");
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Warning: Invalid locomotive number: " + locoNumStr + " (skipping)");
                }
            }
        }

        if (locomotives.isEmpty()) {
            throw new IllegalArgumentException("Train must have at least one locomotive");
        }

        // Parse wagon IDs
        List<Wagon> wagons = new ArrayList<>();
        if (!wagonsStr.isEmpty()) {
            String[] wagonIds = wagonsStr.split(",");
            for (String wagonId : wagonIds) {
                wagonId = wagonId.trim();
                if (wagonId.isEmpty()) continue;

                Optional<Wagon> wagonOpt = wagonRepository.findById(wagonId);

                if (wagonOpt.isPresent()) {
                    wagons.add(wagonOpt.get());
                } else {
                    System.err.println("Warning: Wagon not found: " + wagonId + " (skipping)");
                }
            }
        }

        return new Train(trainId, operator, locomotives, wagons, totalPayloadTons);
    }

    /**
     * Parses a CSV line handling quoted fields
     * This properly handles commas inside quoted strings
     */
    private List<String> parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }

        result.add(current.toString());
        return result;
    }

    /**
     * Saves a train to the repository
     */
    public void save(Train train) {
        if (train == null) {
            throw new IllegalArgumentException("Train cannot be null");
        }
        trains.put(train.getTrainId(), train);
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

    /**
     * Clears all trains from repository
     */
    public void clear() {
        trains.clear();
    }
}