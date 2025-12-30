package LAPR3.model;

/**
 * Wagon model with complete technical specifications
 */
public class Wagon {
    private String wagonNumber;
    private String operatorVat;
    private String modelName;
    private String maker;
    private String typeDesc;
    private int numBogies;
    private String bogiesType;
    private double weightTons;
    private double payloadTons;
    private double maxSpeedKmh;
    private double lengthMm;
    private double widthMm;
    private double heightMm;
    private double volumeM3;
    private double gauge;

    // Full constructor
    public Wagon(String wagonNumber, String operatorVat, String modelName, String maker,
                 String typeDesc, int numBogies, String bogiesType, double weightTons,
                 double payloadTons, double maxSpeedKmh, double lengthMm, double widthMm,
                 double heightMm, double volumeM3, double gauge) {
        this.wagonNumber = wagonNumber;
        this.operatorVat = operatorVat;
        this.modelName = modelName;
        this.maker = maker;
        this.typeDesc = typeDesc;
        this.numBogies = numBogies;
        this.bogiesType = bogiesType;
        this.weightTons = weightTons;
        this.payloadTons = payloadTons;
        this.maxSpeedKmh = maxSpeedKmh;
        this.lengthMm = lengthMm;
        this.widthMm = widthMm;
        this.heightMm = heightMm;
        this.volumeM3 = volumeM3;
        this.gauge = gauge;
    }

    // Getters
    public String getWagonNumber() { return wagonNumber; }
    public String getOperatorVat() { return operatorVat; }
    public String getModelName() { return modelName; }
    public String getMaker() { return maker; }
    public String getTypeDesc() { return typeDesc; }
    public int getNumBogies() { return numBogies; }
    public String getBogiesType() { return bogiesType; }
    public double getWeightTons() { return weightTons; }
    public double getPayloadTons() { return payloadTons; }
    public double getMaxSpeedKmh() { return maxSpeedKmh; }
    public double getLengthMm() { return lengthMm; }
    public double getWidthMm() { return widthMm; }
    public double getHeightMm() { return heightMm; }
    public double getVolumeM3() { return volumeM3; }
    public double getGauge() { return gauge; }

    // Setters
    public void setWagonNumber(String wagonNumber) { this.wagonNumber = wagonNumber; }
    public void setOperatorVat(String operatorVat) { this.operatorVat = operatorVat; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public void setMaker(String maker) { this.maker = maker; }
    public void setTypeDesc(String typeDesc) { this.typeDesc = typeDesc; }
    public void setNumBogies(int numBogies) { this.numBogies = numBogies; }
    public void setBogiesType(String bogiesType) { this.bogiesType = bogiesType; }
    public void setWeightTons(double weightTons) { this.weightTons = weightTons; }
    public void setPayloadTons(double payloadTons) { this.payloadTons = payloadTons; }
    public void setMaxSpeedKmh(double maxSpeedKmh) { this.maxSpeedKmh = maxSpeedKmh; }
    public void setLengthMm(double lengthMm) { this.lengthMm = lengthMm; }
    public void setWidthMm(double widthMm) { this.widthMm = widthMm; }
    public void setHeightMm(double heightMm) { this.heightMm = heightMm; }
    public void setVolumeM3(double volumeM3) { this.volumeM3 = volumeM3; }
    public void setGauge(double gauge) { this.gauge = gauge; }

    /**
     * Get total weight including payload
     */
    public double getTotalWeightTons() {
        return weightTons + payloadTons;
    }

    @Override
    public String toString() {
        return String.format("Wagon[%s, %s, %.1ft, %dmm]",
                wagonNumber, typeDesc, getTotalWeightTons(), (int)gauge);
    }
}