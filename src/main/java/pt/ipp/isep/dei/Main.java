package pt.ipp.isep.dei;

import ESINF.DTO.AllocationOutput;
import ESINF.IO.*;
import ESINF.model.*;
import ESINF.service.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        ImportLogger logger = new ImportLogger();
        DataImporter importer = new DataImporter(logger);

        importer.loadAll(
                "warehouseMngt/train_station_dataset/items.csv",
                "warehouseMngt/train_station_dataset/bays.csv",
                "warehouseMngt/train_station_dataset/wagons.csv"
        );

        // Guardar log num ficheiro
        logger.saveToFile("Logs/import_log.txt");

        // Obter objetos carregados
        Map<String, Warehouse> warehouses = importer.getWarehouses();
        List<Wagon> wagons = importer.getWagons();

        // === USEI01 ===
        Warehouse warehouse = warehouses.get("W1");
        WarehouseManager manager = new WarehouseManager(warehouse);
        manager.unloadWagons(wagons);
        manager.printInventory();

        // === USEI02 ===
        OrderImporter orderImporter = new OrderImporter(logger, importer.getItems());
        orderImporter.loadOrders(
                "warehouseMngt/train_station_dataset/orders.csv",
                "warehouseMngt/train_station_dataset/order_lines.csv"
        );

        orderImporter.printStats();
        List<Order> orders = orderImporter.getOrders();
        AllocationManager allocationManager = new AllocationManager(manager);

        // === MODO PARCIAL ===
        System.out.println("\n=== ORDER ELIGIBILITY RESULTS (PARTIAL MODE) ===");
        AllocationOutput outputPartial = allocationManager.allocateOrders(orders, false);
        for (OrderEligibility res : outputPartial.results()) {
            System.out.printf("Order %s | Line %d | SKU=%s | Req=%d | Alloc=%d | %s%n",
                    res.getOrderId(), res.getLineNo(), res.getSku(),
                    res.getRequestedQty(), res.getAllocatedQty(), res.getStatus());
        }

        System.out.println("\n=== ALLOCATION DETAILS (PARTIAL MODE) ===");
        for (Allocation row : outputPartial.allocations()) {
            System.out.printf("Order %s | Line %d | SKU=%s | Qty=%d | Box=%s | Aisle=%s | Bay=%s%n",
                    row.getOrderId(), row.getLineNo(), row.getSku(), row.getQty(),
                    row.getBoxId(), row.getAisle(), row.getBay());
        }

        saveEligibilityCSV(outputPartial, "Logs/eligibility_partial.csv");
        saveAllocationCSV(outputPartial, "Logs/allocation_partial.csv");

        // === MODO ESTRITO ===
        System.out.println("\n=== ORDER ELIGIBILITY RESULTS (STRICT MODE) ===");
        AllocationOutput outputStrict = allocationManager.allocateOrders(orders, true);
        for (OrderEligibility res : outputStrict.results()) {
            System.out.printf("Order %s | Line %d | SKU=%s | Req=%d | Alloc=%d | %s%n",
                    res.getOrderId(), res.getLineNo(), res.getSku(),
                    res.getRequestedQty(), res.getAllocatedQty(), res.getStatus());
        }

        System.out.println("\n=== ALLOCATION DETAILS (STRICT MODE) ===");
        for (Allocation row : outputStrict.allocations()) {
            System.out.printf("Order %s | Line %d | SKU=%s | Qty=%d | Box=%s | Aisle=%s | Bay=%s%n",
                    row.getOrderId(), row.getLineNo(), row.getSku(), row.getQty(),
                    row.getBoxId(), row.getAisle(), row.getBay());
        }

        saveEligibilityCSV(outputStrict, "Logs/eligibility_strict.csv");
        saveAllocationCSV(outputStrict, "Logs/allocation_strict.csv");


        // === USEI05 – RETURNS & QUARANTINE ===

        System.out.println("\n=== USEI05 – RETURNS & QUARANTINE ===");

        ReturnImporter returnImporter = new ReturnImporter(logger, importer.getItems());
        List<ReturnItem> returns = returnImporter.loadReturns("warehouseMngt/train_station_dataset/returns.csv");

        // Create the audit logger and quarantine manager
        AuditLogger auditLogger = new AuditLogger("Logs/audit_log.txt");
        QuarantineManager quarantineManager = new QuarantineManager(warehouse, auditLogger);

        // Process all returns
        quarantineManager.processReturns(returns);

        System.out.println("Returns processed successfully. Audit log generated at Logs/audit_log.txt");
        manager.printInventory(); //apenas para testar

    }

    /**
     * Guarda o resultado de elegibilidade em formato CSV.
     */
    private static void saveEligibilityCSV(AllocationOutput output, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("orderId,lineNo,sku,requestedQty,allocatedQty,status\n");
            for (OrderEligibility res : output.results()) {
                writer.write(String.format("%s,%d,%s,%d,%d,%s\n",
                        res.getOrderId(),
                        res.getLineNo(),
                        res.getSku(),
                        res.getRequestedQty(),
                        res.getAllocatedQty(),
                        res.getStatus()));
            }
        } catch (IOException e) {
            System.err.println("Erro ao escrever " + filePath + ": " + e.getMessage());
        }
    }

    /**
     * Guarda os detalhes da alocação em formato CSV.
     */
    private static void saveAllocationCSV(AllocationOutput output, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("orderId,lineNo,sku,qty,boxId,aisle,bay\n");
            for (Allocation row : output.allocations()) {
                writer.write(String.format("%s,%d,%s,%d,%s,%s,%s\n",
                        row.getOrderId(),
                        row.getLineNo(),
                        row.getSku(),
                        row.getQty(),
                        row.getBoxId(),
                        row.getAisle(),
                        row.getBay()));
            }
        } catch (IOException e) {
            System.err.println("Erro ao escrever " + filePath + ": " + e.getMessage());
        }
    }
}
