package crvp;

import java.util.Arrays;

public class Route {

    int[] nodes;

    public Route(int[] nodes) {
        this.nodes = nodes;
    }

    public int[] getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return "Route{" +
                "nodes=" + Arrays.toString(nodes) +
                '}';
    }
}
