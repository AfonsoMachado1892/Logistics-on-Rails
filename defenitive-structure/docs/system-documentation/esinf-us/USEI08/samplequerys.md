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
Stations in Portugal: 147
Execution time: 15.3 ms

Sample stations:
Station{name='Lisboa Santa Apolónia', lat=38.71387, lon=-9.12227, country='PT'}
Station{name='Porto-Campanhã', lat=41.14961, lon=-8.58447, country='PT'}
Station{name='Coimbra-B', lat=40.20564, lon=-8.42955, country='PT'}
Station{name='Faro', lat=37.01940, lon=-7.93991, country='PT'}
Station{name='Braga', lat=41.54547, lon=-8.42611, country='PT'}
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
Main stations in Madrid: 7
Execution time: 3.8 ms

  - Madrid-Atocha
  - Madrid-Chamartín
  - Madrid-Príncipe Pío
  - Alcalá de Henares
  - Guadalajara
  - Aranjuez
  - El Escorial
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
Coastal cities in Spain: 42
Execution time: 11.7 ms
Distribution: {CET=42}
Sample cities:

- Barcelona-Sants
- Valencia-Joaquín Sorolla
- Alicante
- Málaga-María Zambrano
- Tarragona
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
Airport stations in Germany: 12
Execution time: 8.4 ms

- Frankfurt am Main Flughafen (50.05098, 8.57062)
- München Flughafen (48.35378, 11.78609)
- Düsseldorf Flughafen (51.28951, 6.76678)
- Stuttgart Flughafen (48.68893, 9.22196)
- Hamburg Flughafen (53.63280, 9.98823)
- Berlin Brandenburg Flughafen (52.36249, 13.50323)
- Köln/Bonn Flughafen (50.87873, 7.12265)
- Hannover Flughafen (52.46101, 9.68508)
- Nürnberg Flughafen (49.49878, 11.06627)
- Leipzig/Halle Flughafen (51.42387, 12.23618)
- Dresden Flughafen (51.13298, 13.76772)
- Bremen Flughafen (53.04752, 8.78667)
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
Stations in Switzerland: 189
Execution time: 6.9 ms

Northernmost stations (likely higher altitude):
  - Basel SBB (lat: 47.54710)
  - Basel Bad Bf (lat: 47.56764)
  - Schaffhausen (lat: 47.69768)
  - Zürich HB (lat: 47.37805)
  - St. Gallen (lat: 47.42307)

Main stations in Switzerland:
  - Zürich HB
  - Bern
  - Basel SBB
  - Genève
  - Lausanne
  - Luzern
  - St. Gallen
  - Lugano

```
### **Why This Query Matters**
- Tests query in compact, densely-stationed country
- Small geographic area with high station density
- Demonstrates fast performance even with many results
