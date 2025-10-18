package ESINF.IO;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Handles deterministic audit log creation for quarantine inspections.
 */
public class AuditLogger {

    private final String filePath;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public AuditLogger(String filePath) {
        this.filePath = filePath;
    }

    public void writeEntry(String returnId, String sku, int qty, String action) {
        String timestamp = LocalDateTime.now().format(fmt);
        String line = String.format("%s | returnId=%s | sku=%s | action=%s | qty=%d",
                timestamp, returnId, sku, action, qty);

        try (FileWriter writer = new FileWriter(filePath, true)) {
            writer.write(line + System.lineSeparator());
        } catch (IOException e) {
            System.err.println("Error writing audit log: " + e.getMessage());
        }
    }
}
