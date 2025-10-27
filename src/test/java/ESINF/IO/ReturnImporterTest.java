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

/**
 * Testes ReturnImporter - verifica a importação e validação das devoluções do CSV.
 */
public class ReturnImporterTest {

    private ImportLogger logger;
    private Map<String, Item> itemCatalog;
    private ReturnImporter importer;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    /**
     * Cria um ficheiro CSV temporário para os testes.
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
     * Inicializa o catálogo de produtos e o importador antes de cada teste.
     */
    @Before
    public void setUp() {
        logger = new ImportLogger();
        itemCatalog = new HashMap<>();
        itemCatalog.put("SKU001", new Item("SKU001", "Banana", "food", "kg", 0.05, 0.18));
        itemCatalog.put("SKU002", new Item("SKU002", "Milk", "beverage", "L", 1.0, 1.0));
        itemCatalog.put("SKU003", new Item("SKU003", "Drill", "machinery", "units", 0.2, 1.5));

        importer = new ReturnImporter(logger, itemCatalog);
    }

    /**
     * Testa a importação de 2 devoluções válidas.
     * Verifica: parsing correto, datas de expiração opcionais (null quando ausente)
     */
    @Test
    public void loadReturns() throws IOException {
        String header = "returnId,SKU,qty,reason,timestamp,expiryDate";
        String line1 = "R100,SKU001,10,damaged,2025-10-20T10:00:00,2025-11-01";
        String line2 = "R101,SKU002,5,customer-remorse,2025-10-21T14:30:00,"; //Sem data de expiração

        String filePath = createTestFile(header, line1, line2);
        List<ReturnItem> returns = importer.loadReturns(filePath);

        assertEquals(2, returns.size());
        assertEquals(1, logger.getLogs().stream().filter(s -> s.contains("INFO")).count());
        assertEquals(0, logger.getLogs().stream().filter(s -> s.contains("ERROR")).count());

        // R101 é mais recente, vem primeiro (LIFO)
        ReturnItem r1 = returns.get(1); // R100
        assertEquals("R100", r1.getReturnId());
        assertEquals("SKU001", r1.getSku());
        assertEquals(10, r1.getQty());
        assertEquals(LocalDate.parse("2025-11-01"), r1.getExpiryDate());

        ReturnItem r2 = returns.get(0); // R101
        assertEquals("R101", r2.getReturnId());
        assertNull(r2.getExpiryDate()); //deve ser nula, já que não foi fornecida uma data
    }

    /**
     * Testa ordenação LIFO: timestamp DESC (mais recentes primeiro), returnId ASC (desempate).
     * Ordem esperada: NEW, MID, TIE-A, TIE-B, OLD.
     */
    @Test
    public void loadReturns_Success_CheckSorting() throws IOException {
        String header = "returnId,SKU,qty,reason,timestamp,expiryDate";
        String line1 = "R-MID,SKU001,1,damaged,2025-10-20T12:00:00,";
        String line2 = "R-NEW,SKU001,1,damaged,2025-10-21T12:00:00,";
        String line3 = "R-OLD,SKU001,1,damaged,2025-10-19T12:00:00,";
        // Os TIE vão ser desempatados pela ordem alfabética, ou seja, ASC
        String line4 = "R-TIE-B,SKU001,1,damaged,2025-10-20T12:00:00,";
        String line5 = "R-TIE-A,SKU001,1,damaged,2025-10-20T12:00:00,";

        String filePath = createTestFile(header, line1, line2, line3, line4, line5);
        List<ReturnItem> returns = importer.loadReturns(filePath);

        assertEquals(5, returns.size());
        assertEquals("R-NEW", returns.get(0).getReturnId());
        assertEquals("R-MID", returns.get(1).getReturnId());
        assertEquals("R-TIE-A", returns.get(2).getReturnId());
        assertEquals("R-TIE-B", returns.get(3).getReturnId());
        assertEquals("R-OLD", returns.get(4).getReturnId());
    }

    /**
     * Testa um ficheiro inexistente - deve lançar IOException.
     */
    @Test(expected = IOException.class)
    public void loadReturns_Error_FileNotFound() throws IOException {
        importer.loadReturns("nonexistentfile.csv");
    }

    /**
     * Testa se linhas vazias e com espaços são ignoradas (sem gerar erro).
     */
    @Test
    public void loadReturns_Skip_EmptyFileAndEmptyLines() throws IOException {
        String header = "returnId,SKU,qty,reason,timestamp,expiryDate";
        String line1 = "R100,SKU001,10,damaged,2025-10-20T10:00:00,2025-11-01";
        String emptyLine1 = "";
        String emptyLine2 = "   ";
        String line2 = "R101,SKU002,5,customer-remorse,2025-10-21T14:30:00,";

        String filePath = createTestFile(header, line1, emptyLine1, emptyLine2, line2);
        List<ReturnItem> returns = importer.loadReturns(filePath);

        assertEquals(2, returns.size()); //não conta as linhas vazias
        assertEquals("R101", returns.get(0).getReturnId()); //é mais recente
        assertEquals("R100", returns.get(1).getReturnId());
        assertEquals(0, logger.getLogs().stream().filter(s -> s.contains("ERROR")).count());
    }

