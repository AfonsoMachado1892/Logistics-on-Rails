package LAPR3.repository;

import LAPR3.model.Wagon;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Repository for managing Wagon data
 * Loads from CSV and provides query methods
 * Part of USLP07 - Subtask #62
 */
public class WagonRepository {
    private final Map<String, Wagon> wagons;

    public WagonRepository() {
        this.wagons = new HashMap<>();
    }

    /**
     * Loads wagons from CSV file
     *
     * @param filePath path to wagons.csv
     * @throws IOException if file cannot be read
     */
    public void loadFromCSV(String filePath) throws IOException {
        wagons.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                try {
                    Wagon wagon = parseWagonFromCSV(line);
                    wagons.put(wagon.getWagonId(), wagon);
                } catch (Exception e) {
                    System.err.println("Error parsing wagon line: " + line);
                    System.err.println("Error: " + e.getMessage());
                }
            }
        }

        System.out.println("Loaded " + wagons.size() + " wagons");
    }

    /**
     * Parses a wagon from CSV line
     */
    private Wagon parseWagonFromCSV(String line) {
        String[] parts = line.split(",");

        if (parts.length != 8) {
            throw new IllegalArgumentException("Invalid CSV format: expected 8 fields");
        }

        String wagonId = parts[0].trim();
        Wagon.WagonType type = Wagon.WagonType.valueOf(parts[1].trim());
        String operator = parts[2].trim();
        int gauge = Integer.parseInt(parts[3].trim());
        double tareWeightTons = Double.parseDouble(parts[4].trim());
        double maxPayloadTons = Double.parseDouble(parts[5].trim());
        double lengthMeters = Double.parseDouble(parts[6].trim());
        double volumeM3 = Double.parseDouble(parts[7].trim());

        return new Wagon(wagonId, type, operator, gauge, tareWeightTons,
                maxPayloadTons, lengthMeters, volumeM3);
    }

    /**
     * Finds a wagon by ID
     */
    public Optional<Wagon> findById(String wagonId) {
        return Optional.ofNullable(wagons.get(wagonId));
    }

    /**
     * Gets all wagons
     */
    public List<Wagon> findAll() {
        return new ArrayList<>(wagons.values());
    }

    /**
     * Finds wagons by operator
     */
    public List<Wagon> findByOperator(String operator) {
        List<Wagon> result = new ArrayList<>();
        for (Wagon wagon : wagons.values()) {
            if (wagon.getOperator().equalsIgnoreCase(operator)) {
                result.add(wagon);
            }
        }
        return result;
    }

    /**
     * Finds wagons by type
     */
    public List<Wagon> findByType(Wagon.WagonType type) {
        List<Wagon> result = new ArrayList<>();
        for (Wagon wagon : wagons.values()) {
            if (wagon.getType() == type) {
                result.add(wagon);
            }
        }
        return result;
    }

    /**
     * Finds wagons compatible with a specific gauge
     */
    public List<Wagon> findByGauge(int gauge) {
        List<Wagon> result = new ArrayList<>();
        for (Wagon wagon : wagons.values()) {
            if (wagon.isCompatibleWithGauge(gauge)) {
                result.add(wagon);
            }
        }
        return result;
    }

    /**
     * Gets the number of wagons in the repository
     */
    public int count() {
        return wagons.size();
    }

    /**
     * Checks if repository contains a wagon with given ID
     */
    public boolean exists(String wagonId) {
        return wagons.containsKey(wagonId);
    }
}