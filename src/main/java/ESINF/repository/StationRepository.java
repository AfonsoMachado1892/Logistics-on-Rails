package ESINF.repository;

import ESINF.IO.StationCSVReader;
import ESINF.model.Station;
import ESINF.model.StationIndex;

import java.io.IOException;
import java.util.List;

/**
 * Repository for Station data
 * Manages the station index and provides query operations
 */
public class StationRepository {

    private StationIndex index;
    private boolean isLoaded;

    public StationRepository() {
        this.index = null;
        this.isLoaded = false;
    }

    /**
     * Loads stations from CSV file and builds the index
     *
     * @param filePath path to CSV file
     * @return import result with statistics
     * @throws IOException if file cannot be read
     */
    public ImportResult loadStations(String filePath) throws IOException {
        StationCSVReader reader = new StationCSVReader();
        List<Station> stations = reader.readStations(filePath);

        // Build index
        index = new StationIndex();
        for (Station station : stations) {
            index.addStation(station);
        }
        index.finalizeIndex();

        isLoaded = true;

        return new ImportResult(
                reader.getValidCount(),
                reader.getInvalidCount(),
                reader.getErrors(),
                reader.getSummary()
        );
    }

    /**
     * Gets the station index
     * @throws IllegalStateException if data not loaded
     */
    public StationIndex getIndex() {
        if (!isLoaded) {
            throw new IllegalStateException("Station data not loaded. Please load CSV first.");
        }
        return index;
    }

    /**
     * Checks if station data is loaded
     */
    public boolean isLoaded() {
        return isLoaded;
    }

    /**
     * Clears the repository
     */
    public void clear() {
        this.index = null;
        this.isLoaded = false;
    }

    /**
     * Result of import operation
     */
    public record ImportResult(
            int validCount,
            int invalidCount,
            List<String> errors,
            String summary
    ) {
        public boolean hasErrors() {
            return invalidCount > 0;
        }
    }
}
