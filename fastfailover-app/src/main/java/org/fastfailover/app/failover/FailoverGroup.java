package org.fastfailover.app.failover;

import java.util.ArrayList;
import java.util.List;

public class FailoverGroup {
	
	private int id;
	private List<String> buckets = new ArrayList<String>();
	private String inputPort, srcMac, dstMac;

	public FailoverGroup(int id, String srcMac, String dstMac, String inputPort) {
		this.setId(id);
		this.setInputPort(inputPort);
		this.setSrcMac(srcMac);
		this.setDstMac(dstMac);
	}

	public String getInputPort() {
		return inputPort;
	}

	public void setInputPort(String inputPort) {
		this.inputPort = inputPort;
	}

	public String getSrcMac() {
		return srcMac;
	}

	public void setSrcMac(String srcMac) {
		this.srcMac = srcMac;
	}

	public String getDstMac() {
		return dstMac;
	}

	public void setDstMac(String dstMac) {
		this.dstMac = dstMac;
	}

	public void addBucket(String outPort) {
		if (!buckets.contains(outPort)) {
			buckets.add(outPort);
		}
	}
	
	public List<String> getBuckets() {
		return buckets;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
