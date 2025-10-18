package ESINF.IO;

import ESINF.model.ReturnItem;
import ESINF.model.Item;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Reads and validates the returns.csv file.
 */
public class ReturnImporter {

    private final ImportLogger logger;
    private final Map<String, Item> itemCatalog;
    private final List<ReturnItem> returns = new ArrayList<>();

    public ReturnImporter(ImportLogger logger, Map<String, Item> itemCatalog) {
        this.logger = logger;
        this.itemCatalog = itemCatalog;
    }

    public List<ReturnItem> loadReturns(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String header = br.readLine();
            String line;
            int lineNo = 1;

            while ((line = br.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",", -1);
                if (parts.length < 5) {
                    logger.error("returns.csv", lineNo, "Invalid number of columns (expected at least 5): " + line);
                    continue;
                }

                // If no expiry date
                if (parts.length == 5) {
                    parts = new String[]{parts[0], parts[1], parts[2], parts[3], parts[4], ""};
                }

                try {
                    String returnId = parts[0].trim();
                    String sku = parts[1].trim();
                    int qty = Integer.parseInt(parts[2].trim());
                    String reason = parts[3].trim();
                    String tsStr = parts[4].trim();
                    String expStr = parts[5].trim();

                    if (returnId.isEmpty() || sku.isEmpty() || reason.isEmpty()) {
                        logger.error("returns.csv", lineNo, "Missing required fields: " + line);
                        continue;
                    }

                    if (!itemCatalog.containsKey(sku)) {
                        logger.error("returns.csv", lineNo, "Unknown SKU=" + sku);
                        continue;
                    }

                    LocalDateTime timestamp;
                    try {
                        timestamp = LocalDateTime.parse(tsStr);
                    } catch (DateTimeParseException e) {
                        logger.error("returns.csv", lineNo, "Invalid timestamp=" + tsStr);
                        continue;
                    }

                    LocalDate expiry = null;
                    if (!expStr.isEmpty()) {
                        try {
                            expiry = LocalDate.parse(expStr);
                        } catch (DateTimeParseException e) {
                            logger.warning("returns.csv", lineNo, "Invalid expiryDate=" + expStr + " (ignored)");
                        }
                    }

                    returns.add(new ReturnItem(returnId, sku, qty, reason, timestamp, expiry));

                } catch (Exception e) {
                    logger.error("returns.csv", lineNo, "Unexpected error: " + e.getMessage());
                }
            }
        }

        // sort newest first, then by returnId
        returns.sort(Comparator
                .comparing(ReturnItem::getTimestamp).reversed()
                .thenComparing(ReturnItem::getReturnId));

        logger.info("returns.csv", 0, "Loaded " + returns.size() + " valid returns.");
        return returns;
    }
}
