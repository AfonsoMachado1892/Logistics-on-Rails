package LAPR3.DTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TravelTimeResult {
    private final String originFacilityName;
    private final String destinationFacilityName;
    private final String lineName;
    private final int locomotiveNumber;
    private final String locomotiveName;
    private final List<SegmentTravelDetail> segments;
    private final double totalDistanceMeters;
    private final double totalDistanceKilometers;
    private final double totalTimeSeconds;
    private final double totalTimeMinutes;
    private final double totalTimeHours;
    private final double averageSpeedKmh;

    public TravelTimeResult(String originFacilityName, String destinationFacilityName,
                            String lineName, int locomotiveNumber, String locomotiveName,
                            List<SegmentTravelDetail> segments) {
        this.originFacilityName = originFacilityName;
        this.destinationFacilityName = destinationFacilityName;
        this.lineName = lineName;
        this.locomotiveNumber = locomotiveNumber;
        this.locomotiveName = locomotiveName;
        this.segments = new ArrayList<>(segments);

        // Calculate totals
        this.totalDistanceMeters = segments.stream()
                .mapToDouble(SegmentTravelDetail::getLengthMeters)
                .sum();
        this.totalDistanceKilometers = totalDistanceMeters / 1000.0;
        this.totalTimeSeconds = segments.stream()
                .mapToDouble(SegmentTravelDetail::getTimeSeconds)
                .sum();
        this.totalTimeMinutes = totalTimeSeconds / 60.0;
        this.totalTimeHours = totalTimeSeconds / 3600.0;
        this.averageSpeedKmh = totalTimeHours > 0 ? totalDistanceKilometers / totalTimeHours : 0;
    }

    public String getOriginFacilityName() {
        return originFacilityName;
    }

    public String getDestinationFacilityName() {
        return destinationFacilityName;
    }

    public String getLineName() {
        return lineName;
    }

    public int getLocomotiveNumber() {
        return locomotiveNumber;
    }

    public String getLocomotiveName() {
        return locomotiveName;
    }

    public List<SegmentTravelDetail> getSegments() {
        return Collections.unmodifiableList(segments);
    }

    public double getTotalDistanceMeters() {
        return totalDistanceMeters;
    }

    public double getTotalDistanceKilometers() {
        return totalDistanceKilometers;
    }

    public double getTotalTimeSeconds() {
        return totalTimeSeconds;
    }

    public double getTotalTimeMinutes() {
        return totalTimeMinutes;
    }

    public double getTotalTimeHours() {
        return totalTimeHours;
    }

    public double getAverageSpeedKmh() {
        return averageSpeedKmh;
    }

    /**
     * Format total time as HH:MM:SS
     */
    public String getFormattedTotalTime() {
        int hours = (int) (totalTimeSeconds / 3600);
        int minutes = (int) ((totalTimeSeconds % 3600) / 60);
        int seconds = (int) (totalTimeSeconds % 60);

        if (hours > 0) {
            return String.format("%dh %02dm %02ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %02ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }

    /**
     * Generate a detailed report
     */
    public String generateReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔════════════════════════════════════════════════════════════════╗\n");
        sb.append("║              TRAVEL TIME CALCULATION REPORT                   ║\n");
        sb.append("╠════════════════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║ Origin:      %-49s ║\n", originFacilityName));
        sb.append(String.format("║ Destination: %-49s ║\n", destinationFacilityName));
        sb.append(String.format("║ Line:        %-49s ║\n", lineName));
        sb.append(String.format("║ Locomotive:  #%d - %-42s ║\n", locomotiveNumber, locomotiveName));
        sb.append("╠════════════════════════════════════════════════════════════════╣\n");
        sb.append("║                      SEGMENT DETAILS                          ║\n");
        sb.append("╠════════════════════════════════════════════════════════════════╣\n");

        for (SegmentTravelDetail segment : segments) {
            sb.append(String.format("║ Segment %d:                                                    ║\n",
                    segment.getSegmentOrder()));
            sb.append(String.format("║   Distance:     %6.2f km                                       ║\n",
                    segment.getLengthKilometers()));
            sb.append(String.format("║   Speed:        %6.1f km/h                                     ║\n",
                    segment.getSpeedKmh()));
            sb.append(String.format("║   Time:         %-20s                         ║\n",
                    segment.getFormattedTime()));
            sb.append(String.format("║   Electrified:  %-5s  |  Tracks: %d                          ║\n",
                    segment.isElectrified() ? "Yes" : "No",
                    segment.getNumberTracks()));
            sb.append("║                                                                ║\n");
        }

        sb.append("╠════════════════════════════════════════════════════════════════╣\n");
        sb.append("║                      JOURNEY SUMMARY                          ║\n");
        sb.append("╠════════════════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║ Total Distance:   %8.2f km                                 ║\n",
                totalDistanceKilometers));
        sb.append(String.format("║ Total Time:       %-20s                         ║\n",
                getFormattedTotalTime()));
        sb.append(String.format("║ Average Speed:    %8.2f km/h                               ║\n",
                averageSpeedKmh));
        sb.append(String.format("║ Segments:         %d                                          ║\n",
                segments.size()));
        sb.append("╚════════════════════════════════════════════════════════════════╝\n");

        return sb.toString();
    }

    @Override
    public String toString() {
        return generateReport();
    }
}
