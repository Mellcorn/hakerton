package crvp;

import java.util.List;

public interface CrvpOptimizer {

    void setTimeMatrix(long[][] timeMatrix);
    void setNodeMoney(long[] nodeMoneyArray);

    void setVehicleCapacities(long[] capacites);

    void setVehicleStartNodes(int[] nodes);
    void setVehicleEndNodes(int[] nodes);

    void setTimeWindow(long[] timeWindow);
    void setTimeWindows(long[][] timeWindows);

    Route[] calculateGreedyRoute();
    Route[] calculateFastRoute();
    Route[] calculateFullyOptimizedRoute(int timeLimit);
}
