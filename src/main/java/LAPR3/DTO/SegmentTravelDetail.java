package LAPR3.DTO;

public class SegmentTravelDetail {
    private final int segmentId;
    private final int segmentOrder;
    private final double lengthMeters;
    private final double lengthKilometers;
    private final double speedKmh;
    private final double timeSeconds;
    private final double timeMinutes;
    private final boolean electrified;
    private final int numberTracks;

    public SegmentTravelDetail(int segmentId, int segmentOrder, double lengthMeters,
                               double speedKmh, double timeSeconds,
                               boolean electrified, int numberTracks) {
        this.segmentId = segmentId;
        this.segmentOrder = segmentOrder;
        this.lengthMeters = lengthMeters;
        this.lengthKilometers = lengthMeters / 1000.0;
        this.speedKmh = speedKmh;
        this.timeSeconds = timeSeconds;
        this.timeMinutes = timeSeconds / 60.0;
        this.electrified = electrified;
        this.numberTracks = numberTracks;
    }

    public int getSegmentId() {
        return segmentId;
    }

    public int getSegmentOrder() {
        return segmentOrder;
    }

    public double getLengthMeters() {
        return lengthMeters;
    }

    public double getLengthKilometers() {
        return lengthKilometers;
    }

    public double getSpeedKmh() {
        return speedKmh;
    }

    public double getTimeSeconds() {
        return timeSeconds;
    }

    public double getTimeMinutes() {
        return timeMinutes;
    }

    public boolean isElectrified() {
        return electrified;
    }

    public int getNumberTracks() {
        return numberTracks;
    }

    /**
     * Format time as HH:MM:SS
     */
    public String getFormattedTime() {
        int hours = (int) (timeSeconds / 3600);
        int minutes = (int) ((timeSeconds % 3600) / 60);
        int seconds = (int) (timeSeconds % 60);

        if (hours > 0) {
            return String.format("%dh %02dm %02ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %02ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }

    @Override
    public String toString() {
        return String.format("Segment %d: %.2f km @ %.1f km/h -> %s",
                segmentOrder, lengthKilometers, speedKmh, getFormattedTime());
    }
}
