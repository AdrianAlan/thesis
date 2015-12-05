package org.fastfailover.app.utils;

import java.util.ArrayList;
import java.util.List;

import org.onlab.packet.MacAddress;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.DefaultGroupId;
import org.onosproject.core.GroupId;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flowobjective.DefaultForwardingObjective;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.group.DefaultGroupBucket;
import org.onosproject.net.group.DefaultGroupDescription;
import org.onosproject.net.group.DefaultGroupKey;
import org.onosproject.net.group.GroupBucket;
import org.onosproject.net.group.GroupBuckets;
import org.onosproject.net.group.GroupDescription;
import org.onosproject.net.group.GroupKey;

public class Utils {

	// CHANGE SETTINGS HERE:
	public static int uniqueGroupNumber = 0;

	/*
	 * public static String parseDeviceID(String of) { return "s" +
	 * Integer.parseInt(of.substring(3)); }
	 */

	public static ForwardingObjective insertStaticRuleForPort(String srcMac, String dstMac, int inputPort,
			int outputPort, ApplicationId appId) {
		// macs and ports
		MacAddress ffSourceMAC = MacAddress.valueOf(srcMac);
		MacAddress ffDestinationMAC = MacAddress.valueOf(dstMac);
		PortNumber ffInPortNumber = PortNumber.portNumber(inputPort);
		PortNumber ffOutPortNumber = PortNumber.portNumber(outputPort);

		// generate selector
		TrafficSelector.Builder ffSelectorBuilder = DefaultTrafficSelector.builder();
		ffSelectorBuilder.matchInPort(ffInPortNumber).matchEthSrc(ffSourceMAC).matchEthDst(ffDestinationMAC);

		// generate treatment
		TrafficTreatment ffTreatment = DefaultTrafficTreatment.builder().setOutput(ffOutPortNumber).build();
		ForwardingObjective ffForwardingObjective = DefaultForwardingObjective.builder()
				.withSelector(ffSelectorBuilder.build()).withTreatment(ffTreatment).withPriority(Settings.DEFAULT_PRIORITY)
				.withFlag(ForwardingObjective.Flag.VERSATILE).fromApp(appId).add();
		return ffForwardingObjective;
	}

	public static ForwardingObjective insertStaticRuleForGroup(String srcMac, String dstMac, String inputPort,
			int i, ApplicationId appId) {
		// macs and ports
		MacAddress ffSourceMAC = MacAddress.valueOf(srcMac);
		MacAddress ffDestinationMAC = MacAddress.valueOf(dstMac);
		PortNumber ffInPortNumber = PortNumber.portNumber(inputPort);
		GroupId id = new DefaultGroupId(i);

		// generate selector
		TrafficSelector.Builder ffSelectorBuilder = DefaultTrafficSelector.builder();
		ffSelectorBuilder.matchInPort(ffInPortNumber).matchEthSrc(ffSourceMAC).matchEthDst(ffDestinationMAC);

		// generate treatment
		TrafficTreatment ffTreatment = DefaultTrafficTreatment.builder().group(id).build();
		ForwardingObjective ffForwardingObjective = DefaultForwardingObjective.builder()
				.withSelector(ffSelectorBuilder.build()).withTreatment(ffTreatment).withPriority(Settings.DEFAULT_PRIORITY)
				.withFlag(ForwardingObjective.Flag.VERSATILE).fromApp(appId).add();
		return ffForwardingObjective;
	}

	public static GroupDescription insertGroupRule(List<String> ports, String deviceId, int groupId,
			ApplicationId appId, String inputPort) {
		//PortNumber.IN_PORT;
		// generate first choice port and backup port
		DeviceId ffDeviceId = DeviceId.deviceId(deviceId);
		List<GroupBucket> buckets = new ArrayList<GroupBucket>();
		for (int i=0; i < ports.size(); i++) {
			PortNumber portNumberWatch = PortNumber.portNumber(ports.get(i));
			PortNumber portNumberForward;
			if (ports.get(i).equals(inputPort)) {
				portNumberForward = PortNumber.IN_PORT;
			} else {
				portNumberForward = PortNumber.portNumber(ports.get(i));
			}
			TrafficTreatment treatment = DefaultTrafficTreatment.builder().setOutput(portNumberForward).build();
			GroupBucket bucket = DefaultGroupBucket.createFailoverGroupBucket(treatment, portNumberWatch,
					null);
			buckets.add(bucket);
		}
		
		GroupBuckets groupBuckets = new GroupBuckets(buckets);
		// group handling
		GroupKey groupKey = new DefaultGroupKey((groupId + "").getBytes());
		GroupDescription groupDesc = new DefaultGroupDescription(ffDeviceId, GroupDescription.Type.FAILOVER,
				groupBuckets, groupKey, groupId, appId);
		return groupDesc;
	}
}
