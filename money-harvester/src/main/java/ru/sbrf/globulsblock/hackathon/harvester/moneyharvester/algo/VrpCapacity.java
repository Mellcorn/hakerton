package ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.algo;

// Copyright 2010-2018 Google LLC
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// [START program]
// [START import]
import com.google.ortools.constraintsolver.*;
import com.google.protobuf.Duration;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Logger;
// [END import]

/** Minimal VRP.*/
public class VrpCapacity {
    static {
        System.loadLibrary("jniortools");
    }

    private static final Logger logger = Logger.getLogger(VrpCapacity.class.getName());

    /// @brief Print the solution.
    static void printSolution(
            CvrpDataSource data, RoutingModel routing, RoutingIndexManager manager, Assignment solution) {

        // Display dropped nodes.
        String droppedNodes = "Dropped nodes:";
        for (int node = 0; node < routing.size(); ++node) {
            if (routing.isStart(node) || routing.isEnd(node)) {
                continue;
            }
            if (solution.value(routing.nextVar(node)) == node) {
                droppedNodes += " " + manager.indexToNode(node);
            }
        }
        logger.info(droppedNodes);

        // Inspect solution.
        long totalDistance = 0;
        long totalLoad = 0;
        for (int i = 0; i < data.getVehicleNumber(); ++i) {
            long index = routing.start(i);
            logger.info("Route for Vehicle " + i + ":");
            long routeDistance = 0;
            long routeLoad = 0;
            String route = "";
            while (!routing.isEnd(index)) {
                long nodeIndex = manager.indexToNode(index);
                routeLoad += data.getDemands()[(int) nodeIndex];
                route += nodeIndex + " Load(" + routeLoad + ") -> ";
                long previousIndex = index;
                index = solution.value(routing.nextVar(index));
                routeDistance += routing.getArcCostForVehicle(previousIndex, index, i);
            }
            route += manager.indexToNode(routing.end(i));
            logger.info(route);
            logger.info("Distance of the route: " + routeDistance + "m");
            totalDistance += routeDistance;
            totalLoad += routeLoad;
        }
        logger.info("Total distance of all routes: " + totalDistance + "m");
        logger.info("Total load of all routes: " + totalLoad);
    }

    public static void main(String[] args) throws Exception {
        CvrpDataSource data = new HackatonData();
      //  ((HackatonData) data).resetDataRandomly(100, 10, 1500, 50, 15, 150);
        System.out.println(Arrays.toString(data.getDemands()));
        cvrpWithPenaltiesAndMultipleFixedDepot(data);

        Route[] obtainOptimalRoutes = obtainOptimalRoutes(data);
        System.out.println(Arrays.toString(obtainOptimalRoutes));
    }

    public static void cvrpWithPenaltiesAndMultipleFixedDepot(CvrpDataSource data) {



        // Create Routing Index Manager
        RoutingIndexManager manager =
                new RoutingIndexManager(data.getDistanceMatrix().length,
                        data.getVehicleNumber(),
                        data.getVehicleStartNodes(),
                        data.getVehicleEndNodes());

        // Create Routing Model.
        RoutingModel routing = new RoutingModel(manager);

        // Create and register a transit callback.
        final int transitCallbackIndex =
                routing.registerTransitCallback((long fromIndex, long toIndex) -> {
                    // Convert from routing variable Index to user NodeIndex.
                    int fromNode = manager.indexToNode(fromIndex);
                    int toNode = manager.indexToNode(toIndex);
                    return data.getDistanceMatrix()[fromNode][toNode] / (data.getDemands()[toNode] == 0 ? 1 : data.getDemands()[toNode]);
                });

        // Define cost of each arc.
        routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);

        // Add Capacity constraint.
        final int demandCallbackIndex = routing.registerUnaryTransitCallback((long fromIndex) -> {
            // Convert from routing variable Index to user NodeIndex.
            int fromNode = manager.indexToNode(fromIndex);
            return data.getDemands()[fromNode];
        });
        routing.addDimensionWithVehicleCapacity(demandCallbackIndex,
                0, // null capacity slack
                data.getVehicleCapacities(), // vehicle maximum capacities
                true, // start cumul to zero
                "Capacity");
//        RoutingDimension capacityDimension = routing.getDimensionOrDie("Capacity");
//        long index = manager.nodeToIndex(1);
//        capacityDimension.slackVar(index).setRange(0, 20);
//        routing.addDisjunction(new long[] {index}, 0);


        // Allow to drop nodes.
        for (int i = 1; i < data.getDistanceMatrix().length; ++i) {
            routing.addDisjunction(new long[] {manager.nodeToIndex(i)}, data.getPenaltiesPerPoint()[i]);
        }
/*
        def create_time_evaluator(data):
        """Creates callback to get total times between locations."""

        def service_time(data, node):
        """Gets the service time for the specified location."""
        return abs(data['demands'][node]) * data['time_per_demand_unit']

        def travel_time(data, from_node, to_node):
        """Gets the travel times between two locations."""
        if from_node == to_node:
        travel_time = 0
        else:
        travel_time = manhattan_distance(data['locations'][
                from_node], data['locations'][to_node]) / data['vehicle_speed']
        return travel_time

        _total_time = {}
    # precompute total time to have time callback in O(1)
        for from_node in xrange(data['num_locations']):
        _total_time[from_node] = {}
        for to_node in xrange(data['num_locations']):
        if from_node == to_node:
        _total_time[from_node][to_node] = 0
            else:
        _total_time[from_node][to_node] = int(
                service_time(data, from_node) + travel_time(
                        data, from_node, to_node))

        def time_evaluator(manager, from_node, to_node):
        """Returns the total time between the two nodes"""
        return _total_time[manager.IndexToNode(from_node)][manager.IndexToNode(
                to_node)]

        return time_evaluator*/

