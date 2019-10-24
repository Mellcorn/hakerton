package ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model.rest;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor(staticName = "of", access = AccessLevel.PUBLIC)
public class CarArrivedResponse {
	private int point;
	private String car;
	private int carsum;
}
