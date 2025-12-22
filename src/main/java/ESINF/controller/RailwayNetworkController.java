package ESINF.controller;


import ESINF.model.MaxFlowResult;
import ESINF.model.RailwayNetwork;
import ESINF.model.RailwayStation;
import ESINF.model.Connection;
import ESINF.service.MaxFlowCalculator;

import java.io.*;
import java.util.*;

/**
 * CONTROLLER - Railway Network Operations Controller
 *
 * Orchestrates business operations related to the railway network.
 * Handles use cases like:
 * - Loading data from CSV
 * - Calculating max flow between hubs
 * - Validating inputs
 * - Coordinating between models and algorithms
 *
 * This is where BUSINESS LOGIC and USE CASES live.
 *
 * @author ESINF Team
 * @version 1.0
 */
public class RailwayNetworkController {

    private RailwayNetwork network;

    /**
     * Creates a new controller with an empty network
     */
    public RailwayNetworkController() {
        this.network = new RailwayNetwork();
    }

    /**
     * Creates a controller with an existing network
     *
     * @param network the railway network
     */
    public RailwayNetworkController(RailwayNetwork network) {
        this.network = network;
    }

    
}