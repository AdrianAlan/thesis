package org.fastfailover.app.models;

import org.onosproject.net.PortNumber;

public class Edge {

	private final Vertex target;
	private final double weight;
	private PortNumber portNumber;

	public Edge(Vertex target, PortNumber portNumber, double weight) {
		this.target = target;
		this.portNumber = portNumber;
		this.weight = weight;
	}

	public PortNumber getPortNumber() {
		return portNumber;
	}

	public Vertex getTarget() {
		return target;
	}

	public double getWeight() {
		return weight;
	}
}
