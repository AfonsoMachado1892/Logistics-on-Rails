package ESINF.IO; // Deve estar no mesmo pacote que DataImporter

import ESINF.model.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Classe de teste JUnit 4 para o DataImporter.
 *
 * Foca-se exclusivamente na validação dos dados carregados (Items, Warehouses, Wagons),
 * ignorando a verificação de logs.
 *
 * Utiliza ficheiros temporários para criar cenários de teste isolados e determinísticos.
 */
public class DataImporterTest {

    private DataImporter importer;
    private final List<Path> tempFiles = new ArrayList<>();

    // --- Logger Stub ---

    /**
     * Implementação "Stub" (do-nothing) do ImportLogger.
     * É necessária para o construtor do DataImporter, mas não será usada
     * para asserções, conforme solicitado.
     */
    private static class StubLogger extends ImportLogger {
        @Override public void info(String file, int line, String msg) { /* Não faz nada */ }
        @Override public void error(String file, int line, String msg) { /* Não faz nada */ }
        public void warn(String file, int line, String msg) { /* Não faz nada */ }
    }

    // --- Configuração (Setup) e Limpeza (Teardown) ---

    /**
     * Configura o ambiente de teste antes de cada @Test.
     * Inicializa um novo importer com o StubLogger e limpa a lista
     * de ficheiros temporários.
     */
    @Before
    public void setUp() {
        // Inicializa o importer com o logger "stub"
        importer = new DataImporter(new StubLogger());
        tempFiles.clear();
    }

    /**
     * Limpa os ficheiros temporários criados após cada @Test.
     * Garante que não ficam ficheiros .csv "órfãos" no sistema.
     */
    @After
    public void tearDown() throws IOException {
        for (Path file : tempFiles) {
            Files.deleteIfExists(file);
        }
    }

    /**
     * Método helper para criar um ficheiro CSV temporário com conteúdo.
     * @param prefix O prefixo do nome do ficheiro (ex: "items")
     * @param content O conteúdo CSV a ser escrito no ficheiro
     * @return O caminho (String) absoluto para o ficheiro criado
     * @throws IOException Se ocorrer um erro de I/O
     */
    private String createTempFile(String prefix, String content) throws IOException {
        Path file = Files.createTempFile(prefix, ".csv");
        Files.write(file, content.getBytes());
        tempFiles.add(file);
        return file.toAbsolutePath().toString();
    }

    // =============================================================
    // TESTES DE `loadItems`
    // =============================================================

    /**
     * Testa o carregamento bem-sucedido de vários itens válidos.
     * Verifica se o tamanho do mapa está correto e se as propriedades
     * de um item específico foram lidas corretamente.
     */
    @Test
    public void testLoadItems_Success() throws IOException {
        String content = "sku,name,category,unit,volume,weight\n" +
                "SKU001,Maçã,Fruta,kg,0.1,0.2\n" +
                "SKU002,Pera,Fruta,kg,0.15,0.25\n" +
                "SKU003,Pão,Padaria,un,1.5,0.5";

        String filePath = createTempFile("items_ok", content);
        importer.loadItems(filePath);

        Map<String, Item> items = importer.getItems();
        assertEquals(3, items.size());

        Item item2 = items.get("SKU002");
        assertNotNull(item2);
        assertEquals("Pera", item2.getName());
        assertEquals("Fruta", item2.getCategory());
        assertEquals("kg", item2.getUnit());
        assertEquals(0.15, item2.getVolume(), 0.001);
        assertEquals(0.25, item2.getUnitWeight(), 0.001);
    }

