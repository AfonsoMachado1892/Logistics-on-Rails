package LAPR3.repository;

import LAPR3.IO.CSVReader;
import LAPR3.model.Locomotive;

import java.io.IOException;
import java.util.*;

public class LocomotiveRepository {
    private final Map<Integer, Locomotive> locomotives;

    public LocomotiveRepository() {
        this.locomotives = new HashMap<>();
    }

    /**
     * Load locomotives from CSV file
     * Expected format: Number, Name, Make, Model, Service, Number Bogies, Bogies,
     *                  Power, Length, Width, Height, Weight (t), Max Speed,
     *                  Operational Speed, Traction (KN), Type, Voltage, Frequency,
     *                  Operator, Bitola, Combustível (l)
     * @param filePath path to locomotives.csv
     * @throws IOException if file cannot be read
     */
    public void loadFromCSV(String filePath) throws IOException {
        List<String[]> records = CSVReader.readCSV(filePath);

        for (String[] record : records) {
            try {
                if (record.length < 20) {
                    System.err.println("Invalid locomotive record: insufficient columns");
                    continue;
                }

                int number = CSVReader.parseInt(record[0], -1);
                String name = record[1];
                String make = record[2];
                String model = record[3];
                int serviceYear = CSVReader.parseInt(record[4], 0);
                int numberBogies = CSVReader.parseInt(record[5], -1);
                String bogiesType = record[6];
                double power = CSVReader.parseDouble(record[7], -1);
                double length = CSVReader.parseDouble(record[8], -1);
                double width = CSVReader.parseDouble(record[9], -1);
                double height = CSVReader.parseDouble(record[10], -1);
                double weight = CSVReader.parseDouble(record[11], -1);
                double maxSpeed = CSVReader.parseDouble(record[12], -1);
                double operationalSpeed = CSVReader.parseDouble(record[13], -1);
                double traction = CSVReader.parseDouble(record[14], -1);
                String typeStr = record[15];
                String voltage = record[16];
                String frequency = record[17];
                String operator = record[18];
                int gauge = CSVReader.parseInt(record[19], -1);
                String fuelStr = record.length > 20 ? record[20] : "";

                if (number < 0 || operationalSpeed <= 0 || gauge < 0) {
                    System.err.println("Invalid locomotive data: " + number);
                    continue;
                }

                // Parse type
                Locomotive.LocomotiveType type;
                if (typeStr.equalsIgnoreCase("Electric")) {
                    type = Locomotive.LocomotiveType.ELECTRIC;
                } else if (typeStr.equalsIgnoreCase("Diesel")) {
                    type = Locomotive.LocomotiveType.DIESEL;
                } else {
                    System.err.println("Unknown locomotive type: " + typeStr);
                    continue;
                }

                // Parse fuel capacity (only for diesel)
                Double fuelCapacity = null;
                if (type == Locomotive.LocomotiveType.DIESEL && !fuelStr.isEmpty()) {
                    fuelCapacity = CSVReader.parseDouble(fuelStr, 0);
                }

                Locomotive locomotive = new Locomotive(
                        number, name, make, model, serviceYear, numberBogies,
                        bogiesType, power, length, width, height, weight,
                        maxSpeed, operationalSpeed, traction, type, voltage,
                        frequency, operator, gauge, fuelCapacity
                );

                locomotives.put(number, locomotive);

            } catch (IllegalArgumentException e) {
                System.err.println("Error loading locomotive: " + e.getMessage());
            }
        }

        System.out.println("Loaded " + locomotives.size() + " locomotives");
    }

    /**
     * Find locomotive by number
     */
    public Optional<Locomotive> findByNumber(int number) {
        return Optional.ofNullable(locomotives.get(number));
    }

    /**
     * Find all locomotives of a specific type
     */
    public List<Locomotive> findByType(Locomotive.LocomotiveType type) {
        return locomotives.values().stream()
                .filter(loco -> loco.getType() == type)
                .toList();
    }

    /**
     * Find all locomotives compatible with a specific gauge
     */
    public List<Locomotive> findByGauge(int gauge) {
        return locomotives.values().stream()
                .filter(loco -> loco.getGauge() == gauge)
                .toList();
    }

    /**
     * Find all locomotives of a specific operator
     */
    public List<Locomotive> findByOperator(String operator) {
        return locomotives.values().stream()
                .filter(loco -> loco.getOperator().equalsIgnoreCase(operator))
                .toList();
    }

    /**
     * Get all locomotives
     */
    public List<Locomotive> findAll() {
        return new ArrayList<>(locomotives.values());
    }

    /**
     * Check if locomotive exists
     */
    public boolean exists(int number) {
        return locomotives.containsKey(number);
    }

    /**
     * Get total count
     */
    public int count() {
        return locomotives.size();
    }
}
