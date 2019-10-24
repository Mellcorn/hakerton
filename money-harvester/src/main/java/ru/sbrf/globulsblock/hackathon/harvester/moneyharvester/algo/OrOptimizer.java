package crvp;

import com.google.ortools.constraintsolver.*;
import com.google.protobuf.Duration;

import java.util.ArrayList;
import java.util.List;

public class OrOptimizer implements CrvpOptimizer {

    private long[][] timeMatrix;
    private long[] nodeMoneyArray;
    private long[] nodePenalties;

    private long[] vehicleCapacites;
    private int[] startNodes;
    private int[] endNodes;

    private long[][] timeWindows;
    private long[] timeWindow;

    @Override
    public void setTimeMatrix(long[][] timeMatrix) {
        this.timeMatrix = timeMatrix;
    }

    @Override
    public void setNodeMoney(long[] nodeMoneyArray) {
        this.nodeMoneyArray = nodeMoneyArray;
        this.nodePenalties = nodeMoneyArray; // simplification as money expected much bigger then time values
    }

    @Override
    public void setVehicleCapacities(long[] capacites) {
        this.vehicleCapacites = capacites;
    }

    @Override
    public void setVehicleStartNodes(int[] nodes) {
        this.startNodes = nodes;
    }

    @Override
    public void setVehicleEndNodes(int[] nodes) {
        this.endNodes = nodes;
    }

    @Override
    public void setTimeWindow(long[] timeWindow) {
        this.timeWindow = timeWindow;
    }

    @Override
    public void setTimeWindows(long[][] timeWindows) {
        this.timeWindows = timeWindows;
    }

    @Override
    public Route[] calculateGreedyRoute() {
        return new Route[0];
    }

    @Override
    public Route[] calculateFastRoute() {
        return calculateRoute(true, null);
    }

    private void validateData() {
        if (timeMatrix == null || nodeMoneyArray == null || vehicleCapacites == null
                || startNodes == null || endNodes == null ||
                (timeWindow == null && timeWindows == null) ) {
            throw new RuntimeException("Not all data set");
        }
        if (timeMatrix.length != nodeMoneyArray.length ) {
            throw new RuntimeException("Inconsistent node arrays length");
        }
        if (vehicleCapacites.length != startNodes.length ) {
            throw new RuntimeException("Inconsistent vehicle arrays length");
        }
        if (startNodes.length != endNodes.length ) {
            throw new RuntimeException("Inconsistent vehicle arrays length");
        }
    }

    @Override
    public Route[] calculateFullyOptimizedRoute(int timeLimit) {
        return calculateRoute(false, timeLimit);
    }

    private Route[] calculateRoute(boolean isFastOptimization, Integer timeLimit) {
        validateData();

        int vehicleNumber = vehicleCapacites.length;
        int nodesNumber = timeMatrix.length;

        // Create Routing Index Manager
        RoutingIndexManager manager =
                new RoutingIndexManager(
                        nodesNumber,
                        vehicleNumber,
                        startNodes,
                        endNodes);

        RoutingModel routing = prepareRoutingModel(vehicleNumber, nodesNumber, manager);

        // ==============================================================================================
        // Setting first solution heuristic.
        RoutingSearchParameters searchParameters;
        if (isFastOptimization) {
            searchParameters =
                    main.defaultRoutingSearchParameters()
                            .toBuilder()
                            .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
                            .build();
        }
        else {
            searchParameters =
                    main.defaultRoutingSearchParameters()
                            .toBuilder()
                            .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
                            .setLocalSearchMetaheuristic(LocalSearchMetaheuristic.Value.GUIDED_LOCAL_SEARCH)
                            .setTimeLimit(Duration.newBuilder().setSeconds(timeLimit).build())
                            .setLogSearch(true)
                            .build();
        }

        // Solve the problem.
        Assignment solution = routing.solveWithParameters(searchParameters);

        return getRoutes(solution, routing, manager);
    }

