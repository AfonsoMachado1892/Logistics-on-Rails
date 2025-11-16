package ESINF.model;

import java.util.Objects;

/**
 * USEI08 - Filter criteria for station searches
 * Supports optional filtering by isCity, isMainStation, and country.
 *
 * A filter with all null values matches all stations (no filtering).
 *
 * @author ESINF Team
 * @version 1.0
 */
public class StationFilter {

    private final Boolean isCity;          // null = no filter
    private final Boolean isMainStation;   // null = no filter
    private final String country;          // null or "all" = no filter

    /**
     * Creates a new StationFilter.
     * Use null values to skip filtering on that criterion.
     *
     * @param isCity Filter by city status (null = no filter)
     * @param isMainStation Filter by main station status (null = no filter)
     * @param country Filter by country code (null or "all" = no filter)
     */
    public StationFilter(Boolean isCity, Boolean isMainStation, String country) {
        this.isCity = isCity;
        this.isMainStation = isMainStation;
        // Treat "all" as null (no filter)
        this.country = (country != null && country.equalsIgnoreCase("all")) ? null : country;
    }

    /**
     * Creates a filter with no criteria (matches all stations).
     *
     * @return Filter that matches everything
     */
    public static StationFilter noFilter() {
        return new StationFilter(null, null, null);
    }

    /**
     * Creates a filter for cities only.
     *
     * @return Filter that matches only cities
     */
    public static StationFilter citiesOnly() {
        return new StationFilter(true, null, null);
    }

    /**
     * Creates a filter for non-cities only.
     *
     * @return Filter that matches only non-cities
     */
    public static StationFilter nonCitiesOnly() {
        return new StationFilter(false, null, null);
    }

    /**
     * Creates a filter for main stations only.
     *
     * @return Filter that matches only main stations
     */
    public static StationFilter mainStationsOnly() {
        return new StationFilter(null, true, null);
    }

    /**
     * Creates a filter for non-main stations only.
     *
     * @return Filter that matches only non-main stations
     */
    public static StationFilter nonMainStationsOnly() {
        return new StationFilter(null, false, null);
    }

    /**
     * Creates a filter for a specific country.
     *
     * @param country Country code (e.g., "PT", "ES")
     * @return Filter that matches only stations in that country
     */
    public static StationFilter forCountry(String country) {
        return new StationFilter(null, null, country);
    }

    /**
     * Creates a combined filter: cities in a specific country.
     *
     * @param country Country code
     * @return Filter for cities in that country
     */
    public static StationFilter citiesInCountry(String country) {
        return new StationFilter(true, null, country);
    }

    /**
     * Creates a combined filter: main stations in a specific country.
     *
     * @param country Country code
     * @return Filter for main stations in that country
     */
    public static StationFilter mainStationsInCountry(String country) {
        return new StationFilter(null, true, country);
    }

    /**
     * Checks if a station matches all active filter criteria.
     *
     * @param station The station to check
     * @return true if station passes all filters, false otherwise
     */
    public boolean matches(Station station) {
        if (station == null) {
            return false;
        }

        // Check isCity filter
        if (isCity != null && station.isCity() != isCity) {
            return false;
        }

        // Check isMainStation filter
        if (isMainStation != null && station.isMainStation() != isMainStation) {
            return false;
        }

        // Check country filter (case-insensitive)
        if (country != null && !country.equalsIgnoreCase(station.getCountry())) {
            return false;
        }

        return true;
    }

    /**
     * Checks if this filter has any active criteria.
     *
     * @return true if at least one filter is active
     */
    public boolean hasFilters() {
        return isCity != null || isMainStation != null || country != null;
    }

    /**
     * Gets the number of active filter criteria.
     *
     * @return Number of active filters (0-3)
     */
    public int getActiveFilterCount() {
        int count = 0;
        if (isCity != null) count++;
        if (isMainStation != null) count++;
        if (country != null) count++;
        return count;
    }

    // Getters
    public Boolean getIsCity() { return isCity; }
    public Boolean getIsMainStation() { return isMainStation; }
    public String getCountry() { return country; }

    @Override
    public String toString() {
        if (!hasFilters()) {
            return "StationFilter{NO FILTERS - matches all}";
        }

        StringBuilder sb = new StringBuilder("StationFilter{");
        boolean first = true;

        if (isCity != null) {
            sb.append("isCity=").append(isCity);
            first = false;
        }

        if (isMainStation != null) {
            if (!first) sb.append(", ");
            sb.append("isMainStation=").append(isMainStation);
            first = false;
        }

        if (country != null) {
            if (!first) sb.append(", ");
            sb.append("country='").append(country).append("'");
        }

        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StationFilter)) return false;

        StationFilter that = (StationFilter) o;
        return Objects.equals(isCity, that.isCity) &&
                Objects.equals(isMainStation, that.isMainStation) &&
                Objects.equals(country, that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isCity, isMainStation, country);
    }
}