package ESINF.IO;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.*;

public class AuditLoggerTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void writeEntry() throws Exception {
        // Testa a escrita de uma única entrada e verifica se o ficheiro foi criado.
        File logFile = tempFolder.newFile("audit.log");
        AuditLogger logger = new AuditLogger(logFile.getAbsolutePath());

        logger.writeEntry("R001", "SKU123", 10, "Restocked");

        assertTrue(logFile.exists());

        List<String> lines = Files.readAllLines(logFile.toPath());
        assertEquals( 1, lines.size());

        String line = lines.get(0);
        // Verifica se todos os campos obrigatórios estão no formato de saída correto.
        assertTrue( line.contains("returnId=R001"));
        assertTrue(line.contains("sku=SKU123"));
        assertTrue(line.contains("action=Restocked"));
        assertTrue(line.contains("qty=10"));
    }

    @Test
    public void writeEntry_MultipleEntries() throws Exception {
        // Testa a escrita de múltiplas entradas e verifica se todas estão presentes no ficheiro.
        File logFile = tempFolder.newFile("audit_multiple.log");
        AuditLogger logger = new AuditLogger(logFile.getAbsolutePath());

        logger.writeEntry("R001", "SKU123", 10, "Restocked");
        logger.writeEntry("R002", "SKU456", 5, "Discarded");
        logger.writeEntry("R003", "SKU789", 8, "FailedRestock-NoSpace");

        List<String> lines = Files.readAllLines(logFile.toPath());
        assertEquals(3, lines.size());

        assertTrue(lines.get(0).contains("R001"));
        assertTrue(lines.get(1).contains("R002"));
        assertTrue(lines.get(2).contains("R003"));
    }

    @Test
    public void writeEntry_AppendsToExistingFile() throws Exception {
        // Testa se o logger anexa dados a um ficheiro existente, e não sobrescreve.
        File logFile = tempFolder.newFile("audit_append.log");

        // Primeira instância escreve R001
        AuditLogger logger1 = new AuditLogger(logFile.getAbsolutePath());
        logger1.writeEntry("R001", "SKU123", 10, "Restocked");

        // Segunda instância escreve R002 (deve ser anexado)
        AuditLogger logger2 = new AuditLogger(logFile.getAbsolutePath());
        logger2.writeEntry("R002", "SKU456", 5, "Discarded");

        List<String> lines = Files.readAllLines(logFile.toPath());
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).contains("R001"));
        assertTrue(lines.get(1).contains("R002"));
    }

    @Test
    public void writeEntry_DifferentActions() throws Exception {
        // Testa a escrita com várias ações possíveis para o log de auditoria.
        File logFile = tempFolder.newFile("audit_actions.log");
        AuditLogger logger = new AuditLogger(logFile.getAbsolutePath());

        logger.writeEntry("R001", "SKU001", 10, "Restocked");
        logger.writeEntry("R002", "SKU002", 5, "Discarded");
        logger.writeEntry("R003", "SKU003", 8, "Discarded-ExpiredDuringQuarantine");
        logger.writeEntry("R004", "SKU004", 3, "FailedRestock-NoSpace");
        logger.writeEntry("R005", "SKU005", 12, "Discarded-InvalidSKU");

        List<String> lines = Files.readAllLines(logFile.toPath());
        assertEquals(5, lines.size());

        // Verifica a presença das diferentes strings de ação
        assertTrue(lines.get(0).contains("action=Restocked"));
        assertTrue(lines.get(1).contains("action=Discarded"));
        assertTrue(lines.get(2).contains("action=Discarded-ExpiredDuringQuarantine"));
        assertTrue(lines.get(3).contains("action=FailedRestock-NoSpace"));
        assertTrue(lines.get(4).contains("action=Discarded-InvalidSKU"));
    }

    @Test
    public void writeEntry_ZeroQuantity() throws Exception {
        // Testa a escrita de uma entrada com quantidade zero (ex.: para logs informativos ou parciais).
        File logFile = tempFolder.newFile("audit_zero.log");
        AuditLogger logger = new AuditLogger(logFile.getAbsolutePath());

        logger.writeEntry("R001", "SKU123", 0, "Discarded");

        List<String> lines = Files.readAllLines(logFile.toPath());
        assertTrue(lines.get(0).contains("qty=0"));
    }

    @Test
    public void writeEntry_EmptyAction() throws Exception {
        // Testa a escrita quando a string de ação é vazia.
        File logFile = tempFolder.newFile("audit_empty_action.log");
        AuditLogger logger = new AuditLogger(logFile.getAbsolutePath());

        logger.writeEntry("R001", "SKU123", 10, "");

        List<String> lines = Files.readAllLines(logFile.toPath());
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains("action="));
    }
}