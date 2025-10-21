package ESINF.IO;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Writes quarantine inspection audit logs to a file.
 * Each entry records the timestamp, return ID, SKU, action, and quantity.
 */
public class AuditLogger {

    /** Path of the audit log file. */
    private final String filePath;

    /** Formatter for timestamps (yyyy-MM-dd HH:mm). */
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Creates a new AuditLogger for the given file path.
     *
     * @param filePath path to the log file
     */
    public AuditLogger(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Writes one audit log entry.
     *
     * @param returnId return identifier
     * @param sku item SKU
     * @param qty quantity affected
     * @param action action performed (e.g. "Restocked", "Discarded")
     */
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
