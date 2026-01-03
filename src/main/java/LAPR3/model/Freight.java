package LAPR3.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Freight {
    private final int id;
    private final LocalDate date;
    private final int originId;
    private final int destinationId;
    private final String originName;
    private final String destinationName;
    private final List<Wagon> assignedWagons;
    private String assignedRouteId;

    public Freight(int id, LocalDate date, int originId, int destinationId, String originName, String destinationName) {
        this.id = id;
        this.date = date;
        this.originId = originId;
        this.destinationId = destinationId;
        this.originName = originName;
        this.destinationName = destinationName;
        this.assignedWagons = new ArrayList<>();
        this.assignedRouteId = null;
    }

    public int getId() { return id; }
    public LocalDate getDate() { return date; }
    public int getOriginId() { return originId; }
    public int getDestinationId() { return destinationId; }
    public String getOriginName() { return originName; }
    public String getDestinationName() { return destinationName; }
    public String getAssignedRouteId() { return assignedRouteId; }
    public void setAssignedRouteId(String routeId) { this.assignedRouteId = routeId; }

    public void addWagon(Wagon wagon) {
        if (wagon != null) {
            boolean alreadyExists = assignedWagons.stream()
                    .anyMatch(w -> w.getWagonNumber().equals(wagon.getWagonNumber()));
            if (!alreadyExists) {
                this.assignedWagons.add(wagon);
            }
        }
    }

    public List<Wagon> getAssignedWagons() { return assignedWagons; }
}