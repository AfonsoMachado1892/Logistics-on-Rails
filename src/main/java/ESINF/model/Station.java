package ESINF.model;

import java.text.Collator;
import java.util.Locale;
import java.util.Objects;

public class Station implements Comparable<Station>{

    private final String name;
    private final double latitude;
    private final double longitude;
    private final String country;
    private final String timeZone;
    private final String timeZoneGroup;
    private final boolean isCity;
    private final boolean isMainStation;
    private final boolean isAirport;

    public Station(String name, double latitude, double longitude, String country,
                   String timeZone, String timeZoneGroup, boolean isCity,
                   boolean isMainStation, boolean isAirport) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = country;
        this.timeZone = timeZone;
        this.timeZoneGroup = timeZoneGroup;
        this.isCity = isCity;
        this.isMainStation = isMainStation;
        this.isAirport = isAirport;
    }

    // Getters
    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getCountry() { return country; }
    public String getTimeZone() { return timeZone; }
    public String getTimeZoneGroup() { return timeZoneGroup; }
    public boolean isCity() { return isCity; }
    public boolean isMainStation() { return isMainStation; }
    public boolean isAirport() { return isAirport; }

    /**
     * Natural ordering by name (ascending) for deterministic sorting
     */
    @Override
    public int compareTo(Station other) {
        Collator collator = Collator.getInstance(new Locale("pt", "PT"));
        collator.setStrength(Collator.PRIMARY); // Ignora acentos para ordenação
        return collator.compare(this.name, other.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Station)) return false;
        Station station = (Station) o;
        return Double.compare(station.latitude, latitude) == 0 &&
                Double.compare(station.longitude, longitude) == 0 &&
                Objects.equals(name, station.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, latitude, longitude);
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
                "Station{name='%s', lat=%.5f, lon=%.5f, country='%s', tzGroup='%s'}",
                name, latitude, longitude, country, timeZoneGroup);
    }
}
