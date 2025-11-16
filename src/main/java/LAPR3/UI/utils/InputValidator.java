package LAPR3.UI.utils;

import java.util.Scanner;

/**
 * Utility class for validating and reading user input
 */
public class InputValidator {

    private final Scanner scanner;

    public InputValidator(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Read an integer within a range
     */
    public int readInt(String prompt, int min, int max) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    System.out.println("Input cannot be empty. Please try again.");
                    continue;
                }

                int value = Integer.parseInt(input);

                if (value < min || value > max) {
                    System.out.printf("Value must be between %d and %d. Please try again.%n", min, max);
                    continue;
                }

                return value;

            } catch (NumberFormatException e) {
                System.out.println("Invalid number format. Please enter a valid integer.");
            }
        }
    }

    /**
     * Read a non-empty string
     */
    public String readString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("Input cannot be empty. Please try again.");
                continue;
            }

            return input;
        }
    }

    /**
     * Read a file path
     */
    public String readFilePath(String prompt, String defaultPath) {
        System.out.print(prompt);
        System.out.printf(" [default: %s]: ", defaultPath);
        String input = scanner.nextLine().trim();

        return input.isEmpty() ? defaultPath : input;
    }

    /**
     * Read a yes/no confirmation
     */
    public boolean readConfirmation(String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("y") || input.equals("yes")) {
                return true;
            } else if (input.equals("n") || input.equals("no")) {
                return false;
            } else {
                System.out.println("Please enter 'y' or 'n'.");
            }
        }
    }

    /**
     * Read a selection from a list
     */
    public int readSelection(String prompt, int optionsCount) {
        return readInt(prompt, 1, optionsCount);
    }

    /**
     * Reads a double value within a specified range with validation.
     *
     * @param prompt Message to display to user
     * @param min Minimum acceptable value (inclusive)
     * @param max Maximum acceptable value (inclusive)
     * @return Valid double within range
     */
    public double readDouble(String prompt, double min, double max) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    System.out.println("❌ Input cannot be empty. Please try again.");
                    continue;
                }

                double value = Double.parseDouble(input);

                if (value < min || value > max) {
                    System.out.printf("❌ Value must be between %.2f and %.2f. Please try again.%n", min, max);
                    continue;
                }

                return value;

            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid number format. Please enter a valid decimal number.");
            }
        }
    }

    /**
     * Reads a double value without range restriction.
     *
     * @param prompt Message to display to user
     * @return Valid double value
     */
    public double readDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    System.out.println("❌ Input cannot be empty. Please try again.");
                    continue;
                }

                return Double.parseDouble(input);

            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid number format. Please enter a valid decimal number.");
            }
        }
    }

    /**
     * Reads an optional double value (allows empty input).
     *
     * @param prompt Message to display to user
     * @param min Minimum acceptable value
     * @param max Maximum acceptable value
     * @return Valid double or null if user skips (presses ENTER)
     */
    public Double readOptionalDouble(String prompt, double min, double max) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();

                // Allow empty input (user skips)
                if (input.isEmpty()) {
                    return null;
                }

                double value = Double.parseDouble(input);

                if (value < min || value > max) {
                    System.out.printf("❌ Value must be between %.2f and %.2f. Please try again.%n", min, max);
                    continue;
                }

                return value;

            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid number format. Please enter a valid decimal number or press ENTER to skip.");
            }
        }
    }
}