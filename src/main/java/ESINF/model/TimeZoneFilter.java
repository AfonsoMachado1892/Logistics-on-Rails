package ESINF.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * USEI09 - Filter criteria for proximity search (Nearest-N).
 * <p>
 * This class represents a filter strategy based on Time Zone Groups (e.g., "CET", "EET").
 * It is designed to be used within spatial queries to restrict results to specific regions.
 * </p>
 * <p>
 * <strong>Key Behaviors:</strong>
 * <ul>
 * <li><strong>Case-Insensitive:</strong> Inputs are converted to lowercase for comparison.</li>
 * <li><strong>Inactive State:</strong> If initialized with null or empty values, the filter is considered "inactive" and will match ALL stations.</li>
 * </ul>
 */
public class TimeZoneFilter {

    /**
     * The set of allowed time zone groups, stored in lowercase for case-insensitive matching.
     * If this is null or empty, the filter is considered inactive.
     */
    private final Set<String> timeZoneGroups;

    /**
     * Creates a new TimeZoneFilter.
     * <p>
     * Accepting variable arguments, this constructor parses the input strings,
     * removes nulls, converts them to lowercase, and stores them.
     * </p>
     * <p>
     * If the input is {@code null}, empty, or contains only a single empty string,
     * the filter is set to an inactive state (matching everything).
     * </p>
     *
     * @param groups One or more time zone groups (e.g., "CET", "EET", "WET/GMT").
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
     * Checks if a specific station matches the filter criteria.
     * <p>
     * The match logic is as follows:
     * <ol>
     * <li>If the filter is inactive (no groups defined), returns {@code true} (match all).</li>
     * <li>If the station or its time zone group is null, returns {@code false}.</li>
     * <li>Otherwise, returns {@code true} if the station's group exists in the allowed set (case-insensitive).</li>
     * </ol>
     *
     * @param station The {@link Station} to check.
     * @return {@code true} if the station is accepted by the filter; {@code false} otherwise.
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
     * <p>
     * Useful for UI logic to determine if filtering is currently applied.
     * </p>
     *
     * @return {@code true} if specific groups are being filtered; {@code false} if the filter matches everything.
     */
    public boolean hasFilter() {
        return timeZoneGroups != null && !timeZoneGroups.isEmpty();
    }

    /**
     * Returns a string representation of the filter state.
     *
     * @return A string listing the active time zone groups, or "NO FILTERS" if inactive.
     */
    @Override
    public String toString() {
        if (!hasFilter()) {
            return "TimeZoneFilter{NO FILTERS}";
        }
        return "TimeZoneFilter{groups=" + timeZoneGroups + "}";
    }

    /**
     * Compares this filter with another object for equality.
     *
     * @param o The object to compare with.
     * @return {@code true} if the other object is a TimeZoneFilter with the same set of allowed groups.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeZoneFilter that = (TimeZoneFilter) o;
        return Objects.equals(timeZoneGroups, that.timeZoneGroups);
    }

    /**
     * Generates a hash code for this filter.
     *
     * @return The hash code based on the set of time zone groups.
     */
    @Override
    public int hashCode() {
        return Objects.hash(timeZoneGroups);
    }
}