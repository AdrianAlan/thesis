package org.fastfailover.app.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public static Map<Integer, ForwardingObjective> tf = new HashMap<Integer, ForwardingObjective>();

	public static ForwardingObjective insertStaticRuleForFirewall(String srcMac, String dstMac, ApplicationId appId) {
		// macs and ports
		MacAddress ffSourceMAC = MacAddress.valueOf(srcMac);
		MacAddress ffDestinationMAC = MacAddress.valueOf(dstMac);

		// generate selector
		TrafficSelector.Builder ffSelectorBuilder = DefaultTrafficSelector.builder();
		ffSelectorBuilder.matchEthSrc(ffSourceMAC).matchEthDst(ffDestinationMAC);

		// generate treatment
		TrafficTreatment ffTreatment = DefaultTrafficTreatment.builder().drop().build();
		ForwardingObjective ffForwardingObjective = DefaultForwardingObjective.builder()
				.withSelector(ffSelectorBuilder.build()).withTreatment(ffTreatment).withPriority(Settings.MAX_PRIORITY)
				.withFlag(ForwardingObjective.Flag.VERSATILE).fromApp(appId).add();
		return ffForwardingObjective;
	}

	public static ForwardingObjective insertStaticRuleForGroup(String srcMac, String dstMac, String inputPort, int i,
			ApplicationId appId) {
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
				.withSelector(ffSelectorBuilder.build()).withTreatment(ffTreatment)
				.withPriority(Settings.DEFAULT_PRIORITY).withFlag(ForwardingObjective.Flag.VERSATILE).fromApp(appId)
				.add();
		tf.put(i, ffForwardingObjective);
		return ffForwardingObjective;
	}

	public static GroupDescription insertGroupRule(List<String> ports, String deviceId, int groupId,
			ApplicationId appId, String inputPort) {
		// generate first choice port and backup port
		DeviceId ffDeviceId = DeviceId.deviceId(deviceId);
		List<GroupBucket> buckets = new ArrayList<GroupBucket>();
		for (int i = 0; i < ports.size(); i++) {
			PortNumber portNumberWatch = PortNumber.portNumber(ports.get(i));
			PortNumber portNumberForward;
			if (ports.get(i).equals(inputPort)) {
				portNumberForward = PortNumber.IN_PORT;
			} else {
				portNumberForward = PortNumber.portNumber(ports.get(i));
			}
			TrafficTreatment treatment = DefaultTrafficTreatment.builder().setOutput(portNumberForward).build();
			GroupBucket bucket = DefaultGroupBucket.createFailoverGroupBucket(treatment, portNumberWatch, null);
			buckets.add(bucket);
		}

		GroupBuckets groupBuckets = new GroupBuckets(buckets);
		// group handling
		GroupKey groupKey = new DefaultGroupKey((groupId + "").getBytes());
		GroupDescription groupDesc = new DefaultGroupDescription(ffDeviceId, GroupDescription.Type.FAILOVER,
				groupBuckets, groupKey, groupId, appId);
		return groupDesc;
	}

	public static GroupDescription getSelectGroupDescription(DeviceId device, GroupBuckets buckets, String arg2,
			ApplicationId appId) {
		GroupDescription groupDsc = new DefaultGroupDescription(device, GroupDescription.Type.SELECT, buckets,
				new DefaultGroupKey(arg2.getBytes()), new Integer(arg2), appId);
		return groupDsc;
	}

	public static ForwardingObjective getSelectGroupFlowRule(TrafficSelector ffSelectorBuilder, GroupId groupId,
			ApplicationId appId) {
		TrafficTreatment ffTreatment = DefaultTrafficTreatment.builder().group(groupId).build();
		ForwardingObjective ffForwardingObjective = DefaultForwardingObjective.builder()
				.withSelector(ffSelectorBuilder).withTreatment(ffTreatment)
				.withPriority(Settings.DEFAULT_PRIORITY).withFlag(ForwardingObjective.Flag.VERSATILE).fromApp(appId)
				.add();
		return ffForwardingObjective;
	}
}