    /**
     * Testa validação: rejeita linhas com menos de 5 colunas.
     * Erro deve identificar ficheiro e linha.
     */
    @Test
    public void loadReturns_Error_InvalidColumnCount() throws IOException {
        String header = "returnId,SKU,qty,reason,timestamp,expiryDate";
        String line1 = "R100,SKU001,10,damaged";

        String filePath = createTestFile(header, line1);
        List<ReturnItem> returns = importer.loadReturns(filePath);

        assertEquals(0, returns.size());
        String log = logger.getLogs().get(0);
        assertTrue(log.contains("ERROR"));
        assertTrue(log.contains("[returns.csv:2]"));
        assertTrue(log.contains("Invalid number of columns"));
    }

    /**
     * Testa validação: rejeita campos obrigatórios vazios (returnId, SKU, reason).
     * Cada linha inválida gera erro específico com número de linha.
     */
    @Test
    public void loadReturns_Error_MissingRequiredFields() throws IOException {
        String header = "returnId,SKU,qty,reason,timestamp,expiryDate";
        String line1 = ",SKU001,10,damaged,2025-10-20T10:00:00,";
        String line2 = "R101,,10,damaged,2025-10-20T10:00:00,";
        String line3 = "R102,SKU001,10,,2025-10-20T10:00:00,";

        String filePath = createTestFile(header, line1, line2, line3);
        List<ReturnItem> returns = importer.loadReturns(filePath);

        assertEquals(0, returns.size());
        List<String> logs = logger.getLogs();
        assertEquals(3, logs.stream().filter(s -> s.contains("ERROR") && s.contains("Missing required fields")).count());
        assertTrue(logs.get(0).contains("[returns.csv:2]"));
        assertTrue(logs.get(1).contains("[returns.csv:3]")); // apenas indica a linha do erro
        assertTrue(logs.get(2).contains("[returns.csv:4]"));
    }

    /**
     * Testa validação: rejeita SKU não existente no catálogo.
     * Erro identifica o SKU inválido.
     */
    @Test
    public void loadReturns_Error_UnknownSKU() throws IOException {
        String header = "returnId,SKU,qty,reason,timestamp,expiryDate";
        String line1 = "R100,SKU999,10,damaged,2025-10-20T10:00:00,"; //Não pertence ao catalogo

        String filePath = createTestFile(header, line1);
        List<ReturnItem> returns = importer.loadReturns(filePath);

        assertEquals(0, returns.size());
        String log = logger.getLogs().get(0);
        assertTrue(log.contains("ERROR"));
        assertTrue(log.contains("[returns.csv:2]"));
        assertTrue(log.contains("Unknown SKU=SKU999"));
    }

    /**
     * Testa validação: rejeita timestamp em formato inválido
     */
    @Test
    public void loadReturns_Error_InvalidTimestamp() throws IOException {
        String header = "returnId,SKU,qty,reason,timestamp,expiryDate";
        String line1 = "R100,SKU001,10,damaged,20-10-2025 10:00,";

        String filePath = createTestFile(header, line1);
        List<ReturnItem> returns = importer.loadReturns(filePath);

        assertEquals(0, returns.size());
        String log = logger.getLogs().get(0);
        assertTrue(log.contains("ERROR"));
        assertTrue(log.contains("Invalid timestamp=20-10-2025 10:00"));
    }

    /**
     * Testa que data de validade inválida gera WARNING (não ERROR).
     * Item é carregado com expiryDate=null.
     */
    @Test
    public void loadReturns_Warning_InvalidExpiryDate() throws IOException {
        String header = "returnId,SKU,qty,reason,timestamp,expiryDate";
        String line1 = "R100,SKU001,10,customer-remorse,2025-10-20T10:00:00,invalid-date";

        String filePath = createTestFile(header, line1);
        List<ReturnItem> returns = importer.loadReturns(filePath);

        assertEquals(1, returns.size());
        assertNull(returns.get(0).getExpiryDate());
        assertTrue(logger.getLogs().stream().anyMatch(s -> s.contains("WARNING")));
    }

