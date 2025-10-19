package LAPR3.model;

import java.util.Objects;

public class Facility {
    private final int id;
    private final String name;

    /**
     * Creates a new Facility
     * @param id facility identifier
     * @param name facility name
     * @throws IllegalArgumentException if id is negative or name is null/empty
     */
    public Facility(int id, String name) {
        if (id < 0) {
            throw new IllegalArgumentException("Facility ID cannot be negative");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Facility name cannot be null or empty");
        }
        this.id = id;
        this.name = name.trim();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Facility facility = (Facility) o;
        return id == facility.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Facility{id=%d, name='%s'}", id, name);
    }
}
