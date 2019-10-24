package ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.Queue;

@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor(staticName = "of", access = AccessLevel.PUBLIC)
public class Car {
	private String id;
	private int capacity;
	private int startPointId;
	private int endPointId;
	private Queue<Integer> path;
}
