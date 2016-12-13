package de.oglimmer.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionList {

	private AtomicInteger idGenerator = new AtomicInteger();

	private List<HttpRequestProcessor> hrp = new ArrayList<>();

	public void addConnection(int delay) {
		hrp.add(new HttpRequestProcessor(delay, idGenerator.incrementAndGet()));
	}

	public List<HttpRequestProcessor> getConnections() {
		return hrp;
	}

}
