package ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model.rest;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import ru.sbrf.globulsblock.hackathon.harvester.moneyharvester.model.Point;

import java.util.List;

@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor(staticName = "of", access = AccessLevel.PUBLIC)
public class PointsResponse {
	private List<Point> points;
}
