package LAPR3.service;

import LAPR3.model.*;
import LAPR3.repository.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for USLP09 - Train Assembly and Route Assignment
 * Assembles trains and assigns them to routes
 * Everything stored IN-MEMORY only
 */
public class TrainAssemblyService {
    private final LocomotiveRepository locomotiveRepo;
    private final WagonRepository wagonRepo;
    private final RoutePlanningService routePlanningService;

    private final List<AssembledTrain> assembledTrains = new ArrayList<>();
    private int nextId = 1;

    public TrainAssemblyService(LocomotiveRepository locomotiveRepo,
                                WagonRepository wagonRepo,
                                RoutePlanningService routePlanningService) {
        this.locomotiveRepo = locomotiveRepo;
        this.wagonRepo = wagonRepo;
        this.routePlanningService = routePlanningService;
    }

    /**
     * USLP09 - Assemble train and assign to route
     */
    public AssembledTrain assembleAndAssign(String operatorVat,
                                            List<String> locomotiveIds,
                                            List<String> wagonIds,
                                            String routeId) {

        // Validate route exists and is unassigned
        PlannedRoute route = routePlanningService.getRoute(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + routeId));

        if (route.isAssigned()) {
            throw new IllegalArgumentException("Route " + routeId + " is already assigned to a train");
        }

        // Validate locomotives
        List<Locomotive> locomotives = new ArrayList<>();
        for (String locoId : locomotiveIds) {
            Locomotive loco = locomotiveRepo.findByNumber(locoId)
                    .orElseThrow(() -> new IllegalArgumentException("Locomotive not found: " + locoId));
            locomotives.add(loco);
        }

        // Validate wagons
        List<Wagon> wagons = new ArrayList<>();
        for (String wagonId : wagonIds) {
            Wagon wagon = wagonRepo.findById(wagonId)
                    .orElseThrow(() -> new IllegalArgumentException("Wagon not found: " + wagonId));
            wagons.add(wagon);
        }

        // Calculate totals
        double totalPowerKw = locomotives.stream().mapToDouble(Locomotive::getPowerKw).sum();
        double totalWeightTons = locomotives.stream().mapToDouble(Locomotive::getWeightTons).sum()
                + wagons.stream().mapToDouble(w -> w.getWeightTons() + w.getPayloadTons()).sum();

        // Create assembled train
        String trainId = generateTrainId();
        AssembledTrain train = new AssembledTrain(
                trainId, operatorVat, locomotiveIds, wagonIds, routeId,
                totalPowerKw, totalWeightTons
        );

        // Mark route as assigned
        route.setAssignedTrainId(trainId);

        assembledTrains.add(train);
        return train;
    }

    public List<AssembledTrain> getAllTrains() {
        return new ArrayList<>(assembledTrains);
    }

    public Optional<AssembledTrain> getTrain(String trainId) {
        return assembledTrains.stream()
                .filter(t -> t.getId().equals(trainId))
                .findFirst();
    }

    public List<AssembledTrain> getTrainsByRoute(String routeId) {
        return assembledTrains.stream()
                .filter(t -> t.getRouteId().equals(routeId))
                .collect(Collectors.toList());
    }

    private String generateTrainId() {
        return "T-" + String.format("%03d", nextId++);
    }
}