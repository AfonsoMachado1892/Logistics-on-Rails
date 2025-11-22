package LAPR3.UI.menu;

import ESINF.controller.StationQueryController;
import ESINF.model.KDTree2D;
import ESINF.model.RadiusSearchResult;
import LAPR3.UI.utils.InputValidator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

public class RadiusSearchResultMenu {

    private final Scanner scanner;
    private final InputValidator validator;
    private final StationQueryController stationController;


    public RadiusSearchResultMenu(Scanner scanner,
                                  InputValidator validator,
                                  StationQueryController stationController) {
        this.scanner = scanner;
        this.validator = validator;
        this.stationController = stationController;
    }

    public void show() {
        if (!stationController.isDataLoaded()) {
            System.out.println("\n Station data not loaded!");
            System.out.println("Please load station data first (Main Menu → Option 10 → Option 1)");
            return;
        }

        KDTree2D kdTree = stationController.getKDTree();
        if (kdTree == null || kdTree.getRoot() == null) {
            System.out.println("\n 2D-Tree not built!");
            System.out.println("Please build 2D-Tree first (Main Menu → Option 11 → Option 1)");
            return;
        }

        boolean back = false;
        while (!back) {
            displayMenu();
            int choice = validator.readInt("Enter your choice: ", 0, 3);

            switch (choice) {
                case 1 -> runRadiusSearch(kdTree); // Chama KDTree real
               
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
        System.out.println("\n╔═════════════════════════════════════════════════╗");
        System.out.println("║     USEI10 - RADIUS SEARCH (KD-TREE)            ║");
        System.out.println("╚═════════════════════════════════════════════════╝");
        System.out.println(" 1. Search Stations by Radius");
        System.out.println(" 2. View Complexity Analysis");
        System.out.println(" 3. About this Use Case");
        System.out.println(" 0. Back to Main Menu");
    }

    private void runRadiusSearch(KDTree2D kdTree) {
        System.out.println("\n╔══ RADIUS SEARCH ══╗");

        double lat = validator.readDouble("  Latitude [-90, 90]: ", -90, 90);
        double lon = validator.readDouble("  Longitude [-180, 180]: ", -180, 180);
        double radius = validator.readDouble("  Radius (km) [0-20000]: ", 0, 20000);

        System.out.println("\n Searching stations in radius...");

        try {
            // Chama USEI10 no KDTree2D
            RadiusSearchResult result = kdTree.radiusSearchWithDensity(lat, lon, radius);

            displayResult(result);


        } catch (Exception e) {
            System.out.println(" Error: " + e.getMessage());
        }
    }

    private void displayResult(RadiusSearchResult result) {
        System.out.println("\n" + "═".repeat(90));
        System.out.println(result); // Usa o toString do RadiusSearchResult
        System.out.println("═".repeat(90));
    }


}