        // Add time window =============================================================================
        final int timeCallbackIndex = routing.registerTransitCallback((long fromIndex, long toIndex) -> {
            // Convert from routing variable Index to user NodeIndex.
            int fromNode = manager.indexToNode(fromIndex);
            int toNode = manager.indexToNode(toIndex);
            return data.getDistanceMatrix()[fromNode][toNode];
        });

        routing.addDimension(timeCallbackIndex, // transit callback
                0, // allow waiting time
                3000, // vehicle maximum capacities !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                false, // start cumul to zero
                "Time");

        RoutingDimension timeDimension = routing.getMutableDimension("Time");
        // Add time window constraints for each location except depot.
        for (int i = 0; i < data.getDemands().length; ++i) {
            long index = manager.nodeToIndex(i);
            timeDimension.cumulVar(index).setRange(data.getTimeWindow()[0], data.getTimeWindow()[1]);
        }

        // Add time window constraints for each vehicle start node.
//        for (int i = 0; i < data.vehicleNumber; ++i) {
//            long index = routing.start(i);
//            timeDimension.cumulVar(index).setRange(data.timeWindows[0][0], data.timeWindows[0][1]);
//        }

        for (int i = 0; i < data.getVehicleNumber(); ++i) {
            routing.addVariableMinimizedByFinalizer(timeDimension.cumulVar(routing.start(i)));
            routing.addVariableMinimizedByFinalizer(timeDimension.cumulVar(routing.end(i)));
        }

        // ==============================================================================================

        // Setting first solution heuristic.
        RoutingSearchParameters searchParameters =
                main.defaultRoutingSearchParameters()
                        .toBuilder()
                        .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
                        .setLocalSearchMetaheuristic(LocalSearchMetaheuristic.Value.GUIDED_LOCAL_SEARCH)
                        .setTimeLimit(Duration.newBuilder().setSeconds(5).build())
                        .setLogSearch(true)
                        .build();

        // Solve the problem.
        Assignment solution = routing.solveWithParameters(searchParameters);

        // Print solution on console.
        printSolution(data, routing, manager, solution);
    }

    public static Route[] obtainOptimalRoutes(CvrpDataSource data) {

        int countVehicle = data.getVehicleNumber();

        Route routeOptimals[] = new Route[countVehicle];

        // По дефолту заполняе оптимальные путями первые.
        // Уточнить что количество путей больше количества машин

        for (int i = 0; i < countVehicle; ++i) {

            long[][] distanceMatrix = data.getDistanceMatrix();
            long distance = distanceMatrix[0][i];

            long []demands = data.getDemands();
            long pointCost = demands[i];
            if(distance != 0) {
                double profit = (double)pointCost / (double)distance;
                Route route = new Route(0, i, profit, pointCost);
                routeOptimals[i] = route;
            } else {
                Route route = new Route(0, i, 0, pointCost);
                routeOptimals[i] = route;
            }

        }

        Arrays.sort(routeOptimals, new SortByCost());

        // Проходимся по остальным точкам и обновляем оптимальные

        int countDemands =  data.getDemands().length;

        for (int i = countVehicle; i < countDemands; ++i) {

            long[][] distanceMatrix = data.getDistanceMatrix();
            long distance = distanceMatrix[0][i];

            long []demands = data.getDemands();
            long pointCost = demands[i];

            if(distance == 0) { continue; }

            double profit = (double)pointCost / (double)distance;

            Route route = new Route(0, i, profit, pointCost);

            // Если минимальная оптимальная точка в списке текущих оптимальных путель
            // больше текущей, то нет смысьла обновлять список
            int lastOptimusNumber = data.getVehicleNumber()- 1;
            if(routeOptimals[lastOptimusNumber].profit > profit) { continue; }

            for (int j = 0; j < data.getVehicleNumber(); ++j) {

                if(routeOptimals[j].profit < route.profit) {
                    routeOptimals[j] = route;
                    break;
                }
            }
        }

        return routeOptimals;

    }
}

class Route {

    int fromPoint;
    int toPonint;
    long demand;
    double profit;

    public Route(int fromPoint, int toPonint, double profit, long demand) {
        this.fromPoint = fromPoint;
        this.toPonint = toPonint;
        this.profit = profit;
        this.demand = demand;
    }

}

class SortByCost implements Comparator<Route> {
    public int compare(Route a, Route b) {
        if ( a.profit > b.profit ) return -1;
        else if ( a.profit == b.profit ) return 0;
        else return 1;
    }
}