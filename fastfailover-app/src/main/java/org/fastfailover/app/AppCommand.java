/*
 * Copyright 2014 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fastfailover.app;

import java.util.ArrayList;
import java.util.List;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.fastfailover.app.utils.Settings;
import org.fastfailover.app.utils.Utils;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.onosproject.net.group.DefaultGroupBucket;
import org.onosproject.net.group.DefaultGroupKey;
import org.onosproject.net.group.Group;
import org.onosproject.net.group.GroupBucket;
import org.onosproject.net.group.GroupBuckets;
import org.onosproject.net.group.GroupKey;
import org.onosproject.net.group.GroupService;
import org.onosproject.net.topology.TopologyService;
import org.onosproject.net.topology.TopologyVertex;

/**
 * Sample Apache Karaf CLI command
 */
@Command(scope = "onos", name = "add", description = "Add new rule")
public class AppCommand extends AbstractShellCommand {

	final CoreService service1 = get(CoreService.class);
	final TopologyService service2 = get(TopologyService.class);
	final FlowObjectiveService service3 = get(FlowObjectiveService.class);
	final GroupService service4 = get(GroupService.class);

	@Argument(index = 0, name = "type", description = "Type of rule", required = true, multiValued = false)
	private String type = null;
	@Argument(index = 1, name = "arg1", description = "Argument", required = false, multiValued = false)
	private String arg1 = null;
	@Argument(index = 2, name = "arg2", description = "Argument", required = false, multiValued = false)
	private String arg2 = null;

	@Override
	protected void execute() {
		ApplicationId id = service1.getAppId("org.fastfailover.app");
		if (type.equals("fw")) {
			System.out.println("Blocking communincation from " + arg1 + " to " + arg2);
			for (TopologyVertex v : service2.getGraph(service2.currentTopology()).getVertexes()) {
				service3.forward(v.deviceId(), Utils.insertStaticRuleForFirewall(arg1, arg2, id));
			}
			
			System.out.println("Success");
		} else if (type.equals("lb")) {
			System.out.println("Changing group " + arg2 + " on node " + arg1 + " into type SELECT");
			DeviceId deviceId = DeviceId.deviceId(arg1);
			GroupKey groupKey = new DefaultGroupKey(arg2.getBytes());
			Group g = service4.getGroup(deviceId, groupKey);
			GroupBuckets gb = g.buckets();
			
			List<GroupBucket> buckets = new ArrayList<GroupBucket>();
			for (GroupBucket b: gb.buckets()) {
				GroupBucket bucket = DefaultGroupBucket.createSelectGroupBucket(b.treatment(), (short) 50);
				buckets.add(bucket);
			}
			
			GroupBuckets groupBuckets = new GroupBuckets(buckets);
			service4.removeGroup(deviceId, groupKey, id);
			while (true) {
				if (service4.getGroup(deviceId, groupKey) == null) {
					service4.addGroup(Utils.getSelectGroupDescription(deviceId, groupBuckets, arg2, id));
					service3.forward(deviceId, Utils.tf.get(new Integer(arg2)));
					break;
				}
			}
			
			System.out.println("Success");
		} else if (type.equals("opt")) {
			System.out.print("Enabling optimization...");
			Settings.OPTIMIZATION = true;
			System.out.println("Success");
		}
	}

}
