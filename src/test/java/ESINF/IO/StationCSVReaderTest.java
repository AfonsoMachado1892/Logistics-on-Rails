package ESINF.IO;

import ESINF.model.Station;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for StationCSVReader
 * Tests CSV parsing, validation, and error handling
 */
public class StationCSVReaderTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private StationCSVReader reader;

    @Before
    public void setUp() {
        reader = new StationCSVReader();
    }

    // ============ Successful Parsing Tests ============

    @Test
    public void testReadValidSingleStation() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,Lisboa Santa Apolónia,38.71387,-9.122271,True,True,False"
        );

        List<Station> stations = reader.readStations(csvFile.getAbsolutePath());
        assertEquals(1, stations.size());
    }

    @Test
    public void testReadValidMultipleStations() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,Lisboa Santa Apolónia,38.71387,-9.122271,True,True,False",
                "PT,Europe/Lisbon,WET/GMT,Porto Campanhã,41.14961,-8.58507,True,True,False"
        );

        List<Station> stations = reader.readStations(csvFile.getAbsolutePath());
        assertEquals(2, stations.size());
    }

    @Test
    public void testReadStationName() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,Lisboa Santa Apolónia,38.71387,-9.122271,True,True,False"
        );

        List<Station> stations = reader.readStations(csvFile.getAbsolutePath());
        assertEquals("Lisboa Santa Apolónia", stations.get(0).getName());
    }

    @Test
    public void testReadStationLatitude() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,Lisboa Santa Apolónia,38.71387,-9.122271,True,True,False"
        );

        List<Station> stations = reader.readStations(csvFile.getAbsolutePath());
        assertEquals(38.71387, stations.get(0).getLatitude(), 0.00001);
    }

    @Test
    public void testReadStationLongitude() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,Lisboa Santa Apolónia,38.71387,-9.122271,True,True,False"
        );

        List<Station> stations = reader.readStations(csvFile.getAbsolutePath());
        assertEquals(-9.122271, stations.get(0).getLongitude(), 0.00001);
    }

    @Test
    public void testReadStationCountry() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,Lisboa Santa Apolónia,38.71387,-9.122271,True,True,False"
        );

        List<Station> stations = reader.readStations(csvFile.getAbsolutePath());
        assertEquals("PT", stations.get(0).getCountry());
    }

    @Test
    public void testReadStationTimeZone() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,Lisboa Santa Apolónia,38.71387,-9.122271,True,True,False"
        );

        List<Station> stations = reader.readStations(csvFile.getAbsolutePath());
        assertEquals("Europe/Lisbon", stations.get(0).getTimeZone());
    }

    @Test
    public void testReadStationTimeZoneGroup() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,Lisboa Santa Apolónia,38.71387,-9.122271,True,True,False"
        );

        List<Station> stations = reader.readStations(csvFile.getAbsolutePath());
        assertEquals("WET/GMT", stations.get(0).getTimeZoneGroup());
    }

    @Test
    public void testReadStationIsCity() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,Lisboa Santa Apolónia,38.71387,-9.122271,True,True,False"
        );

        List<Station> stations = reader.readStations(csvFile.getAbsolutePath());
        assertTrue(stations.get(0).isCity());
    }

    @Test
    public void testReadStationIsMainStation() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,Lisboa Santa Apolónia,38.71387,-9.122271,True,True,False"
        );

        List<Station> stations = reader.readStations(csvFile.getAbsolutePath());
        assertTrue(stations.get(0).isMainStation());
    }

    @Test
    public void testReadStationIsAirport() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,Lisboa Santa Apolónia,38.71387,-9.122271,True,True,False"
        );

        List<Station> stations = reader.readStations(csvFile.getAbsolutePath());
        assertFalse(stations.get(0).isAirport());
    }

    @Test
    public void testReadBooleanFalseLowercase() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,Lisboa,38.71387,-9.122271,false,false,false"
        );

        List<Station> stations = reader.readStations(csvFile.getAbsolutePath());
        assertFalse(stations.get(0).isCity());
    }

    @Test
    public void testReadBooleanTrueMixedCase() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,Lisboa,38.71387,-9.122271,TrUe,false,false"
        );

        List<Station> stations = reader.readStations(csvFile.getAbsolutePath());
        assertTrue(stations.get(0).isCity());
    }

    // ============ Validation Error Tests ============

    @Test
    public void testInvalidLatitudeBelowMin() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,Lisboa,-91.0,-9.122271,True,True,False"
        );

        reader.readStations(csvFile.getAbsolutePath());
        assertEquals(1, reader.getInvalidCount());
    }

    @Test
    public void testInvalidLatitudeAboveMax() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,Lisboa,91.0,-9.122271,True,True,False"
        );

        reader.readStations(csvFile.getAbsolutePath());
        assertEquals(1, reader.getInvalidCount());
    }

    @Test
    public void testInvalidLongitudeBelowMin() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,Lisboa,38.71387,-181.0,True,True,False"
        );

        reader.readStations(csvFile.getAbsolutePath());
        assertEquals(1, reader.getInvalidCount());
    }

    @Test
    public void testInvalidLongitudeAboveMax() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,Lisboa,38.71387,181.0,True,True,False"
        );

        reader.readStations(csvFile.getAbsolutePath());
        assertEquals(1, reader.getInvalidCount());
    }

    @Test
    public void testEmptyStationName() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,,38.71387,-9.122271,True,True,False"
        );

        reader.readStations(csvFile.getAbsolutePath());
        assertEquals(1, reader.getInvalidCount());
    }

    @Test
    public void testEmptyCountry() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                ",Europe/Lisbon,WET/GMT,Lisboa,38.71387,-9.122271,True,True,False"
        );

        reader.readStations(csvFile.getAbsolutePath());
        assertEquals(1, reader.getInvalidCount());
    }

    @Test
    public void testEmptyTimeZoneGroup() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,,Lisboa,38.71387,-9.122271,True,True,False"
        );

        reader.readStations(csvFile.getAbsolutePath());
        assertEquals(1, reader.getInvalidCount());
    }

    @Test
    public void testMixedValidAndInvalid() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,Lisboa,38.71387,-9.122271,True,True,False",
                "PT,Europe/Lisbon,WET/GMT,Invalid,-91.0,-9.122271,True,True,False"
        );

        List<Station> stations = reader.readStations(csvFile.getAbsolutePath());
        assertEquals(1, stations.size());
    }

    @Test
    public void testMixedValidAndInvalidCount() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,Lisboa,38.71387,-9.122271,True,True,False",
                "PT,Europe/Lisbon,WET/GMT,Invalid,-91.0,-9.122271,True,True,False"
        );

        reader.readStations(csvFile.getAbsolutePath());
        assertEquals(1, reader.getInvalidCount());
    }

    // ============ Parse Error Tests ============

    @Test
    public void testInvalidNumberOfFields() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,Lisboa,38.71387"
        );

        reader.readStations(csvFile.getAbsolutePath());
        assertEquals(1, reader.getInvalidCount());
    }

    @Test
    public void testInvalidLatitudeFormat() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,Lisboa,invalid,-9.122271,True,True,False"
        );

        reader.readStations(csvFile.getAbsolutePath());
        assertEquals(1, reader.getInvalidCount());
    }

    @Test
    public void testInvalidLongitudeFormat() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,Lisboa,38.71387,invalid,True,True,False"
        );

        reader.readStations(csvFile.getAbsolutePath());
        assertEquals(1, reader.getInvalidCount());
    }

    // ============ Empty/Special Case Tests ============

    @Test
    public void testEmptyLines() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "",
                "PT,Europe/Lisbon,WET/GMT,Lisboa,38.71387,-9.122271,True,True,False"
        );

        List<Station> stations = reader.readStations(csvFile.getAbsolutePath());
        assertEquals(1, stations.size());
    }

    @Test
    public void testQuotedFields() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "\"PT\",\"Europe/Lisbon\",\"WET/GMT\",\"Lisboa Santa Apolónia\",38.71387,-9.122271,True,True,False"
        );

        List<Station> stations = reader.readStations(csvFile.getAbsolutePath());
        assertEquals("Lisboa Santa Apolónia", stations.get(0).getName());
    }

    @Test
    public void testWhitespaceInFields() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                " PT , Europe/Lisbon , WET/GMT , Lisboa ,38.71387,-9.122271,True,True,False"
        );

        List<Station> stations = reader.readStations(csvFile.getAbsolutePath());
        assertEquals("Lisboa", stations.get(0).getName());
    }

    // ============ Counter and Error Reporting Tests ============

    @Test
    public void testGetValidCount() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,Lisboa,38.71387,-9.122271,True,True,False",
                "PT,Europe/Lisbon,WET/GMT,Porto,41.14961,-8.58507,True,True,False"
        );

        reader.readStations(csvFile.getAbsolutePath());
        assertEquals(2, reader.getValidCount());
    }

    @Test
    public void testGetInvalidCount() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,Invalid1,-91.0,-9.122271,True,True,False",
                "PT,Europe/Lisbon,WET/GMT,Invalid2,91.0,-9.122271,True,True,False"
        );

        reader.readStations(csvFile.getAbsolutePath());
        assertEquals(2, reader.getInvalidCount());
    }

    @Test
    public void testHasErrorsTrue() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,Invalid,-91.0,-9.122271,True,True,False"
        );

        reader.readStations(csvFile.getAbsolutePath());
        assertTrue(reader.hasErrors());
    }

    @Test
    public void testHasErrorsFalse() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,Lisboa,38.71387,-9.122271,True,True,False"
        );

        reader.readStations(csvFile.getAbsolutePath());
        assertFalse(reader.hasErrors());
    }

    @Test
    public void testGetErrorsNotEmpty() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,Invalid,-91.0,-9.122271,True,True,False"
        );

        reader.readStations(csvFile.getAbsolutePath());
        assertFalse(reader.getErrors().isEmpty());
    }

    @Test
    public void testGetErrorsEmpty() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,Lisboa,38.71387,-9.122271,True,True,False"
        );

        reader.readStations(csvFile.getAbsolutePath());
        assertTrue(reader.getErrors().isEmpty());
    }

    @Test(expected = IOException.class)
    public void testEmptyFile() throws IOException {
        File csvFile = tempFolder.newFile("empty.csv");
        reader.readStations(csvFile.getAbsolutePath());
    }

    @Test(expected = IOException.class)
    public void testNonExistentFile() throws IOException {
        reader.readStations("nonexistent.csv");
    }

    // ============ Boundary Value Tests ============

    @Test
    public void testMinLatitude() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,South Pole,-90.0,0.0,False,False,False"
        );

        List<Station> stations = reader.readStations(csvFile.getAbsolutePath());
        assertEquals(1, stations.size());
    }

    @Test
    public void testMaxLatitude() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,North Pole,90.0,0.0,False,False,False"
        );

        List<Station> stations = reader.readStations(csvFile.getAbsolutePath());
        assertEquals(1, stations.size());
    }

    @Test
    public void testMinLongitude() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,West Point,0.0,-180.0,False,False,False"
        );

        List<Station> stations = reader.readStations(csvFile.getAbsolutePath());
        assertEquals(1, stations.size());
    }

    @Test
    public void testMaxLongitude() throws IOException {
        File csvFile = createCSVFile(
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport",
                "PT,Europe/Lisbon,WET/GMT,East Point,0.0,180.0,False,False,False"
        );

        List<Station> stations = reader.readStations(csvFile.getAbsolutePath());
        assertEquals(1, stations.size());
    }

    // ============ Helper Method ============

    private File createCSVFile(String... lines) throws IOException {
        File file = tempFolder.newFile("stations.csv");
        try (FileWriter writer = new FileWriter(file)) {
            for (String line : lines) {
                writer.write(line + "\n");
            }
        }
        return file;
    }
}