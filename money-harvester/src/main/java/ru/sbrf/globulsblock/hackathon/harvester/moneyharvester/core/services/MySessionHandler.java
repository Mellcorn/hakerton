package ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.core.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.algo.OrOptimizer;
import ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model.Car;
import ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model.Point;
import ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model.rest.CarArrivedResponse;
import ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model.rest.PointsResponse;
import ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model.rest.RegisterResponse;
import ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model.rest.RoutesResponse;
import ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model.rest.TrafficResponse;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MySessionHandler extends AbstractWebSocketHandler {
	@Autowired
	private ObjectMapper mapper;
	@Autowired
	private GraphService graphService;

	private static String token;
	public List<String> cars;
	private List<Car> carsList;
	private Map<String, Integer[]> carsRoutes;

	private static int crashes = 0;

	private static boolean isGraphInitialized = false;

	private static RoutesResponse routesResponse;
	private static PointsResponse pointsResponse;
	private static TrafficResponse trafficResponse;

	//private static OrOptimizer orOptimizer = new OrOptimizer();

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		session.setBinaryMessageSizeLimit(999999999);
		session.setTextMessageSizeLimit(999999999);
		if (token != null) {
			session.sendMessage(new TextMessage("{ \"token\": \"" + token + "\"}"));
		} else {
			session.sendMessage(new TextMessage("{ \"team\": \"Глобулы Блока\"}"));
		}
	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
		log.info("accepted message: {}", message.getPayload());
		try {
			parseMessage((String) message.getPayload(), session);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		crashes++;
		log.error("SOSESSION!!!!!!!!!!!!!!!!!!!! Crashes: {}", crashes);
		super.afterConnectionClosed(session, status);
	}

	private void parseMessage(String jsonBody, WebSocketSession session) throws IOException {
		try {
			if (jsonBody.startsWith("{ \"token\"")) {
				RegisterResponse registerResponse = mapper.readValue(jsonBody, RegisterResponse.class);
				log.info("Parsed value {}", registerResponse);
				token = registerResponse.getToken();
				cars = registerResponse.getCars();
			} else if (jsonBody.contains("routes") && jsonBody.contains("points") && jsonBody.contains("traffic")) { //КОСТЫЛЬ ДЛЯ НЕВАЛИДНОГО ДЖЕЙСОНА
				String routesBody = jsonBody.substring(0, jsonBody.indexOf("{ \"points\":"));
				String pointsBody = jsonBody.substring(jsonBody.indexOf("{ \"points\":"), jsonBody.indexOf("{ \"traffic\":"));
				String trafficBody = jsonBody.substring(jsonBody.indexOf("{ \"traffic\":"));
				log.info("PARSED INVALID BODY: \n {} \n {} \n {}", routesBody, pointsBody, trafficBody);
				routesResponse = mapper.readValue(routesBody, RoutesResponse.class);
				log.info("Parsed value {}", routesResponse);
				pointsResponse = mapper.readValue(pointsBody, PointsResponse.class);
				log.info("Parsed value {}", pointsResponse);
				trafficResponse = mapper.readValue(trafficBody, TrafficResponse.class);
				log.info("Parsed value {}", trafficResponse);

			} else if (jsonBody.startsWith("{ \"routes\"")) {
				routesResponse = mapper.readValue(jsonBody, RoutesResponse.class);
				log.info("Parsed value {}", routesResponse);
			} else if (jsonBody.startsWith("{ \"points\"")) {
				pointsResponse = mapper.readValue(jsonBody, PointsResponse.class);
				log.info("Parsed value {}", pointsResponse);
			} else if (jsonBody.startsWith("{ \"traffic\"")) {
				trafficResponse = mapper.readValue(jsonBody, TrafficResponse.class);
				log.info("Parsed value {}", trafficResponse);
				if (isGraphInitialized) {
					graphService.updateGraph(trafficResponse.getTraffic());
				}
			} else if (jsonBody.startsWith("{ \"point\":")) {
				CarArrivedResponse carArrivedResponse = mapper.readValue(jsonBody, CarArrivedResponse.class);
				//todo call recalculation
				log.info("Parsed value {}", carArrivedResponse);
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		if (routesResponse != null && pointsResponse != null && trafficResponse != null && !isGraphInitialized) { // init graph if have all data
			graphService.initGraph(pointsResponse.getPoints(), routesResponse.getRoutes(), trafficResponse.getTraffic());
			isGraphInitialized = true;
			// Call first calculation
			OrOptimizer orOptimizer = new OrOptimizer();
			orOptimizer.setTimeWindow(new long[] {0, 480});
			orOptimizer.updatePointsFrom(pointsResponse.getPoints());
			orOptimizer.updateDistanceFrom(graphService.getGraph());

			carsList = createCars(this.cars);
			orOptimizer.updateVehiclesFromCars(carsList);
			carsRoutes = orOptimizer.calculateFirstRouteFromZeroPoint();
					//orOptimizer.calculateFastRoute();
			log.info("Fast routes: {}", carsRoutes);
			insertRouteToCar(carsList, carsRoutes);
			//TODO send cars
			for (Car car : carsList) {
				Integer pointId = car.getPath().poll();
				sendCar(session, pointId, car.getId());
				for (int i = 0; i < pointsResponse.getPoints().size(); i++) {
					Point point = pointsResponse.getPoints().get(i);
					if (point.getP() == pointId) {
						pointsResponse.getPoints().set(i, point.withMoney(-1f));
					}
				}
			}

			// TODO: refactor copypaste...
			orOptimizer = new OrOptimizer();
			orOptimizer.setTimeWindow(new long[] {0, 480});
			orOptimizer.updatePointsFrom(pointsResponse.getPoints());
			orOptimizer.updateDistanceFrom(graphService.getGraph());
			carsList = createCars(this.cars);
			orOptimizer.updateVehiclesFromCars(carsList);
			Map<String, Integer[]> stringMap = orOptimizer.calculateFullyOptimizedRoute(5);
			log.info("Created graph: {}", graphService.getGraph());
		}
	}

	private void sendCar(WebSocketSession session, int pointId, String carId) throws IOException {
		session.sendMessage(new TextMessage("{ \"goto\": " + pointId + ", \"car\": \"" + carId + "\" }"));
	}

	private void insertRouteToCar(List<Car> cars, Map<String, Integer[]> carsRoutes) {
		for (Car car : cars) {
			for (int i = 1; i < carsRoutes.get(car.getId()).length; i++) {
				car.getPath().add(carsRoutes.get(car.getId())[i]);
			}
		}
	}

	private List<Car> createCars(List<String> carsIds) {
		ArrayList<Car> result = new ArrayList<>();
		for (String carsId : carsIds) {
			result.add(createCar(carsId));
		}
		return result;
	}

	private Car createCar(String id) {
		return Car.builder()
				.id(id)
				.path(new ArrayDeque<>())
				.build();
	}

}
