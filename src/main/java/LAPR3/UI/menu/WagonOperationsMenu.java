package LAPR3.UI.menu;

import LAPR3.controller.DataController;
import LAPR3.UI.utils.InputValidator;
import ESINF.model.Box;
import ESINF.service.WarehouseManager;

import java.util.List;
import java.util.Scanner;

/**
 * Submenu for wagon operations (USEI01)
 */
public class WagonOperationsMenu {

    private final Scanner scanner;
    private final DataController dataController;
    private final InputValidator validator;

    public WagonOperationsMenu(Scanner scanner, DataController dataController, InputValidator validator) {
        this.scanner = scanner;
        this.dataController = dataController;
        this.validator = validator;
    }

    public void show() {
        while (true) {
            displayMenu();
            int choice = validator.readInt("Enter your choice: ", 0, 3);

            if (choice == 0) {
                return; // Back to main menu
            }

            try {
                handleChoice(choice);
            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
            }

            pause();
        }
    }

    private void displayMenu() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║         WAGON OPERATIONS (USEI01)                ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────┐");
        System.out.println("│  1. Unload Wagons                               │");
        System.out.println("│  2. Dispatch Products                           │");
        System.out.println("│  3. View Current Inventory                      │");
        System.out.println("│  0. Back to Main Menu                           │");
        System.out.println("└─────────────────────────────────────────────────┘");
        System.out.println();
    }

    private void handleChoice(int choice) {
        switch (choice) {
            case 1 -> unloadWagons();
            case 2 -> dispatchProducts();
            case 3 -> viewInventory();
        }
    }

    private void unloadWagons() {
        System.out.println("\n═══ UNLOAD WAGONS ═══");

        // Validate prerequisites
        if (!validatePrerequisites()) return;

        WarehouseManager manager = dataController.getWarehouseManager();

        System.out.println("📦 Wagons to unload: " + dataController.getWagons().size());

        boolean confirm = validator.readConfirmation("\n⚠️  Proceed with unloading?");
        if (!confirm) {
            System.out.println("Operation cancelled.");
            return;
        }

        System.out.println("\n⏳ Unloading wagons...");
        manager.unloadWagons(dataController.getWagons());

        System.out.println("✓ Wagons unloaded successfully!");
        System.out.println("\n📊 Inventory updated. Use option 3 to view details.");
    }

    private void dispatchProducts() {
        System.out.println("\n═══ DISPATCH PRODUCTS ═══");

        if (!validatePrerequisites()) return;

        WarehouseManager manager = dataController.getWarehouseManager();

        String sku = validator.readString("Enter SKU to dispatch: ");
        int qty = validator.readInt("Enter quantity: ", 1, Integer.MAX_VALUE);

        System.out.println("\n⏳ Dispatching " + qty + " units of SKU=" + sku + "...");

        List<Box> dispatched = manager.dispatch(sku, qty);

        if (dispatched.isEmpty()) {
            System.out.println("❌ No products dispatched. Check if SKU exists and has sufficient stock.");
            return;
        }

        int totalDispatched = dispatched.stream().mapToInt(Box::getQty).sum();

        System.out.println("\n✓ Successfully dispatched " + totalDispatched + " units!");
        System.out.println("Boxes used: " + dispatched.size());

        System.out.println("\nDispatch details:");
        System.out.println("─".repeat(80));
        for (Box box : dispatched) {
            System.out.printf("  BoxID: %-15s | Qty: %5d | Exp: %s%n",
                    box.getBoxId(), box.getQty(),
                    box.getExpiryDate() != null ? box.getExpiryDate() : "N/A");
        }
        System.out.println("─".repeat(80));
    }

    private void viewInventory() {
        System.out.println("\n═══ WAREHOUSE INVENTORY ═══");

        if (!validatePrerequisites()) return;

        dataController.getWarehouseManager().printInventory();
    }

    private boolean validatePrerequisites() {
        if (!dataController.isWarehouseSelected()) {
            System.out.println("❌ No warehouse selected!");
            System.out.println("Please select a warehouse from the Data Import menu first.");
            return false;
        }

        if (!dataController.isWagonsLoaded()) {
            System.out.println("❌ No wagons loaded!");
            System.out.println("Please load wagons from the Data Import menu first.");
            return false;
        }

        return true;
    }

    private void pause() {
        System.out.println("\nPress ENTER to continue...");
        scanner.nextLine();
    }
}