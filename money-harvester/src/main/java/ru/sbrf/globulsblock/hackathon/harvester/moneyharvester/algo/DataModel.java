package ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.algo;

import ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.core.services.GraphService;
import ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model.Point;

import java.util.List;

public class DataModel implements CvrpDataSource {

    private final long maxCapacities = 1000000;

    public void updateDistanceFrom(float[][] matrix) {

        long[][] longArray = new long[matrix.length][matrix.length];
        for (int i = 0 ; i < matrix.length; i++)
            for (int j = 0 ; j < matrix.length; j++)
            {
                longArray[i][j] = (long) matrix[i][j];
            }

        distanceMatrix = longArray;
    }

    public void updateСarsFrom(List<String> cars) {

        vehicleNumber = cars.size();
        long []vapacities = new long[vehicleNumber];

        for(int i = 0; i < vehicleNumber; i++) {
            vapacities[i] = maxCapacities;
        }
    }

    public void updatePointsFrom(List<Point> points) {

        float []demands = new float[points.size()];

        for(int i= 0; i< points.size(); i++ ) {
            demands[i] = points.get(i).getMoney();
        }

        //this.demands = demands;
    }

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
    private long[] demands = {0, 1, 1, 2, 4, 2, 4, 8, 8, 1, 2, 1, 2, 4, 4, 8, 8};
    private long[] vehicleCapacities = {15, 15, 15, 15};
    private int vehicleNumber = 4;
    private int depot = 0;

    @Override
    public long[][] getDistanceMatrix() {
        return distanceMatrix;
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
        return new int[0];
    }

    @Override
    public int[] getVehicleEndNodes() {
        return new int[0];
    }

    @Override
    public long[] getNodeProcessingTime() {
        return new long[0];
    }

    @Override
    public long[] getPenaltiesPerPoint() {
        return new long[0];
    }

    @Override
    public int getFixedVehicleSpeed() {
        return 0;
    }

    @Override
    public long[] getTimeWindow() {
        return new long[] {0, 10};
    }
}
