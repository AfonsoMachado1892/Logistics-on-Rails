package ESINF.DTO;

import java.util.Locale; // <--- 1. Adicionar este import

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
    public boolean isValid() {
        // ... (o código mantém-se igual aqui)
        if (station == null || station.trim().isEmpty()) return false;
        if (country == null || country.trim().isEmpty()) return false;
        if (timeZoneGroup == null || timeZoneGroup.trim().isEmpty()) return false;
        if (latitude < -90.0 || latitude > 90.0) return false;
        if (longitude < -180.0 || longitude > 180.0) return false;
        return true;
    }

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

        // 2. Alterar as linhas abaixo para usar Locale.US
        if (latitude < -90.0 || latitude > 90.0) {
            return String.format(Locale.US, "Invalid latitude: %.5f (must be in [-90, 90])", latitude);
        }
        if (longitude < -180.0 || longitude > 180.0) {
            return String.format(Locale.US, "Invalid longitude: %.5f (must be in [-180, 180])", longitude);
        }

        return "Unknown validation error";
    }
}