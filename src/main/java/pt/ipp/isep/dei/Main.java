package pt.ipp.isep.dei;

import ESINF.IO.*;
import ESINF.model.*;
import ESINF.service.*;

import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        ImportLogger logger = new ImportLogger();
        DataImporter importer = new DataImporter(logger);

        importer.loadAll("warehouseMngt/train_station_dataset/items.csv", "warehouseMngt/train_station_dataset/bays.csv", "warehouseMngt/train_station_dataset/wagons.csv");

        // Guardar log num ficheiro
        logger.saveToFile("Logs/import_log.txt");

        // Obter objetos carregados
        Map<String, Warehouse> warehouses = importer.getWarehouses();
        List<Wagon> wagons = importer.getWagons();

        // Exemplo de descarregamento
        Warehouse warehouse = warehouses.get("W1");
        WarehouseManager manager = new WarehouseManager(warehouse);
        manager.unloadWagons(wagons);
        manager.printInventory();
    }
}
