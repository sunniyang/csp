package com.suomee.csp.lib.communication;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.suomee.csp.domain.DomainNames;
import com.suomee.csp.domain.proto.QuerySrvNodesReq;
import com.suomee.csp.domain.proto.QuerySrvNodesRsp;
import com.suomee.csp.domain.proxy.DomainSrvProxy;
import com.suomee.csp.lib.future.SrvFuture;
import com.suomee.csp.lib.log.Logger;
import com.suomee.csp.lib.proxy.SrvProxyFactory;
import com.suomee.csp.lib.server.Server;
import com.suomee.csp.lib.util.ScheduledTask;

/**
 * 远程服务可用节点管理器
 * 当调用客户端向管理器请求某个服务的可用节点列表时，如果管理器已有缓存则直接返回，否则向主控获取该列表，并缓存起来
 * 每分钟从主控更新当前缓存起来的服务节点
 * 获取到的各个服务的可用节点中，对于DomainServer的可用节点除了更新缓存，还要负责更新本地启动文件的-Dcsp.domains参数
 * 
 * 管理器接受调用客户端向其报告节点失效，如果在一个刷新周期内管理器接收到同一个节点失效达到3次，则会将该服务节点从可用队列中去除
 * 去除的节点是否恢复取决于从主控更新到的信息
 * @author sunniyang
 *
 */
public class SrvNodeManager extends ScheduledTask {
	private static SrvNodeManager instance = null;
	private static Object mutex = new Object();
	public static SrvNodeManager getInstance() {
		if (instance == null) {
			synchronized (mutex) {
				if (instance == null) {
					instance = new SrvNodeManager();
				}
			}
		}
		return instance;
	}
	
	private static final int REFRESH_MILLISECONDS = 60000;
	private Map<String, List<SrvNode>> srvNodes;
	//private Map<String, List<Long>> invalidSrvNodes;
	
	private SrvNodeManager() {
		super(0, REFRESH_MILLISECONDS, true);
		this.setDaemon(true);
		this.srvNodes = new HashMap<String, List<SrvNode>>();
		//this.invalidSrvNodes = new HashMap<String, List<Long>>();
	}
	
	public void initDomainNodes(List<SrvNode> nodes) {
		this.srvNodes.put(com.suomee.csp.domain.DomainNames.DOMAIN_SRV_NAME, nodes);
	}
	
	public void init() {
		this.start();
	}
	
	public void shutdown() {
		this.stop();
	}
	
	public List<SrvNode> getSrvNodes(String srvName) {
		if (srvName == null || srvName.isEmpty()) {
			return null;
		}
		List<SrvNode> nodes = this.srvNodes.get(srvName);
		if (nodes != null) {
			return nodes;
		}
		
		try {
			Set<String> srvNames = new HashSet<String>();
			srvNames.add(srvName);
			Map<String, List<SrvNode>> srvNodes = this.refresh(srvNames);
			synchronized (this) {
				this.srvNodes.putAll(srvNodes);
			}
		}
		catch (Exception e) {
			Logger.getLogger("csp_error").error("query srvNodes from domain exception. srvName=" + srvName, e);
		}
		
		return this.srvNodes.get(srvName);
	}
	
	//TODO:上报无效节点
	public void reportInvalidSrvNode(String srvName, SrvNode srvNode) {
		/*if (srvName == null || srvName.isEmpty() || srvNode == null) {
			return;
		}
		String key = srvName + "-" + srvNode.toString();
		long invalidTime = DateTimeUtil.getNowSeconds();
		Logger.getLogger("invalid_node").info(invalidTime + "|" + key);
		synchronized (this) {
			List<Long> invalids = this.invalidSrvNodes.get(key);
			if (invalids == null) {
				invalids = new LinkedList<Long>();
				invalids.add(invalidTime);
				this.invalidSrvNodes.put(key, invalids);
			}
			else {
				if (invalids.size() < 2) {
					invalids.add(invalidTime);
				}
				else {
					long firstInvalidTime = invalids.get(0);
					if (invalidTime - firstInvalidTime > REFRESH_MILLISECONDS) {
						invalids.remove(0);
						invalids.add(invalidTime);
					}
					else {
						invalids.clear();
						List<SrvNode> nodes = this.srvNodes.get(srvName);
						if (nodes == null) {
							return;
						}
						for (int i = 0; i < nodes.size(); i++) {
							SrvNode node = nodes.get(i);
							if (node.toString().equals(srvNode.toString())) {
								nodes.remove(i);
								break;
							}
						}
					}
				}
			}
		}*/
	}
	
