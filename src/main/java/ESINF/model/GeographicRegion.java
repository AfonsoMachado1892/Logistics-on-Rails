package ESINF.model;

/**
 * USEI08 - Geographic Region for spatial queries.
 * <p>
 * Represents a rectangular bounding box defined by latitude and longitude ranges.
 * This class is immutable and validates coordinates upon instantiation to ensure
 * they fall within standard Earth coordinate ranges (Latitude: [-90, 90], Longitude: [-180, 180]).
 * </p>
 *
 */
public class GeographicRegion {

    /** The minimum latitude boundary. */
    private final double latMin;
    /** The maximum latitude boundary. */
    private final double latMax;
    /** The minimum longitude boundary. */
    private final double lonMin;
    /** The maximum longitude boundary. */
    private final double lonMax;

    /**
     * Creates a new GeographicRegion with the specified boundaries.
     * <p>
     * All coordinates are validated. The minimum values must not be greater than
     * the maximum values.
     * </p>
     *
     * @param latMin Minimum latitude (inclusive), must be within [-90, 90].
     * @param latMax Maximum latitude (inclusive), must be within [-90, 90].
     * @param lonMin Minimum longitude (inclusive), must be within [-180, 180].
     * @param lonMax Maximum longitude (inclusive), must be within [-180, 180].
     * @throws IllegalArgumentException if coordinates are out of bounds or if min values exceed max values.
     */
    public GeographicRegion(double latMin, double latMax, double lonMin, double lonMax) {
        validateLatitude(latMin, "latMin");
        validateLatitude(latMax, "latMax");
        validateLongitude(lonMin, "lonMin");
        validateLongitude(lonMax, "lonMax");

        if (latMin > latMax) {
            throw new IllegalArgumentException(
                    String.format("latMin (%.5f) cannot be greater than latMax (%.5f)", latMin, latMax)
            );
        }

        if (lonMin > lonMax) {
            throw new IllegalArgumentException(
                    String.format("lonMin (%.5f) cannot be greater than lonMax (%.5f)", lonMin, lonMax)
            );
        }

        this.latMin = latMin;
        this.latMax = latMax;
        this.lonMin = lonMin;
        this.lonMax = lonMax;
    }

    /**
     * Validates that a latitude value is within the valid range [-90, 90].
     *
     * @param lat       Latitude value to validate.
     * @param fieldName Name of the field (used for error messages).
     * @throws IllegalArgumentException if latitude is invalid.
     */
    private void validateLatitude(double lat, String fieldName) {
        if (lat < -90.0 || lat > 90.0) {
            throw new IllegalArgumentException(
                    String.format("%s must be in [-90, 90], got: %.5f", fieldName, lat)
            );
        }
    }

    /**
     * Validates that a longitude value is within the valid range [-180, 180].
     *
     * @param lon       Longitude value to validate.
     * @param fieldName Name of the field (used for error messages).
     * @throws IllegalArgumentException if longitude is invalid.
     */
    private void validateLongitude(double lon, String fieldName) {
        if (lon < -180.0 || lon > 180.0) {
            throw new IllegalArgumentException(
                    String.format("%s must be in [-180, 180], got: %.5f", fieldName, lon)
            );
        }
    }

    /**
     * Checks if a specific point (latitude, longitude) lies within this region.
     * Boundaries are considered inclusive.
     *
     * @param lat Latitude of the point to check.
     * @param lon Longitude of the point to check.
     * @return {@code true} if the point is inside or on the boundary of the region; {@code false} otherwise.
     */
    public boolean contains(double lat, double lon) {
        return lat >= latMin && lat <= latMax &&
                lon >= lonMin && lon <= lonMax;
    }

    /**
     * Validates that this region is well-formed (min less or equal to max).
     * <p>
     * Note: Since the constructor enforces this logic, this method will always
     * return {@code true} for an instantiated object.
     * </p>
     *
     * @return {@code true} if valid.
     */
    public boolean isValid() {
        return latMin <= latMax && lonMin <= lonMax;
    }

    /**
     * Calculates the area of this region in square degrees.
     *
     * @return The area in square degrees.
     */
    public double getAreaSquareDegrees() {
        return (latMax - latMin) * (lonMax - lonMin);
    }

    /**
     * Calculates the geometric center point of this region.
     *
     * @return A two-element array containing {@code [centerLatitude, centerLongitude]}.
     */
    public double[] getCenter() {
        return new double[] {
                (latMin + latMax) / 2.0,
                (lonMin + lonMax) / 2.0
        };
    }

    /**
     * Gets the width of this region in degrees of longitude.
     *
     * @return The width in degrees.
     */
    public double getWidthDegrees() {
        return lonMax - lonMin;
    }

    /**
     * Gets the height of this region in degrees of latitude.
     *
     * @return The height in degrees.
     */
    public double getHeightDegrees() {
        return latMax - latMin;
    }

    // region Getters

    /**
     * Gets the minimum latitude boundary.
     *
     * @return the minimum latitude.
     */
    public double getLatMin() { return latMin; }

    /**
     * Gets the maximum latitude boundary.
     *
     * @return the maximum latitude.
     */
    public double getLatMax() { return latMax; }

    /**
     * Gets the minimum longitude boundary.
     *
     * @return the minimum longitude.
     */
    public double getLonMin() { return lonMin; }

    /**
     * Gets the maximum longitude boundary.
     *
     * @return the maximum longitude.
     */
    public double getLonMax() { return lonMax; }

    // endregion

    /**
     * Returns a string representation of the GeographicRegion.
     *
     * @return A string containing the coordinate ranges and the total area.
     */
    @Override
    public String toString() {
        return String.format(
                "GeographicRegion{lat=[%.5f, %.5f], lon=[%.5f, %.5f], area=%.5f°²}",
                latMin, latMax, lonMin, lonMax, getAreaSquareDegrees()
        );
    }

    /**
     * Compares this region to another object for equality.
     *
     * @param o The object to compare with.
     * @return {@code true} if the other object is a GeographicRegion with identical boundaries; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GeographicRegion)) return false;

        GeographicRegion that = (GeographicRegion) o;
        return Double.compare(that.latMin, latMin) == 0 &&
                Double.compare(that.latMax, latMax) == 0 &&
                Double.compare(that.lonMin, lonMin) == 0 &&
                Double.compare(that.lonMax, lonMax) == 0;
    }

    /**
     * Generates a hash code based on the region's boundaries.
     *
     * @return The hash code value.
     */
    @Override
    public int hashCode() {
        int result = 17;
        long temp;

        temp = Double.doubleToLongBits(latMin);
        result = 31 * result + (int) (temp ^ (temp >>> 32));

        temp = Double.doubleToLongBits(latMax);
        result = 31 * result + (int) (temp ^ (temp >>> 32));

        temp = Double.doubleToLongBits(lonMin);
        result = 31 * result + (int) (temp ^ (temp >>> 32));

        temp = Double.doubleToLongBits(lonMax);
        result = 31 * result + (int) (temp ^ (temp >>> 32));

        return result;
    }
}