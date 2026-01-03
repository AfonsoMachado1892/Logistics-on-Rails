package LAPR3.DTO;

public class OperatorDTO {
    private final String vatNumber;
    private final String name;

    public OperatorDTO(String vatNumber, String name) {
        this.vatNumber = vatNumber;
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("VAT: %-12s | Nome: %s", vatNumber, name);
    }
}