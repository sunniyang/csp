package com.suomee.csp.lib.proxy;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.suomee.csp.lib.communication.Communicator;
import com.suomee.csp.lib.communication.SrvNode;
import com.suomee.csp.lib.communication.SrvNodeManager;

abstract class AbstractSrvProxy {
	AtomicLong cspId;
	ThreadLocal<Integer> localTimeoutMilliseconds;
	ThreadLocal<List<SrvNode>> localSrvNodes;
	ThreadLocal<String> localHashKey;
	
	AbstractSrvProxy() {
		this.cspId = new AtomicLong(1L);
		this.localTimeoutMilliseconds = new ThreadLocal<Integer>();
		this.localSrvNodes = new ThreadLocal<List<SrvNode>>();
		this.localHashKey = new ThreadLocal<String>();
	}
	
	void setTimeoutMilliseconds(Integer timeoutMilliseconds) {
		this.localTimeoutMilliseconds.set(timeoutMilliseconds);
	}
	void setSrvNodes(List<SrvNode> srvNodes) {
		this.localSrvNodes.set(srvNodes);
	}
	void setHashKey(String hashKey) {
		this.localHashKey.set(hashKey);
	}
	
	protected abstract String getSrvName();
	
	abstract int getProtocal();
	
	List<SrvNode> prepareSrvNodes() {
		List<SrvNode> srvNodes = this.localSrvNodes.get();
		if (srvNodes == null) {
			srvNodes = SrvNodeManager.getInstance().getSrvNodes(this.getSrvName());
		}
		List<SrvNode> localSrvNodes = new ArrayList<SrvNode>();
		if (srvNodes != null) {
			localSrvNodes.addAll(srvNodes);
		}
		return localSrvNodes;
	}
	
	Channel getChannel(List<SrvNode> srvNodes, SrvNode selectedSrvNode) {
		SrvNode srvNode = this.selectSrvNode(srvNodes);
		if (srvNode == null) {
			return null;
		}
		
		Channel channel = this.getChannel(srvNode);
		if (channel != null) {
			selectedSrvNode.setHost(srvNode.getHost());
			selectedSrvNode.setPort(srvNode.getPort());
			return channel;
		}
		return this.getChannel(srvNodes, selectedSrvNode);
	}
	
	private SrvNode selectSrvNode(List<SrvNode> srvNodes) {
		if (srvNodes == null || srvNodes.isEmpty()) {
			return null;
		}
		int i = -1;
		String hashKey = this.localHashKey.get();
		if (hashKey == null) {
			i = (int)(Math.random() * srvNodes.size());
		}
		else {
			i = hashKey.hashCode() % srvNodes.size();//TODO:这里要改成一致性hash，否则会随着节点的增删而影响hash结果
		}
		SrvNode srvNode = srvNodes.get(i);
		srvNodes.remove(i);
		return srvNode;
	}
	
	private Channel getChannel(SrvNode srvNode) {
		Channel channel = ChannelPool.getInstance().get(srvNode.getHost(), srvNode.getPort());
		if (channel == null) {
			if (this.getSrvName().equals(com.suomee.csp.domain.DomainNames.DOMAIN_SRV_NAME)) {
				channel = Communicator.getInstance().connectDomain(srvNode.getHost(), srvNode.getPort());
			}
			else {
				channel = Communicator.getInstance().connect(srvNode.getHost(), srvNode.getPort(), this.getProtocal());
			}
			if (channel == null) {
				//如果没有成功连接，则上报无效节点
				SrvNodeManager.getInstance().reportInvalidSrvNode(this.getSrvName(), srvNode);
				return null;
			}
		}
		else {
			//从池里取出来的连接要判断是否存活，有可能被长时间不活动被池钝化了，或者连接本身已经断开了
			if (!channel.isActive()) {
				if (channel.isOpen()) {
					channel.close();
				}
				//如果从池里取出来的连接无效，则再从池里取一遍（如果池内所有连接失效，最终一定会触发一次尝试建立新连接）
				return this.getChannel(srvNode);
			}
		}
		return channel;
	}
}