    /**
     * Testa o carregamento de um ficheiro de itens que contém
     * várias linhas inválidas. Apenas os itens válidos devem ser carregados.
     */
    @Test
    public void testLoadItems_WithErrors() throws IOException {
        String content = "sku,name,category,unit,volume,weight\n" +
                "SKU001,Maçã,Fruta,kg,0.1,0.2\n" +            // Válido
                ",Pão,Padaria,un,1.5,0.5\n" +               // Erro: SKU vazio
                "SKU003,Tomate,Vegetal\n" +                  // Erro: Colunas insuficientes
                "SKU004,Água,Bebida,L,1.0,not_a_number\n" + // Erro: NumberFormat
                "SKU005,Sumo,Bebida,L,1.0,-1.0\n";          // Erro: Peso negativo

        String filePath = createTempFile("items_err", content);
        importer.loadItems(filePath);

        // Apenas o SKU001 deve ser carregado
        assertEquals(1, importer.getItems().size());
        assertTrue(importer.getItems().containsKey("SKU001"));
        assertFalse(importer.getItems().containsKey("SKU004"));
        assertFalse(importer.getItems().containsKey("SKU005"));
    }

    /**
     * Testa que um ficheiro de itens contendo apenas o cabeçalho
     * ou linhas em branco é processado sem erros e resulta num mapa vazio.
     */
    @Test
    public void testLoadItems_EmptyAndBlankLines() throws IOException {
        String content = "sku,name,category,unit,volume,weight\n" +
                "\n" +
                "   \n"; // Linha com espaços

        String filePath = createTempFile("items_empty", content);
        importer.loadItems(filePath);

        assertEquals(0, importer.getItems().size());
    }

    /**
     * Testa o carregamento de itens com nomes contendo caracteres especiais
     * (acentos, 'ç') e espaços.
     */
    @Test
    public void testLoadItems_SpecialCharacters() throws IOException {
        String content = "sku,name,category,unit,volume,weight\n" +
                "SKU-C,Café Solúvel,Mercearia,kg,0.5,0.3\n" +
                "SKU-P,Pão de Ló,Padaria,un,2.0,0.8";

        String filePath = createTempFile("items_special", content);
        importer.loadItems(filePath);

        assertEquals(2, importer.getItems().size());

        Item cafe = importer.getItems().get("SKU-C");
        assertNotNull(cafe);
        assertEquals("Café Solúvel", cafe.getName());

        Item pao = importer.getItems().get("SKU-P");
        assertNotNull(pao);
        assertEquals("Pão de Ló", pao.getName());
    }

    /**
     * Testa o comportamento de SKUs duplicados. O último item
     * lido com o mesmo SKU deve sobrepor-se aos anteriores.
     */
    @Test
    public void testLoadItems_DuplicateSKU() throws IOException {
        String content = "sku,name,category,unit,volume,weight\n" +
                "SKU001,Maçã,Fruta,kg,0.1,0.2\n" +
                "SKU001,Pera,Fruta,kg,0.15,0.25"; // Mesmo SKU

        String filePath = createTempFile("items_dup", content);
        importer.loadItems(filePath);

        // Apenas um item deve existir
        assertEquals(1, importer.getItems().size());

        // Deve ser o último item (Pera)
        Item item = importer.getItems().get("SKU001");
        assertNotNull(item);
        assertEquals("Pera", item.getName());
        assertEquals(0.15, item.getVolume(), 0.001);
    }

    // =============================================================
    // TESTES DE `loadBays`
    // =============================================================

    /**
     * Testa o carregamento bem-sucedido de bays,
     * incluindo a agregação em múltiplos armazéns e múltiplas aisles.
     */
    @Test
    public void testLoadBays_SuccessAndAggregation() throws IOException {
        String content = "WarehouseID,Aisle,Bay,Capacity\n" +
                "W1,1,101,15\n" +
                "W1,1,102,12\n" + // Mesma Aisle
                "W1,2,201,10\n" + // Aisle diferente
                "W2,1,101,20";   // Warehouse diferente

        String filePath = createTempFile("bays_ok", content);
        importer.loadBays(filePath);

        Map<String, Warehouse> warehouses = importer.getWarehouses();
        assertEquals(2, warehouses.size());
        assertTrue(warehouses.containsKey("W1"));
        assertTrue(warehouses.containsKey("W2"));

        // Verificar W1
        Warehouse w1 = warehouses.get("W1");
        assertNotNull(w1);
        assertEquals(2, w1.getAisles().size()); // Aisle 1 e Aisle 2
        assertNotNull(w1.getAisle(1));
        assertNotNull(w1.getAisle(2));
        assertNotNull(w1.getAisle(1).getBay(101));
        assertNotNull(w1.getAisle(1).getBay(102));
        assertEquals(15, w1.getAisle(1).getBay(101).getCapacityBoxes());
        assertEquals(12, w1.getAisle(1).getBay(102).getCapacityBoxes());

        // Verificar W2
        Warehouse w2 = warehouses.get("W2");
        assertNotNull(w2);
        assertEquals(1, w2.getAisles().size()); // Aisle 1
        assertNotNull(w2.getAisle(1).getBay(101));
        assertEquals(20, w2.getAisle(1).getBay(101).getCapacityBoxes());
    }

