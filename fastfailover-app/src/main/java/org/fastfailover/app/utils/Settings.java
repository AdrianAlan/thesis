package org.fastfailover.app.utils;

public class Settings {

	public static final int DEFAULT_TIMEOUT = 10000;
	public static final int RESTORATION_TIME = 10000;
	public static final int DEFAULT_PRIORITY = 100;
	public static final int MAX_PRIORITY = (1 << 16) - 1;
	public static final String NET_CONFIG = "/tmp/network.conf";
	public static final String BFD_CONFIG = "/tmp/bfd.cfg";
	public static boolean OPTIMIZATION = false;
}
