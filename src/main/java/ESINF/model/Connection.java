package ESINF.model;

public class Connection {

        private final RailwayStation from;
        private final RailwayStation to;
        private final double distance;      // km
        private final double capacity;      // trains/day
        private final double cost;          // metric combining distance, congestion, etc.

        public Connection(RailwayStation from, RailwayStation to,
                          double distance, double capacity, double cost) {
            this.from = from;
            this.to = to;
            this.distance = distance;
            this.capacity = capacity;
            this.cost = cost;
        }
        public RailwayStation getFrom() { return from; }
        public RailwayStation getTo() { return to; }
        public double getDistance() { return distance; }
        public double getCapacity() { return capacity; }
        public double getCost() { return cost; }

        
    }

