package org.fastfailover.app.bfd;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Set;

import org.fastfailover.app.utils.Settings;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.DeviceId;
import org.onosproject.net.topology.TopologyEdge;
import org.slf4j.Logger;

public class BFDUtils {

	public static String deviceIdToLogicalName(DeviceId id) {
		return "s" + Long.parseLong(id.toString().substring(3), 16);
	}
	
	public static void getBFDDetails(Set<TopologyEdge> te, ApplicationId appId, Logger log) {
		String jsonBuilder = "[";
		for (TopologyEdge e : te) {
			String linkFrom = deviceIdToLogicalName(e.link().src().deviceId()) + "-eth" + e.link().src().port().toString();
			String linkTo = deviceIdToLogicalName(e.link().dst().deviceId()) + "-eth" + e.link().dst().port().toString();
			jsonBuilder += "{\"from\":\"" + linkFrom + "\",\"to\":\"" + linkTo + "\"},";
		}
		jsonBuilder = jsonBuilder.substring(0, jsonBuilder.length()-1) + "]";
		log.info(jsonBuilder);
		try {
			PrintWriter writer = new PrintWriter(Settings.BFD_CONFIG, "UTF-8");
			writer.println(jsonBuilder);
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
