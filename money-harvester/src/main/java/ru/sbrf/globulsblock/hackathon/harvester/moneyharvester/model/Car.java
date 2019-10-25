package ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model;

import lombok.*;
import lombok.experimental.Wither;

import java.util.Queue;

@Setter
@Getter
@Wither
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor(staticName = "of", access = AccessLevel.PUBLIC)
public class Car {
	private String id;
	private int capacity = 1_000_000;
	private int startPointId = 0;
	private int endPointId = 0;
	private Queue<Integer> path;
}
