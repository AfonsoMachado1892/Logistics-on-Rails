package ESINF.repository;

import ESINF.IO.StationCSVReader;
import ESINF.model.Station;
import ESINF.model.StationIndex;

import java.io.IOException;
import java.util.List;

/**
 * Repository for managing Station data and the associated spatial index.
 * <p>
 * This class acts as an abstraction layer between the data source (CSV files)
 * and the application logic. It handles the loading of stations, the construction
 * of the {@link StationIndex}, and ensures data integrity before access.
 * </p>
 *
 */
public class StationRepository {

    /**
     * The central index containing the BSTs and 2D-Tree for station queries.
     */
    private StationIndex index;

    /**
     * Flag indicating if the repository has successfully loaded and indexed data.
     */
    private boolean isLoaded;

    /**
     * Constructs a new, empty StationRepository.
     * The repository is initially in an unloaded state.
     */
    public StationRepository() {
        this.index = null;
        this.isLoaded = false;
    }

    /**
     * Loads stations from a specified CSV file, builds the station index, and finalizes it.
     * <p>
     * This method delegates reading to {@link StationCSVReader}, adds all valid stations
     * to the {@link StationIndex}, and triggers the balancing/building of the
     * internal tree structures (e.g., 2D-Tree).
     * </p>
     *
     * @param filePath the relative or absolute path to the CSV file containing station data.
     * @return an {@link ImportResult} record containing statistics about the load operation
     * (valid records, skipped records, errors, and a summary string).
     * @throws IOException if the file cannot be found or a read error occurs.
     */
    public ImportResult loadStations(String filePath) throws IOException {
        StationCSVReader reader = new StationCSVReader();
        List<Station> stations = reader.readStations(filePath);

        // Build index
        index = new StationIndex();
        for (Station station : stations) {
            index.addStation(station);
        }
        // Ensure the trees (BSTs/KD-Tree) are built and balanced
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
     * Retrieves the loaded Station Index.
     *
     * @return the {@link StationIndex} containing the loaded data.
     * @throws IllegalStateException if the station data has not been loaded yet
     * (i.e., {@link #loadStations(String)} has not been called successfully).
     */
    public StationIndex getIndex() {
        if (!isLoaded) {
            throw new IllegalStateException("Station data not loaded. Please load CSV first.");
        }
        return index;
    }

    /**
     * Checks if the repository currently holds loaded data.
     *
     * @return {@code true} if data has been successfully loaded; {@code false} otherwise.
     */
    public boolean isLoaded() {
        return isLoaded;
    }

    /**
     * Clears the repository, removing the current index and resetting the loaded state.
     * This allows for reloading data from a fresh file.
     */
    public void clear() {
        this.index = null;
        this.isLoaded = false;
    }

    /**
     * A record representing the outcome of a bulk import operation.
     *
     * @param validCount   The number of stations successfully parsed and added.
     * @param invalidCount The number of lines skipped due to validation errors.
     * @param errors       A list of specific error messages for the skipped lines.
     * @param summary      A human-readable summary string of the operation.
     */
    public record ImportResult(
            int validCount,
            int invalidCount,
            List<String> errors,
            String summary
    ) {
        /**
         * Checks if there were any errors during the import process.
         *
         * @return {@code true} if any records were invalid; {@code false} otherwise.
         */
        public boolean hasErrors() {
            return invalidCount > 0;
        }
    }
}