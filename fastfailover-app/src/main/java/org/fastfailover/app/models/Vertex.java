package org.fastfailover.app.models;

import java.util.HashMap;
import java.util.Map;

import org.onosproject.net.DeviceId;

public class Vertex implements Comparable<Vertex> {
	private final DeviceId device;
	private Map<Vertex, Edge> adjacencies = new HashMap<Vertex, Edge>();
	private double minDistance = Double.POSITIVE_INFINITY;
	private Vertex previous;

	public Vertex(DeviceId deviceId) {
		this.device = deviceId;
	}
	
	public DeviceId getDeviceId() {
		return device;
	}

	public String toString() {
		return device.toString();
	}

	public int compareTo(Vertex other) {
		return Double.compare(minDistance, other.minDistance);
	}

	public void addEdge(Vertex vx, Edge ed) {
		adjacencies.put(vx, ed);
	}

	public Vertex getPrevious() {
		return previous;
	}

	public void setPrevious(Vertex previous) {
		this.previous = previous;
	}
	
	public double getMinDistance() {
		return minDistance;
	}

	public void setMinDistance(double minDistance) {
		this.minDistance = minDistance;
	}
	
	public Map<Vertex, Edge> getAdjacencies() {
		return adjacencies;
	}
}