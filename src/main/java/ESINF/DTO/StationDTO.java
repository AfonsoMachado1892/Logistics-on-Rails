package ESINF.DTO;

public record StationDTO (
        String country,
        String timeZone,
        String timeZoneGroup,
        String station,
        double latitude,
        double longitude,
        boolean isCity,
        boolean isMainStation,
        boolean isAirport
) {
    /**
     * Validates the DTO data according to USEI06 acceptance criteria
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        // Non-empty name, country, and timeZoneGroup
        if (station == null || station.trim().isEmpty()) return false;
        if (country == null || country.trim().isEmpty()) return false;
        if (timeZoneGroup == null || timeZoneGroup.trim().isEmpty()) return false;

        // Latitude ∈ [-90, 90]
        if (latitude < -90.0 || latitude > 90.0) return false;

        // Longitude ∈ [-180, 180]
        if (longitude < -180.0 || longitude > 180.0) return false;

        return true;
    }

    /**
     * Gets validation error message if invalid
     */
    public String getValidationError() {
        if (station == null || station.trim().isEmpty()) {
            return "Empty or null station name";
        }
        if (country == null || country.trim().isEmpty()) {
            return "Empty or null country";
        }
        if (timeZoneGroup == null || timeZoneGroup.trim().isEmpty()) {
            return "Empty or null timeZoneGroup";
        }
        if (latitude < -90.0 || latitude > 90.0) {
            return String.format("Invalid latitude: %.5f (must be in [-90, 90])", latitude);
        }
        if (longitude < -180.0 || longitude > 180.0) {
            return String.format("Invalid longitude: %.5f (must be in [-180, 180])", longitude);
        }
        return "Unknown validation error";
    }
}
