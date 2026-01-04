package LAPR3.UI.javafx;

import LAPR3.repository.*;
import LAPR3.service.*;
import LAPR3.service.AutomaticPathPlanningService;

/**
 * Singleton manager to share services across all controllers
 * Ensures all controllers use the same service instances
 */
public class ServiceManager {
    private static ServiceManager instance;
    
    private FreightRepository freightRepo;
    private FacilityRepository facilityRepo;
    private LocomotiveRepository locomotiveRepo;
    private WagonRepository wagonRepo;
    private TrainRepository trainRepo;
    private ScheduleRepository scheduleRepo;
    private LineRepository lineRepo;
    private SegmentRepository segmentRepo;
    private RouteRepository routeRepo;
    
    private RoutePlanningService routePlanningService;
    private AutomaticPathPlanningService automaticPathService;
    private TrainDispatcherService dispatcherService;
    private ScheduleVisualizationService visualService;
    
    private ServiceManager() {
        initializeRepositories();
        initializeServices();
    }
    
    public static synchronized ServiceManager getInstance() {
        if (instance == null) {
            instance = new ServiceManager();
        }
        return instance;
    }
    
    private void initializeRepositories() {
        freightRepo = new FreightRepository();
        facilityRepo = new FacilityRepository();
        locomotiveRepo = new LocomotiveRepository();
        wagonRepo = new WagonRepository();
        trainRepo = new TrainRepository();
        scheduleRepo = new ScheduleRepository();
        lineRepo = new LineRepository();
        segmentRepo = new SegmentRepository();
        routeRepo = new RouteRepository();
        
        try {
            freightRepo.loadFromDatabase();
            facilityRepo.loadFromDatabase();
            locomotiveRepo.loadFromDatabase();
            wagonRepo.loadFromDatabase();
            trainRepo.loadFromDatabase();
            scheduleRepo.loadFromDatabase();
            lineRepo.loadFromDatabase();
            segmentRepo.loadFromDatabase();
            routeRepo.loadFromDatabase();
        } catch (Exception e) {
            System.err.println("Error loading repositories: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initializeServices() {
        routePlanningService = new RoutePlanningService(freightRepo, facilityRepo);
        automaticPathService = new AutomaticPathPlanningService(facilityRepo, lineRepo, segmentRepo);
        
        RoutePathService routePathService = new RoutePathService(routeRepo, facilityRepo, lineRepo, segmentRepo);
        ScheduleCalculationService scheduleService = new ScheduleCalculationService(
            routeRepo, facilityRepo, lineRepo, segmentRepo, trainRepo, scheduleRepo);
        CrossingConflictDetectionService conflictService = 
            new CrossingConflictDetectionService(scheduleService, segmentRepo);
        CrossingResolutionService resolutionService = 
            new CrossingResolutionService(conflictService, scheduleService, facilityRepo);
        dispatcherService = new TrainDispatcherService(
            scheduleService, conflictService, resolutionService, scheduleRepo, segmentRepo);
        visualService = new ScheduleVisualizationService(facilityRepo);
    }
    
    // Getters for repositories
    public FreightRepository getFreightRepo() { return freightRepo; }
    public FacilityRepository getFacilityRepo() { return facilityRepo; }
    public LocomotiveRepository getLocomotiveRepo() { return locomotiveRepo; }
    public WagonRepository getWagonRepo() { return wagonRepo; }
    public TrainRepository getTrainRepo() { return trainRepo; }
    public ScheduleRepository getScheduleRepo() { return scheduleRepo; }
    public LineRepository getLineRepo() { return lineRepo; }
    public SegmentRepository getSegmentRepo() { return segmentRepo; }
    public RouteRepository getRouteRepo() { return routeRepo; }
    
    // Getters for services
    public RoutePlanningService getRoutePlanningService() { return routePlanningService; }
    public AutomaticPathPlanningService getAutomaticPathService() { return automaticPathService; }
    public TrainDispatcherService getDispatcherService() { return dispatcherService; }
    public ScheduleVisualizationService getVisualService() { return visualService; }
}

