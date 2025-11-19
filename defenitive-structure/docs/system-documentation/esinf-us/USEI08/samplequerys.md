# USEI08 - Sample Queries

This document contains 5 representative sample queries demonstrating the geographic area search functionality.

---

## Setup Code
```java
// Load data and build tree (once)
StationCSVReader reader = new StationCSVReader();
List<Station> stations = reader.readStations("data/stations.csv");

StationIndex index = new StationIndex();
for (Station s : stations) index.addStation(s);
index.finalizeIndex();

KDTree2D kdTree = new KDTree2D();
kdTree.buildBalanced(
    index.getStationsByLatitudeRange(-90, 90),
    index.getStationsByLongitudeRange(-180, 180)
);

GeographicSearchController controller = new GeographicSearchController(kdTree);
```

---

## Query 1: All Stations in Portugal Continental

### **Description**
Find all railway stations within Portugal's mainland territory.

### **Code**
```java
GeographicSearchController.SearchResult result = controller.searchByGeographicArea(
    36.96, 42.15,   // Latitude: Algarve to Minho
    -9.50, -6.19,   // Longitude: Atlantic coast to Spanish border
    StationFilter.forCountry("PT")
);

System.out.println("Stations in Portugal: " + result.getStationCount());
System.out.println("Execution time: " + result.getExecutionTimeMs() + " ms");
```

### **Expected Output**
```
Region: GeographicRegion{lat=[36,96000, 42,15000], lon=[-9,50000, -6,19000], area=17,17890°²}
Filter: StationFilter{country='PT'}
✅ Found: 168 stations in 0,290 ms
Sample results:
  • Mogadouro (PT)
  • Vila Real de San Antonio Guad (PT)
  • Lagos (PT)
```

### **Why This Query Matters**
- Tests country filtering
- Medium-sized geographic region
- Realistic use case for national rail network analysis

---

## Query 2: Main Stations in Madrid Region

### **Description**
Find major railway hubs (main stations) in the Madrid metropolitan area.

### **Code**
```java
GeographicSearchController.SearchResult result = controller.searchByGeographicArea(
    40.2, 40.7,     // Latitude: Madrid region
    -4.0, -3.4,     // Longitude: Madrid region
    StationFilter.mainStationsInCountry("ES")
);

System.out.println("Main stations in Madrid: " + result.getStationCount());

for (Station station : result.getStations()) {
    System.out.println("  - " + station.getName());
}
```

### **Expected Output**
```
Region: GeographicRegion{lat=[40,20000, 40,70000], lon=[-4,00000, -3,40000], area=0,30000°²}
Filter: StationFilter{isMainStation=true, country='ES'}
✅ Found: 1 stations in 0,064 ms
Sample results:
  • Madrid Atocha (ES)
```

### **Why This Query Matters**
- Tests combined filter (isMainStation + country)
- Small geographic region (fast query)
- Useful for finding major hubs in metropolitan areas

---

## Query 3: Coastal Cities in Spain

### **Description**
Find cities along Spain's Mediterranean coast.

### **Code**
```java
GeographicSearchController.SearchResult result = controller.searchByGeographicArea(
    36.5, 43.0,     // Latitude: Southern coast to northern coast
    -1.0, 4.5,      // Longitude: Mediterranean coastline
    StationFilter.citiesInCountry("ES")
);

System.out.println("Coastal cities in Spain: " + result.getStationCount());
System.out.println("Execution time: " + result.getExecutionTimeMs() + " ms");

// Group by region
Map<String, Long> byRegion = result.getStations().stream()
    .collect(Collectors.groupingBy(
        s -> s.getTimeZoneGroup(),
        Collectors.counting()
    ));
System.out.println("Distribution: " + byRegion);
```

### **Expected Output**
```
Region: GeographicRegion{lat=[36,50000, 43,00000], lon=[-1,00000, 4,50000], area=35,75000°²}
Filter: StationFilter{isCity=true, country='ES'}
✅ Found: 740 stations in 0,136 ms
Sample results:
  • Llardecans (ES)
  • Almatret (ES)
  • La Manga del Mar Menor (ES)
```
### **Why This Query Matters**
- Tests isCity filter
- Long, narrow geographic region (tests pruning efficiency)
- Real-world use case for coastal transport planning

## Query 4: Airports in Germany

### **Description**
Find railway stations located at or near airports in Germany.

### Code
```java
// Create custom filter for airports
StationFilter airportFilter = new StationFilter(null, null, "DE") {
@Override
public boolean matches(Station station) {
return super.matches(station) && station.isAirport();
}
};

GeographicSearchController.SearchResult result = controller.searchByGeographicArea(
47.3, 55.0,     // Latitude: Southern to Northern Germany
5.9, 15.0,      // Longitude: Western to Eastern border
airportFilter
);

System.out.println("Airport stations in Germany: " + result.getStationCount());
System.out.println("Execution time: " + result.getExecutionTimeMs() + " ms");

result.getStations().forEach(station ->
System.out.println("  - " + station.getName() +
" (" + station.getLatitude() + ", " +
station.getLongitude() + ")")
);
```

### **Expected Output**
```
Region: GeographicRegion{lat=[45,80000, 47,80000], lon=[5,90000, 10,50000], area=9,20000°²}
Filter: StationFilter{country='CH'}
✅ Found: 22225 stations in 3,623 ms
Sample results:
  • Langnau am Albis Unterrengg (CH)
  • Munsingen Neuhaus (CH)
  • Chemin-Dessous (CH)
```
### **Why This Query Matters**
- Tests isAirport flag (custom filter extension)
- Medium-sized country search
- Important for intermodal transport planning (rail-air connections)

## Query 5: Swiss Mountain Stations

### **Description**
Find all railway stations in Switzerland (mountainous terrain).

### **Code**
```java
GeographicSearchController.SearchResult result = controller.searchByGeographicArea(
    45.8, 47.8,     // Latitude: South to North Switzerland
    5.9, 10.5,      // Longitude: West to East Switzerland
    StationFilter.forCountry("CH")
);

System.out.println("Stations in Switzerland: " + result.getStationCount());
System.out.println("Execution time: " + result.getExecutionTimeMs() + " ms");

// Find highest altitude stations (approximate by latitude)
List<Station> sortedByLat = result.getStations().stream()
    .sorted(Comparator.comparingDouble(Station::getLatitude).reversed())
    .limit(5)
    .toList();

System.out.println("\nNorthernmost stations (likely higher altitude):");
sortedByLat.forEach(s -> 
    System.out.println("  - " + s.getName() + 
                       " (lat: " + String.format("%.5f", s.getLatitude()) + ")")
);
```

### **Expected Output**
```
Region: GeographicRegion{lat=[49,50000, 53,50000], lon=[2,50000, 7,20000], area=18,80000°²}
Filter: StationFilter{isCity=true}
✅ Found: 72 stations in 0,252 ms
Sample results:
  • Peronne (FR)
  • Chambry (FR)
  • Hirson (FR)

```
### **Why This Query Matters**
- Tests query in compact, densely-stationed country
- Small geographic area with high station density
- Demonstrates fast performance even with many results
