package org.fastfailover.app.failover;

import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.group.DefaultGroupBucket;
import org.onosproject.net.group.GroupBucket;

public class FastFailoverBucket {
	
	public static GroupBucket getFastFailoverBucket(PortNumber watchPort) {
		TrafficTreatment ffFirstTreatment = DefaultTrafficTreatment.builder().setOutput(watchPort).build();
		return DefaultGroupBucket.createFailoverGroupBucket(ffFirstTreatment, watchPort, null);
	}
}
