package LAPR3.DTO;

public class FacilityDTO {
    private final String id; // Tratado como String por consistência
    private final String name;


    public FacilityDTO(String id, String name) {
        this.id = id;
        this.name = name;

    }

    @Override
    public String toString() {
        return String.format("ID: %-10s | Nome: %-20s ", id, name);
    }
}