	@Override
	protected void doExecute() {
		try {
			Set<String> srvNames = new HashSet<String>();
			srvNames.addAll(this.srvNodes.keySet());
			Map<String, List<SrvNode>> srvNodes = this.refresh(srvNames);
			//TODO:
			//List<SrvNode> oldDomainNodes = this.srvNodes.get(com.suomee.csp.domain.DomainNames.DOMAIN_SRV_NAME);
			//List<SrvNode> newDomainNodes = srvNodes.get(com.suomee.csp.domain.DomainNames.DOMAIN_SRV_NAME);
			//boolean domainChanged = this.isDomainNodesChanged(oldDomainNodes, newDomainNodes);
			
			this.srvNodes.putAll(srvNodes);
			//if (domainChanged) {
			//	this.updateStartFile(newDomainNodes);
			//}
		}
		catch (Exception e) {
			Logger.getLogger("csp_error").error("refresh srvNodes from domain exception.", e);
		}
	}
	
	private Map<String, List<SrvNode>> refresh(Set<String> srvNames) throws Exception {
		if (srvNames == null || srvNames.isEmpty()) {
			return new HashMap<String, List<SrvNode>>();
		}
		if (Server.getInstance() != null) {
			String serverFullName = Server.getInstance().getServerInfo().getFullName();
			if (serverFullName != null && serverFullName.equals(DomainNames.SERVER_FULL_NAME)) {
				return new HashMap<String, List<SrvNode>>();
			}
		}
		DomainSrvProxy proxy = SrvProxyFactory.getSrvProxy(DomainSrvProxy.class);
		QuerySrvNodesReq req = new QuerySrvNodesReq();
		req.setSrvNames(srvNames);
		SrvFuture<QuerySrvNodesRsp> rspFuture = proxy.querySrvNodes(req);
		rspFuture.sync();
		if (rspFuture.getException() != null) {
			throw rspFuture.getException();
		}
		QuerySrvNodesRsp rsp = rspFuture.getResult();
		return rsp.getSrvNodes();
	}
	
	/*private boolean isDomainNodesChanged(List<SrvNode> oldDomainNodes, List<SrvNode> newDomainNodes) {
		if (oldDomainNodes == null || newDomainNodes == null) {
			return false;
		}
		if (oldDomainNodes.size() != newDomainNodes.size()) {
			return true;
		}
		else {
			for (SrvNode newDomainNode : newDomainNodes) {
				boolean exist = false;
				for (SrvNode oldDomainNode : oldDomainNodes) {
					if (newDomainNode.toString().equals(oldDomainNode.toString())) {
						exist = true;
						break;
					}
				}
				if (!exist) {
					return true;
				}
			}
		}
		return false;
	}
	
	private void updateStartFile(List<SrvNode> domainNodes) {
		File startFile = new File("./start.sh");
		if (!startFile.exists()) {
			return;
		}
		String domainKey = "-Dcsp.domains=";
		String content = null;
		String oldDomains = null;
		FileInputStream startFileIn = null;
		try {
			startFileIn = new FileInputStream(startFile);
			byte[] data = new byte[startFileIn.available()];
			startFileIn.read(data);
			content = new String(data, "UTF-8");
			int keyIndex = content.indexOf(domainKey);
			if (keyIndex >= 0) {
				int endIndex = content.indexOf(" ", keyIndex);
				oldDomains = content.substring(keyIndex, endIndex);
			}
		}
		catch (Exception e) {
			Logger.getLogger("csp_error").error("update domain nodes for start script exception.", e);
		}
		finally {
			try {
				if (startFileIn != null) {
					startFileIn.close();
				}
			}
			catch (Exception e) {}
		}
		
		if (oldDomains == null) {
			return;
		}
		
		String newDomains = domainKey + StringUtil.join(domainNodes.iterator(), ",");
		content.replace(oldDomains, newDomains);
		FileWriter startFileOut = null;
		try {
			startFileOut = new FileWriter(startFile, false);
			startFileOut.write(content);
		}
		catch (Exception e) {
			Logger.getLogger("csp_error").error("update domain nodes for start script exception.", e);
		}
		finally {
			try {
				if (startFileOut != null) {
					startFileOut.close();
				}
			}
			catch (Exception e) {}
		}
	}*/
}
