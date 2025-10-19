package LAPR3.IO;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {
    /**
     * Read CSV file and return list of rows (excluding header)
     * @param filePath path to CSV file
     * @return list of string arrays, each representing a row
     * @throws IOException if file cannot be read
     */
    public static List<String[]> readCSV(String filePath) throws IOException {
        List<String[]> records = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                // Skip header line
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] values = parseLine(line);
                records.add(values);
            }
        }

        return records;
    }

    /**
     * Parse a CSV line handling quoted fields and tabs
     * @param line CSV line to parse
     * @return array of values
     */
    private static String[] parseLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == '\t' && !inQuotes) {
                result.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        result.add(current.toString().trim());
        return result.toArray(new String[0]);
    }

    /**
     * Parse integer value, return default if invalid
     */
    public static int parseInt(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Parse double value, return default if invalid
     */
    public static double parseDouble(String value, double defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Parse boolean value (Yes/No)
     */
    public static boolean parseBoolean(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        String v = value.trim().toLowerCase();
        return v.equals("yes") || v.equals("true") || v.equals("1");
    }
}