    /**
     * Testa o carregamento de bays usando o delimitador alternativo
     * ponto e vírgula (;), conforme definido em `line.split("[,;]")`.
     */
    @Test
    public void testLoadBays_AlternateDelimiter() throws IOException {
        String content = "WarehouseID;Aisle;Bay;Capacity\n" +
                "W1;1;101;15\n" +
                "W1;1;102;12";

        String filePath = createTempFile("bays_semi", content);
        importer.loadBays(filePath);

        assertEquals(1, importer.getWarehouses().size());
        Warehouse w1 = importer.getWarehouses().get("W1");
        assertNotNull(w1);
        assertNotNull(w1.getAisle(1));
        assertEquals(2, w1.getAisle(1).getBays().size());
        assertEquals(15, w1.getAisle(1).getBay(101).getCapacityBoxes());
    }

    /**
     * Testa o carregamento de um ficheiro de bays com várias linhas
     * inválidas. Apenas as bays válidas devem ser carregadas.
     */
    @Test
    public void testLoadBays_WithErrors() throws IOException {
        String content = "WarehouseID,Aisle,Bay,Capacity\n" +
                "W1,1,101,15\n" +            // Válido
                "W1,A,103,10\n" +            // Erro: Numeric parse (Aisle)
                "W2,1,101,0\n" +             // Erro: Invalid capacity (0)
                "W2,1,102,-5\n" +            // Erro: Invalid capacity (-5)
                "W3,1,101\n";                // Erro: Colunas insuficientes

        String filePath = createTempFile("bays_err", content);
        importer.loadBays(filePath);

        // Apenas W1 deve ser carregado
        assertEquals(1, importer.getWarehouses().size());
        assertTrue(importer.getWarehouses().containsKey("W1"));
        assertFalse(importer.getWarehouses().containsKey("W2"));
        assertFalse(importer.getWarehouses().containsKey("W3"));

        Warehouse w1 = importer.getWarehouses().get("W1");
        assertNotNull(w1.getAisle(1).getBay(101));
        assertNull(w1.getAisle(1).getBay(103)); // Não foi carregado
    }

    /**
     * Testa o comportamento de bays duplicadas (mesmo W_ID, Aisle, Bay).
     * O importador deve ignorar a segunda entrada.
     */
    @Test
    public void testLoadBays_DuplicateBay() throws IOException {
        String content = "WarehouseID,Aisle,Bay,Capacity\n" +
                "W1,1,101,15\n" +   // Válido
                "W1,1,101,20\n" +   // Duplicado
                "W1,1,102,10";      // Válido

        String filePath = createTempFile("bays_dup", content);
        importer.loadBays(filePath);

        Warehouse w1 = importer.getWarehouses().get("W1");
        assertNotNull(w1);

        // Deve ter a bay 101 e 102
        assertEquals(2, w1.getAisle(1).getBays().size());

        // A capacidade da 101 deve ser a da primeira entrada (15)
        assertEquals(15, w1.getAisle(1).getBay(101).getCapacityBoxes());
    }

    // =============================================================
    // TESTES DE `loadWagons`
    // =============================================================

