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
import ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model.rest.CarArrivedResponse;
import ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model.rest.PointsResponse;
import ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model.rest.RegisterResponse;
import ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model.rest.RoutesResponse;
import ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model.rest.TrafficResponse;

@Slf4j
@Component
public class MySessionHandler extends AbstractWebSocketHandler {
	@Autowired
	private ObjectMapper mapper;
	@Autowired
	private GraphService graphService;

	private static String token;
	private static int crashes = 0;

	private static boolean isGraphInitialized = false;

	private static RoutesResponse routesResponse;
	private static PointsResponse pointsResponse;
	private static TrafficResponse trafficResponse;

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
		parseMessage((String) message.getPayload());
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		crashes++;
		log.error("SOSESSION!!!!!!!!!!!!!!!!!!!! Crashes: {}", crashes);
		super.afterConnectionClosed(session, status);
	}

	private void parseMessage(String jsonBody) {
		try {
			if (jsonBody.startsWith("{ \"token\"")) {
				RegisterResponse registerResponse = mapper.readValue(jsonBody, RegisterResponse.class);
				log.info("Parsed value {}", registerResponse);
				token = registerResponse.getToken();
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
			graphService.initGraph(pointsResponse.getPoints().size(), routesResponse.getRoutes(), trafficResponse.getTraffic());
			isGraphInitialized = true;

			log.info("Created graph: {}", graphService.getGraph());
		}
	}

}
