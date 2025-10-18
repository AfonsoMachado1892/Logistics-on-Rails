package LAPR3;

import LAPR3.UI.ConsoleUI;

/**
 * Entry point for the Railway Terminal Cargo Handling System
 * USLP02 - User-friendly text-based interface implementation
 */
public class Application {

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║   RAILWAY TERMINAL - CARGO HANDLING SYSTEM                 ║");
        System.out.println("║   Station Storage Manager Interface                        ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();

        ConsoleUI ui = new ConsoleUI();
        ui.run();

        System.out.println("\nThank you for using the Cargo Handling System!");
    }
}