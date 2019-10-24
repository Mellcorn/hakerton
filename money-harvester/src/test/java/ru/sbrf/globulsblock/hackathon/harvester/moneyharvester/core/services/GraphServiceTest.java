package ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.core.services;

import org.junit.jupiter.api.Test;
import ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model.Point;
import ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model.Route;
import ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model.Traffic;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;


class GraphServiceTest {
	private GraphService testee = new GraphService();

	private final List<Point> points = Arrays.asList(
			Point.of(0, 1000.0f),
			Point.of(1, 2000.0f),
			Point.of(2, 3000.0f)
	);


	private final List<Route> routes = Arrays.asList(
			Route.of(0, 0, 0), Route.of(0, 1, 1), Route.of(0, 2, 2),
			Route.of(1, 0, 1), Route.of(1, 1, 0), Route.of(1, 2, 2),
			Route.of(2, 0, 2), Route.of(2, 1, 1), Route.of(2, 2, 0)
	);

	private final List<Traffic> traffic = Arrays.asList(
			Traffic.of(0, 0, 1.0f), Traffic.of(0, 1, 1.5f), Traffic.of(0, 2, 2.0f),
			Traffic.of(1, 0, 1.5f), Traffic.of(1, 1, 1.0f), Traffic.of(1, 2, 2.0f),
			Traffic.of(2, 0, 2.0f), Traffic.of(2, 1, 1.5f), Traffic.of(2, 2, 1.0f)
	);

	@Test
	public void shouldCreateGraph() {
		boolean b = testee.initGraph(points, routes, traffic);
		assertTrue(b);
		float[][] graph = testee.getGraph();
		printGraph(graph);
	}

	private void printGraph(float [][] graph) {
		for (int i = 0; i < graph.length; i++) {
			for (int j = 0; j < graph.length; j++) {
				System.out.print(graph[i][j] + " ");
			}
			System.out.print("\n");
		}
	}
}