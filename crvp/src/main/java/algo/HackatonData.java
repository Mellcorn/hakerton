package algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class HackatonData implements CvrpDataSource {

    private long[][] distanceMatrix = {
            {0, 548, 776, 696, 582, 274, 502, 194, 308, 194, 536, 502, 388, 354, 468, 776, 662},

            {548, 0, 684, 308, 194, 502, 730, 354, 696, 742, 1084, 594, 480, 674, 1016, 868, 1210},
            {776, 684, 0, 992, 878, 502, 274, 810, 468, 742, 400, 1278, 1164, 1130, 788, 1552, 754},
            {696, 308, 992, 0, 114, 650, 878, 502, 844, 890, 1232, 514, 628, 822, 1164, 560, 1358},
            {582, 194, 878, 114, 0, 536, 764, 388, 730, 776, 1118, 400, 514, 708, 1050, 674, 1244},
            {274, 502, 502, 650, 536, 0, 228, 308, 194, 240, 582, 776, 662, 628, 514, 1050, 708},

            {502, 730, 274, 878, 764, 228, 0, 536, 194, 468, 354, 1004, 890, 856, 514, 1278, 480},
            {194, 354, 810, 502, 388, 308, 536, 0, 342, 388, 730, 468, 354, 320, 662, 742, 856},
            {308, 696, 468, 844, 730, 194, 194, 342, 0, 274, 388, 810, 696, 662, 320, 1084, 514},
            {194, 742, 742, 890, 776, 240, 468, 388, 274, 0, 342, 536, 422, 388, 274, 810, 468},
            {536, 1084, 400, 1232, 1118, 582, 354, 730, 388, 342, 0, 878, 764, 730, 388, 1152, 354},

            {502, 594, 1278, 514, 400, 776, 1004, 468, 810, 536, 878, 0, 114, 308, 650, 274, 844},
            {388, 480, 1164, 628, 514, 662, 890, 354, 696, 422, 764, 114, 0, 194, 536, 388, 730},
            {354, 674, 1130, 822, 708, 628, 856, 320, 662, 388, 730, 308, 194, 0, 342, 422, 536},
            {468, 1016, 788, 1164, 1050, 514, 514, 662, 320, 274, 388, 650, 536, 342, 0, 764, 194},
            {776, 868, 1552, 560, 674, 1050, 1278, 742, 1084, 810, 1152, 274, 388, 422, 764, 0, 798},

            {662, 1210, 754, 1358, 1244, 708, 480, 856, 514, 468, 354, 844, 730, 536, 194, 798, 0},
    };

    private long[] demands = {0, 1, 1, 2, 4, 17, 0, 8, 8, 1, 2, 11, 2, 4, 4, 8, 0};
    private long[] vehicleCapacities = {10, 100, 100, 100};

    private int vehicleNumber = 4;
    //private final int depot = 0;

    // hackaton special conditions...
    private int vehicleSpeed = 100; // m/min
    private long[] processingTime = {0, 1, 1, 7, 20, 0, 3, 4, 9, 3, 2, 2, 2, 15, 3, 5, 0};
    private long[] penalties = {0, 100, 100, 200, 400, 1700, 0, 800, 800, 100, 200, 1100, 200, 400, 400, 800, 0};

    private int[] startNodes = {6, 6, 16, 16};
    private int[] endNodes = {16, 6, 16, 6};

    private long[][] getAdjustedDistanceMatrix() {
        long[][] adjustedArray = new long[distanceMatrix.length][];
        int index = 0;
        for (long[] array : distanceMatrix) {
            Long[] boxed = IntStream
                    .range(0, array.length)
                    .mapToObj(i -> array[i] * 10L / vehicleSpeed / (demands[i] == 0 ? 1 : demands[i]) + processingTime[i])
                    .toArray(Long[]::new);
            long[] unboxed = Arrays
                    .stream(boxed)
                    .mapToLong(Long::longValue)
                    .toArray();
            adjustedArray[index] = unboxed;
            index++;
        }
        return adjustedArray;
    }

    @Override
    public long[][] getDistanceMatrix() {
        return getAdjustedDistanceMatrix();
    }

    @Override
    public long[] getDemands() {
        return demands;
    }

    @Override
    public long[] getVehicleCapacities() {
        return vehicleCapacities;
    }

    @Override
    public int getVehicleNumber() {
        return vehicleNumber;
    }

    @Override
    public int[] getVehicleStartNodes() {
        return startNodes;
    }

    @Override
    public int[] getVehicleEndNodes() {
        return endNodes;
    }

    @Override
    public long[] getNodeProcessingTime() {
        return processingTime;
    }

    @Override
    public long[] getPenaltiesPerPoint() {
        return penalties;
    }

    @Override
    public int getFixedVehicleSpeed() {
        return 0;
    }

    public void resetDataRandomly(int numberOfPoints,
                                  int numberOfVehicles,
                                  int maxLength,
                                  int maxDemands,
                                  int maxProcessingTime,
                                  int vehicleCapacity) {

        // generate x,y points, capacities, processing time, starting points
        List<Node> nodes = new ArrayList<>();
        List<Long> demands = new ArrayList<>();
        List<Long> processingTime = new ArrayList<>();

        for (int i = 0; i < numberOfPoints; i++) {
            int x = getRandomInt(maxLength);
            int y = getRandomInt(maxLength);
            nodes.add(new Node(x, y));
            demands.add(getRandomLong(maxDemands));
            processingTime.add(getRandomLong(maxProcessingTime));
        }

        int[] startingPoints = IntStream
                .range(0, numberOfVehicles)
                .map(i -> getRandomInt(numberOfPoints - 1) + 1)
                .toArray();
        this.startNodes = startingPoints;

        for (int i = 0; i < startingPoints.length; i++) {
            demands.set(startingPoints[i], 0L);
            processingTime.set(startingPoints[i], 0L);
        }

        this.demands = toLongArray(demands);
        this.processingTime = toLongArray(processingTime);
        this.vehicleNumber = numberOfVehicles;

        this.vehicleCapacities = new long[numberOfVehicles];
        this.endNodes = new int[numberOfVehicles];
        IntStream.range(0, numberOfVehicles).forEachOrdered(i -> {
            this.vehicleCapacities[i] = vehicleCapacity;
            this.endNodes[i] = 0;
        });

        this.penalties = new long[numberOfPoints];
        IntStream.range(0, numberOfPoints).forEachOrdered(i -> {
            this.penalties[i] = this.demands[i] * 10;
        });

        this.distanceMatrix = new long[numberOfPoints][numberOfPoints];
        for (int i = 0; i < numberOfPoints; i++) {
            for (int j = 0; j < numberOfPoints; j++) {
                if (i == j) {
                    this.distanceMatrix[i][j] = 0;
                } else {
                    this.distanceMatrix[i][j] = distance(nodes.get(i), nodes.get(j));
                }
            }
        }
    }

    private long distance(Node fromNode, Node toNode) {
        int xDelta = toNode.x - fromNode.x;
        int yDelta = toNode.y - fromNode.y;
        return (long) Math.sqrt(pow2(xDelta) + pow2(yDelta));
    }

    private int getRandomInt(int max) {
        return (int) (Math.random() * (double) max);
    }

    private long getRandomLong(int max) {
        return (long) (Math.random() * (double) max);
    }

    private long[] toLongArray(List<Long> list) {
        Long[] boxedDemands = new Long[list.size()];
        return Arrays
                .stream(list.toArray(boxedDemands))
                .mapToLong(Long::longValue)
                .toArray();
    }

    private int[] toIntArray(List<Integer> list) {
        Integer[] boxedDemands = new Integer[list.size()];
        return Arrays
                .stream(list.toArray(boxedDemands))
                .mapToInt(Integer::intValue)
                .toArray();
    }

    private int pow2(int value) {
        return value * value;
    }

    public static class Node {
        private int x;
        private int y;

        public Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }
}
