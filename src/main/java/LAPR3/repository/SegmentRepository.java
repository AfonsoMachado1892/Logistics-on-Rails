package LAPR3.repository;

import LAPR3.IO.CSVReader;
import LAPR3.model.Segment;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SegmentRepository {
    private final Map<Integer, Segment> segments;

    public SegmentRepository() {
        this.segments = new HashMap<>();
    }

    /**
     * Load segments from CSV file
     * Expected format: ID, Line, Order, Electrified, Max Weight (kg/m), Length (m), NumberTracks
     * @param filePath path to segments.csv
     * @throws IOException if file cannot be read
     */
    public void loadFromCSV(String filePath) throws IOException {
        List<String[]> records = CSVReader.readCSV(filePath);

        for (String[] record : records) {
            try {
                if (record.length < 7) {
                    System.err.println("Invalid segment record: insufficient columns");
                    continue;
                }

                int id = CSVReader.parseInt(record[0], -1);
                int lineId = CSVReader.parseInt(record[1], -1);
                int order = CSVReader.parseInt(record[2], -1);
                boolean electrified = CSVReader.parseBoolean(record[3]);
                double maxWeight = CSVReader.parseDouble(record[4], -1);
                double length = CSVReader.parseDouble(record[5], -1);
                int numberTracks = CSVReader.parseInt(record[6], -1);

                if (id < 0 || lineId < 0 || order < 0 || maxWeight < 0 ||
                        length < 0 || numberTracks < 0) {
                    System.err.println("Invalid segment data: negative values");
                    continue;
                }

                Segment segment = new Segment(id, lineId, order, electrified,
                        maxWeight, length, numberTracks);
                segments.put(id, segment);

            } catch (IllegalArgumentException e) {
                System.err.println("Error loading segment: " + e.getMessage());
            }
        }

        System.out.println("Loaded " + segments.size() + " segments");
    }

    /**
     * Find segment by ID
     */
    public Optional<Segment> findById(int id) {
        return Optional.ofNullable(segments.get(id));
    }

    /**
     * Find all segments of a line, ordered by segment order
     */
    public List<Segment> findByLineId(int lineId) {
        return segments.values().stream()
                .filter(segment -> segment.getLineId() == lineId)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Get total length of all segments in a line
     */
    public double getTotalLengthByLine(int lineId) {
        return segments.values().stream()
                .filter(segment -> segment.getLineId() == lineId)
                .mapToDouble(Segment::getLengthMeters)
                .sum();
    }

    /**
     * Check if all segments of a line are electrified
     */
    public boolean isLineFullyElectrified(int lineId) {
        return segments.values().stream()
                .filter(segment -> segment.getLineId() == lineId)
                .allMatch(Segment::isElectrified);
    }

    /**
     * Get all segments
     */
    public List<Segment> findAll() {
        return new ArrayList<>(segments.values());
    }

    /**
     * Check if segment exists
     */
    public boolean exists(int id) {
        return segments.containsKey(id);
    }

    /**
     * Get total count
     */
    public int count() {
        return segments.size();
    }
}
