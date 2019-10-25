package ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.algo;

import com.google.ortools.constraintsolver.*;
import com.google.protobuf.Duration;
import ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model.Car;
import ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model.Point;
import ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model.RouteNodes;

import java.util.*;

public class OrOptimizer implements CrvpOptimizer {

    static {
        System.loadLibrary("jniortools");
    }

    private long[][] timeMatrix;
    private long[] nodeMoneyArray;
    private long[] nodePenalties;

    private long[] vehicleCapacites;
    private int[] startNodes;
    private int[] endNodes;

    private long[][] timeWindows;
    private long[] timeWindow;

    private Set<Integer> excludedNodeIndexes;
    private Map<Integer, Integer> indexMapping;
    private Map<Integer, String> carIndexToNames;

    // THIS MUST UPDATED FIRST
    // points with negative money means they should be excluded from optimization
    // updateMoneyArrayFromPoints
    public void updatePointsFrom(List<Point> points) {
        excludedNodeIndexes = new HashSet<>();
        indexMapping = new HashMap<>();

        long[] moneyArrays = new long[points.size()];

        int indexAfterRemoving = 0;
        for (int i = 0; i < points.size(); i++) {
            float money = points.get(i).getMoney();
            if (money < 0) {
                excludedNodeIndexes.add(i);
            } else {
                moneyArrays[indexAfterRemoving] = (long) money;
                indexMapping.put(indexAfterRemoving, i);
                indexAfterRemoving++;
            }
        }
//        int newLength = indexAfterRemoving + 1;
        int newLength = indexAfterRemoving;
        nodeMoneyArray = new long[newLength];
        System.arraycopy(moneyArrays, 0, nodeMoneyArray, 0, newLength);
        this.nodePenalties = nodeMoneyArray; // simplification as money expected much bigger then time values
    }

    // updateTimeMatrix
    public void updateDistanceFrom(float[][] matrix) {
        if (nodeMoneyArray == null) {
            throw new RuntimeException("Money array must be set first");
        }

        int oldArrayLength = matrix.length;
        int newArrayLength = nodeMoneyArray.length;

        timeMatrix = new long[newArrayLength][newArrayLength];
        int iAdjusted = 0;
        for (int i = 0; i < oldArrayLength; i++) {
            if (excludedNodeIndexes.contains(i)) {
                continue;
            }
            int jAdjusted = 0;
            for (int j = 0; j < oldArrayLength; j++) {
                if (excludedNodeIndexes.contains(i)) {
                    continue;
                }
                timeMatrix[iAdjusted][jAdjusted] = (long) matrix[i][j];
                jAdjusted++;
            }
            iAdjusted++;
        }
    }

    public void updateVehiclesFromCars(List<Car> cars) {
        if (nodeMoneyArray == null) {
            throw new RuntimeException("Money array must be set first");
        }

        int vehicleNumber = cars.size() * 2;
        vehicleCapacites = new long[vehicleNumber];
        startNodes = new int[vehicleNumber];
        endNodes = new int[vehicleNumber];

        carIndexToNames = new HashMap<>();

        for (int i = 0; i < vehicleNumber / 2; i++) {
            Car car = cars.get(i);
            carIndexToNames.put(i, car.getId());
            vehicleCapacites[i] = (long) car.getCapacity();
            startNodes[i] = car.getStartPointId();
            endNodes[i] = car.getEndPointId();
        }

        for (int i = vehicleNumber/2; i < vehicleNumber; i++) {
            Car car = cars.get(i-vehicleNumber/2);
            carIndexToNames.put(i, "DUMMY CAR");
            vehicleCapacites[i] = 1L;
            startNodes[i] = car.getEndPointId();
            endNodes[i] = car.getStartPointId();
        }
    }


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
    public Map<String, Integer[]> calculateGreedyRoute() {
        return new HashMap<>();
    }

    @Override
    public Map<String, Integer[]> calculateFastRoute() {
        return calculateRoute(true, null);
    }

    private void validateData() {
        if (timeMatrix == null || nodeMoneyArray == null || vehicleCapacites == null
                || startNodes == null || endNodes == null ||
                (timeWindow == null && timeWindows == null)) {
            throw new RuntimeException("Not all data set");
        }
        if (timeMatrix.length != nodeMoneyArray.length) {
            throw new RuntimeException("Inconsistent node arrays length");
        }
        if (vehicleCapacites.length != startNodes.length) {
            throw new RuntimeException("Inconsistent vehicle arrays length");
        }
        if (startNodes.length != endNodes.length) {
            throw new RuntimeException("Inconsistent vehicle arrays length");
        }
    }

