package ESINF.model;

/**
 * USEI08 - Geographic Region for spatial queries
 * Represents a rectangular bounding box defined by latitude/longitude ranges.
 *
 * @author ESINF Team
 * @version 1.0
 */
public class GeographicRegion {

    private final double latMin;
    private final double latMax;
    private final double lonMin;
    private final double lonMax;

    /**
     * Creates a new GeographicRegion.
     *
     * @param latMin Minimum latitude (inclusive)
     * @param latMax Maximum latitude (inclusive)
     * @param lonMin Minimum longitude (inclusive)
     * @param lonMax Maximum longitude (inclusive)
     * @throws IllegalArgumentException if validation fails
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
     * Validates that latitude is within valid range [-90, 90].
     *
     * @param lat Latitude value to validate
     * @param fieldName Name of the field (for error messages)
     * @throws IllegalArgumentException if latitude is invalid
     */
    private void validateLatitude(double lat, String fieldName) {
        if (lat < -90.0 || lat > 90.0) {
            throw new IllegalArgumentException(
                    String.format("%s must be in [-90, 90], got: %.5f", fieldName, lat)
            );
        }
    }

    /**
     * Validates that longitude is within valid range [-180, 180].
     *
     * @param lon Longitude value to validate
     * @param fieldName Name of the field (for error messages)
     * @throws IllegalArgumentException if longitude is invalid
     */
    private void validateLongitude(double lon, String fieldName) {
        if (lon < -180.0 || lon > 180.0) {
            throw new IllegalArgumentException(
                    String.format("%s must be in [-180, 180], got: %.5f", fieldName, lon)
            );
        }
    }

    /**
     * Checks if a point (latitude, longitude) is within this region.
     *
     * @param lat Latitude to check
     * @param lon Longitude to check
     * @return true if point is inside region (inclusive bounds)
     */
    public boolean contains(double lat, double lon) {
        return lat >= latMin && lat <= latMax &&
                lon >= lonMin && lon <= lonMax;
    }

    /**
     * Validates that this region is well-formed.
     *
     * @return true if valid (latMin <= latMax and lonMin <= lonMax)
     */
    public boolean isValid() {
        return latMin <= latMax && lonMin <= lonMax;
    }

    /**
     * Calculates the area of this region in square degrees.
     *
     * @return Area in square degrees
     */
    public double getAreaSquareDegrees() {
        return (latMax - latMin) * (lonMax - lonMin);
    }

    /**
     * Gets the center point of this region.
     *
     * @return Array [centerLat, centerLon]
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
     * @return Width in degrees
     */
    public double getWidthDegrees() {
        return lonMax - lonMin;
    }

    /**
     * Gets the height of this region in degrees of latitude.
     *
     * @return Height in degrees
     */
    public double getHeightDegrees() {
        return latMax - latMin;
    }

    // Getters
    public double getLatMin() { return latMin; }
    public double getLatMax() { return latMax; }
    public double getLonMin() { return lonMin; }
    public double getLonMax() { return lonMax; }

    @Override
    public String toString() {
        return String.format(
                "GeographicRegion{lat=[%.5f, %.5f], lon=[%.5f, %.5f], area=%.5f°²}",
                latMin, latMax, lonMin, lonMax, getAreaSquareDegrees()
        );
    }

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