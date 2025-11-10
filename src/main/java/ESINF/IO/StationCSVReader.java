package ESINF.IO;

import ESINF.DTO.StationDTO;
import ESINF.model.Station;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV Reader for Railway Stations data
 * Implements validation and error reporting as per USEI06
 */
public class StationCSVReader {

    private final List<String> errors;
    private int validCount;
    private int invalidCount;
    private int lineNumber;

    public StationCSVReader() {
        this.errors = new ArrayList<>();
        this.validCount = 0;
        this.invalidCount = 0;
        this.lineNumber = 0;
    }

    /**
     * Reads and validates stations from CSV file
     * Format: country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport
     *
     * @param filePath path to CSV file
     * @return list of valid stations
     * @throws IOException if file cannot be read
     */
    public List<Station> readStations(String filePath) throws IOException {
        List<Station> stations = new ArrayList<>();
        errors.clear();
        validCount = 0;
        invalidCount = 0;
        lineNumber = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // Skip header
            lineNumber++;

            if (line == null) {
                throw new IOException("CSV file is empty");
            }

            while ((line = br.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty()) {
                    continue; // Skip empty lines
                }

                try {
                    StationDTO dto = parseLine(line);

                    if (dto.isValid()) {
                        Station station = toStation(dto);
                        stations.add(station);
                        validCount++;
                    } else {
                        String error = String.format("Line %d: %s - Data: %s",
                                lineNumber, dto.getValidationError(), line);
                        errors.add(error);
                        invalidCount++;
                    }

                } catch (Exception e) {
                    String error = String.format("Line %d: Parse error - %s - Data: %s",
                            lineNumber, e.getMessage(), line);
                    errors.add(error);
                    invalidCount++;
                }
            }
        }

        return stations;
    }

    /**
     * Parses a CSV line into StationDTO
     */
    private StationDTO parseLine(String line) {
        // Split by comma, but respect quoted fields
        String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

        if (parts.length < 9) {
            throw new IllegalArgumentException("Invalid number of fields: expected 9, got " + parts.length);
        }

        String country = cleanField(parts[0]);
        String timeZone = cleanField(parts[1]);
        String timeZoneGroup = cleanField(parts[2]);
        String station = cleanField(parts[3]);
        double latitude = parseDouble(parts[4], "latitude");
        double longitude = parseDouble(parts[5], "longitude");
        boolean isCity = parseBoolean(parts[6]);
        boolean isMainStation = parseBoolean(parts[7]);
        boolean isAirport = parseBoolean(parts[8]);

        return new StationDTO(country, timeZone, timeZoneGroup, station,
                latitude, longitude, isCity, isMainStation, isAirport);
    }

    /**
     * Cleans a CSV field by removing quotes and trimming
     */
    private String cleanField(String field) {
        if (field == null) return "";
        field = field.trim();
        if (field.startsWith("\"") && field.endsWith("\"")) {
            field = field.substring(1, field.length() - 1);
        }
        return field;
    }

    /**
     * Parses a double value
     */
    private double parseDouble(String value, String fieldName) {
        try {
            return Double.parseDouble(cleanField(value));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + ": " + value);
        }
    }

    /**
     * Parses a boolean value (True/False case-insensitive)
     */
    private boolean parseBoolean(String value) {
        String cleaned = cleanField(value).toLowerCase();
        return "true".equals(cleaned);
    }

    /**
     * Converts StationDTO to Station model
     */
    private Station toStation(StationDTO dto) {
        return new Station(
                dto.station(),
                dto.latitude(),
                dto.longitude(),
                dto.country(),
                dto.timeZone(),
                dto.timeZoneGroup(),
                dto.isCity(),
                dto.isMainStation(),
                dto.isAirport()
        );
    }

    /**
     * Gets the list of validation errors
     */
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }

    /**
     * Gets count of valid records
     */
    public int getValidCount() {
        return validCount;
    }

    /**
     * Gets count of invalid records
     */
    public int getInvalidCount() {
        return invalidCount;
    }

    /**
     * Checks if there were any errors
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Gets a summary of the import
     */
    public String getSummary() {
        return String.format("Import Summary: %d valid, %d invalid, %d total lines processed",
                validCount, invalidCount, lineNumber);
    }
}
