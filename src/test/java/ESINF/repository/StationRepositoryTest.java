package ESINF.repository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Unit tests for StationRepository
 * Tests repository operations and index management
 */
public class StationRepositoryTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private StationRepository repository;

    @Before
    public void setUp() {
        repository = new StationRepository();
    }

    // ============ LoadStations Tests ============

    @Test
    public void testLoadStationsSuccess() throws IOException {
        File csvFile = createValidCSVFile();
        StationRepository.ImportResult result = repository.loadStations(csvFile.getAbsolutePath());
        assertEquals(2, result.validCount());
    }

    @Test
    public void testLoadStationsInvalidCount() throws IOException {
        File csvFile = createCSVWithErrors();
        StationRepository.ImportResult result = repository.loadStations(csvFile.getAbsolutePath());
        assertEquals(1, result.invalidCount());
    }

    @Test
    public void testLoadStationsSetsLoaded() throws IOException {
        File csvFile = createValidCSVFile();
        repository.loadStations(csvFile.getAbsolutePath());
        assertTrue(repository.isLoaded());
    }

    @Test
    public void testLoadStationsWithErrors() throws IOException {
        File csvFile = createCSVWithErrors();
        StationRepository.ImportResult result = repository.loadStations(csvFile.getAbsolutePath());
        assertTrue(result.hasErrors());
    }

    @Test
    public void testLoadStationsWithoutErrors() throws IOException {
        File csvFile = createValidCSVFile();
        StationRepository.ImportResult result = repository.loadStations(csvFile.getAbsolutePath());
        assertFalse(result.hasErrors());
    }

    @Test
    public void testLoadStationsErrorsList() throws IOException {
        File csvFile = createCSVWithErrors();
        StationRepository.ImportResult result = repository.loadStations(csvFile.getAbsolutePath());
        assertFalse(result.errors().isEmpty());
    }

    @Test
    public void testLoadStationsSummary() throws IOException {
        File csvFile = createValidCSVFile();
        StationRepository.ImportResult result = repository.loadStations(csvFile.getAbsolutePath());
        assertNotNull(result.summary());
    }

    // ============ GetIndex Tests ============

    @Test
    public void testGetIndexAfterLoad() throws IOException {
        File csvFile = createValidCSVFile();
        repository.loadStations(csvFile.getAbsolutePath());
        assertNotNull(repository.getIndex());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetIndexBeforeLoad() {
        repository.getIndex();
    }

    @Test
    public void testGetIndexReturnsCorrectStationCount() throws IOException {
        File csvFile = createValidCSVFile();
        repository.loadStations(csvFile.getAbsolutePath());
        assertEquals(2, repository.getIndex().getTotalStations());
    }

    // ============ IsLoaded Tests ============

    @Test
    public void testIsLoadedInitially() {
        assertFalse(repository.isLoaded());
    }

    @Test
    public void testIsLoadedAfterLoad() throws IOException {
        File csvFile = createValidCSVFile();
        repository.loadStations(csvFile.getAbsolutePath());
        assertTrue(repository.isLoaded());
    }

    @Test
    public void testIsLoadedAfterClear() throws IOException {
        File csvFile = createValidCSVFile();
        repository.loadStations(csvFile.getAbsolutePath());
        repository.clear();
        assertFalse(repository.isLoaded());
    }

    // ============ Clear Tests ============

    @Test
    public void testClearSetsLoadedToFalse() throws IOException {
        File csvFile = createValidCSVFile();
        repository.loadStations(csvFile.getAbsolutePath());
        repository.clear();
        assertFalse(repository.isLoaded());
    }

    @Test(expected = IllegalStateException.class)
    public void testClearMakesIndexUnavailable() throws IOException {
        File csvFile = createValidCSVFile();
        repository.loadStations(csvFile.getAbsolutePath());
        repository.clear();
        repository.getIndex();
    }

    @Test
    public void testClearOnEmptyRepository() {
        repository.clear();
        assertFalse(repository.isLoaded());
    }

    // ============ ImportResult Tests ============

    @Test
    public void testImportResultValidCount() throws IOException {
        File csvFile = createValidCSVFile();
        StationRepository.ImportResult result = repository.loadStations(csvFile.getAbsolutePath());
        assertEquals(2, result.validCount());
    }

    @Test
    public void testImportResultInvalidCountZero() throws IOException {
        File csvFile = createValidCSVFile();
        StationRepository.ImportResult result = repository.loadStations(csvFile.getAbsolutePath());
        assertEquals(0, result.invalidCount());
    }

    @Test
    public void testImportResultInvalidCountNonZero() throws IOException {
        File csvFile = createCSVWithErrors();
        StationRepository.ImportResult result = repository.loadStations(csvFile.getAbsolutePath());
        assertEquals(1, result.invalidCount());
    }

    @Test
    public void testImportResultHasErrorsTrue() throws IOException {
        File csvFile = createCSVWithErrors();
        StationRepository.ImportResult result = repository.loadStations(csvFile.getAbsolutePath());
        assertTrue(result.hasErrors());
    }

    @Test
    public void testImportResultHasErrorsFalse() throws IOException {
        File csvFile = createValidCSVFile();
        StationRepository.ImportResult result = repository.loadStations(csvFile.getAbsolutePath());
        assertFalse(result.hasErrors());
    }

    @Test
    public void testImportResultErrorsNotNull() throws IOException {
        File csvFile = createValidCSVFile();
        StationRepository.ImportResult result = repository.loadStations(csvFile.getAbsolutePath());
        assertNotNull(result.errors());
    }

    @Test
    public void testImportResultSummaryNotNull() throws IOException {
        File csvFile = createValidCSVFile();
        StationRepository.ImportResult result = repository.loadStations(csvFile.getAbsolutePath());
        assertNotNull(result.summary());
    }

    // ============ Reload Tests ============

    @Test
    public void testReloadStations() throws IOException {
        File csvFile1 = createValidCSVFile();
        repository.loadStations(csvFile1.getAbsolutePath());

        File csvFile2 = createAnotherValidCSVFile();
        StationRepository.ImportResult result = repository.loadStations(csvFile2.getAbsolutePath());

        assertEquals(3, result.validCount());
    }

    @Test
    public void testReloadStationsStillLoaded() throws IOException {
        File csvFile1 = createValidCSVFile();
        repository.loadStations(csvFile1.getAbsolutePath());

        File csvFile2 = createAnotherValidCSVFile();
        repository.loadStations(csvFile2.getAbsolutePath());

        assertTrue(repository.isLoaded());
    }

    // ============ Error Handling Tests ============

    @Test(expected = IOException.class)
    public void testLoadNonExistentFile() throws IOException {
        repository.loadStations("nonexistent.csv");
    }

    @Test(expected = IOException.class)
    public void testLoadEmptyFile() throws IOException {
        File csvFile = tempFolder.newFile("empty.csv");
        repository.loadStations(csvFile.getAbsolutePath());
    }

    @Test
    public void testLoadStationsWithAllInvalid() throws IOException {
        File csvFile = createCSVWithAllInvalid();
        StationRepository.ImportResult result = repository.loadStations(csvFile.getAbsolutePath());
        assertEquals(0, result.validCount());
    }

    @Test
    public void testLoadStationsWithAllInvalidStillSetsLoaded() throws IOException {
        File csvFile = createCSVWithAllInvalid();
        repository.loadStations(csvFile.getAbsolutePath());
        assertTrue(repository.isLoaded());
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

    private File createCSVWithErrors() throws IOException {
        File file = tempFolder.newFile("stations_with_errors.csv");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport\n");
            writer.write("PT,Europe/Lisbon,WET/GMT,Lisboa Santa Apolónia,38.71387,-9.122271,True,True,False\n");
            writer.write("PT,Europe/Lisbon,WET/GMT,Invalid,-91.0,-9.122271,True,True,False\n");
        }
        return file;
    }

    private File createAnotherValidCSVFile() throws IOException {
        File file = tempFolder.newFile("stations2.csv");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport\n");
            writer.write("ES,Europe/Madrid,CET,Madrid Atocha,40.40677,-3.68848,True,True,False\n");
            writer.write("ES,Europe/Madrid,CET,Madrid Chamartín,40.47259,-3.68159,True,True,False\n");
            writer.write("FR,Europe/Paris,CET,Paris Nord,48.88053,2.35508,True,True,False\n");
        }
        return file;
    }

    private File createCSVWithAllInvalid() throws IOException {
        File file = tempFolder.newFile("stations_all_invalid.csv");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport\n");
            writer.write("PT,Europe/Lisbon,WET/GMT,Invalid1,-91.0,-9.122271,True,True,False\n");
            writer.write("PT,Europe/Lisbon,WET/GMT,Invalid2,91.0,-9.122271,True,True,False\n");
        }
        return file;
    }
}