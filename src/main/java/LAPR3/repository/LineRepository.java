package LAPR3.repository;

import LAPR3.IO.CSVReader;
import LAPR3.model.Line;

import java.io.IOException;
import java.util.*;

/**
 * Repository for managing line entities.
 * Provides access to lines and their connections between facilities.
 */
public class LineRepository {
    private final Map<Integer, Line> lines;

    /**
     * Constructs a new line repository
     */
    public LineRepository() {
        this.lines = new HashMap<>();
    }

    /**
     * Load lines from CSV file
     * Expected format: ID, Name, Owner, Start ID, Start, End ID, End, Gauge
     * @param filePath path to lines.csv
     * @throws IOException if file cannot be read
     */
    public void loadFromCSV(String filePath) throws IOException {
        List<String[]> records = CSVReader.readCSV(filePath);

        for (String[] record : records) {
            try {
                if (record.length < 8) {
                    System.err.println("Invalid line record: insufficient columns");
                    continue;
                }

                int id = CSVReader.parseInt(record[0], -1);
                String name = record[1];
                String owner = record[2];
                int startId = CSVReader.parseInt(record[3], -1);
                String startName = record[4];
                int endId = CSVReader.parseInt(record[5], -1);
                String endName = record[6];
                int gauge = CSVReader.parseInt(record[7], -1);

                if (id < 0 || startId < 0 || endId < 0 || gauge < 0) {
                    System.err.println("Invalid line data: negative values");
                    continue;
                }

                Line line = new Line(id, name, owner, startId, startName,
                        endId, endName, gauge);
                lines.put(id, line);

            } catch (IllegalArgumentException e) {
                System.err.println("Error loading line: " + e.getMessage());
            }
        }

        System.out.println("Loaded " + lines.size() + " lines");
    }

    /**
     * Find line by ID
     */
    public Optional<Line> findById(int id) {
        return Optional.ofNullable(lines.get(id));
    }

    /**
     * Find line that directly connects two facilities
     */
    public Optional<Line> findLineConnecting(int facilityId1, int facilityId2) {
        return lines.values().stream()
                .filter(line -> line.connectsFacilities(facilityId1, facilityId2))
                .findFirst();
    }

    /**
     * Find all lines starting from a facility
     */
    public List<Line> findLinesFromFacility(int facilityId) {
        return lines.values().stream()
                .filter(line -> line.getStartFacilityId() == facilityId)
                .toList();
    }

    /**
     * Find all lines ending at a facility
     */
    public List<Line> findLinesToFacility(int facilityId) {
        return lines.values().stream()
                .filter(line -> line.getEndFacilityId() == facilityId)
                .toList();
    }

    /**
     * Get all lines
     */
    public List<Line> findAll() {
        return new ArrayList<>(lines.values());
    }

    /**
     * Check if line exists
     */
    public boolean exists(int id) {
        return lines.containsKey(id);
    }

    /**
     * Get total count
     */
    public int count() {
        return lines.size();
    }
}
