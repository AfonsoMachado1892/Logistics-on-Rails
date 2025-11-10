package ESINF.controller;

import ESINF.model.Station;
import ESINF.model.StationIndex;
import ESINF.repository.StationRepository;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Controller for Station Queries (USEI06)
 * Handles business logic and coordinates between UI and repository
 */
public class StationQueryController {

    private final StationRepository repository;

    public StationQueryController() {
        this.repository = new StationRepository();
    }

    /**
     * Loads station data from CSV file
     *
     * @param filePath path to CSV file
     * @return import result
     * @throws IOException if file cannot be read
     */
    public StationRepository.ImportResult loadStations(String filePath) throws IOException {
        return repository.loadStations(filePath);
    }

    /**
     * Queries stations by time zone group, ordered by country and name
     *
     * @param timeZoneGroup the time zone group to search
     * @return list of stations
     */
    public List<Station> queryByTimeZoneGroup(String timeZoneGroup) {
        ensureDataLoaded();

        long startTime = System.nanoTime();
        List<Station> results = repository.getIndex().getStationsByTimeZoneGroup(timeZoneGroup);
        long endTime = System.nanoTime();

        double elapsedMs = (endTime - startTime) / 1_000_000.0;
        System.out.printf("⏱️  Query executed in %.3f ms%n", elapsedMs);

        return results;
    }

    /**
     * Queries stations by time zone group and country
     *
     * @param timeZoneGroup the time zone group
     * @param country the country code
     * @return list of stations
     */
    public List<Station> queryByTimeZoneGroupAndCountry(String timeZoneGroup, String country) {
        ensureDataLoaded();

        long startTime = System.nanoTime();
        List<Station> results = repository.getIndex()
                .getStationsByTimeZoneGroupAndCountry(timeZoneGroup, country);
        long endTime = System.nanoTime();

        double elapsedMs = (endTime - startTime) / 1_000_000.0;
        System.out.printf("⏱️  Query executed in %.3f ms%n", elapsedMs);

        return results;
    }

    /**
     * Queries stations in a window of time zone groups
     *
     * @param timeZoneGroups array of time zone groups
     * @return list of stations
     */
    public List<Station> queryByTimeZoneWindow(String... timeZoneGroups) {
        ensureDataLoaded();

        long startTime = System.nanoTime();
        List<Station> results = repository.getIndex().getStationsByTimeZoneWindow(timeZoneGroups);
        long endTime = System.nanoTime();

        double elapsedMs = (endTime - startTime) / 1_000_000.0;
        System.out.printf("⏱️  Query executed in %.3f ms%n", elapsedMs);

        return results;
    }

    /**
     * Queries stations within latitude range
     *
     * @param minLat minimum latitude
     * @param maxLat maximum latitude
     * @return list of stations
     */
    public List<Station> queryByLatitudeRange(double minLat, double maxLat) {
        ensureDataLoaded();
        validateLatitude(minLat);
        validateLatitude(maxLat);

        if (minLat > maxLat) {
            throw new IllegalArgumentException("Minimum latitude cannot be greater than maximum");
        }

        long startTime = System.nanoTime();
        List<Station> results = repository.getIndex().getStationsByLatitudeRange(minLat, maxLat);
        long endTime = System.nanoTime();

        double elapsedMs = (endTime - startTime) / 1_000_000.0;
        System.out.printf("⏱️  Query executed in %.3f ms%n", elapsedMs);

        return results;
    }

    /**
     * Queries stations within longitude range
     *
     * @param minLon minimum longitude
     * @param maxLon maximum longitude
     * @return list of stations
     */
    public List<Station> queryByLongitudeRange(double minLon, double maxLon) {
        ensureDataLoaded();
        validateLongitude(minLon);
        validateLongitude(maxLon);

        if (minLon > maxLon) {
            throw new IllegalArgumentException("Minimum longitude cannot be greater than maximum");
        }

        long startTime = System.nanoTime();
        List<Station> results = repository.getIndex().getStationsByLongitudeRange(minLon, maxLon);
        long endTime = System.nanoTime();

        double elapsedMs = (endTime - startTime) / 1_000_000.0;
        System.out.printf("⏱️  Query executed in %.3f ms%n", elapsedMs);

        return results;
    }

    /**
     * Gets all available time zone groups
     */
    public Set<String> getAllTimeZoneGroups() {
        ensureDataLoaded();
        return repository.getIndex().getAllTimeZoneGroups();
    }

    /**
     * Gets all countries in a time zone group
     */
    public Set<String> getCountriesInTimeZoneGroup(String timeZoneGroup) {
        ensureDataLoaded();
        return repository.getIndex().getCountriesInTimeZoneGroup(timeZoneGroup);
    }

    /**
     * Gets index statistics
     */
    public StationIndex.IndexStatistics getStatistics() {
        ensureDataLoaded();
        return repository.getIndex().getStatistics();
    }

    /**
     * Checks if data is loaded
     */
    public boolean isDataLoaded() {
        return repository.isLoaded();
    }

    /**
     * Clears the repository
     */
    public void clearData() {
        repository.clear();
    }

    /**
     * Validates latitude value
     */
    private void validateLatitude(double lat) {
        if (lat < -90.0 || lat > 90.0) {
            throw new IllegalArgumentException(
                    String.format("Invalid latitude: %.5f (must be in [-90, 90])", lat)
            );
        }
    }

    /**
     * Validates longitude value
     */
    private void validateLongitude(double lon) {
        if (lon < -180.0 || lon > 180.0) {
            throw new IllegalArgumentException(
                    String.format("Invalid longitude: %.5f (must be in [-180, 180])", lon)
            );
        }
    }

    /**
     * Ensures data is loaded before querying
     */
    private void ensureDataLoaded() {
        if (!repository.isLoaded()) {
            throw new IllegalStateException(
                    "Station data not loaded. Please import stations first."
            );
        }
    }

    /**
     * Analyzes temporal complexity (for demonstration)
     *
     * @return complexity analysis string
     */
    public String getComplexityAnalysis() {
        return """
                
                ╔══════════════════════════════════════════════════════════════════╗
                ║           TEMPORAL COMPLEXITY ANALYSIS (USEI06)                  ║
                ╚══════════════════════════════════════════════════════════════════╝
                
                1. INDEX CONSTRUCTION:
                   • Adding N stations to TreeMap: O(N log N)
                   • Sorting lists at finalization: O(M log M) where M = max stations per coordinate
                   • Overall: O(N log N)
                
                2. QUERY BY TIME ZONE GROUP:
                   • TreeMap lookup: O(log K) where K = number of time zone groups
                   • Collecting results: O(R) where R = number of results
                   • Overall: O(log K + R)
                
                3. QUERY BY TIME ZONE WINDOW:
                   • For W time zone groups: O(W × (log K + R))
                   • Overall: O(W log K + R_total)
                
                4. RANGE QUERIES (Latitude/Longitude):
                   • TreeMap subMap: O(log N + R) where R = results in range
                   • Overall: O(log N + R)
                
                5. MEMORY COMPLEXITY:
                   • Three TreeMap indices: O(N)
                   • Additional lists for duplicates: O(N)
                   • Overall: O(N)
                
                ══════════════════════════════════════════════════════════════════
                """;
    }
}
