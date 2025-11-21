package ESINF.DTO;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for StationDTO
 * Covers validation logic according to USEI06 acceptance criteria
 */
public class StationDTOTest {

    // ============ Valid DTO Tests ============

    @Test
    public void testValidDTO() {
        StationDTO dto = new StationDTO(
                "PT", "Europe/Lisbon", "WET/GMT",
                "Lisboa Santa Apolónia", 38.71387, -9.122271,
                true, true, false
        );
        assertTrue(dto.isValid());
    }

    @Test
    public void testValidDTOWithMinLatitude() {
        StationDTO dto = new StationDTO(
                "PT", "Europe/Lisbon", "WET/GMT",
                "South Pole", -90.0, 0.0,
                false, false, false
        );
        assertTrue(dto.isValid());
    }

    @Test
    public void testValidDTOWithMaxLatitude() {
        StationDTO dto = new StationDTO(
                "PT", "Europe/Lisbon", "WET/GMT",
                "North Pole", 90.0, 0.0,
                false, false, false
        );
        assertTrue(dto.isValid());
    }

    @Test
    public void testValidDTOWithMinLongitude() {
        StationDTO dto = new StationDTO(
                "PT", "Europe/Lisbon", "WET/GMT",
                "West Station", 0.0, -180.0,
                false, false, false
        );
        assertTrue(dto.isValid());
    }

    @Test
    public void testValidDTOWithMaxLongitude() {
        StationDTO dto = new StationDTO(
                "PT", "Europe/Lisbon", "WET/GMT",
                "East Station", 0.0, 180.0,
                false, false, false
        );
        assertTrue(dto.isValid());
    }

    // ============ Invalid Station Name Tests ============

    @Test
    public void testInvalidDTOWithNullStationName() {
        StationDTO dto = new StationDTO(
                "PT", "Europe/Lisbon", "WET/GMT",
                null, 38.71387, -9.122271,
                true, true, false
        );
        assertFalse(dto.isValid());
    }

    @Test
    public void testInvalidDTOWithEmptyStationName() {
        StationDTO dto = new StationDTO(
                "PT", "Europe/Lisbon", "WET/GMT",
                "", 38.71387, -9.122271,
                true, true, false
        );
        assertFalse(dto.isValid());
    }

    @Test
    public void testInvalidDTOWithWhitespaceStationName() {
        StationDTO dto = new StationDTO(
                "PT", "Europe/Lisbon", "WET/GMT",
                "   ", 38.71387, -9.122271,
                true, true, false
        );
        assertFalse(dto.isValid());
    }

    // ============ Invalid Country Tests ============

    @Test
    public void testInvalidDTOWithNullCountry() {
        StationDTO dto = new StationDTO(
                null, "Europe/Lisbon", "WET/GMT",
                "Lisboa", 38.71387, -9.122271,
                true, true, false
        );
        assertFalse(dto.isValid());
    }

    @Test
    public void testInvalidDTOWithEmptyCountry() {
        StationDTO dto = new StationDTO(
                "", "Europe/Lisbon", "WET/GMT",
                "Lisboa", 38.71387, -9.122271,
                true, true, false
        );
        assertFalse(dto.isValid());
    }

    @Test
    public void testInvalidDTOWithWhitespaceCountry() {
        StationDTO dto = new StationDTO(
                "   ", "Europe/Lisbon", "WET/GMT",
                "Lisboa", 38.71387, -9.122271,
                true, true, false
        );
        assertFalse(dto.isValid());
    }

    // ============ Invalid TimeZoneGroup Tests ============

    @Test
    public void testInvalidDTOWithNullTimeZoneGroup() {
        StationDTO dto = new StationDTO(
                "PT", "Europe/Lisbon", null,
                "Lisboa", 38.71387, -9.122271,
                true, true, false
        );
        assertFalse(dto.isValid());
    }

    @Test
    public void testInvalidDTOWithEmptyTimeZoneGroup() {
        StationDTO dto = new StationDTO(
                "PT", "Europe/Lisbon", "",
                "Lisboa", 38.71387, -9.122271,
                true, true, false
        );
        assertFalse(dto.isValid());
    }

    @Test
    public void testInvalidDTOWithWhitespaceTimeZoneGroup() {
        StationDTO dto = new StationDTO(
                "PT", "Europe/Lisbon", "   ",
                "Lisboa", 38.71387, -9.122271,
                true, true, false
        );
        assertFalse(dto.isValid());
    }

    // ============ Invalid Latitude Tests ============

    @Test
    public void testInvalidDTOWithLatitudeBelowMin() {
        StationDTO dto = new StationDTO(
                "PT", "Europe/Lisbon", "WET/GMT",
                "Lisboa", -90.1, -9.122271,
                true, true, false
        );
        assertFalse(dto.isValid());
    }

    @Test
    public void testInvalidDTOWithLatitudeAboveMax() {
        StationDTO dto = new StationDTO(
                "PT", "Europe/Lisbon", "WET/GMT",
                "Lisboa", 90.1, -9.122271,
                true, true, false
        );
        assertFalse(dto.isValid());
    }

    @Test
    public void testInvalidDTOWithLatitudeFarBelowMin() {
        StationDTO dto = new StationDTO(
                "PT", "Europe/Lisbon", "WET/GMT",
                "Lisboa", -100.0, -9.122271,
                true, true, false
        );
        assertFalse(dto.isValid());
    }

