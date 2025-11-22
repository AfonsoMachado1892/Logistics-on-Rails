package LAPR3.repository;

import LAPR3.IO.CSVReader;
import LAPR3.model.Facility;

import java.io.IOException;
import java.util.*;

/**
 * Repository for managing facility entities.
 * Provides access to facilities loaded from CSV data.
 */
public class FacilityRepository {
    private final Map<Integer, Facility> facilities;

    /**
     * Constructs a new facility repository
     */
    public FacilityRepository() {
        this.facilities = new HashMap<>();
    }

    /**
     * Load facilities from CSV file
     * Expected format: ID, Name
     * @param filePath path to facilities.csv
     * @throws IOException if file cannot be read
     */
    public void loadFromCSV(String filePath) throws IOException {
        List<String[]> records = CSVReader.readCSV(filePath);

        for (String[] record : records) {
            try {
                if (record.length < 2) {
                    System.err.println("Invalid facility record: insufficient columns");
                    continue;
                }

                int id = CSVReader.parseInt(record[0], -1);
                String name = record[1];

                if (id < 0) {
                    System.err.println("Invalid facility ID: " + record[0]);
                    continue;
                }

                Facility facility = new Facility(id, name);
                facilities.put(id, facility);

            } catch (IllegalArgumentException e) {
                System.err.println("Error loading facility: " + e.getMessage());
            }
        }

        System.out.println("Loaded " + facilities.size() + " facilities");
    }

    /**
     * Find facility by ID
     */
    public Optional<Facility> findById(int id) {
        return Optional.ofNullable(facilities.get(id));
    }

    /**
     * Find facility by name (case insensitive)
     */
    public Optional<Facility> findByName(String name) {
        return facilities.values().stream()
                .filter(f -> f.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Get all facilities
     */
    public List<Facility> findAll() {
        return new ArrayList<>(facilities.values());
    }

    /**
     * Check if facility exists
     */
    public boolean exists(int id) {
        return facilities.containsKey(id);
    }

    /**
     * Get total count
     */
    public int count() {
        return facilities.size();
    }

}
