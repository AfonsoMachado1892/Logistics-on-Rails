package LAPR3.DTO;

public class RollingStockItem {
    private final String id;
    private final String type;     // "Locomotive" ou "Wagon"
    private final String status;   // "In Transit" ou "Parked"
    private final String location; // Nome da Estação (Destino ou Local Atual)
    private final double distance; // Distância (em metros)

    public RollingStockItem(String id, String type, String status, String location, double distance) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.location = location;
        this.distance = distance;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public String getLocation() {
        return location;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public String toString() {
        if ("Parked".equals(status)) {
            return String.format("[%s] ID: %-10s | %-12s | Estação: %-18s | Distância: %.0f m",
                    type, id, status, location, distance);
        } else {
            return String.format("[%s] ID: %-10s | %-12s | Destino Final: %-18s",
                    type, id, status, location);
        }
    }
}