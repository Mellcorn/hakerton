package algo;

public interface CvrpDataSource {

    long[][] getDistanceMatrix();
    long[] getDemands();
    long[] getVehicleCapacities();
    int getVehicleNumber();

    int[] getVehicleStartNodes();
    int[] getVehicleEndNodes();

    long[] getNodeProcessingTime();
    long[] getPenaltiesPerPoint();

    int getFixedVehicleSpeed();

    long[] getTimeWindow();
}
