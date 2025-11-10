package ESINF.model;

import java.util.*;

/**
 * Station Index using BST/AVL trees for efficient queries
 * Implements USEI06 - Time-Zone Index and Windowed Queries
 *
 * Maintains three indices:
 * - Latitude index (sorted by latitude, then by name)
 * - Longitude index (sorted by longitude, then by name)
 * - TimeZoneGroup + Country composite index
 */
public class StationIndex {

    // Index by latitude: TreeMap<Latitude, List<Station>> sorted by name
    private final TreeMap<Double, List<Station>> latitudeIndex;

    // Index by longitude: TreeMap<Longitude, List<Station>> sorted by name
    private final TreeMap<Double, List<Station>> longitudeIndex;

    // Index by TimeZoneGroup + Country: TreeMap<TimeZoneGroup, TreeMap<Country, List<Station>>>
    private final TreeMap<String, TreeMap<String, List<Station>>> timeZoneIndex;

    // Total number of stations
    private int totalStations;

    public StationIndex() {
        this.latitudeIndex = new TreeMap<>();
        this.longitudeIndex = new TreeMap<>();
        this.timeZoneIndex = new TreeMap<>();
        this.totalStations = 0;
    }

    /**
     * Adds a station to all indices
     * Stations with same coordinates are kept sorted by name (ascending)
     *
     * @param station the station to add
     */
    public void addStation(Station station) {
        // Add to latitude index
        latitudeIndex.computeIfAbsent(station.getLatitude(), k -> new ArrayList<>())
                .add(station);

        // Add to longitude index
        longitudeIndex.computeIfAbsent(station.getLongitude(), k -> new ArrayList<>())
                .add(station);

        // Add to timeZone index (TimeZoneGroup -> Country -> Stations)
        timeZoneIndex.computeIfAbsent(station.getTimeZoneGroup(), k -> new TreeMap<>())
                .computeIfAbsent(station.getCountry(), k -> new ArrayList<>())
                .add(station);

        totalStations++;
    }

    /**
     * Finalizes the index by sorting all station lists by name
     * Must be called after all stations are added
     */
    public void finalizeIndex() {
        // Sort latitude index stations by name
        for (List<Station> stations : latitudeIndex.values()) {
            Collections.sort(stations);
        }

        // Sort longitude index stations by name
        for (List<Station> stations : longitudeIndex.values()) {
            Collections.sort(stations);
        }

        // Sort timeZone index stations by name
        for (TreeMap<String, List<Station>> countryMap : timeZoneIndex.values()) {
            for (List<Station> stations : countryMap.values()) {
                Collections.sort(stations);
            }
        }
    }

    /**
     * Gets all stations in a time zone group, ordered by country (ascending), then by name
     *
     * @param timeZoneGroup the time zone group to search
     * @return list of stations, empty if not found
     */
    public List<Station> getStationsByTimeZoneGroup(String timeZoneGroup) {
        TreeMap<String, List<Station>> countryMap = timeZoneIndex.get(timeZoneGroup);
        if (countryMap == null) {
            return Collections.emptyList();
        }

        List<Station> result = new ArrayList<>();
        // TreeMap is already sorted by country (natural ordering)
        for (List<Station> stations : countryMap.values()) {
            result.addAll(stations); // Already sorted by name
        }

        return result;
    }

    /**
     * Gets all stations in a time zone group for a specific country
     *
     * @param timeZoneGroup the time zone group
     * @param country the country code
     * @return list of stations, empty if not found
     */
    public List<Station> getStationsByTimeZoneGroupAndCountry(String timeZoneGroup, String country) {
        TreeMap<String, List<Station>> countryMap = timeZoneIndex.get(timeZoneGroup);
        if (countryMap == null) {
            return Collections.emptyList();
        }

        List<Station> stations = countryMap.get(country);
        return stations != null ? new ArrayList<>(stations) : Collections.emptyList();
    }

    /**
     * Gets all stations in a window of time zone groups
     *
     * @param timeZoneGroups array of time zone groups to include
     * @return list of stations ordered by timeZoneGroup, country, name
     */
    public List<Station> getStationsByTimeZoneWindow(String... timeZoneGroups) {
        List<Station> result = new ArrayList<>();

        // Sort time zone groups to ensure deterministic order
        Arrays.sort(timeZoneGroups);

        for (String tzGroup : timeZoneGroups) {
            result.addAll(getStationsByTimeZoneGroup(tzGroup));
        }

        return result;
    }

    /**
     * Gets all stations within a latitude range
     *
     * @param minLat minimum latitude (inclusive)
     * @param maxLat maximum latitude (inclusive)
     * @return list of stations
     */
    public List<Station> getStationsByLatitudeRange(double minLat, double maxLat) {
        List<Station> result = new ArrayList<>();

        for (Map.Entry<Double, List<Station>> entry :
                latitudeIndex.subMap(minLat, true, maxLat, true).entrySet()) {
            result.addAll(entry.getValue());
        }

        return result;
    }

    /**
     * Gets all stations within a longitude range
     *
     * @param minLon minimum longitude (inclusive)
     * @param maxLon maximum longitude (inclusive)
     * @return list of stations
     */
    public List<Station> getStationsByLongitudeRange(double minLon, double maxLon) {
        List<Station> result = new ArrayList<>();

        for (Map.Entry<Double, List<Station>> entry :
                longitudeIndex.subMap(minLon, true, maxLon, true).entrySet()) {
            result.addAll(entry.getValue());
        }

        return result;
    }

    /**
     * Gets all unique time zone groups
     */
    public Set<String> getAllTimeZoneGroups() {
        return timeZoneIndex.keySet();
    }

    /**
     * Gets all countries for a given time zone group
     */
    public Set<String> getCountriesInTimeZoneGroup(String timeZoneGroup) {
        TreeMap<String, List<Station>> countryMap = timeZoneIndex.get(timeZoneGroup);
        return countryMap != null ? countryMap.keySet() : Collections.emptySet();
    }

    /**
     * Gets statistics about the index
     */
    public IndexStatistics getStatistics() {
        return new IndexStatistics(
                totalStations,
                latitudeIndex.size(),
                longitudeIndex.size(),
                timeZoneIndex.size()
        );
    }

    public int getTotalStations() {
        return totalStations;
    }

    public TreeMap<Double, List<Station>> getLatitudeIndex() {
        return latitudeIndex;
    }

    public TreeMap<Double, List<Station>> getLongitudeIndex() {
        return longitudeIndex;
    }

    /**
     * Statistics record for index analysis
     */
    public record IndexStatistics(
            int totalStations,
            int uniqueLatitudes,
            int uniqueLongitudes,
            int uniqueTimeZoneGroups
    ) {
        @Override
        public String toString() {
            return String.format(
                    "Total Stations: %d | Unique Latitudes: %d | Unique Longitudes: %d | Time Zone Groups: %d",
                    totalStations, uniqueLatitudes, uniqueLongitudes, uniqueTimeZoneGroups
            );
        }
    }
}