    @Test
    public void testInvalidDTOWithLatitudeFarAboveMax() {
        StationDTO dto = new StationDTO(
                "PT", "Europe/Lisbon", "WET/GMT",
                "Lisboa", 100.0, -9.122271,
                true, true, false
        );
        assertFalse(dto.isValid());
    }

    // ============ Invalid Longitude Tests ============

    @Test
    public void testInvalidDTOWithLongitudeBelowMin() {
        StationDTO dto = new StationDTO(
                "PT", "Europe/Lisbon", "WET/GMT",
                "Lisboa", 38.71387, -180.1,
                true, true, false
        );
        assertFalse(dto.isValid());
    }

    @Test
    public void testInvalidDTOWithLongitudeAboveMax() {
        StationDTO dto = new StationDTO(
                "PT", "Europe/Lisbon", "WET/GMT",
                "Lisboa", 38.71387, 180.1,
                true, true, false
        );
        assertFalse(dto.isValid());
    }

    @Test
    public void testInvalidDTOWithLongitudeFarBelowMin() {
        StationDTO dto = new StationDTO(
                "PT", "Europe/Lisbon", "WET/GMT",
                "Lisboa", 38.71387, -200.0,
                true, true, false
        );
        assertFalse(dto.isValid());
    }

    @Test
    public void testInvalidDTOWithLongitudeFarAboveMax() {
        StationDTO dto = new StationDTO(
                "PT", "Europe/Lisbon", "WET/GMT",
                "Lisboa", 38.71387, 200.0,
                true, true, false
        );
        assertFalse(dto.isValid());
    }

    // ============ Validation Error Message Tests ============

    @Test
    public void testValidationErrorForNullStationName() {
        StationDTO dto = new StationDTO(
                "PT", "Europe/Lisbon", "WET/GMT",
                null, 38.71387, -9.122271,
                true, true, false
        );
        assertEquals("Empty or null station name", dto.getValidationError());
    }

    @Test
    public void testValidationErrorForEmptyStationName() {
        StationDTO dto = new StationDTO(
                "PT", "Europe/Lisbon", "WET/GMT",
                "", 38.71387, -9.122271,
                true, true, false
        );
        assertEquals("Empty or null station name", dto.getValidationError());
    }

    @Test
    public void testValidationErrorForNullCountry() {
        StationDTO dto = new StationDTO(
                null, "Europe/Lisbon", "WET/GMT",
                "Lisboa", 38.71387, -9.122271,
                true, true, false
        );
        assertEquals("Empty or null country", dto.getValidationError());
    }

    @Test
    public void testValidationErrorForEmptyCountry() {
        StationDTO dto = new StationDTO(
                "", "Europe/Lisbon", "WET/GMT",
                "Lisboa", 38.71387, -9.122271,
                true, true, false
        );
        assertEquals("Empty or null country", dto.getValidationError());
    }

    @Test
    public void testValidationErrorForNullTimeZoneGroup() {
        StationDTO dto = new StationDTO(
                "PT", "Europe/Lisbon", null,
                "Lisboa", 38.71387, -9.122271,
                true, true, false
        );
        assertEquals("Empty or null timeZoneGroup", dto.getValidationError());
    }

    @Test
    public void testValidationErrorForEmptyTimeZoneGroup() {
        StationDTO dto = new StationDTO(
                "PT", "Europe/Lisbon", "",
                "Lisboa", 38.71387, -9.122271,
                true, true, false
        );
        assertEquals("Empty or null timeZoneGroup", dto.getValidationError());
    }

    @Test
    public void testValidationErrorForInvalidLatitude() {
        StationDTO dto = new StationDTO(
                "PT", "Europe/Lisbon", "WET/GMT",
                "Lisboa", -100.0, -9.122271,
                true, true, false
        );
        assertEquals("Invalid latitude: -100.00000 (must be in [-90, 90])", dto.getValidationError());
    }

    @Test
    public void testValidationErrorForInvalidLongitude() {
        StationDTO dto = new StationDTO(
                "PT", "Europe/Lisbon", "WET/GMT",
                "Lisboa", 38.71387, -200.0,
                true, true, false
        );
        assertEquals("Invalid longitude: -200.00000 (must be in [-180, 180])", dto.getValidationError());
    }

    // ============ Edge Cases ============

    @Test
    public void testValidDTOWithAllFlagsTrue() {
        StationDTO dto = new StationDTO(
                "PT", "Europe/Lisbon", "WET/GMT",
                "Lisboa Airport", 38.71387, -9.122271,
                true, true, true
        );
        assertTrue(dto.isValid());
    }

    @Test
    public void testValidDTOWithAllFlagsFalse() {
        StationDTO dto = new StationDTO(
                "PT", "Europe/Lisbon", "WET/GMT",
                "Small Station", 38.71387, -9.122271,
                false, false, false
        );
        assertTrue(dto.isValid());
    }

    @Test
    public void testValidDTOWithZeroCoordinates() {
        StationDTO dto = new StationDTO(
                "PT", "Europe/Lisbon", "WET/GMT",
                "Equator Station", 0.0, 0.0,
                false, false, false
        );
        assertTrue(dto.isValid());
    }
}