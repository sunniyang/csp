package com.suomee.csp.lib.util;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class EnvUtil {
	private static int pid = 0;
	private static String localAddress = null;
	private static final Object mutex = new Object();
	
	//获取当前进程ID
	public static int getPid() {
		if (pid == 0) {
			synchronized (mutex) {
				if (pid == 0) {
					String runName = ManagementFactory.getRuntimeMXBean().getName();
					pid = Integer.parseInt(runName.substring(0, runName.indexOf("@")));
				}
			}
		}
		return pid;
	}
	
	/**
	 * 获取本地第一个非回环地址，如果获取不到，则返回0.0.0.0
	 */
	public static String getLocalAddress() {
		if (localAddress == null) {
			synchronized (mutex) {
				if (localAddress == null) {
					try {
						Enumeration<NetworkInterface> inters = NetworkInterface.getNetworkInterfaces();
						while (inters.hasMoreElements()) {
							NetworkInterface inter = inters.nextElement();
							if (inter.isLoopback() || inter.isVirtual() || !inter.isUp()) {
								continue;
							}
							Enumeration<InetAddress> addrs = inter.getInetAddresses();
							while (addrs.hasMoreElements()) {
								String addr = addrs.nextElement().getHostAddress();
								if (addr.indexOf(":") >= 0) {
									continue;
								}
								localAddress = addr;
								break;
							}
						}
						if (localAddress == null) {
							localAddress = "0.0.0.0";
						}
					}
					catch (SocketException e) {
						localAddress = "0.0.0.0";
					}
				}
			}
		}
		return localAddress;
	}
}
