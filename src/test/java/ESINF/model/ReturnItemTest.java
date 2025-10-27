package ESINF.model;

import org.junit.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.Assert.*;

public class ReturnItemTest {

    // Testes de Getters

    @Test
    public void getReturnId() {
        // Verifica se o ID de devolução é retornado corretamente.
        LocalDateTime timestamp = LocalDateTime.now();
        ReturnItem item = new ReturnItem("R001", "SKU123", 10,
                "customer-remorse", timestamp, null);

        assertEquals("R001", item.getReturnId());
    }

    @Test
    public void getSku() {
        // Verifica se o SKU do produto é retornado corretamente.
        LocalDateTime timestamp = LocalDateTime.now();
        ReturnItem item = new ReturnItem("R001", "SKU123", 10,
                "customer-remorse", timestamp, null);

        assertEquals("SKU123", item.getSku());
    }

    @Test
    public void getQty() {
        // Verifica se a quantidade devolvida é retornado corretamente.
        LocalDateTime timestamp = LocalDateTime.now();
        ReturnItem item = new ReturnItem("R001", "SKU123", 10,
                "customer-remorse", timestamp, null);

        assertEquals(10, item.getQty());
    }

    @Test
    public void getReason() {
        // Verifica se a razão da devolução é retornado corretamente.
        LocalDateTime timestamp = LocalDateTime.now();
        ReturnItem item = new ReturnItem("R001", "SKU123", 10,
                "customer-remorse", timestamp, null);

        assertEquals("customer-remorse", item.getReason());
    }

    @Test
    public void getTimestamp() {
        // Verifica se o timestamp (data e hora da devolução) é retornado corretamente.
        LocalDateTime timestamp = LocalDateTime.of(2025, 10, 20, 14, 30);
        ReturnItem item = new ReturnItem("R001", "SKU123", 10,
                "customer-remorse", timestamp, null);

        assertEquals(timestamp, item.getTimestamp());
    }

    @Test
    public void getExpiryDate() {
        // Verifica se a data de validade é retornada corretamente quando fornecida.
        LocalDateTime timestamp = LocalDateTime.now();
        LocalDate expiry = LocalDate.of(2025, 12, 31);

        ReturnItem item = new ReturnItem("R001", "SKU123", 10,
                "customer-remorse", timestamp, expiry);

        assertEquals(expiry, item.getExpiryDate());
    }

    @Test
    public void getExpiryDate_Null() {
        // Verifica se a data de validade é nula quando não fornecida (para não perecíveis).
        LocalDateTime timestamp = LocalDateTime.now();

        ReturnItem item = new ReturnItem("R001", "SKU123", 10,
                "damaged", timestamp, null);

        assertNull(item.getExpiryDate());
    }

    // Testes de Regras de Negócio (shouldRestock / shouldDiscard)

    @Test
    public void shouldRestock() {
        // Verifica se itens com razão "customer-remorse" ou "cycle-count" (case-insensitive) devem ser reabastecidos.
        LocalDateTime timestamp = LocalDateTime.now();

        ReturnItem item1 = new ReturnItem("R001", "SKU123", 10,
                "customer-remorse", timestamp, null);
        assertTrue(item1.shouldRestock());

        ReturnItem item2 = new ReturnItem("R002", "SKU123", 10,
                "cycle-count", timestamp, null);
        assertTrue(item2.shouldRestock());

        ReturnItem item3 = new ReturnItem("R003", "SKU123", 10,
                "CUSTOMER-REMORSE", timestamp, null);
        assertTrue(item3.shouldRestock());

        ReturnItem item4 = new ReturnItem("R004", "SKU123", 10,
                "damaged", timestamp, null);
        assertFalse(item4.shouldRestock());
    }

    @Test
    public void shouldDiscard() {
        // Verifica se itens com razão "damaged" ou "expired" (case-insensitive) devem ser descartados.
        LocalDateTime timestamp = LocalDateTime.now();

        ReturnItem item1 = new ReturnItem("R001", "SKU123", 10,
                "damaged", timestamp, null);
        assertTrue( item1.shouldDiscard());

        ReturnItem item2 = new ReturnItem("R002", "SKU123", 10,
                "expired", timestamp, null);
        assertTrue( item2.shouldDiscard());

        ReturnItem item3 = new ReturnItem("R003", "SKU123", 10,
                "DAMAGED", timestamp, null);
        assertTrue( item3.shouldDiscard());

        ReturnItem item4 = new ReturnItem("R004", "SKU123", 10,
                "customer-remorse", timestamp, null);
        assertFalse( item4.shouldDiscard());
    }

    @Test
    public void testUnknownReason_NoRestockNoDiscard() {
        // Verifica que uma razão desconhecida não é nem reabastecida nem descartada
        LocalDateTime timestamp = LocalDateTime.now();
        ReturnItem item = new ReturnItem("R014", "SKU123", 10,
                "unknown-reason", timestamp, null);

        assertFalse( item.shouldRestock());
        assertFalse( item.shouldDiscard());
    }

