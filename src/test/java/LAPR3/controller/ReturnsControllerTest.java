package LAPR3.controller;

import ESINF.model.Bay;
import ESINF.model.Warehouse;
import ESINF.repository.RailwayNetworkRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Testes para a classe ReturnsController.
 * Garante o correto carregamento, processamento (USEI05) e resumo das devoluções.
 */
public class ReturnsControllerTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private DataController dataController;
    private ReturnsController returnsController;
    private File itemsFile;
    private File returnsFile;
    private File baysFile;
    private File auditLogFile;

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

    /**
     * Configuração inicial antes de cada teste.
     * Cria ficheiros de dados de teste e inicializa os controladores.
     */
    @Before
    public void setUp() throws Exception {
        itemsFile = createTestFile("items.csv",
                "SKU,name,category,unit,volume,unitWeight",
                "SKU001,Product A,Cat1,unit,1.0,1.0", // Para ser restocked (R001)
                "SKU002,Product B,Cat2,unit,2.0,2.0"  // Para ser discarded (R002)
        );
        baysFile = createTestFile("bays.csv",
                "warehouseId,aisle,bay,capacityBoxes",
                "WH001,1,1,10", // Baía com capacidade
                "WH001,1,2,0"   // Baía sem capacidade
        );
        returnsFile = createTestFile("returns.csv",
                "returnId,SKU,qty,reason,timestamp,expiryDate",
                "R001,SKU001,5,customer-remorse,2025-10-21T10:00:00,", // Restockable (customer-remorse)
                "R002,SKU002,3,damaged,2025-10-22T11:00:00," // Discardable (damaged)
        );

        dataController = new DataController(new RailwayNetworkRepository());
        returnsController = new ReturnsController(dataController);

        dataController.loadItems(itemsFile.getAbsolutePath());
        dataController.loadBays(baysFile.getAbsolutePath());
        dataController.setCurrentWarehouse("WH001");

        auditLogFile = new File("Logs/returns_audit.log");
        if (auditLogFile.exists()) {
            auditLogFile.delete();
        }
    }

    /**
     * Testa o carregamento de devoluções.
     * Verifica se o estado do controlador é atualizado corretamente após o carregamento.
     */
    @Test
    public void loadReturns() throws IOException {
        assertFalse("There should be no returns before loading", returnsController.hasReturns());
        returnsController.loadReturns(returnsFile.getAbsolutePath());

        assertTrue("There must exist returns after loading", returnsController.hasReturns());
        assertEquals("The number of returns should be 2", 2, returnsController.getReturnsCount());
    }

    /**
     * Testa se o carregamento de devoluções lança uma exceção IllegalStateException
     * quando o catálogo de Items não foi previamente carregado
     */
    @Test(expected = IllegalStateException.class)
    public void loadReturns_throwsException_whenItemsNotLoaded() throws IOException {
        // Cria um novo controlador sem items pré-carregados
        DataController freshDataController = new DataController(new RailwayNetworkRepository());
        ReturnsController freshReturnsController = new ReturnsController(freshDataController);

        freshReturnsController.loadReturns(returnsFile.getAbsolutePath());
    }

    /**
     * Testa o processamento normal de devoluções.
     * Verifica:
     * O resultado no armazém (restock)
     * A criação do log de auditoria.
     */
    @Test
    public void processReturns() throws IOException {
        // Pré-condição: Carregar devoluções
        returnsController.loadReturns(returnsFile.getAbsolutePath());

        // Processar devoluções (R001 restock, R002 discard)
        returnsController.processReturns();

        Warehouse warehouse = dataController.getCurrentWarehouse();
        Bay bay = warehouse.getAisle(1).getBay(1);

        // R001 (customer-remorse) deve ser devolvida:
        assertEquals(1, bay.getBoxes().size());
        assertEquals( "RET-R001", bay.getBoxes().peek().getBoxId());

        assertTrue( auditLogFile.exists());
        List<String> logLines = Files.readAllLines(auditLogFile.toPath());

        assertTrue(logLines.stream().anyMatch(line -> line.contains("Restocked") && line.contains("R001")));
        assertTrue(logLines.stream().anyMatch(line -> line.contains("Discarded") && line.contains("R002")));
    }

    /**
     * Testa se o processamento de devoluções limpa a lista interna de devoluções
     * para prevenir ser processado várias vezes
     */
    @Test
    public void processReturns_ClearsReturnsList() throws IOException {
        returnsController.loadReturns(returnsFile.getAbsolutePath());
        assertTrue(returnsController.hasReturns());

        returnsController.processReturns();

        assertFalse(returnsController.hasReturns());
        assertEquals(0, returnsController.getReturnsCount());
    }

    /**
     * Testa se o processamento lança uma exceção IllegalStateException quando
     * as devoluções não foram carregadas.
     */
    @Test(expected = IllegalStateException.class)
    public void processReturns_throwsException_whenReturnsNotLoaded() {
        // Tentar processar sem carregar
        returnsController.processReturns();
    }

    /**
     * Testa se o processamento lança uma exceção IllegalStateException quando
     * nenhum armazém está selecionado.
     */
    @Test(expected = IllegalStateException.class)
    public void processReturns_throwsException_whenWarehouseNotSelected() throws IOException {
        returnsController.loadReturns(returnsFile.getAbsolutePath());

        // Cria um novo controller sem armazém selecionado
        DataController freshDataController = new DataController(new RailwayNetworkRepository());
        freshDataController.loadItems(itemsFile.getAbsolutePath());

        // Carregar Bays, mas não selecionar armazém
        freshDataController.loadBays(baysFile.getAbsolutePath());

        ReturnsController freshReturnsController = new ReturnsController(freshDataController);
        freshReturnsController.loadReturns(returnsFile.getAbsolutePath());

        freshReturnsController.processReturns();
    }

    /**
     * Testa o metodo getReturns para verificar se retorna a lista correta
     * antes e depois do carregamento.
     */
    @Test
    public void getReturns() throws IOException {
        assertNull(returnsController.getReturns());
        returnsController.loadReturns(returnsFile.getAbsolutePath());
        assertNotNull(returnsController.getReturns());
        assertEquals(2, returnsController.getReturns().size());
    }

    /**
     * Testa o metodo getReturnsCount para verificar a contagem correta de devoluções.
     */
    @Test
    public void getReturnsCount() throws IOException {
        assertEquals(0, returnsController.getReturnsCount());
        returnsController.loadReturns(returnsFile.getAbsolutePath());
        assertEquals(2, returnsController.getReturnsCount());
    }

    /**
     * Testa o metodo hasReturns para verificar o estado de devoluções carregadas.
     */
    @Test
    public void hasReturns() throws IOException {
        assertFalse( returnsController.hasReturns());
        returnsController.loadReturns(returnsFile.getAbsolutePath());
        assertTrue( returnsController.hasReturns());
    }

    /**
     * Testa o metodo hasReturns para o caso em que o ficheiro CSV é carregado,
     * mas não contém registos de devolução.
     */
    @Test
    public void hasReturns_returnsFalseForEmptyList() throws IOException {
        File emptyReturnsFile = createTestFile("empty_returns.csv", "returnId,SKU,qty,reason,timestamp,expiryDate");
        returnsController.loadReturns(emptyReturnsFile.getAbsolutePath());
        assertFalse( returnsController.hasReturns());
    }

    /**
     * Testa o metodo printReturnsSummary, verificando a contagem correta
     * para restock e descarte, e a saída para a consola.
     */
    @Test
    public void printReturnsSummary() throws IOException {
        //  System.out captura a saída
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outContent));

            // Sem devoluções (não deve imprimir nada)
            returnsController.printReturnsSummary();
            assertEquals( "", outContent.toString());

            // Carrega devoluções (R001 restock, R002 discard)
            returnsController.loadReturns(returnsFile.getAbsolutePath());

            // Reset do stream para isolar a saída da função printReturnsSummary
            outContent.reset();
            returnsController.printReturnsSummary();

            // Verifica a saída esperada (1 para Restock, 1 para Discard)
            String expectedOutput = "  • To Restock: 1" + System.lineSeparator() +
                    "  • To Discard: 1" + System.lineSeparator();
            assertEquals( expectedOutput, outContent.toString());
        } finally {
            // Repõe System.out original
            System.setOut(originalOut);
        }
    }
}