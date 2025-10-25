package LAPR3.DTO;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SegmentTravelDetailTest {

    private static final double DELTA = 0.001; // Tolerância para comparações de doubles

    @Test
    public void testConstructorAndGetters() {
        // Arrange
        double lengthMeters = 5000;
        double speedKmh = 100;
        double timeSeconds = 180;

        // Act
        SegmentTravelDetail detail = new SegmentTravelDetail(
                1, 2, lengthMeters, speedKmh, timeSeconds, true, 2
        );

        // Assert
        assertEquals(1, detail.getSegmentId());
        assertEquals(2, detail.getSegmentOrder());
        assertEquals(lengthMeters, detail.getLengthMeters(), DELTA);
        assertEquals(speedKmh, detail.getSpeedKmh(), DELTA);
        assertEquals(timeSeconds, detail.getTimeSeconds(), DELTA);
        assertTrue(detail.isElectrified());
        assertEquals(2, detail.getNumberTracks());

        // Testar campos calculados
        assertEquals(5.0, detail.getLengthKilometers(), DELTA);
        assertEquals(3.0, detail.getTimeMinutes(), DELTA);
    }

    @Test
    public void testGetFormattedTime() {
        // Teste 1: Apenas segundos
        SegmentTravelDetail detail1 = new SegmentTravelDetail(1, 1, 1000, 100, 45, true, 1);
        assertEquals("45s", detail1.getFormattedTime());

        // Teste 2: Minutos e segundos
        SegmentTravelDetail detail2 = new SegmentTravelDetail(2, 2, 5000, 100, 140, true, 1);
        assertEquals("2m 20s", detail2.getFormattedTime());

        // Teste 3: Horas, minutos e segundos
        SegmentTravelDetail detail3 = new SegmentTravelDetail(3, 3, 100000, 100, 3661, true, 1);
        assertEquals("1h 01m 01s", detail3.getFormattedTime());

        // Teste 4: Limite (60 segundos)
        SegmentTravelDetail detail4 = new SegmentTravelDetail(4, 4, 2000, 100, 60, true, 1);
        assertEquals("1m 00s", detail4.getFormattedTime());

        // Teste 5: Limite (3600 segundos)
        SegmentTravelDetail detail5 = new SegmentTravelDetail(5, 5, 10000, 100, 3600, true, 1);
        assertEquals("1h 00m 00s", detail5.getFormattedTime());

        // Teste 6: Zero segundos
        SegmentTravelDetail detail6 = new SegmentTravelDetail(6, 6, 0, 0, 0, true, 1);
        assertEquals("0s", detail6.getFormattedTime());
    }

    @Test
    public void testToString() {
        SegmentTravelDetail detail = new SegmentTravelDetail(1, 1, 5000, 100, 180, true, 1);
        String str = detail.toString();

        assertTrue(str.contains("Segment 1"));
        assertTrue(str.contains("5,00 km"));
        assertTrue(str.contains("100,0 km/h"));
        assertTrue(str.contains("3m 00s"));
    }
}
