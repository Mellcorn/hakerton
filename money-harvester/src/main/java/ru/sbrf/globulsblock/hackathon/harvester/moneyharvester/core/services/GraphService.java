package ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.core.services;

import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model.Point;
import ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model.Route;
import ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model.Traffic;

import java.util.List;

@Component
@Slf4j
public class GraphService {
	private static GraphEdge[][] matrix;
	private static List<Point> points;

	public boolean initGraph(List<Point> points, final List<Route> routes, final List<Traffic> traffic) {
		this.points = points;
		int pointsCount = points.size();
		matrix = new GraphEdge[pointsCount][pointsCount];

		for (Route route : routes) {
			int pointA = route.getA();
			int pointB = route.getB();
			if (pointA > pointsCount - 1 || pointB > pointsCount - 1) {
				log.error("Graph initialization failed because route {} not in the graph.", route);
				return false;
			}
			matrix[pointA][pointB] = new GraphEdge(route.getTime());
			matrix[pointB][pointA] = new GraphEdge(route.getTime());
		}
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
				if (matrix[i][j] == null) {
					matrix[i][j] = new GraphEdge(999999);
				}
			}
		}


		updateGraph(traffic);

		log.info("Graph has been initialized successful {}", matrix);
		return true;
	}

	public boolean updateGraph(List<Traffic> trafficjam) {
		log.info("Start update graph");
		for (Traffic trafficNode : trafficjam) {
			int pointA = trafficNode.getA();
			int pointB = trafficNode.getB();

			if (pointA > matrix.length - 1 || pointB > matrix.length - 1) {
				log.error("Graph update failed because trafficNode {} not in the graph.", trafficNode);
				return false;
			}

			if (matrix[pointA][pointB] == null) {
				log.error("Graph update failed because we don't have path between points {}", trafficNode);
				return false;
			}

			matrix[pointA][pointB].setJam(trafficNode.getJam());
			matrix[pointB][pointA].setJam(trafficNode.getJam());
		}
		log.info("Graph successfully updated");
		return true;
	}

	public float[][] getGraph() {
		float[][] graph = new float[matrix.length][matrix.length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
				if (matrix[i][j] != null) {
					graph[i][j] = matrix[i][j].getTime();
				}
			}
		}
		log.info("Final graph {}", graph);
		return graph;
	}


	public List<Point> getPoints() {
		return points;
	}

	@Setter
	@ToString
	private static class GraphEdge {
		private final float time;
		private float jam;


		public GraphEdge(float time) {
			this.time = time;
			this.jam = 1.0f;
		}

		public float getTime() {
			return time * jam;
		}
	}
}
