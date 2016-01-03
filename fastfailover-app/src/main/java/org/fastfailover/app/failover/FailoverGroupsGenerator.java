package org.fastfailover.app.failover;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fastfailover.app.models.Edge;
import org.fastfailover.app.models.Vertex;
import org.fastfailover.app.utils.Settings;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.topology.TopologyEdge;
import org.onosproject.net.topology.TopologyGraph;
import org.onosproject.net.topology.TopologyVertex;

public class FailoverGroupsGenerator {
	private Map<String, FailoverGroupController> failoverGroupList = new HashMap<String, FailoverGroupController>();

	public FailoverGroupsGenerator(DeviceId deviceIdFrom, DeviceId deviceIdTo, TopologyGraph topologyGraph,
			long inputPort, String srcMac, String dstMac) {
		buildNetworkFromTopologyGraph(deviceIdFrom, deviceIdTo, topologyGraph, inputPort, srcMac, dstMac);
	}

	public Map<String, FailoverGroupController> getFailoverGroups() {
		return failoverGroupList;
	}

	private void buildNetworkFromTopologyGraph(DeviceId deviceIdFrom, DeviceId deviceIdTo, TopologyGraph topologyGraph,
			long inputPort, String srcMac, String dstMac) {
		Map<String, Vertex> vertexMap = new HashMap<String, Vertex>();

		Vertex fromVertex = null, toVertex = null;
		for (TopologyVertex v : topologyGraph.getVertexes()) {
			Vertex newVertex = new Vertex(v.deviceId());
			vertexMap.put(v.deviceId().toString(), newVertex);
			if (v.deviceId().toString().equals(deviceIdFrom.toString())) {
				fromVertex = newVertex;
			} else if (v.deviceId().toString().equals(deviceIdTo.toString())) {
				toVertex = newVertex;
			}
		}

		for (TopologyEdge e : topologyGraph.getEdges()) {
			String keySrc = e.src().deviceId().toString();
			String keyDst = e.dst().deviceId().toString();
			PortNumber port = e.link().src().port();
			Vertex vSrc = vertexMap.get(keySrc);
			Vertex vDst = vertexMap.get(keyDst);
			vSrc.addEdge(vDst, new Edge(vDst, port, readWeight(vSrc, vDst)));
		}

		getGroups(fromVertex, toVertex, vertexMap, inputPort, srcMac, dstMac, 0);
	}

	private double readWeight(Vertex vSrc, Vertex vDst) {
		if (!Settings.OPTIMIZATION) {
			return 1;
		}
		try (BufferedReader br = new BufferedReader(new FileReader(Settings.NET_CONFIG))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		       if (line.startsWith(vSrc.toString() + "-" + vDst.toString())) {
		    	   return new Double(line.split("::")[1]);
		       }
		    }
		} catch (FileNotFoundException e) {
			System.out.println("No configuration found in " + Settings.NET_CONFIG);
		} catch (IOException e) {
			System.out.println("Incorrect file formatting in " + Settings.NET_CONFIG);
		}
		return 10;
	}

	private void tearDownConnection(Vertex fromVertex, Vertex toVertex, Map<String, Vertex> vertexMap, long inputPort,
			String pointOne, String pointTwo, String srcMac, String dstMac, int depth) {
		
		
		Map<String, Vertex> newVertexMap = new HashMap<String, Vertex>();
		Vertex newFromVertex = null, newToVertex = null;

		// add vertex
		for (Vertex v : vertexMap.values()) {
			Vertex newVertex = new Vertex(v.getDeviceId());
			newVertexMap.put(v.toString(), newVertex);

			if (v.toString().equals(fromVertex.toString())) {
				newFromVertex = newVertex;
			} else if (v.toString().equals(toVertex.toString())) {
				newToVertex = newVertex;
			}
		}

		// add edge
		for (Vertex v : vertexMap.values()) {
			for (Edge e : v.getAdjacencies().values()) {
				String src = v.toString();
				String dst = e.getTarget().toString();
				if (!((src == pointOne && dst == pointTwo) || (dst == pointOne && src == pointTwo))) {
					Vertex vSrc = newVertexMap.get(src);
					Vertex vDst = newVertexMap.get(dst);
					PortNumber port = e.getPortNumber();
					vSrc.addEdge(vDst, new Edge(vDst, port, readWeight(vSrc, vDst)));
				}
			}
		}

		getGroups(newFromVertex, newToVertex, newVertexMap, inputPort, srcMac, dstMac, depth);
	}

	private void getGroups(Vertex fromVertex, Vertex toVertex, Map<String, Vertex> vertexMap, long inputPort,
			String srcMac, String dstMac, int depth) {
		DijkstraAlgorithm da = new DijkstraAlgorithm();
		da.computePaths(fromVertex);
		List<Vertex> path = da.getShortestPathTo(toVertex);

		if (path.size() < 2 || depth > 5) {
			return;
		}

		Vertex midVertex;
		Vertex maxVertex;

		for (int i = 0; i < path.size() - 1; i++) {
			midVertex = path.get(i);
			maxVertex = path.get(i + 1);
			if (i - 1 >= 0) {
				inputPort = midVertex.getAdjacencies().get(path.get(i - 1)).getPortNumber().toLong();
			}

			if (!failoverGroupList.containsKey(midVertex.toString())) {
				failoverGroupList.put(midVertex.toString(), new FailoverGroupController());
			}
			failoverGroupList.get(midVertex.toString()).addGroups(srcMac, dstMac, inputPort + "",
					midVertex.getAdjacencies().get(maxVertex).getPortNumber().toString());
			depth++;
			tearDownConnection(midVertex, toVertex, vertexMap, inputPort, midVertex.toString(), maxVertex.toString(),
					srcMac, dstMac, depth);
		}
	}
}
