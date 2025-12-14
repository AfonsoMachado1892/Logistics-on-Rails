package LAPR3.UI.menu;

import ESINF.controller.RiskAwareShortestPathController;
import LAPR3.UI.utils.InputValidator;

import java.util.Scanner;

/**
 * Menu for USEI15 - Risk-Aware Shortest Paths (Bellman-Ford)
 * Follows the structure of KDTreeMenu for consistency.
 */
public class RiskAwareShortestPathMenu {

    private final Scanner scanner;
    private final InputValidator validator;
    private final RiskAwareShortestPathController controller;

    public RiskAwareShortestPathMenu(Scanner scanner, InputValidator validator,
                                     RiskAwareShortestPathController controller) {
        this.scanner = scanner;
        this.validator = validator;
        this.controller = controller;
    }

    public void show() {
        // Pre-check: Ensure data is loaded before entering the menu loop
        // (Similar to how KDTreeMenu checks if data is loaded)
        try {
            // We can try a lightweight check or rely on the controller handling empty data
            // For safety, we assume if the repo is empty, the controller might throw or return null paths.
            // If you have a specific isDataLoaded() method exposed, check it here.
        } catch (Exception e) {
            System.out.println("\n Error: Railway data is not loaded correctly.");
            System.out.println("Please load data in the Main Menu first.");
            return;
        }

        boolean back = false;

        while (!back) {
            displayMenu();
            int choice = validator.readInt("Enter your choice: ", 0, 2);

            switch (choice) {
                case 1 -> calculateShortestPath();
                case 2 -> viewComplexityAnalysis();
                case 0 -> back = true;
                default -> System.out.println("Invalid option!");
            }

            if (!back) {
                System.out.println("\nPress ENTER to continue...");
                scanner.nextLine();
            }
        }
    }

    private void displayMenu() {
        System.out.println("\n╔═══════════════════════════════════════════════════════╗");
        System.out.println("║   USEI15 - RISK-AWARE SHORTEST PATH                   ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("┌───────────────────────────────────────────────────────┐");
        System.out.println("│  1. Calculate Path (Source -> Target)                 │");
        System.out.println("│  2. View Complexity Analysis                          │");
        System.out.println("├───────────────────────────────────────────────────────┤");
        System.out.println("│  0. Back to Main Menu                                 │");
        System.out.println("└───────────────────────────────────────────────────────┘");
        System.out.println();
    }

    private void calculateShortestPath() {
        System.out.println("\n╔══ CALCULATE PATH ══╗");
        System.out.println("Finds the path with minimum COST (Distance + Risk/Bonus).");
        System.out.println("Note: Costs can be negative.");
        System.out.println();

        // 1. Get Inputs using Validator
        int sourceId = validator.readInt("  Enter Source Station ID: ", 1, Integer.MAX_VALUE);
        int targetId = validator.readInt("  Enter Target Station ID: ", 1, Integer.MAX_VALUE);

        System.out.println("\n Calculating...");

        try {
            // 2. Call Controller
            String result = controller.calculateShortestPath(sourceId, targetId);

            // 3. Display Result
            System.out.println("\n" + "═".repeat(60));
            System.out.println(" CALCULATION RESULT");
            System.out.println("═".repeat(60));
            System.out.println(result);
            System.out.println("═".repeat(60));

        } catch (IllegalArgumentException e) {
            System.out.println(" Error: Invalid Station ID. Please check your input.");
        } catch (IllegalStateException e) {
            System.out.println(" Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println(" An unexpected error occurred: " + e.getMessage());
        }
    }

    private void viewComplexityAnalysis() {
        System.out.println("\n╔═══════════════════════════════════════════════════════╗");
        System.out.println("║       TEMPORAL COMPLEXITY ANALYSIS (USEI15)           ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("ALGORITHM: Bellman-Ford");
        System.out.println("─".repeat(60));
        System.out.println();

        // Complexity Details
        System.out.println("  WORST / AVERAGE CASE: O(V * E)");
        System.out.println("   Where:");
        System.out.println("     V = Number of Stations (Vertices)");
        System.out.println("     E = Number of Connections (Edges)");
        System.out.println();
        System.out.println("  WHY?");
        System.out.println("   1. We iterate (V - 1) times to relax edges.");
        System.out.println("   2. In each iteration, we traverse all E edges.");
        System.out.println("   3. One final pass of E edges detects negative cycles.");
        System.out.println("   4. Total operations ≈ (V * E).");
        System.out.println();
        System.out.println("  COMPARISON:");
        System.out.println("   - Slower than Dijkstra (O(E + V log V)).");
        System.out.println("   - REQUIRED here because Dijkstra fails with negative costs.");
        System.out.println();
        System.out.println("═".repeat(60));
    }
}