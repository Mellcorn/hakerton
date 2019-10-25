package ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.core.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.websocket.WsWebSocketContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model.Point;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class CommunicationsService {
	@Autowired
	private MySessionHandler handler;
	@Autowired
	private GraphService graphService;

	private static WebSocketSession session;

	public boolean initConnection() throws ExecutionException, InterruptedException {
		log.info("Try to connect WEBSOCKET");


		StandardWebSocketClient client = new StandardWebSocketClient(new WsWebSocketContainer());
//		String url = "ws://172.30.11.123:4242/";
		String url = "ws://localhost:8080/race";
		//String url = "ws://172.30.9.50:8080/race";
			String url = "ws://172.15.9.50:8080/race";
			session = client.doHandshake(handler, url).get();


//		session.sendMessage(new TextMessage("{ \"team\": \"Имя команды\"}"));
//		session.sendMessage(new TextMessage("{ \"goto\": 2, \"car\": \"sb0\" }"));


		log.info("Try to connect WEBSOCKET DONE");
		return true;
	}

	public void sendMessage() throws ExecutionException, InterruptedException, IOException {
		if (session == null || !session.isOpen()) {
			initConnection();
		}
		session.sendMessage(new TextMessage("{ \"goto\": 2, \"car\": \"sb0\" }"));
	}

	public float[][] getGraph() {
		return graphService.getGraph();
	}

	public List<Point> getPoints() {
		return graphService.getPoints();
	}
}
