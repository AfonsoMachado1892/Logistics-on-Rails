package LAPR3.UI.menu;

import LAPR3.UI.utils.InputValidator;
import LAPR3.repository.*;

import java.util.Scanner;

/**
 * Train Data Import Menu - Database Version
 * Loads all data from Oracle database instead of CSV files
 */
public class TrainDataImportMenu {
    private final Scanner scanner;
    private final InputValidator validator;
    private final FacilityRepository facilityRepo;
    private final LineRepository lineRepo;
    private final SegmentRepository segmentRepo;
    private final LocomotiveRepository locomotiveRepo;
    private final WagonRepository wagonRepo;
    private final TrainRepository trainRepo;
    private final RouteRepository routeRepo;
    private final ScheduleRepository scheduleRepo;

    public TrainDataImportMenu(Scanner scanner, InputValidator validator,
                               FacilityRepository facilityRepo,
                               LineRepository lineRepo,
                               SegmentRepository segmentRepo,
                               LocomotiveRepository locomotiveRepo,
                               WagonRepository wagonRepo,
                               TrainRepository trainRepo,
                               RouteRepository routeRepo,
                               ScheduleRepository scheduleRepo) {
        this.scanner = scanner;
        this.validator = validator;
        this.facilityRepo = facilityRepo;
        this.lineRepo = lineRepo;
        this.segmentRepo = segmentRepo;
        this.locomotiveRepo = locomotiveRepo;
        this.wagonRepo = wagonRepo;
        this.trainRepo = trainRepo;
        this.routeRepo = routeRepo;
        this.scheduleRepo = scheduleRepo;
    }

    public void show() {
        boolean running = true;

        while (running) {
            displayMenu();
            int choice = validator.readInt("Enter your choice: ", 0, 10);

            switch (choice) {
                case 1 -> importAllData();
                case 2 -> importFacilities();
                case 3 -> importLines();
                case 4 -> importSegments();
                case 5 -> importLocomotives();
                case 6 -> importWagons();
                case 7 -> importTrains();
                case 8 -> importRoutes();
                case 9 -> importSchedules();
                case 10 -> showDataStatus();
                case 0 -> running = false;
            }

            if (running && choice != 0) {
                pauseForUser();
            }
        }
    }

    private void displayMenu() {
        System.out.println("\n┌────────────────────────────────────────────────┐");
        System.out.println("│  TRAIN DATA IMPORT (Database)                  │");
        System.out.println("└────────────────────────────────────────────────┘");
        System.out.println();

        showDataStatus();

        System.out.println("\n  1. Load ALL Data from Database      ");
        System.out.println("  ────────────────────────────────────────────");
        System.out.println("  2. Load Facilities");
        System.out.println("  3. Load Lines");
        System.out.println("  4. Load Segments");
        System.out.println("  5. Load Locomotives");
        System.out.println("  6. Load Wagons");
        System.out.println("  7. Load Trains");
        System.out.println("  8. Load Routes");
        System.out.println("  9. Load Schedules");
        System.out.println("  ────────────────────────────────────────────");
        System.out.println("  10. Show Data Status");
        System.out.println("  0. Back to Freight Manager Menu");
        System.out.println();
    }

    /**
     * Imports all data from database in correct order
     */
    private void importAllData() {
        System.out.println("\n╔══ LOAD ALL DATA FROM DATABASE ══╗");
        System.out.println("Warning: This will clear existing data in memory and reload from database.");

        System.out.print("\nContinue? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (!confirm.equals("y") && !confirm.equals("yes")) {
            System.out.println("Load cancelled.");
            return;
        }

        System.out.println("\n⏳ Starting data load from database...\n");

        boolean success = true;
        int totalLoaded = 0;

        // Load in dependency order

        // 1. Infrastructure
        if (loadFromDatabase("Facilities", facilityRepo::loadFromDatabase)) {
            totalLoaded += facilityRepo.count();
        } else {
            success = false;
        }

        if (loadFromDatabase("Lines", lineRepo::loadFromDatabase)) {
            totalLoaded += lineRepo.count();
        } else {
            success = false;
        }

        if (loadFromDatabase("Segments", segmentRepo::loadFromDatabase)) {
            totalLoaded += segmentRepo.count();
        } else {
            success = false;
        }

        // 2. Rolling Stock
        if (loadFromDatabase("Locomotives", locomotiveRepo::loadFromDatabase)) {
            totalLoaded += locomotiveRepo.count();
        } else {
            success = false;
        }

        if (loadFromDatabase("Wagons", wagonRepo::loadFromDatabase)) {
            totalLoaded += wagonRepo.count();
        } else {
            success = false;
        }

        // 3. Trains (depends on Locomotives + Wagons)
        if (loadFromDatabase("Trains", trainRepo::loadFromDatabase)) {
            totalLoaded += trainRepo.count();
        } else {
            success = false;
        }

        // 4. Routes (depends on Facilities)
        if (loadFromDatabase("Routes", routeRepo::loadFromDatabase)) {
            totalLoaded += routeRepo.count();
        } else {
            success = false;
        }

        // 5. Schedules (depends on Trains + Routes)
        if (loadFromDatabase("Schedules", scheduleRepo::loadFromDatabase)) {
            totalLoaded += scheduleRepo.count();
        } else {
            success = false;
        }

        System.out.println("\n╔════════════════════════════════════╗");
        if (success) {
            System.out.println("║ ✓ ALL DATA LOADED SUCCESSFULLY!   ║");
            System.out.println("║   Total records: " + String.format("%-17d", totalLoaded) + "║");
        } else {
            System.out.println("║ ⚠ LOAD COMPLETED WITH WARNINGS    ║");
            System.out.println("║   Check database connection        ║");
        }
        System.out.println("╚════════════════════════════════════╝");
    }

