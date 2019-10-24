package ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model;

import java.util.Arrays;

public class RouteNodes {

    Integer[] nodes;

    public RouteNodes(Integer[] nodes) {
        this.nodes = nodes;
    }

    public Integer[] getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return "RouteNodes{" +
                "nodes=" + Arrays.toString(nodes) +
                '}';
    }
}
