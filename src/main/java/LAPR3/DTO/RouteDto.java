package LAPR3.DTO;

public class RouteDto {
    private final String id;
    private final String description;

    public RouteDto(String id, String description) {
        this.id = id;
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format("ID: %-10s | Rota: %s", id, description);
    }
}