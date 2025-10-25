package LAPR3.DTO;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TravelTimeResultTest {

    private static final double DELTA = 0.001; // Tolerância

    @Test
    public void testConstructorAndCalculations() {
        // Arrange
        SegmentTravelDetail seg1 = new SegmentTravelDetail(1, 1, 10000, 100, 360, true, 1); // 10km
        SegmentTravelDetail seg2 = new SegmentTravelDetail(2, 2, 5000, 100, 180, true, 1);  // 5km
        List<SegmentTravelDetail> segments = List.of(seg1, seg2);

        // Act
        TravelTimeResult result = new TravelTimeResult(
                "Station A", "Station B", "Line 1", 101, "Test Loco", segments
        );

        // Assert
        assertEquals("Station A", result.getOriginFacilityName());
        assertEquals("Station B", result.getDestinationFacilityName());
        assertEquals("Line 1", result.getLineName());
        assertEquals(101, result.getLocomotiveNumber());
        assertEquals("Test Loco", result.getLocomotiveName());
        assertEquals(2, result.getSegments().size());

        // Testar totais calculados
        assertEquals(15000.0, result.getTotalDistanceMeters(), DELTA);
        assertEquals(15.0, result.getTotalDistanceKilometers(), DELTA);
        assertEquals(540.0, result.getTotalTimeSeconds(), DELTA); // 360 + 180
        assertEquals(9.0, result.getTotalTimeMinutes(), DELTA);
        assertEquals(0.15, result.getTotalTimeHours(), DELTA); // 540 / 3600

        // Testar velocidade média
        // AvgSpeed = TotalDistKm / TotalTimeHours = 15 / 0.15 = 100 km/h
        assertEquals(100.0, result.getAverageSpeedKmh(), DELTA);
    }

    @Test
    public void testAverageSpeedWithZeroTime() {
        // Se o tempo for 0, a velocidade média deve ser 0 (evitar divisão por zero)
        TravelTimeResult result = new TravelTimeResult("A", "B", "L", 1, "L", new ArrayList<>());

        assertEquals(0.0, result.getTotalTimeHours(), DELTA);
        assertEquals(0.0, result.getAverageSpeedKmh(), DELTA);
    }

    @Test
    public void testGetSegmentsReturnsUnmodifiableList() {
        TravelTimeResult result = new TravelTimeResult("A", "B", "L", 1, "L", new ArrayList<>());
        List<SegmentTravelDetail> segments = result.getSegments();

        try {
            segments.add(new SegmentTravelDetail(1, 1, 100, 10, 10, true, 1));
            fail("A lista de segmentos não devia ser modificável");
        } catch (UnsupportedOperationException e) {
            // Sucesso
        }
    }

    @Test
    public void testGetFormattedTotalTime() {
        // 540s = 9m 00s
        SegmentTravelDetail seg1 = new SegmentTravelDetail(1, 1, 10000, 100, 540, true, 1);
        TravelTimeResult result = new TravelTimeResult("A", "B", "L", 1, "L", List.of(seg1));
        assertEquals("9m 00s", result.getFormattedTotalTime());
    }

    @Test
    public void testGenerateReport() {
        SegmentTravelDetail seg1 = new SegmentTravelDetail(1, 1, 10000, 100, 360, true, 1);
        SegmentTravelDetail seg2 = new SegmentTravelDetail(2, 2, 5000, 80, 225, false, 2);
        List<SegmentTravelDetail> segments = List.of(seg1, seg2);

        TravelTimeResult result = new TravelTimeResult(
                "Porto Campanha", "Lisboa Oriente", "Linha do Norte", 101, "DieselMaster", segments
        );

        String report = result.generateReport();

        // Verificar se o relatório contém os dados chave
        assertTrue(report.contains("TRAVEL TIME CALCULATION REPORT"));
        assertTrue(report.contains("Origin:      Porto Campanha"));
        assertTrue(report.contains("Destination: Lisboa Oriente"));
        assertTrue(report.contains("Locomotive:  #101 - DieselMaster"));
        assertTrue(report.contains("Segment 1"));
        assertTrue(report.contains("Distance:      10,00 km"));
        assertTrue(report.contains("Speed:         100,0 km/h"));
        assertTrue(report.contains("Time:         6m 00s"));
        assertTrue(report.contains("Electrified:  Yes"));
        assertTrue(report.contains("Tracks: 1"));
        assertTrue(report.contains("Segment 2"));
        assertTrue(report.contains("Time:         3m 45s"));
        assertTrue(report.contains("Electrified:  No"));
        assertTrue(report.contains("Tracks: 2"));
        assertTrue(report.contains("JOURNEY SUMMARY"));
        assertTrue(report.contains("Total Distance:      15,00 km"));
        assertTrue(report.contains("Total Time:       9m 45s")); // 360 + 225 = 585s

        // 15km / (585/3600)h = 15 / 0.1625 = 92.307...
        assertTrue(report.contains("Average Speed:       92,31 km/h"));
    }
}