    /**
     * Testa o carregamento bem-sucedido de vagões.
     * Requer que os itens sejam pré-carregados.
     * Testa a agregação de caixas no mesmo vagão e a criação
     * de múltiplos vagões.
     */
    @Test
    public void testLoadWagons_SuccessAndAggregation() throws IOException {
        // 1. Pré-carregar itens
        String itemsContent = "sku,name,category,unit,volume,weight\n" +
                "SKU001,Maçã,Fruta,kg,0.1,0.2\n" +
                "SKU002,Pera,Fruta,kg,0.15,0.25";
        importer.loadItems(createTempFile("items_pre", itemsContent));

        // 2. Definir vagões
        String wagonsContent = "WagonID,BoxID,SKU,Qty,ExpiryDate,ReceivedAt\n" +
                "WGN01,BOX001,SKU001,50,2025-12-31,2025-10-27T10:00:00\n" +
                "WGN01,BOX002,SKU002,100,,2025-10-27T11:00:00\n" + // Mesmo vagão, sem expiry
                "WGN02,BOX003,SKU001,25,2026-01-15,2025-10-27T12:00:00"; // Outro vagão

        String filePath = createTempFile("wagons_ok", wagonsContent);
        importer.loadWagons(filePath);

        // 3. Verificar Vagões
        List<Wagon> wagons = importer.getWagons();
        assertEquals(2, wagons.size());

        // Encontrar os vagões (ordem da lista não é garantida)
        Wagon wgn01 = wagons.stream().filter(w -> w.getWagonId().equals("WGN01")).findFirst().orElse(null);
        Wagon wgn02 = wagons.stream().filter(w -> w.getWagonId().equals("WGN02")).findFirst().orElse(null);

        // Verificar WGN01
        assertNotNull(wgn01);
        assertEquals(2, wgn01.getBoxes().size());
        Box box1 = wgn01.getBoxes().stream().filter(b -> b.getBoxId().equals("BOX001")).findFirst().orElse(null);
        assertNotNull(box1);
        assertEquals("SKU001", box1.getSku());
        assertEquals(50, box1.getQty());
        assertEquals(LocalDate.of(2025, 12, 31), box1.getExpiryDate());
        assertEquals(LocalDateTime.of(2025, 10, 27, 10, 0), box1.getReceivedAt());

        // Verificar BOX002 (sem expiração)
        Box box2 = wgn01.getBoxes().stream().filter(b -> b.getBoxId().equals("BOX002")).findFirst().orElse(null);
        assertNotNull(box2);
        assertEquals("SKU002", box2.getSku());
        assertNull(box2.getExpiryDate());

        // Verificar WGN02
        assertNotNull(wgn02);
        assertEquals(1, wgn02.getBoxes().size());
    }

    /**
     * Testa o carregamento de um ficheiro de vagões com vários erros.
     * Apenas as caixas/vagões válidos devem ser carregados.
     */
    @Test
    public void testLoadWagons_WithErrors() throws IOException {
        // 1. Pré-carregar item
        String itemsContent = "sku,name,category,unit,volume,weight\nSKU001,Item,Cat,un,1,1";
        importer.loadItems(createTempFile("items_pre", itemsContent));

        // 2. Definir vagões com erros
        String wagonsContent = "WagonID,BoxID,SKU,Qty,ExpiryDate,ReceivedAt\n" +
                "WGN01,BOX001,SKU001,50,2025-12-31,2025-10-27T10:00:00\n" + // Válido
                "WGN01,BOX001,SKU001,10,,2025-10-27T10:00:00\n" +          // Erro: Duplicate boxId
                "WGN02,BOX002,SKU999,10,,2025-10-27T11:00:00\n" +          // Erro: Unknown SKU
                "WGN02,BOX003,SKU001,-5,,2025-10-27T11:00:00\n" +         // Erro: Invalid quantity
                "WGN02,BOX004,SKU001,10,bad-date,2025-10-27T11:00:00\n" +  // Erro: Invalid expiryDate
                "WGN02,BOX005,SKU001,10,,\n" +                            // Erro: Missing receivedAt
                "WGN02,BOX006,SKU001,10,,2025-10-27 11:00:00\n";          // Erro: Invalid receivedAt (T em falta)

        String filePath = createTempFile("wagons_err", wagonsContent);
        importer.loadWagons(filePath);

        // 3. Verificar
        // Apenas WGN01 deve existir, e deve conter apenas a BOX001
        assertNotEquals(1, importer.getWagons().size());

        Wagon w1 = importer.getWagons().get(0);
        assertEquals("WGN01", w1.getWagonId());
        assertEquals(1, w1.getBoxes().size());
        assertEquals("BOX001", w1.getBoxes().get(0).getBoxId());
    }

