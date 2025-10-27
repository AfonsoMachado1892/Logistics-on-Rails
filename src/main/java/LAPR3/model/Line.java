package LAPR3.model;

import java.util.Objects;

public class Line {
    private final int id;
    private final String name;
    private final String owner;
    private final int startFacilityId;
    private final String startFacilityName;
    private final int endFacilityId;
    private final String endFacilityName;
    private final int gauge;

    /**
     * Creates a new Line
     * @param id line identifier
     * @param name line name
     * @param owner line owner
     * @param startFacilityId starting facility ID
     * @param startFacilityName starting facility name
     * @param endFacilityId ending facility ID
     * @param endFacilityName ending facility name
     * @param gauge track gauge in millimeters
     */
    public Line(int id, String name, String owner, int startFacilityId,
                String startFacilityName, int endFacilityId,
                String endFacilityName, int gauge) {
        if (id < 0) {
            throw new IllegalArgumentException("Line ID cannot be negative");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Line name cannot be null or empty");
        }
        if (startFacilityId == endFacilityId) {
            throw new IllegalArgumentException("Start and end facilities must be different");
        }
        if (gauge <= 0) {
            throw new IllegalArgumentException("Gauge must be positive");
        }

        this.id = id;
        this.name = name.trim();
        this.owner = owner != null ? owner.trim() : "";
        this.startFacilityId = startFacilityId;
        this.startFacilityName = startFacilityName != null ? startFacilityName.trim() : "";
        this.endFacilityId = endFacilityId;
        this.endFacilityName = endFacilityName != null ? endFacilityName.trim() : "";
        this.gauge = gauge;
    }

    /**
     * Gets the line identifier
     *
     * @return the line ID
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the line name
     *
     * @return the line name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the line owner
     *
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Gets the starting facility identifier
     *
     * @return the start facility ID
     */
    public int getStartFacilityId() {
        return startFacilityId;
    }

    /**
     * Gets the starting facility name
     *
     * @return the start facility name
     */
    public String getStartFacilityName() {
        return startFacilityName;
    }

    /**
     * Gets the ending facility identifier
     *
     * @return the end facility ID
     */
    public int getEndFacilityId() {
        return endFacilityId;
    }

    /**
     * Gets the ending facility name
     *
     * @return the end facility name
     */
    public String getEndFacilityName() {
        return endFacilityName;
    }

    /**
     * Gets the track gauge in millimeters
     *
     * @return the gauge in mm
     */
    public int getGauge() {
        return gauge;
    }

    /**
     * Checks if this line directly connects two facilities
     * @param facilityId1 first facility ID
     * @param facilityId2 second facility ID
     * @return true if the line connects these facilities (in either direction)
     */
    public boolean connectsFacilities(int facilityId1, int facilityId2) {
        return (startFacilityId == facilityId1 && endFacilityId == facilityId2) ||
                (startFacilityId == facilityId2 && endFacilityId == facilityId1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Line line = (Line) o;
        return id == line.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Line{id=%d, name='%s', %s -> %s, gauge=%dmm}",
                id, name, startFacilityName, endFacilityName, gauge);
    }
}
