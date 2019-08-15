package com.suomee.csp.lib.communication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SrvNode implements Serializable {
	private static final long serialVersionUID = 1L;
	private String host;
	private int port;
	
	public void setHost(String host) {
		this.host = host;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getHost() {
		return host;
	}
	public int getPort() {
		return port;
	}
	
	public SrvNode(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	@Override
	public String toString() {
		return this.host + ":" + this.port;
	}
	
	/**
	 * 把形如192.168.0.10:10011,192.168.0.20:10022的字符串解析成节点列表
	 * @param nodeString
	 * @return
	 * @throws Exception
	 */
	public static List<SrvNode> parseSrvNodes(String nodeString) throws Exception {
		return parse(nodeString, false);
	}
	
	public static List<SrvNode> parseSrvNodesSilently(String nodeString) {
		List<SrvNode> srvNodes = null;
		try {
			srvNodes = parse(nodeString, true);
		}
		catch (Exception e) {}
		return srvNodes;
	}
	
	private static List<SrvNode> parse(String nodeString, boolean silently) throws Exception {
		List<SrvNode> srvNodes = new ArrayList<SrvNode>();
		if (nodeString == null || nodeString.isEmpty()) {
			return srvNodes;
		}
		String[] nodeList = nodeString.trim().split(",");
		for (String node : nodeList) {
			String[] nodeParts = node.split(":");
			if (nodeParts.length != 2) {
				if (silently) {
					continue;
				}
				else {
					throw new Exception(node);
				}
			}
			String host = nodeParts[0];
			int port = 0;
			try {
				port = Integer.parseInt(nodeParts[1]);
			}
			catch (Exception e) {
				if (silently) {
					continue;
				}
				else {
					throw new Exception(node);
				}
			}
			srvNodes.add(new SrvNode(host, port));
		}
		return srvNodes;
	}
}