    /**
     * Helper to load data from database with error handling
     */
    private boolean loadFromDatabase(String entityName, Runnable loader) {
        try {
            System.out.print("⏳ Loading " + entityName + "...");
            loader.run();
            System.out.println(" ✓");
            return true;
        } catch (Exception e) {
            System.out.println(" ✗ Error: " + e.getMessage());
            return false;
        }
    }

    // --- Individual load methods ---

    private void importFacilities() {
        System.out.println("\n⏳ Loading Facilities from database...");
        try {
            facilityRepo.loadFromDatabase();
            System.out.println("✓ Facilities loaded: " + facilityRepo.count());
        } catch (Exception e) {
            System.err.println("✗ Error loading Facilities: " + e.getMessage());
        }
    }

    private void importLines() {
        System.out.println("\n⏳ Loading Lines from database...");
        try {
            lineRepo.loadFromDatabase();
            System.out.println("✓ Lines loaded: " + lineRepo.count());
        } catch (Exception e) {
            System.err.println("✗ Error loading Lines: " + e.getMessage());
        }
    }

    private void importSegments() {
        System.out.println("\n⏳ Loading Segments from database...");
        try {
            segmentRepo.loadFromDatabase();
            System.out.println("✓ Segments loaded: " + segmentRepo.count());
        } catch (Exception e) {
            System.err.println("✗ Error loading Segments: " + e.getMessage());
        }
    }

    private void importLocomotives() {
        System.out.println("\n⏳ Loading Locomotives from database...");
        try {
            locomotiveRepo.loadFromDatabase();
            System.out.println("✓ Locomotives loaded: " + locomotiveRepo.count());
        } catch (Exception e) {
            System.err.println("✗ Error loading Locomotives: " + e.getMessage());
        }
    }

    private void importWagons() {
        System.out.println("\n⏳ Loading Wagons from database...");
        try {
            wagonRepo.loadFromDatabase();
            System.out.println("✓ Wagons loaded: " + wagonRepo.count());
        } catch (Exception e) {
            System.err.println("✗ Error loading Wagons: " + e.getMessage());
        }
    }

    private void importTrains() {
        System.out.println("\n⏳ Loading Trains from database...");
        try {
            trainRepo.loadFromDatabase();
            System.out.println("✓ Trains loaded: " + trainRepo.count());
        } catch (Exception e) {
            System.err.println("✗ Error loading Trains: " + e.getMessage());
        }
    }

    private void importRoutes() {
        System.out.println("\n⏳ Loading Routes from database...");
        try {
            routeRepo.loadFromDatabase();
            System.out.println("✓ Routes loaded: " + routeRepo.count());
        } catch (Exception e) {
            System.err.println("✗ Error loading Routes: " + e.getMessage());
        }
    }

    private void importSchedules() {
        System.out.println("\n⏳ Loading Schedules from database...");
        try {
            scheduleRepo.loadFromDatabase();
            System.out.println("✓ Schedules loaded: " + scheduleRepo.count());
        } catch (Exception e) {
            System.err.println("✗ Error loading Schedules: " + e.getMessage());
        }
    }

    private void showDataStatus() {
        System.out.println("┌─────────────────────────────────────────────┐");
        System.out.println("│ DATA STATUS (In Memory)                     │");
        System.out.println("├─────────────────────────────────────────────┤");
        System.out.printf("│ Facilities:  %-6d  Lines:      %-6d  │%n",
                facilityRepo.count(), lineRepo.count());
        System.out.printf("│ Segments:    %-6d  Locomotives: %-6d  │%n",
                segmentRepo.count(), locomotiveRepo.count());
        System.out.printf("│ Wagons:      %-6d  Trains:      %-6d  │%n",
                wagonRepo.count(), trainRepo.count());
        System.out.printf("│ Routes:      %-6d  Schedules:   %-6d  │%n",
                routeRepo.count(), scheduleRepo.count());
        System.out.println("└─────────────────────────────────────────────┘");
    }

    private void pauseForUser() {
        System.out.println("\nPress ENTER to continue...");
        scanner.nextLine();
    }
}