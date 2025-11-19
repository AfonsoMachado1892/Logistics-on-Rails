package ESINF.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * USEI09 - Filter criteria for proximity search (Nearest-N).
 * Supports filtering by one or more Time Zone Groups.
 *
 * @author ESINF Team
 * @version 1.0
 */
public class TimeZoneFilter {

    private final Set<String> timeZoneGroups;

    /**
     * Creates a new TimeZoneFilter.
     *
     * @param groups One or more time zone groups (e.g., "CET", "EET").
     * If null or empty, no filter is applied.
     */
    public TimeZoneFilter(String... groups) {
        if (groups == null || groups.length == 0 || (groups.length == 1 && (groups[0] == null || groups[0].isEmpty()))) {
            this.timeZoneGroups = null; // No filter
        } else {
            this.timeZoneGroups = Arrays.stream(groups)
                    .filter(Objects::nonNull)
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
        }
    }

    /**
     * Checks if a station matches the filter criteria.
     *
     * @param station The station to check.
     * @return true if the filter is inactive (null) or if the station's
     * timeZoneGroup is in the filter set.
     */
    public boolean matches(Station station) {
        // No filter active
        if (timeZoneGroups == null || timeZoneGroups.isEmpty()) {
            return true;
        }

        if (station == null || station.getTimeZoneGroup() == null) {
            return false;
        }

        // Check if the station's group (in lowercase) is in the set
        return timeZoneGroups.contains(station.getTimeZoneGroup().toLowerCase());
    }

    /**
     * Checks if this filter has any active criteria.
     *
     * @return true if at least one filter is active.
     */
    public boolean hasFilter() {
        return timeZoneGroups != null && !timeZoneGroups.isEmpty();
    }

    @Override
    public String toString() {
        if (!hasFilter()) {
            return "TimeZoneFilter{NO FILTERS}";
        }
        return "TimeZoneFilter{groups=" + timeZoneGroups + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeZoneFilter that = (TimeZoneFilter) o;
        return Objects.equals(timeZoneGroups, that.timeZoneGroups);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeZoneGroups);
    }
}