package org.fastfailover.app.failover;

import java.util.ArrayList;
import java.util.List;

import org.fastfailover.app.utils.Utils;

public class FailoverGroupController {

	private List<FailoverGroup> groups = new ArrayList<FailoverGroup>();
	
	public void addGroups(String src, String dst, String inPort, String outPort) {
		for (FailoverGroup g: groups) {
			if (g.getSrcMac().equals(src) && g.getDstMac().equals(dst) && g.getInputPort().equals(inPort)) {
				g.addBucket(outPort);
				return;
			}
		}
		
		FailoverGroup gr = new FailoverGroup(Utils.uniqueGroupNumber, src, dst, inPort);
		Utils.uniqueGroupNumber++;
		gr.addBucket(outPort);
		groups.add(gr);
	}
	
	public List<FailoverGroup> getGroups() {
		return groups;
	}
}
