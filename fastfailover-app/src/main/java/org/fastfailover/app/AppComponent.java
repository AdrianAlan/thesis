package org.fastfailover.app;

import org.apache.felix.scr.annotations.Activate;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.fastfailover.app.bfd.BFDUtils;
import org.fastfailover.app.failover.FailoverGroup;
import org.fastfailover.app.failover.FailoverGroupController;
import org.fastfailover.app.failover.FailoverGroupsGenerator;
import org.fastfailover.app.utils.Settings;
import org.fastfailover.app.utils.Utils;
import org.onlab.packet.Ethernet;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.event.Event;
import org.onosproject.net.Host;
import org.onosproject.net.HostId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flowobjective.DefaultForwardingObjective;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.group.Group;
import org.onosproject.net.group.GroupService;
import org.onosproject.net.host.HostService;
import org.onosproject.net.link.LinkEvent;
import org.onosproject.net.packet.InboundPacket;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketPriority;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketService;
import org.onosproject.net.topology.TopologyEvent;
import org.onosproject.net.topology.TopologyGraph;
import org.onosproject.net.topology.TopologyListener;
import org.onosproject.net.topology.TopologyService;
import org.onosproject.net.topology.TopologyVertex;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import org.slf4j.LoggerFactory;

/**
 * ONOS Fast Failover Application.
 * 
 * @author Adrian Alan Pol
 */
@Component(immediate = true)
public class AppComponent {

	// References
	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	protected CoreService coreService;
	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	protected FlowRuleService flowRuleService;
	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	protected GroupService groupService;
	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	protected HostService hostService;
	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	protected FlowObjectiveService flowObjectiveService;
	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	protected PacketService packetService;
	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	protected TopologyService topologyService;

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final TopologyListener topologyListener = new InternalTopologyListener();

	private ReactivePacketProcessor processor = new ReactivePacketProcessor();

	public static ApplicationId appId;
	public static TopologyGraph topologyGraph;

	@Activate
	protected void activate() {
		appId = coreService.registerApplication("org.fastfailover.app");
		packetService.addProcessor(processor, PacketProcessor.director(2));
		topologyService.addListener(topologyListener);
		requestIntercepts();
		topologyGraph = topologyService.getGraph(topologyService.currentTopology());
		BFDUtils.getBFDDetails(topologyGraph.getEdges(), appId, log);
		log.info("Start Completed", appId.id());
	}

	@Deactivate
	protected void deactivate() {
		withdrawIntercepts();
		flowRuleService.removeFlowRulesById(appId);
		// removing group entries
		for (TopologyVertex v : topologyService.getGraph(topologyService.currentTopology()).getVertexes()) {
			for (Group g : groupService.getGroups(v.deviceId())) {
				groupService.removeGroup(v.deviceId(), g.appCookie(), appId);
			}
		}
		packetService.removeProcessor(processor);
		topologyService.removeListener(topologyListener);
		processor = null;
		log.info("Termination completed", appId.id());
	}

	/**
	 * Cancel request for packet in via packet service.
	 */
	private void withdrawIntercepts() {
		TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
		selector.matchEthType(Ethernet.TYPE_IPV4);
		packetService.cancelPackets(selector.build(), PacketPriority.REACTIVE, appId);
		selector.matchEthType(Ethernet.TYPE_ARP);
		packetService.cancelPackets(selector.build(), PacketPriority.REACTIVE, appId);
		selector.matchEthType(Ethernet.TYPE_IPV6);
		packetService.cancelPackets(selector.build(), PacketPriority.REACTIVE, appId);
	}

	/**
	 * Request packet in via packet service.
	 */
	private void requestIntercepts() {
		TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
		selector.matchEthType(Ethernet.TYPE_IPV4);
		packetService.requestPackets(selector.build(), PacketPriority.REACTIVE, appId);
		selector.matchEthType(Ethernet.TYPE_ARP);
		packetService.requestPackets(selector.build(), PacketPriority.REACTIVE, appId);
		selector.matchEthType(Ethernet.TYPE_IPV6);
		packetService.cancelPackets(selector.build(), PacketPriority.REACTIVE, appId);
	}

	/**
	 * Packet processor responsible for forwarding packets along their paths.
	 */
	private class ReactivePacketProcessor implements PacketProcessor {