    private RoutingModel prepareRoutingModel(int vehicleNumber, int nodesNumber, RoutingIndexManager manager) {

        // Create Routing Model.
        RoutingModel routing = new RoutingModel(manager);

        // Create and register a transit callback.
        final int transitCallbackIndex =
                routing.registerTransitCallback((long fromIndex, long toIndex) -> {
                    // Convert from routing variable Index to user NodeIndex.
                    int fromNode = manager.indexToNode(fromIndex);
                    int toNode = manager.indexToNode(toIndex);
                    return timeMatrix[fromNode][toNode] / (nodeMoneyArray[toNode] == 0 ? 1 : nodeMoneyArray[toNode]);
                });

        // Define cost of each arc.
        routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);

        // =============================================================================
        // Add Capacity constraint.
        final int demandCallbackIndex = routing.registerUnaryTransitCallback((long fromIndex) -> {
            // Convert from routing variable Index to user NodeIndex.
            int fromNode = manager.indexToNode(fromIndex);
            return nodeMoneyArray[fromNode];
        });
        routing.addDimensionWithVehicleCapacity(demandCallbackIndex,
                0, // null capacity slack
                vehicleCapacites, // vehicle maximum capacities
                true, // start cumul to zero
                "Capacity");

        // =============================================================================
        // Allow to drop nodes.
        for (int i = 1; i < timeMatrix.length; ++i) {
            routing.addDisjunction(new long[] {manager.nodeToIndex(i)}, nodePenalties[i]);
        }

        // =============================================================================
        // Add time window
        final int timeCallbackIndex = routing.registerTransitCallback((long fromIndex, long toIndex) -> {
            // Convert from routing variable Index to user NodeIndex.
            int fromNode = manager.indexToNode(fromIndex);
            int toNode = manager.indexToNode(toIndex);
            return timeMatrix[fromNode][toNode];
        });

        routing.addDimension(timeCallbackIndex, // transit callback
                0, // allow waiting time
                3000, // vehicle maximum capacities ???????????
                false, // start cumul to zero
                "Time");

        RoutingDimension timeDimension = routing.getMutableDimension("Time");
        // Add time window constraints for each location except depot.
        if (timeWindow != null) {
            for (int i = 0; i < nodesNumber; ++i) {
                long index = manager.nodeToIndex(i);
                timeDimension.cumulVar(index).setRange(timeWindow[0], timeWindow[1]);
            }
        }
        else {
            for (int i = 0; i < nodesNumber; ++i) {
                long index = manager.nodeToIndex(i);
                timeDimension.cumulVar(index).setRange(timeWindows[i][0], timeWindows[i][1]);
            }
        }

        for (int i = 0; i < vehicleNumber; ++i) {
            routing.addVariableMinimizedByFinalizer(timeDimension.cumulVar(routing.start(i)));
            routing.addVariableMinimizedByFinalizer(timeDimension.cumulVar(routing.end(i)));
        }
        return routing;
    }

    private Route[] getRoutes(Assignment solution, RoutingModel routing, RoutingIndexManager manager) {

        int vehicleNumber = vehicleCapacites.length;
        Route[] routes = new Route[vehicleNumber];

        for (int i = 0; i <vehicleNumber; ++i) {
            List<Integer> routeIndexes = new ArrayList<>();

            long index = routing.start(i);
            while (!routing.isEnd(index)) {
                routeIndexes.add(manager.indexToNode(index));
                index = solution.value(routing.nextVar(index));
            }
            routeIndexes.add(manager.indexToNode(routing.end(i)));

            int[] nodesIndexes = new int[routeIndexes.size()];
            for(int j = 0; j < routeIndexes.size(); j++) {
                nodesIndexes[j] = routeIndexes.get(j);
            }
            routes[i] = new Route(nodesIndexes);
        }
        return routes;
    }
}