    // Testes de Representação (toString)

    @Test
    public void testToString() {
        // Verifica se o metodo toString inclui todos os campos, incluindo a data de validade.
        LocalDateTime timestamp = LocalDateTime.of(2025, 10, 20, 14, 30);
        LocalDate expiry = LocalDate.of(2025, 12, 31);

        ReturnItem item = new ReturnItem("R001", "SKU789", 15,
                "customer-remorse", timestamp, expiry);

        String result = item.toString();

        assertTrue(result.contains("R001"));
        assertTrue(result.contains("SKU789"));
        assertTrue(result.contains("qty=15"));
        assertTrue(result.contains("customer-remorse"));
        assertTrue(result.contains("2025-12-31"));
    }

    @Test
    public void testToString_WithoutExpiryDate() {
        // Verifica se o metodo toString funciona corretamente quando a data de validade é nula.
        LocalDateTime timestamp = LocalDateTime.of(2025, 10, 20, 14, 30);

        ReturnItem item = new ReturnItem("R001", "SKU999", 25,
                "damaged", timestamp, null);

        String result = item.toString();

        assertTrue(result.contains("R001"));
        assertTrue(result.contains("SKU999"));
        assertTrue(result.contains("qty=25"));
        assertTrue(result.contains("damaged"));
    }

    // Testes de Identidade (equals / hashCode)

    @Test
    public void testEquals() {
        // Verifica que a igualdade é baseada APENAS no 'returnId', ignorando os outros campos.
        LocalDateTime timestamp = LocalDateTime.now();

        ReturnItem item1 = new ReturnItem("R001", "SKU123", 10,
                "damaged", timestamp, null);
        ReturnItem item2 = new ReturnItem("R001", "SKU456", 20, // SKU/Qty/Reason/TS diferentes
                "expired", timestamp.plusHours(1), null);
        assertEquals(item1, item2);

        ReturnItem item3 = new ReturnItem("R002", "SKU123", 10, // ReturnId diferente
                "damaged", timestamp, null);
        assertNotEquals( item1, item3);

        assertNotEquals(item1, null);
        assertNotEquals(item1, "Not a ReturnItem");
    }

    @Test
    public void testHashCode() {
        // Verifica que o hashCode é consistente e baseado apenas no 'returnId'.
        LocalDateTime timestamp = LocalDateTime.now();

        ReturnItem item = new ReturnItem("R001", "SKU123", 10,
                "damaged", timestamp, null);
        int hash1 = item.hashCode();
        int hash2 = item.hashCode();
        assertEquals( hash1, hash2);

        ReturnItem item1 = new ReturnItem("R001", "SKU123", 10,
                "damaged", timestamp, null);
        ReturnItem item2 = new ReturnItem("R001", "SKU456", 20,
                "expired", timestamp, null);
        assertEquals( item1.hashCode(), item2.hashCode());

        ReturnItem item3 = new ReturnItem("R002", "SKU123", 10,
                "damaged", timestamp, null);
        assertNotEquals( item1.hashCode(), item3.hashCode());
    }

    // Testes de Validação do Construtor (Argumentos Inválidos)

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidReturnId_Null() {
        // Deve rejeitar ReturnId nulo.
        LocalDateTime timestamp = LocalDateTime.now();
        new ReturnItem(null, "SKU123", 10, "damaged", timestamp, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidReturnId_Blank() {
        // Deve rejeitar ReturnId vazio ou só com espaços.
        LocalDateTime timestamp = LocalDateTime.now();
        new ReturnItem("  ", "SKU123", 10, "damaged", timestamp, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSku_Null() {
        // Deve rejeitar SKU nulo.
        LocalDateTime timestamp = LocalDateTime.now();
        new ReturnItem("R001", null, 10, "damaged", timestamp, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSku_Blank() {
        // Deve rejeitar SKU vazio.
        LocalDateTime timestamp = LocalDateTime.now();
        new ReturnItem("R001", "", 10, "damaged", timestamp, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidQuantity_Zero() {
        // Deve rejeitar quantidade igual a zero.
        LocalDateTime timestamp = LocalDateTime.now();
        new ReturnItem("R001", "SKU123", 0, "damaged", timestamp, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidQuantity_Negative() {
        // Deve rejeitar quantidade negativa.
        LocalDateTime timestamp = LocalDateTime.now();
        new ReturnItem("R001", "SKU123", -5, "damaged", timestamp, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidReason_Null() {
        // Deve rejeitar Reason nula.
        LocalDateTime timestamp = LocalDateTime.now();
        new ReturnItem("R001", "SKU123", 10, null, timestamp, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidReason_Blank() {
        // Deve rejeitar Reason vazia ou só com espaços.
        LocalDateTime timestamp = LocalDateTime.now();
        new ReturnItem("R001", "SKU123", 10, "   ", timestamp, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTimestamp_Null() {
        // Deve rejeitar Timestamp nulo.
        new ReturnItem("R001", "SKU123", 10, "damaged", null, null);
    }
}