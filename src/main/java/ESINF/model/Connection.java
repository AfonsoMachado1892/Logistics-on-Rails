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
       
    }