    /**
     * Testa importação parcial: carrega 2 válidos, rejeita 1 inválido.
     * R002 tem SKU desconhecido.
     */
    @Test
    public void loadReturns_Partial_MixedValidAndInvalid() throws IOException {
        String header = "returnId,SKU,qty,reason,timestamp,expiryDate";
        String line1 = "R001,SKU001,10,damaged,2025-10-20T10:00:00,";
        String line2 = "R002,INVALID_SKU,5,expired,2025-10-20T11:00:00,";
        String line3 = "R003,SKU003,8,customer-remorse,2025-10-20T12:00:00,";

        String filePath = createTestFile(header, line1, line2, line3);
        List<ReturnItem> returns = importer.loadReturns(filePath);

        assertEquals(2, returns.size());
        assertTrue(returns.stream().anyMatch(r -> r.getReturnId().equals("R001")));
        assertTrue(returns.stream().anyMatch(r -> r.getReturnId().equals("R003")));
        assertFalse(returns.stream().anyMatch(r -> r.getReturnId().equals("R002")));
        assertTrue(logger.getLogs().stream().anyMatch(s -> s.contains("ERROR")));
    }

    /**
     * Testa validação: rejeita quantidade invalida (numerica).
     * A quantidade não pode ser numérica
     */
    @Test
    public void loadReturns_Error_InvalidQuantityFormat() throws IOException {
        String header = "returnId,SKU,qty,reason,timestamp,expiryDate";
        String line1 = "R100,SKU001,abc,damaged,2025-10-20T10:00:00,";

        String filePath = createTestFile(header, line1);
        List<ReturnItem> returns = importer.loadReturns(filePath);

        assertEquals(0, returns.size());
        assertTrue(logger.getLogs().stream().anyMatch(s -> s.contains("ERROR")));
    }

    /**
     * Testa que ficheiro vazio (só header) não gera erro, retorna uma lista vazia.
     */
    @Test
    public void loadReturns_EmptyFile_NoError() throws IOException {
        String header = "returnId,SKU,qty,reason,timestamp,expiryDate";

        String filePath = createTestFile(header);
        List<ReturnItem> returns = importer.loadReturns(filePath);

        assertEquals(0, returns.size());
        assertTrue(logger.getLogs().stream().anyMatch(s -> s.contains("INFO") && s.contains("Loaded 0")));
    }

    /**
     * Testa todas as razões de devolução válidas.
     * customer-remorse, damaged, expired, cycle-count
     */
    @Test
    public void loadReturns_AllReasons() throws IOException {
        String header = "returnId,SKU,qty,reason,timestamp,expiryDate";
        String line1 = "R001,SKU001,10,customer-remorse,2025-10-20T10:00:00,";
        String line2 = "R002,SKU001,5,damaged,2025-10-20T11:00:00,";
        String line3 = "R003,SKU001,8,expired,2025-10-20T12:00:00,";
        String line4 = "R004,SKU001,3,cycle-count,2025-10-20T13:00:00,";

        String filePath = createTestFile(header, line1, line2, line3, line4);
        List<ReturnItem> returns = importer.loadReturns(filePath);

        assertEquals(4, returns.size());
        assertTrue(returns.stream().anyMatch(r -> r.getReason().equals("customer-remorse")));
        assertTrue(returns.stream().anyMatch(r -> r.getReason().equals("damaged")));
        assertTrue(returns.stream().anyMatch(r -> r.getReason().equals("expired")));
        assertTrue(returns.stream().anyMatch(r -> r.getReason().equals("cycle-count")));
    }

    /**
     * Testa que campo expiryDate ausente (5 colunas) é tratado como null.
     * Sem ter parametro
     */
    @Test
    public void loadReturns_NoExpiryDateColumn() throws IOException {
        String header = "returnId,SKU,qty,reason,timestamp";
        String line1 = "R100,SKU001,10,damaged,2025-10-20T10:00:00";

        String filePath = createTestFile(header, line1);
        List<ReturnItem> returns = importer.loadReturns(filePath);

        assertEquals(1, returns.size());
        assertNull(returns.get(0).getExpiryDate());
    }

    /**
     * Testa catálogo vazio - as devoluções são rejeitadas
     * Deve rejeitar as devoluções por o produto não pertencer ao catálogo
     */
    @Test
    public void loadReturns_EmptyCatalog() throws IOException {
        String header = "returnId,SKU,qty,reason,timestamp,expiryDate";
        String line1 = "R100,SKU001,10,damaged,2025-10-20T10:00:00,";

        String filePath = createTestFile(header, line1);
        ReturnImporter importerEmpty = new ReturnImporter(logger, new HashMap<>());
        List<ReturnItem> returns = importerEmpty.loadReturns(filePath);

        assertEquals(0, returns.size());
        assertTrue(logger.getLogs().stream().anyMatch(s -> s.contains("ERROR") && s.contains("Unknown SKU")));
    }
}