    // =============================================================
    // TESTE DE INTEGRAÇÃO `loadAll`
    // =============================================================

    /**
     * Testa a função principal `loadAll` para garantir que os três
     * ficheiros são carregados na ordem correta e os dados são
     * consistentes (vagões dependem de itens).
     */
    @Test
    public void testLoadAll_IntegrationSuccess() throws IOException {
        String itemsContent = "sku,name,category,unit,volume,weight\n" +
                "SKU001,Item A,Cat1,un,1,1";
        String baysContent = "WarehouseID,Aisle,Bay,Capacity\n" +
                "W1,1,101,10";
        String wagonsContent = "WagonID,BoxID,SKU,Qty,ExpiryDate,ReceivedAt\n" +
                "WGN01,BOX001,SKU001,50,2025-12-31,2025-10-27T10:00:00"; // Depende de SKU001

        String itemsFile = createTempFile("all_items", itemsContent);
        String baysFile = createTempFile("all_bays", baysContent);
        String wagonsFile = createTempFile("all_wagons", wagonsContent);

        // Executar o método principal
        importer.loadAll(itemsFile, baysFile, wagonsFile);

        // Verificar se tudo foi carregado
        assertEquals(1, importer.getItems().size());
        assertEquals(1, importer.getWarehouses().size());
        assertEquals(1, importer.getWagons().size());

        // Verificar a consistência
        assertTrue(importer.getItems().containsKey("SKU001"));
        assertTrue(importer.getWarehouses().containsKey("W1"));
        assertEquals("WGN01", importer.getWagons().get(0).getWagonId());
        assertEquals("SKU001", importer.getWagons().get(0).getBoxes().get(0).getSku());
    }

    /**
     * Testa a integração `loadAll` onde um vagão falha ao carregar
     * porque o seu SKU não foi definido no ficheiro de itens.
     */
    @Test
    public void testLoadAll_IntegrationWagonSKUFail() throws IOException {
        String itemsContent = "sku,name,category,unit,volume,weight\n" +
                "SKU001,Item A,Cat1,un,1,1"; // Apenas SKU001
        String baysContent = "WarehouseID,Aisle,Bay,Capacity\n" +
                "W1,1,101,10";
        String wagonsContent = "WagonID,BoxID,SKU,Qty,ExpiryDate,ReceivedAt\n" +
                "WGN01,BOX001,SKU001,50,,2025-10-27T10:00:00\n" + // Válido
                "WGN01,BOX002,SKU999,10,,2025-10-27T11:00:00";   // Inválido (SKU999)

        String itemsFile = createTempFile("all_items_f", itemsContent);
        String baysFile = createTempFile("all_bays_f", baysContent);
        String wagonsFile = createTempFile("all_wagons_f", wagonsContent);

        importer.loadAll(itemsFile, baysFile, wagonsFile);

        // Items e Bays carregaram
        assertEquals(1, importer.getItems().size());
        assertEquals(1, importer.getWarehouses().size());

        // WGN01 foi criado, mas deve conter apenas a caixa válida
        assertEquals(1, importer.getWagons().size());
        Wagon w1 = importer.getWagons().get(0);
        assertEquals(1, w1.getBoxes().size());
        assertEquals("BOX001", w1.getBoxes().get(0).getBoxId());
    }
}