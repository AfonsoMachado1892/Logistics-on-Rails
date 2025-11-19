package LAPR3.DTO;

import LAPR3.model.Line;
import LAPR3.model.Segment;

import java.util.Collections;
import java.util.List;

/**
 * Represents a leg (section) of a route between two consecutive facilities
 * Contains the line and segments that connect them
 * Part of USLP07 - Subtask #65
 */
public class RouteLeg {
    private final int fromFacilityId;
    private final String fromFacilityName;
    private final int toFacilityId;
    private final String toFacilityName;
    private final Line line;
    private final List<Segment> segments;

    /**
     * Creates a route leg
     *
     * @param fromFacilityId origin facility ID
     * @param fromFacilityName origin facility name
     * @param toFacilityId destination facility ID
     * @param toFacilityName destination facility name
     * @param line line connecting the facilities
     * @param segments ordered list of segments
     */
    public RouteLeg(int fromFacilityId, String fromFacilityName,
                    int toFacilityId, String toFacilityName,
                    Line line, List<Segment> segments) {
        this.fromFacilityId = fromFacilityId;
        this.fromFacilityName = fromFacilityName;
        this.toFacilityId = toFacilityId;
        this.toFacilityName = toFacilityName;
        this.line = line;
        this.segments = segments;
    }

    public int getFromFacilityId() {
        return fromFacilityId;
    }

    public String getFromFacilityName() {
        return fromFacilityName;
    }

    public int getToFacilityId() {
        return toFacilityId;
    }

    public String getToFacilityName() {
        return toFacilityName;
    }

    public Line getLine() {
        return line;
    }

    public List<Segment> getSegments() {
        return Collections.unmodifiableList(segments);
    }

    /**
     * Gets total distance of this leg
     * @return distance in meters
     */
    public double getTotalDistance() {
        return segments.stream()
                .mapToDouble(Segment::getLengthMeters)
                .sum();
    }

    /**
     * Gets number of segments in this leg
     * @return segment count
     */
    public int getSegmentCount() {
        return segments.size();
    }

    /**
     * Checks if leg is fully electrified
     * @return true if all segments are electrified
     */
    public boolean isFullyElectrified() {
        return segments.stream().allMatch(Segment::isElectrified);
    }

    /**
     * Checks if leg has any single-track segments
     * @return true if at least one segment is single-track
     */
    public boolean hasSingleTrackSegments() {
        return segments.stream().anyMatch(s -> s.getNumberTracks() == 1);
    }

    @Override
    public String toString() {
        return String.format("%s → %s via %s (%.2fkm, %d segments)",
                fromFacilityName, toFacilityName, line.getName(),
                getTotalDistance() / 1000.0, segments.size());
    }
}