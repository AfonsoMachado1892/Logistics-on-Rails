package LAPR3.controller;

import ESINF.model.Bay;
import ESINF.model.Warehouse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.*;

public class ReturnsControllerTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private DataController dataController;
    private ReturnsController returnsController;
    private File itemsFile;
    private File returnsFile;
    private File baysFile;

    /**
     * Helper method to create a temporary CSV file with the given content.
     */
    private File createTestFile(String fileName, String... lines) throws IOException {
        File file = tempFolder.newFile(fileName);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        }
        return file;
    }

    @Before
    public void setUp() throws Exception {
        // Create dummy CSV files for setup
        itemsFile = createTestFile("items.csv",
                "SKU,name,category,unit,volume,unitWeight",
                "SKU001,Product A,Cat1,unit,1.0,1.0",
                "SKU002,Product B,Cat2,unit,2.0,2.0"
        );
        baysFile = createTestFile("bays.csv",
                "warehouseId,aisle,bay,capacityBoxes",
                "WH001,1,1,10"
        );
        returnsFile = createTestFile("returns.csv",
                "returnId,SKU,qty,reason,timestamp,expiryDate",
                "R001,SKU001,5,customer-remorse,2025-10-21T10:00:00,",
                "R002,SKU002,3,damaged,2025-10-22T11:00:00,"
        );

        // Initialize controllers
        dataController = new DataController();
        returnsController = new ReturnsController(dataController);

        // Pre-load necessary data
        dataController.loadItems(itemsFile.getAbsolutePath());
        dataController.loadBays(baysFile.getAbsolutePath());
        dataController.setCurrentWarehouse("WH001");
    }

    @Test
    public void loadReturns() throws IOException {
        assertFalse(returnsController.hasReturns());
        returnsController.loadReturns(returnsFile.getAbsolutePath());

        assertTrue(returnsController.hasReturns());
        assertEquals(2, returnsController.getReturnsCount());
    }

    @Test(expected = IllegalStateException.class)
    public void loadReturns_throwsException_whenItemsNotLoaded() throws IOException {
        // Create a new controller without preloaded items
        DataController freshDataController = new DataController();
        ReturnsController freshReturnsController = new ReturnsController(freshDataController);

        freshReturnsController.loadReturns(returnsFile.getAbsolutePath());
    }

    @Test
    public void processReturns() throws IOException {
        returnsController.loadReturns(returnsFile.getAbsolutePath());
        returnsController.processReturns();

        // Verify the outcome in the warehouse
        Warehouse warehouse = dataController.getCurrentWarehouse();
        Bay bay = warehouse.getAisle(1).getBay(1);

        // R001 should be restocked, R002 should be discarded
        assertEquals(1, bay.getBoxes().size());
        assertEquals("RET-R001", bay.getBoxes().peek().getBoxId());

        // Verify that an audit log was created
        File auditLog = new File("Logs/returns_audit.log");
        assertTrue(auditLog.exists());
        List<String> logLines = Files.readAllLines(auditLog.toPath());
        assertTrue(logLines.stream().anyMatch(line -> line.contains("Restocked") && line.contains("R001")));
        assertTrue(logLines.stream().anyMatch(line -> line.contains("Discarded") && line.contains("R002")));
    }


    @Test(expected = IllegalStateException.class)
    public void processReturns_throwsException_whenReturnsNotLoaded() {
        returnsController.processReturns(); // Should fail
    }

    @Test(expected = IllegalStateException.class)
    public void processReturns_throwsException_whenWarehouseNotSelected() throws IOException {
        returnsController.loadReturns(returnsFile.getAbsolutePath());

        // Create a new controller and don't select a warehouse
        DataController freshDataController = new DataController();
        freshDataController.loadItems(itemsFile.getAbsolutePath());
        ReturnsController freshReturnsController = new ReturnsController(freshDataController);
        freshReturnsController.loadReturns(returnsFile.getAbsolutePath());

        freshReturnsController.processReturns(); // Should fail
    }

    @Test
    public void getReturns() throws IOException {
        assertNull("Returns list should be null initially", returnsController.getReturns());
        returnsController.loadReturns(returnsFile.getAbsolutePath());
        assertNotNull("Returns list should not be null after loading", returnsController.getReturns());
        assertEquals(2, returnsController.getReturns().size());
    }

    @Test
    public void getReturnsCount() throws IOException {
        assertEquals(0, returnsController.getReturnsCount());
        returnsController.loadReturns(returnsFile.getAbsolutePath());
        assertEquals(2, returnsController.getReturnsCount());
    }

    @Test
    public void hasReturns() throws IOException {
        assertFalse("Should not have returns initially", returnsController.hasReturns());
        returnsController.loadReturns(returnsFile.getAbsolutePath());
        assertTrue("Should have returns after loading", returnsController.hasReturns());
    }

    @Test
    public void hasReturns_returnsFalseForEmptyList() throws IOException {
        File emptyReturnsFile = createTestFile("empty_returns.csv", "returnId,SKU,qty,reason,timestamp,expiryDate");
        returnsController.loadReturns(emptyReturnsFile.getAbsolutePath());
        assertFalse("Should return false when the loaded list is empty", returnsController.hasReturns());
    }

    @Test
    public void printReturnsSummary() throws IOException {
        // Keep a reference to the original System.out
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            // Redirect System.out to capture the output
            System.setOut(new PrintStream(outContent));

            // Test when there are no returns (should print nothing)
            returnsController.printReturnsSummary();
            assertEquals("", outContent.toString());

            // Load returns (this will also print log info to outContent)
            returnsController.loadReturns(returnsFile.getAbsolutePath());

            // Reset the stream to only capture the output of the next method call
            outContent.reset();
            returnsController.printReturnsSummary();

            String expectedOutput = "  • To Restock: 1" + System.lineSeparator() +
                    "  • To Discard: 1" + System.lineSeparator();
            assertEquals(expectedOutput, outContent.toString());
        } finally {
            // ALWAYS reset System.out to its original state
            System.setOut(originalOut);
        }
    }
}
