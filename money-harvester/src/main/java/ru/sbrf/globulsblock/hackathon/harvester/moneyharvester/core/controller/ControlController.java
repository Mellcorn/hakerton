package ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.core.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.core.services.CommunicationsService;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
@Slf4j
public class ControlController {
	@Autowired
	private CommunicationsService communicationsService;

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
}
