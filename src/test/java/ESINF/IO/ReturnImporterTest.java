package ESINF.IO;

import ESINF.model.Item;
import ESINF.model.ReturnItem;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ReturnImporterTest {

    private ImportLogger logger;
    private Map<String, Item> itemCatalog;
    private ReturnImporter importer;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    /**
     * Helper method to create a temporary CSV file with the given content.
     * @param lines The lines to write to the file.
     * @return The absolute path to the created file.
     * @throws IOException If an I/O error occurs.
     */
    private String createTestFile(String... lines) throws IOException {
        File file = tempFolder.newFile("returns.csv");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        }
        return file.getAbsolutePath();
    }

    /**
     * Sets up the common dependencies for each test.
     */
    @Before
    public void setUp() {
        logger = new ImportLogger();
        itemCatalog = new HashMap<>();

        // Populate the item catalog with some valid items for testing
        itemCatalog.put("SKU001", new Item("SKU001", "Banana", "food", "kg", 0.05, 0.18));
        itemCatalog.put("SKU002", new Item("SKU002", "Milk", "beverage", "L", 1.0, 1.0));
        itemCatalog.put("SKU003", new Item("SKU003", "Drill", "machinery", "units", 0.2, 1.5));

        importer = new ReturnImporter(logger, itemCatalog);
    }

    @Test
    public void loadReturns() throws IOException {
        String header = "returnId,SKU,qty,reason,timestamp,expiryDate";
        String line1 = "R100,SKU001,10,damaged,2025-10-20T10:00:00,2025-11-01";
        String line2 = "R101,SKU002,5,customer-remorse,2025-10-21T14:30:00,"; // Sem data de expiração

        String filePath = createTestFile(header, line1, line2);
        List<ReturnItem> returns = importer.loadReturns(filePath);

        assertEquals(2, returns.size());
        assertEquals(1, logger.getLogs().stream().filter(s -> s.contains("INFO")).count());
        assertEquals(0, logger.getLogs().stream().filter(s -> s.contains("ERROR")).count());
        assertEquals(0, logger.getLogs().stream().filter(s -> s.contains("WARNING")).count());

        // Returns are sorted newest first
        ReturnItem r1 = returns.get(1); // R100 (older)
        assertEquals("R100", r1.getReturnId());
        assertEquals("SKU001", r1.getSku());
        assertEquals(10, r1.getQty());
        assertEquals("damaged", r1.getReason());
        assertEquals(LocalDateTime.parse("2025-10-20T10:00:00"), r1.getTimestamp());
        assertEquals(LocalDate.parse("2025-11-01"), r1.getExpiryDate());

        ReturnItem r2 = returns.get(0); // R101 (newer)
        assertEquals("R101", r2.getReturnId());
        assertEquals("SKU002", r2.getSku());
        assertNull(r2.getExpiryDate()); // Check that null expiry is handled
    }

    @Test
    public void loadReturns_Success_CheckSorting() throws IOException {
        String header = "returnId,SKU,qty,reason,timestamp,expiryDate";
        String line1 = "R-MID,SKU001,1,damaged,2025-10-20T12:00:00,";
        String line2 = "R-NEW,SKU001,1,damaged,2025-10-21T12:00:00,";
        String line3 = "R-OLD,SKU001,1,damaged,2025-10-19T12:00:00,";
        // Tie-breaker lines (same timestamp)
        String line4 = "R-TIE-B,SKU001,1,damaged,2025-10-20T12:00:00,";
        String line5 = "R-TIE-A,SKU001,1,damaged,2025-10-20T12:00:00,";


        String filePath = createTestFile(header, line1, line2, line3, line4, line5);
        List<ReturnItem> returns = importer.loadReturns(filePath);

        assertEquals(5, returns.size());

        // Check the sorted order
        // 1. R-NEW (newest timestamp)
        // 2. R-MID (same timestamp as TIE-A/B, but loaded first, tie-break)
        // 3. R-TIE-A (same timestamp, sorted by ID ASC)
        // 4. R-TIE-B (same timestamp, sorted by ID ASC)
        // 5. R-OLD (oldest timestamp)
        assertEquals("R-NEW", returns.get(0).getReturnId());
        assertEquals("R-MID", returns.get(1).getReturnId());
        assertEquals("R-TIE-A", returns.get(2).getReturnId());
        assertEquals("R-TIE-B", returns.get(3).getReturnId());
        assertEquals("R-OLD", returns.get(4).getReturnId());
    }


    @Test(expected = IOException.class)
    public void loadReturns_Error_FileNotFound() throws IOException {
        importer.loadReturns("nonexistentfile.csv");
    }

    @Test
    public void loadReturns_Skip_EmptyFileAndEmptyLines() throws IOException {
        String header = "returnId,SKU,qty,reason,timestamp,expiryDate";
        String line1 = "R100,SKU001,10,damaged,2025-10-20T10:00:00,2025-11-01";
        String emptyLine1 = "";
        String emptyLine2 = "   "; // Whitespace only
        String line2 = "R101,SKU002,5,customer-remorse,2025-10-21T14:30:00,";

        String filePath = createTestFile(header, line1, emptyLine1, emptyLine2, line2);
        List<ReturnItem> returns = importer.loadReturns(filePath);

        assertEquals(2, returns.size()); // Empty lines are skipped
        assertEquals("R101", returns.get(0).getReturnId()); // R101 is newer
        assertEquals("R100", returns.get(1).getReturnId());
        assertEquals(0, logger.getLogs().stream().filter(s -> s.contains("ERROR")).count());
    }

    @Test
    public void loadReturns_Error_InvalidColumnCount() throws IOException {
        String header = "returnId,SKU,qty,reason,timestamp,expiryDate";
        String line1 = "R100,SKU001,10,damaged"; // Only 4 columns

        String filePath = createTestFile(header, line1);
        List<ReturnItem> returns = importer.loadReturns(filePath);

        assertEquals(0, returns.size());
        String log = logger.getLogs().get(0);
        assertTrue(log.contains("ERROR"));
        assertTrue(log.contains("[returns.csv:2]"));
        assertTrue(log.contains("Invalid number of columns"));
    }

    @Test
    public void loadReturns_Error_MissingRequiredFields() throws IOException {
        String header = "returnId,SKU,qty,reason,timestamp,expiryDate";
        String line1 = ",SKU001,10,damaged,2025-10-20T10:00:00,";   // Missing returnId
        String line2 = "R101,,10,damaged,2025-10-20T10:00:00,";   // Missing SKU
        String line3 = "R102,SKU001,10,,2025-10-20T10:00:00,";   // Missing reason

        String filePath = createTestFile(header, line1, line2, line3);
        List<ReturnItem> returns = importer.loadReturns(filePath);

        assertEquals(0, returns.size());
        List<String> logs = logger.getLogs();
        assertEquals(3, logs.stream().filter(s -> s.contains("ERROR") && s.contains("Missing required fields")).count());
        assertTrue(logs.get(0).contains("[returns.csv:2]"));
        assertTrue(logs.get(1).contains("[returns.csv:3]"));
        assertTrue(logs.get(2).contains("[returns.csv:4]"));
    }

    @Test
    public void loadReturns_Error_UnknownSKU() throws IOException {
        String header = "returnId,SKU,qty,reason,timestamp,expiryDate";
        String line1 = "R100,SKU999,10,damaged,2025-10-20T10:00:00,"; // SKU999 not in catalog

        String filePath = createTestFile(header, line1);
        List<ReturnItem> returns = importer.loadReturns(filePath);

        assertEquals(0, returns.size());
        String log = logger.getLogs().get(0);
        assertTrue(log.contains("ERROR"));
        assertTrue(log.contains("[returns.csv:2]"));
        assertTrue(log.contains("Unknown SKU=SKU999"));
    }

    @Test
    public void loadReturns_Error_InvalidTimestamp() throws IOException {
        String header = "returnId,SKU,qty,reason,timestamp,expiryDate";
        String line1 = "R100,SKU001,10,damaged,20-10-2025 10:00,"; // Invalid format

        String filePath = createTestFile(header, line1);
        List<ReturnItem> returns = importer.loadReturns(filePath);

        assertEquals(0, returns.size());
        String log = logger.getLogs().get(0);
        assertTrue(log.contains("ERROR"));
        assertTrue(log.contains("[returns.csv:2]"));
        assertTrue(log.contains("Invalid timestamp=20-10-2025 10:00"));
    }
}