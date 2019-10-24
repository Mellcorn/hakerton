package ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.core.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.core.services.CommunicationsService;
import ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model.Point;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@Slf4j
public class ControlController {

	private CommunicationsService communicationsService;

	@Autowired
	public ControlController(CommunicationsService communicationsService) {

		this.communicationsService = communicationsService;

		try {
			start();
		} catch (Throwable t) {}
	}

	@RequestMapping(value = "/start", method = RequestMethod.GET)
	public void start() throws ExecutionException, InterruptedException {
		log.info("Start method called");
		communicationsService.initConnection();
	}
	@RequestMapping(value = "/sendMessage", method = RequestMethod.GET)
	public void send() throws ExecutionException, InterruptedException, IOException {
		log.info("send method called");
		communicationsService.sendMessage();
	}
	@RequestMapping(value = "/getGraph", method = RequestMethod.GET)
	@ResponseBody
	public float[][] getGraph() {
		log.info("GetGraph method called");
		return communicationsService.getGraph();
	}

	@RequestMapping(value = "/getCSVGraph", method = RequestMethod.GET)
	@ResponseBody
	public String getCSVGraph() {
		log.info("GetGraph method called");
		float[][] graph = communicationsService.getGraph();
		List<Point> points = communicationsService.getPoints();
		StringBuilder stringBuilder = new StringBuilder();

		points.sort((o1, o2) -> Integer.compare(o1.getP(), o2.getP()));

		stringBuilder.append("money");
		for (int i = 0; i < points.size(); i++) {
			stringBuilder.append(",");
			stringBuilder.append(points.get(i).getP());
		}
		stringBuilder.append("\n");

		for (int i = 0; i < graph.length; i++) {
			for (int j = 0; j < graph.length; j++) {
				if (j == 0) {
					stringBuilder.append(points.get(i).getMoney());
				}
				stringBuilder.append(",");
				stringBuilder.append(graph[i][j]);
			}
			stringBuilder.append("\n");
		}
		return stringBuilder.toString();
	}
}
