package LAPR3.model;

import java.util.Objects;

public class Line {
    private final int id;
    private final String name;
    private final String ownerVat;
    private final int startFacilityId;
    private final String startFacilityName; // DADO NOVO (Vem do JOIN)
    private final int endFacilityId;
    private final String endFacilityName;   // DADO NOVO (Vem do JOIN)
    private final double gauge;

    public Line(int id, String name, String ownerVat,
                int startFacilityId, String startFacilityName,
                int endFacilityId, String endFacilityName,
                double gauge) {
        this.id = id;
        this.name = name;
        this.ownerVat = ownerVat;
        this.startFacilityId = startFacilityId;
        this.startFacilityName = startFacilityName;
        this.endFacilityId = endFacilityId;
        this.endFacilityName = endFacilityName;
        this.gauge = gauge;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getOwnerVat() { return ownerVat; }
    public int getStartFacilityId() { return startFacilityId; }
    public String getStartFacilityName() { return startFacilityName; }
    public int getEndFacilityId() { return endFacilityId; }
    public String getEndFacilityName() { return endFacilityName; }
    public double getGauge() { return gauge; }

    @Override
    public String toString() {
        return String.format("Line %d: %s (%s -> %s)", id, name, startFacilityName, endFacilityName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Line line = (Line) o;
        return id == line.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}