		@Override
		public void process(PacketContext context) {
			// Stop processing if the packet has been handled, since we
			// can't do any more to it.
			if (context.isHandled()) {
				return;
			}

			InboundPacket pkt = context.inPacket();
			Ethernet ethPkt = pkt.parsed();
			if (ethPkt == null) {
				return;
			}

			// Bail if this is deemed to be a control packet.
			if (isControlPacket(ethPkt)) {
				return;
			}

			// Skip IPv6 multicast packet when IPv6 forward is disabled.
			if (isIpv6Multicast(ethPkt)) {
				return;
			}

			HostId id = HostId.hostId(ethPkt.getDestinationMAC());
			// Do not process link-local addresses in any way.
			if (id.mac().isLinkLocal()) {
				return;
			}

			// Do we know who this is for? If not, flood and bail.
			Host dst = hostService.getHost(id);
			if (dst == null) {
				flood(context);
				return;
			}

			// Are we on an edge switch that our destination is on? If so,
			// simply forward out to the destination and bail.
			if (pkt.receivedFrom().deviceId().equals(dst.location().deviceId())) {
				if (!context.inPacket().receivedFrom().port().equals(dst.location().port())) {
					installRule(context, dst.location().port());
				}
				return;
			}

			FailoverGroupsGenerator fgg = new FailoverGroupsGenerator(pkt.receivedFrom().deviceId(),
					dst.location().deviceId(), topologyService.getGraph(topologyService.currentTopology()),
					context.inPacket().receivedFrom().port().toLong(), pkt.parsed().getSourceMAC().toString(),
					pkt.parsed().getDestinationMAC().toString());

			Map<String, FailoverGroupController> smth = fgg.getFailoverGroups();
			String portNumber = null;
			for (TopologyVertex v : topologyService.getGraph(topologyService.currentTopology()).getVertexes()) {
				if (smth.containsKey(v.deviceId().toString())) {
					FailoverGroupController g = smth.get(v.deviceId().toString());
					for (FailoverGroup g1 : g.getGroups()) {
						groupService.addGroup(Utils.insertGroupRule(g1.getBuckets(), v.deviceId().toString(),
								g1.getId(), appId, g1.getInputPort()));
						flowObjectiveService.forward(v.deviceId(),
								Utils.insertStaticRuleForGroup(pkt.parsed().getSourceMAC().toString(),
										pkt.parsed().getDestinationMAC().toString(), g1.getInputPort(), g1.getId(),
										appId));
						if (pkt.receivedFrom().deviceId().equals(v.deviceId())) {
							if (g1.getInputPort().equals(context.inPacket().receivedFrom().port().toString())
									&& g1.getDstMac().equals(pkt.parsed().getDestinationMAC().toString())
									&& g1.getSrcMac().equals(pkt.parsed().getSourceMAC().toString())) {
								portNumber = g1.getBuckets().get(0);
							}
						}
					}
				}
			}
			if (portNumber != null) {
				packetOut(context, PortNumber.portNumber(portNumber));
			}
		}
	}

	// Indicates whether this is a control packet, e.g. LLDP, BDDP
	private boolean isControlPacket(Ethernet eth) {
		short type = eth.getEtherType();
		return type == Ethernet.TYPE_LLDP || type == Ethernet.TYPE_BSN;
	}

	// Indicated whether this is an IPv6 multicast packet.
	private boolean isIpv6Multicast(Ethernet eth) {
		return eth.getEtherType() == Ethernet.TYPE_IPV6 && eth.isMulticast();
	}

	// Floods the specified packet if permissible.
	private void flood(PacketContext context) {
		if (topologyService.isBroadcastPoint(topologyService.currentTopology(), context.inPacket().receivedFrom())) {
			packetOut(context, PortNumber.FLOOD);
		} else {
			context.block();
		}
	}

	// Sends a packet out the specified port.
	private void packetOut(PacketContext context, PortNumber portNumber) {
		context.treatmentBuilder().setOutput(portNumber);
		context.send();
	}

	// Install a rule forwarding the packet to the specified port.
	private void installRule(PacketContext context, PortNumber portNumber) {
		//
		// We don't support (yet) buffer IDs in the Flow Service so
		// packet out first.
		//
		Ethernet inPkt = context.inPacket().parsed();

		// If PacketOutOnly or ARP packet than forward directly to output port
		if (inPkt.getEtherType() == Ethernet.TYPE_ARP) {
			packetOut(context, portNumber);
			return;
		}

		TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();
		// Create flows with default matching and include configured fields
		selectorBuilder.matchInPort(context.inPacket().receivedFrom().port()).matchEthSrc(inPkt.getSourceMAC())
				.matchEthDst(inPkt.getDestinationMAC()); // MATCH ACTION
		TrafficTreatment treatment = DefaultTrafficTreatment.builder().setOutput(portNumber).build();
		ForwardingObjective forwardingObjective = DefaultForwardingObjective.builder()
				.withSelector(selectorBuilder.build()).withTreatment(treatment).withPriority(Settings.DEFAULT_PRIORITY)
				.withFlag(ForwardingObjective.Flag.VERSATILE).fromApp(appId).add();
		flowObjectiveService.forward(context.inPacket().receivedFrom().deviceId(), forwardingObjective);

		// Send packet direction on the appropriate port
		packetOut(context, portNumber);
	}

	private void Removal() {
		for (TopologyVertex v : topologyService.getGraph(topologyService.currentTopology()).getVertexes()) {
			for (Group g : groupService.getGroups(v.deviceId())) {
				groupService.removeGroup(v.deviceId(), g.appCookie(), appId);
			}
		}
		flowRuleService.removeFlowRulesById(appId);
	}

	public class MyThread extends Thread {
		public void run() {
			try {
				Thread.sleep(Settings.RESTORATION_TIME);	    
			} catch(InterruptedException ex) {
				log.info("Interrupted");
			    Thread.currentThread().interrupt();
			}
			log.info("Running");
			Removal();
		}
	}
	
	private class InternalTopologyListener implements TopologyListener {
		@Override
		public void event(TopologyEvent event) {
			List<Event> reasons = event.reasons();
			if (reasons != null) {
				reasons.forEach(re -> {
					if (re instanceof LinkEvent) {
						LinkEvent le = (LinkEvent) re;
						MyThread myThread = new MyThread();
						if (le.type() == LinkEvent.Type.LINK_REMOVED) {
							log.info("Restoration procedure");
							myThread.start();
						}
						if (le.type() == LinkEvent.Type.LINK_ADDED) {
							log.info("Cancel Restoration");
							myThread.interrupt();
						}
					}
				});
			}
		}
	}
}