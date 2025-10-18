package LAPR3.UI.utils;

import java.util.List;

/**
 * Utility class for formatting data in table format
 */
public class TableFormatter {

    /**
     * Format data as a simple table
     */
    public void printTable(String[] headers, List<String[]> rows) {
        if (headers == null || headers.length == 0) return;

        // Calculate column widths
        int[] widths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            widths[i] = headers[i].length();
        }

        for (String[] row : rows) {
            for (int i = 0; i < Math.min(row.length, widths.length); i++) {
                widths[i] = Math.max(widths[i], row[i].length());
            }
        }

        // Print header
        printSeparator(widths);
        printRow(headers, widths);
        printSeparator(widths);

        // Print rows
        for (String[] row : rows) {
            printRow(row, widths);
        }

        printSeparator(widths);
    }

    /**
     * Print a box with title and content
     */
    public void printBox(String title, String content) {
        int width = Math.max(title.length(), content.length()) + 4;

        System.out.println("┌" + "─".repeat(width) + "┐");
        System.out.println("│ " + padRight(title, width - 2) + " │");
        System.out.println("├" + "─".repeat(width) + "┤");
        System.out.println("│ " + padRight(content, width - 2) + " │");
        System.out.println("└" + "─".repeat(width) + "┘");
    }

    /**
     * Print a simple separator line
     */
    public void printSeparator(int length) {
        System.out.println("─".repeat(length));
    }

    private void printSeparator(int[] widths) {
        for (int i = 0; i < widths.length; i++) {
            System.out.print("─".repeat(widths[i] + 2));
            if (i < widths.length - 1) {
                System.out.print("┼");
            }
        }
        System.out.println();
    }

    private void printRow(String[] cells, int[] widths) {
        for (int i = 0; i < cells.length && i < widths.length; i++) {
            System.out.print(" " + padRight(cells[i], widths[i]) + " ");
            if (i < cells.length - 1) {
                System.out.print("│");
            }
        }
        System.out.println();
    }

    private String padRight(String text, int length) {
        return String.format("%-" + length + "s", text);
    }
}