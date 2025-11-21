package ESINF.controller;

import ESINF.model.Station;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Unit tests for StationQueryController
 * Tests controller operations and query coordination
 */
public class StationQueryControllerTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private StationQueryController controller;

    @Before
    public void setUp() {
        controller = new StationQueryController();
    }

    // ============ LoadStations Tests ============

    @Test
    public void testLoadStationsSuccess() throws IOException {
        File csvFile = createValidCSVFile();
        controller.loadStations(csvFile.getAbsolutePath());
        assertTrue(controller.isDataLoaded());
    }

    @Test
    public void testLoadStationsReturnsResult() throws IOException {
        File csvFile = createValidCSVFile();
        assertNotNull(controller.loadStations(csvFile.getAbsolutePath()));
    }

    @Test(expected = IOException.class)
    public void testLoadStationsNonExistentFile() throws IOException {
        controller.loadStations("nonexistent.csv");
    }

    // ============ IsDataLoaded Tests ============

    @Test
    public void testIsDataLoadedInitially() {
        assertFalse(controller.isDataLoaded());
    }

    @Test
    public void testIsDataLoadedAfterLoad() throws IOException {
        File csvFile = createValidCSVFile();
        controller.loadStations(csvFile.getAbsolutePath());
        assertTrue(controller.isDataLoaded());
    }

    @Test
    public void testIsDataLoadedAfterClear() throws IOException {
        File csvFile = createValidCSVFile();
        controller.loadStations(csvFile.getAbsolutePath());
        controller.clearData();
        assertFalse(controller.isDataLoaded());
    }

    // ============ ClearData Tests ============

    @Test
    public void testClearData() throws IOException {
        File csvFile = createValidCSVFile();
        controller.loadStations(csvFile.getAbsolutePath());
        controller.clearData();
        assertFalse(controller.isDataLoaded());
    }

    @Test
    public void testClearDataOnEmpty() {
        controller.clearData();
        assertFalse(controller.isDataLoaded());
    }

    // ============ QueryByTimeZoneGroup Tests ============

    @Test
    public void testQueryByTimeZoneGroup() throws IOException {
        File csvFile = createValidCSVFile();
        controller.loadStations(csvFile.getAbsolutePath());
        List<Station> results = controller.queryByTimeZoneGroup("WET/GMT");
        assertEquals(2, results.size());
    }

    @Test
    public void testQueryByTimeZoneGroupNonExistent() throws IOException {
        File csvFile = createValidCSVFile();
        controller.loadStations(csvFile.getAbsolutePath());
        List<Station> results = controller.queryByTimeZoneGroup("NONEXISTENT");
        assertTrue(results.isEmpty());
    }

    @Test(expected = IllegalStateException.class)
    public void testQueryByTimeZoneGroupNotLoaded() {
        controller.queryByTimeZoneGroup("WET/GMT");
    }

    // ============ QueryByTimeZoneGroupAndCountry Tests ============

    @Test
    public void testQueryByTimeZoneGroupAndCountry() throws IOException {
        File csvFile = createValidCSVFile();
        controller.loadStations(csvFile.getAbsolutePath());
        List<Station> results = controller.queryByTimeZoneGroupAndCountry("WET/GMT", "PT");
        assertEquals(2, results.size());
    }

    @Test
    public void testQueryByTimeZoneGroupAndCountryNonExistent() throws IOException {
        File csvFile = createValidCSVFile();
        controller.loadStations(csvFile.getAbsolutePath());
        List<Station> results = controller.queryByTimeZoneGroupAndCountry("WET/GMT", "ES");
        assertTrue(results.isEmpty());
    }

    @Test(expected = IllegalStateException.class)
    public void testQueryByTimeZoneGroupAndCountryNotLoaded() {
        controller.queryByTimeZoneGroupAndCountry("WET/GMT", "PT");
    }

    // ============ QueryByTimeZoneWindow Tests ============

    @Test
    public void testQueryByTimeZoneWindowSingle() throws IOException {
        File csvFile = createValidCSVFile();
        controller.loadStations(csvFile.getAbsolutePath());
        List<Station> results = controller.queryByTimeZoneWindow("WET/GMT");
        assertEquals(2, results.size());
    }

    @Test
    public void testQueryByTimeZoneWindowMultiple() throws IOException {
        File csvFile = createMixedCSVFile();
        controller.loadStations(csvFile.getAbsolutePath());
        List<Station> results = controller.queryByTimeZoneWindow("WET/GMT", "CET");
        assertEquals(3, results.size());
    }

    @Test
    public void testQueryByTimeZoneWindowEmpty() throws IOException {
        File csvFile = createValidCSVFile();
        controller.loadStations(csvFile.getAbsolutePath());
        List<Station> results = controller.queryByTimeZoneWindow("NONEXISTENT");
        assertTrue(results.isEmpty());
    }

    @Test(expected = IllegalStateException.class)
    public void testQueryByTimeZoneWindowNotLoaded() {
        controller.queryByTimeZoneWindow("WET/GMT");
    }

    // ============ QueryByLatitudeRange Tests ============

    @Test
    public void testQueryByLatitudeRange() throws IOException {
        File csvFile = createValidCSVFile();
        controller.loadStations(csvFile.getAbsolutePath());
        List<Station> results = controller.queryByLatitudeRange(38.0, 42.0);
        assertEquals(2, results.size());
    }

    @Test
    public void testQueryByLatitudeRangeEmpty() throws IOException {
        File csvFile = createValidCSVFile();
        controller.loadStations(csvFile.getAbsolutePath());
        List<Station> results = controller.queryByLatitudeRange(50.0, 60.0);
        assertTrue(results.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQueryByLatitudeRangeInvalidMin() throws IOException {
        File csvFile = createValidCSVFile();
        controller.loadStations(csvFile.getAbsolutePath());
        controller.queryByLatitudeRange(-91.0, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQueryByLatitudeRangeInvalidMax() throws IOException {
        File csvFile = createValidCSVFile();
        controller.loadStations(csvFile.getAbsolutePath());
        controller.queryByLatitudeRange(0.0, 91.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQueryByLatitudeRangeMinGreaterThanMax() throws IOException {
        File csvFile = createValidCSVFile();
        controller.loadStations(csvFile.getAbsolutePath());
        controller.queryByLatitudeRange(50.0, 40.0);
    }

    @Test(expected = IllegalStateException.class)
    public void testQueryByLatitudeRangeNotLoaded() {
        controller.queryByLatitudeRange(38.0, 42.0);
    }

    // ============ QueryByLongitudeRange Tests ============

    @Test
    public void testQueryByLongitudeRange() throws IOException {
        File csvFile = createValidCSVFile();
        controller.loadStations(csvFile.getAbsolutePath());
        List<Station> results = controller.queryByLongitudeRange(-10.0, -8.0);
        assertEquals(2, results.size());
    }

    @Test
    public void testQueryByLongitudeRangeEmpty() throws IOException {
        File csvFile = createValidCSVFile();
        controller.loadStations(csvFile.getAbsolutePath());
        List<Station> results = controller.queryByLongitudeRange(0.0, 10.0);
        assertTrue(results.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQueryByLongitudeRangeInvalidMin() throws IOException {
        File csvFile = createValidCSVFile();
        controller.loadStations(csvFile.getAbsolutePath());
        controller.queryByLongitudeRange(-181.0, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQueryByLongitudeRangeInvalidMax() throws IOException {
        File csvFile = createValidCSVFile();
        controller.loadStations(csvFile.getAbsolutePath());
        controller.queryByLongitudeRange(0.0, 181.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQueryByLongitudeRangeMinGreaterThanMax() throws IOException {
        File csvFile = createValidCSVFile();
        controller.loadStations(csvFile.getAbsolutePath());
        controller.queryByLongitudeRange(10.0, -10.0);
    }

    @Test(expected = IllegalStateException.class)
    public void testQueryByLongitudeRangeNotLoaded() {
        controller.queryByLongitudeRange(-10.0, -8.0);
    }

    // ============ GetAllTimeZoneGroups Tests ============

    @Test
    public void testGetAllTimeZoneGroups() throws IOException {
        File csvFile = createValidCSVFile();
        controller.loadStations(csvFile.getAbsolutePath());
        Set<String> groups = controller.getAllTimeZoneGroups();
        assertEquals(1, groups.size());
    }

    @Test
    public void testGetAllTimeZoneGroupsContainsWET() throws IOException {
        File csvFile = createValidCSVFile();
        controller.loadStations(csvFile.getAbsolutePath());
        Set<String> groups = controller.getAllTimeZoneGroups();
        assertTrue(groups.contains("WET/GMT"));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetAllTimeZoneGroupsNotLoaded() {
        controller.getAllTimeZoneGroups();
    }

    // ============ GetCountriesInTimeZoneGroup Tests ============

    @Test
    public void testGetCountriesInTimeZoneGroup() throws IOException {
        File csvFile = createValidCSVFile();
        controller.loadStations(csvFile.getAbsolutePath());
        Set<String> countries = controller.getCountriesInTimeZoneGroup("WET/GMT");
        assertEquals(1, countries.size());
    }

    @Test
    public void testGetCountriesInTimeZoneGroupContainsPT() throws IOException {
        File csvFile = createValidCSVFile();
        controller.loadStations(csvFile.getAbsolutePath());
        Set<String> countries = controller.getCountriesInTimeZoneGroup("WET/GMT");
        assertTrue(countries.contains("PT"));
    }

    @Test
    public void testGetCountriesInTimeZoneGroupNonExistent() throws IOException {
        File csvFile = createValidCSVFile();
        controller.loadStations(csvFile.getAbsolutePath());
        Set<String> countries = controller.getCountriesInTimeZoneGroup("NONEXISTENT");
        assertTrue(countries.isEmpty());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetCountriesInTimeZoneGroupNotLoaded() {
        controller.getCountriesInTimeZoneGroup("WET/GMT");
    }

    // ============ GetStatistics Tests ============

    @Test
    public void testGetStatistics() throws IOException {
        File csvFile = createValidCSVFile();
        controller.loadStations(csvFile.getAbsolutePath());
        assertNotNull(controller.getStatistics());
    }

    @Test
    public void testGetStatisticsTotalStations() throws IOException {
        File csvFile = createValidCSVFile();
        controller.loadStations(csvFile.getAbsolutePath());
        assertEquals(2, controller.getStatistics().totalStations());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetStatisticsNotLoaded() {
        controller.getStatistics();
    }

    // ============ GetComplexityAnalysis Tests ============

    @Test
    public void testGetComplexityAnalysis() {
        assertNotNull(controller.getComplexityAnalysis());
    }

    @Test
    public void testGetComplexityAnalysisNotEmpty() {
        assertFalse(controller.getComplexityAnalysis().isEmpty());
    }

    // ============ GetIndex Tests ============

    @Test
    public void testGetIndex() throws IOException {
        File csvFile = createValidCSVFile();
        controller.loadStations(csvFile.getAbsolutePath());
        assertNotNull(controller.getIndex());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetIndexNotLoaded() {
        controller.getIndex();
    }

    // ============ Helper Methods ============

    private File createValidCSVFile() throws IOException {
        File file = tempFolder.newFile("stations.csv");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport\n");
            writer.write("PT,Europe/Lisbon,WET/GMT,Lisboa Santa Apolónia,38.71387,-9.122271,True,True,False\n");
            writer.write("PT,Europe/Lisbon,WET/GMT,Porto Campanhã,41.14961,-8.58507,True,True,False\n");
        }
        return file;
    }

    private File createMixedCSVFile() throws IOException {
        File file = tempFolder.newFile("stations_mixed.csv");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport\n");
            writer.write("PT,Europe/Lisbon,WET/GMT,Lisboa Santa Apolónia,38.71387,-9.122271,True,True,False\n");
            writer.write("PT,Europe/Lisbon,WET/GMT,Porto Campanhã,41.14961,-8.58507,True,True,False\n");
            writer.write("ES,Europe/Madrid,CET,Madrid Atocha,40.40677,-3.68848,True,True,False\n");
        }
        return file;
    }
}