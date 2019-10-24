package ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.algo;

import ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model.RouteNodes;

import java.util.Map;

public interface CrvpOptimizer {

    void setTimeMatrix(long[][] timeMatrix);
    void setNodeMoney(long[] nodeMoneyArray);

    void setVehicleCapacities(long[] capacites);

    void setVehicleStartNodes(int[] nodes);
    void setVehicleEndNodes(int[] nodes);

    void setTimeWindow(long[] timeWindow);
    void setTimeWindows(long[][] timeWindows);

    Map<String, Integer[]> calculateGreedyRoute();
    Map<String, Integer[]> calculateFastRoute();
    Map<String, Integer[]> calculateFullyOptimizedRoute(int timeLimit);
}
