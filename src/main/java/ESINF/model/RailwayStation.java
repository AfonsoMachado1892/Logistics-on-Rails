package ESINF.model;

import java.util.Objects;

/**
 * Model class representing a Railway Station
 * Used as vertices in the railway network graph
 * Based on Belgian railway network dataset
 */
public class RailwayStation {

    private final int stationId;
    private final String name;
    private final double latitude;
    private final double longitude;
    private final double coordX;
    private final double coordY;

    /**
     * Constructor for complete station data
     * @param stationId unique station identifier
     * @param name station name
     * @param latitude geographic latitude
     * @param longitude geographic longitude
     * @param coordX X coordinate for visualization
     * @param coordY Y coordinate for visualization
     */
    public RailwayStation(int stationId, String name, double latitude,
                          double longitude, double coordX, double coordY) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Station name cannot be null or empty");
        }
        this.stationId = stationId;
        this.name = name.trim();
        this.latitude = latitude;
        this.longitude = longitude;
        this.coordX = coordX;
        this.coordY = coordY;
    }

    public int getStationId() {
        return stationId;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getCoordX() {
        return coordX;
    }

    public double getCoordY() {
        return coordY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RailwayStation that = (RailwayStation) o;
        return stationId == that.stationId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stationId);
    }

    @Override
    public String toString() {
        return String.format("%s (ID: %d)", name, stationId);
    }
}