package LAPR3.UI.menu;

import LAPR3.controller.DataController;
import LAPR3.controller.ReturnsController;
import LAPR3.UI.utils.InputValidator;
import ESINF.model.ReturnItem;

import java.util.List;
import java.util.Scanner;

/**
 * Submenu for returns and quarantine management (USEI05)
 */
public class ReturnsManagementMenu {

    private final Scanner scanner;
    private final DataController dataController;
    private final ReturnsController returnsController;
    private final InputValidator validator;

    public ReturnsManagementMenu(Scanner scanner, DataController dataController, InputValidator validator) {
        this.scanner = scanner;
        this.dataController = dataController;
        this.validator = validator;
        this.returnsController = new ReturnsController(dataController);
    }

    public void show() {
        while (true) {
            displayMenu();
            int choice = validator.readInt("Enter your choice: ", 0, 4);

            if (choice == 0) {
                return; // Back to main menu
            }

            try {
                handleChoice(choice);
            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
                e.printStackTrace();
            }

            pause();
        }
    }

    private void displayMenu() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║      RETURNS & QUARANTINE MANAGEMENT (USEI05)    ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────┐");
        System.out.println("│  1. Load Returns                                │");
        System.out.println("│  2. View Quarantine Items                       │");
        System.out.println("│  3. Process Returns (Inspect & Restock/Discard) │");
        System.out.println("│  4. View Audit Log                              │");
        System.out.println("│  0. Back to Main Menu                           │");
        System.out.println("└─────────────────────────────────────────────────┘");
        System.out.println();
    }

    private void handleChoice(int choice) {
        switch (choice) {
            case 1 -> loadReturns();
            case 2 -> viewQuarantineItems();
            case 3 -> processReturns();
            case 4 -> viewAuditLog();
        }
    }

    private void loadReturns() {
        System.out.println("\n═══ LOAD RETURNS ═══");

        if (!dataController.isItemsLoaded()) {
            System.out.println("❌ Items must be loaded first!");
            return;
        }

        String defaultPath = "warehouseMngt/train_station_dataset/returns.csv";
        String path = validator.readFilePath("Enter returns CSV path", defaultPath);

        try {
            System.out.println("⏳ Loading returns...");
            returnsController.loadReturns(path);

            int count = returnsController.getReturnsCount();
            System.out.println("✓ Successfully loaded " + count + " return(s)!");

            if (count > 0) {
                System.out.println("\n📊 Returns Summary:");
                returnsController.printReturnsSummary();
            }

        } catch (Exception e) {
            System.err.println("❌ Error loading returns: " + e.getMessage());
        }
    }

    private void viewQuarantineItems() {
        System.out.println("\n═══ QUARANTINE ITEMS ═══");

        if (!returnsController.hasReturns()) {
            System.out.println("❌ No returns loaded!");
            System.out.println("Please load returns first (option 1).");
            return;
        }

        List<ReturnItem> returns = returnsController.getReturns();

        System.out.println("Total items in quarantine: " + returns.size());
        System.out.println("\n📦 Items awaiting inspection (newest first):");
        System.out.println("─".repeat(100));
        System.out.printf("%-12s | %-10s | %-5s | %-15s | %-20s | %-12s%n",
                "ReturnID", "SKU", "Qty", "Reason", "Timestamp", "Expiry");
        System.out.println("─".repeat(100));

        int displayCount = Math.min(20, returns.size());
        for (int i = 0; i < displayCount; i++) {
            ReturnItem ret = returns.get(i);
            System.out.printf("%-12s | %-10s | %5d | %-15s | %-20s | %-12s%n",
                    ret.getReturnId(),
                    ret.getSku(),
                    ret.getQty(),
                    ret.getReason(),
                    ret.getTimestamp().toString(),
                    ret.getExpiryDate() != null ? ret.getExpiryDate().toString() : "N/A");
        }

        if (returns.size() > 20) {
            System.out.println("... (" + (returns.size() - 20) + " more items)");
        }

        System.out.println("─".repeat(100));

        // Statistics by reason
        System.out.println("\n📊 Returns by Reason:");
        long damaged = returns.stream().filter(r -> r.getReason().equalsIgnoreCase("damaged")).count();
        long expired = returns.stream().filter(r -> r.getReason().equalsIgnoreCase("expired")).count();
        long remorse = returns.stream().filter(r -> r.getReason().equalsIgnoreCase("customer-remorse")).count();
        long cycleCount = returns.stream().filter(r -> r.getReason().equalsIgnoreCase("cycle-count")).count();

        System.out.println("  • Damaged: " + damaged + " (will be discarded)");
        System.out.println("  • Expired: " + expired + " (will be discarded)");
        System.out.println("  • Customer Remorse: " + remorse + " (can be restocked)");
        System.out.println("  • Cycle Count: " + cycleCount + " (can be restocked)");
    }

    private void processReturns() {
        System.out.println("\n═══ PROCESS RETURNS ═══");

        if (!validatePrerequisites()) return;

        int count = returnsController.getReturnsCount();
        System.out.println("Items to process: " + count);
        System.out.println();
        System.out.println("ℹ️  Processing order: Latest first (LIFO - newest returns inspected first)");
        System.out.println();
        System.out.println("Actions:");
        System.out.println("  • Damaged/Expired → Discard");
        System.out.println("  • Customer Remorse/Cycle Count → Restock (if possible)");
        System.out.println();

        boolean confirm = validator.readConfirmation("⚠️  Proceed with processing all returns?");
        if (!confirm) {
            System.out.println("Operation cancelled.");
            return;
        }

        System.out.println("\n⏳ Processing returns...");
        System.out.println("─".repeat(80));

        returnsController.processReturns();

        System.out.println("─".repeat(80));
        System.out.println("✓ All returns processed successfully!");
        System.out.println("\n💡 TIP: Check audit log (option 4) for detailed processing results.");
        System.out.println("💡 TIP: View inventory (main menu → option 4) to see restocked items.");
    }

    private void viewAuditLog() {
        System.out.println("\n═══ RETURNS AUDIT LOG ═══");

        String auditPath = "Logs/returns_audit.log";

        try {
            java.nio.file.Path path = java.nio.file.Paths.get(auditPath);
            if (!java.nio.file.Files.exists(path)) {
                System.out.println("❌ No audit log found yet.");
                System.out.println("Process returns first (option 3) to generate the audit log.");
                return;
            }

            List<String> lines = java.nio.file.Files.readAllLines(path);

            if (lines.isEmpty()) {
                System.out.println("Audit log is empty.");
                return;
            }

            System.out.println("Total audit entries: " + lines.size());
            System.out.println("\nRecent entries (last 30):");
            System.out.println("─".repeat(100));

            int start = Math.max(0, lines.size() - 30);
            for (int i = start; i < lines.size(); i++) {
                System.out.println(lines.get(i));
            }

            System.out.println("─".repeat(100));
            System.out.println("\n💾 Full log available at: " + auditPath);

            // Statistics
            long restocked = lines.stream().filter(l -> l.contains("action=Restocked")).count();
            long discarded = lines.stream().filter(l -> l.contains("action=Discarded")).count();
            long failed = lines.stream().filter(l -> l.contains("action=FailedRestock")).count();

            System.out.println("\n📊 Processing Summary:");
            System.out.println("  ✓ Restocked: " + restocked);
            System.out.println("  ✗ Discarded: " + discarded);
            if (failed > 0) {
                System.out.println("  ⚠ Failed to restock: " + failed + " (warehouse full?)");
            }

        } catch (Exception e) {
            System.err.println("❌ Error reading audit log: " + e.getMessage());
        }
    }

    private boolean validatePrerequisites() {
        if (!dataController.isWarehouseSelected()) {
            System.out.println("❌ No warehouse selected!");
            System.out.println("Please select a warehouse from the Data Import menu first.");
            return false;
        }

        if (!returnsController.hasReturns()) {
            System.out.println("❌ No returns loaded!");
            System.out.println("Please load returns first (option 1).");
            return false;
        }

        return true;
    }

    private void pause() {
        System.out.println("\nPress ENTER to continue...");
        scanner.nextLine();
    }
}