    @Override
    public Map<String, Integer[]> calculateFullyOptimizedRoute(int timeLimit) {
        return calculateRoute(false, timeLimit);
    }

    private Map<String, Integer[]> calculateRoute(boolean isFastOptimization, Integer timeLimit) {
        //validateData();

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
        } else {
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
                    return timeMatrix[fromNode][toNode] * 2000 / (nodeMoneyArray[toNode] == 0 ? 1 : nodeMoneyArray[toNode]);
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
            routing.addDisjunction(new long[]{manager.nodeToIndex(i)}, nodePenalties[i]);
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
                30000, // vehicle maximum capacities ???????????
                false, // start cumul to zero
                "Time");

        RoutingDimension timeDimension = routing.getMutableDimension("Time");
        // Add time window constraints for each location except depot.
        if (timeWindow != null) {
            for (int i = 0; i < nodesNumber; ++i) {
                long index = manager.nodeToIndex(i);
                timeDimension.cumulVar(index).setRange(timeWindow[0], timeWindow[1]);
            }
        } else {
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

    private Map<String, Integer[]> getRoutes(Assignment solution, RoutingModel routing, RoutingIndexManager manager) {

        int vehicleNumber = vehicleCapacites.length/2;
        RouteNodes[] routes = new RouteNodes[vehicleNumber];

        for (int i = 0; i < vehicleNumber; ++i) {
            List<Integer> routeIndexes = new ArrayList<>();

            long index = routing.start(i);
            while (!routing.isEnd(index)) {
                routeIndexes.add(manager.indexToNode(index));
                index = solution.value(routing.nextVar(index));
            }
            routeIndexes.add(manager.indexToNode(routing.end(i)));

            Integer[] nodesIndexes = new Integer[routeIndexes.size()];
            for (int j = 0; j < routeIndexes.size(); j++) {
                nodesIndexes[j] = routeIndexes.get(j);
            }
            routes[i] = new RouteNodes(nodesIndexes);
        }

        Map<String, Integer[]> carRouteMap = new HashMap<>();
        for (int i = 0; i < routes.length; i++) {
            String carId = carIndexToNames.get(i);
            carRouteMap.put(carId, routes[i].getNodes());
        }
        return carRouteMap;
    }

    public Map<String, Integer[]> obtainFirstRouteFromZeroPoint() {

        int countVehicle = vehicleCapacites.length;

        Route routeOptimals[] = new Route[countVehicle];

        // По дефолту заполняе оптимальные путями первые.
        // Уточнить что количество путей больше количества машин

        for (int i = 0; i < countVehicle; ++i) {
            long distance = timeMatrix[0][i];

            long pointCost = nodeMoneyArray[i];
            if (distance != 0) {
                double profit = (double) pointCost / (double) distance;
                Route route = new Route(0, i, profit);
                routeOptimals[i] = route;
            } else {
                Route route = new Route(0, i, 0);
                routeOptimals[i] = route;
            }
        }

        Arrays.sort(routeOptimals, new SortByCost());

        // Проходимся по остальным точкам и обновляем оптимальные

        int countDemands = nodeMoneyArray.length;

        for (int i = countVehicle; i < countDemands; ++i) {

            long distance = timeMatrix[0][i];
            long pointCost = nodeMoneyArray[i];

            if (distance == 0) {
                continue;
            }

            double profit = (double) pointCost / (double) distance;

            Route route = new Route(0, i, profit);

            // Если минимальная оптимальная точка в списке текущих оптимальных путель
            // больше текущей, то нет смысьла обновлять список
            int lastOptimusNumber = countVehicle - 1;
            if (routeOptimals[lastOptimusNumber].profit > profit) {
                continue;
            }

            for (int j = 0; j < countVehicle; ++j) {

                if (routeOptimals[j].profit < route.profit) {
                    routeOptimals[j] = route;
                    break;
                }
            }
        }

        Map<String, Integer[]> carRouteMap = new HashMap<>();
        for (int i = 0; i < routeOptimals.length; i++) {
            String carId = carIndexToNames.get(i);
            Route route = routeOptimals[i];
            carRouteMap.put(carId, new Integer[]{route.fromPoint, route.toPoint});
        }
        return carRouteMap;
    }


    public static class Route {

        int fromPoint;
        int toPoint;
        double profit;

        public Route(int fromPoint, int toPoint, double profit) {
            this.fromPoint = fromPoint;
            this.toPoint = toPoint;
            this.profit = profit;
        }

    }

    public static class SortByCost implements Comparator<Route> {
        public int compare(Route a, Route b) {
            if (a.profit > b.profit) return -1;
            else if (a.profit == b.profit) return 0;
            else return 1;
        }
    }
}
