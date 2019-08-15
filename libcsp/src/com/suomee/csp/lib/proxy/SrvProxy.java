package com.suomee.csp.lib.proxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.List;

import com.google.protobuf.ByteString;
import com.suomee.csp.lib.communication.Communicator;
import com.suomee.csp.lib.communication.SrvException;
import com.suomee.csp.lib.communication.SrvNode;
import com.suomee.csp.lib.future.SrvFuture;
import com.suomee.csp.lib.info.EProtocal;
import com.suomee.csp.lib.proto.Csp;
import com.suomee.csp.lib.proto.CspRequest;
import com.suomee.csp.lib.proto.EResultCode;
import com.suomee.csp.lib.util.DateTimeUtil;

public abstract class SrvProxy extends AbstractSrvProxy {
	@Override
	int getProtocal() {
		return EProtocal.NATIVE;
	}
	
	protected <T> SrvFuture<T> invoke(String funName, Object... parameters) {//TODO: 没有考虑单向调用（单靠这个结构是判断不出是否是单向调用的）
		long cspId = this.cspId.getAndIncrement();
		SrvFuture<T> future = new SrvFuture<T>(cspId, this.getSrvName(), funName);
		
		//构造请求包
		CspRequest cspRequest = null;
		try {
			cspRequest = this.buildRequestPackage(cspId, funName, parameters);
		}
		catch (SrvException e) {
			future.setException(e).complete();
			return future;
		}
		
		//准备可用节点列表
		List<SrvNode> srvNodes = this.prepareSrvNodes();
		
		//按节点获取连接
		SrvNode selectedSrvNode = new SrvNode(null, 0);
		Channel channel = this.getChannel(srvNodes, selectedSrvNode);
		if (channel == null) {
			future.setException(new SrvException(EResultCode.NO_ACTIVE_SERVER, "no available node exist.")).complete();
			return future;
		}
		
		//为异步队列设置超时
		future.setTime(cspRequest.getTime());
		future.setTimeoutMilliseconds(cspRequest.getTimeouts());
		//保持住channel
		future.setChannel(channel);
		future.setHost(selectedSrvNode.getHost());
		future.setPort(selectedSrvNode.getPort());
		
		//发送之前，先把future放入异步队列
		SrvFutureQueue.getInstance().push(future.getSrvName(), future.getCspId(), future);
		
		//发送数据
		Csp.CspRequest.Builder cspRequestBuilder = Csp.CspRequest.newBuilder();
		cspRequestBuilder.setCspId(cspRequest.getCspId());
		cspRequestBuilder.setSrvName(cspRequest.getSrvName());
		cspRequestBuilder.setFunName(cspRequest.getFunName());
		cspRequestBuilder.setTime(cspRequest.getTime());
		cspRequestBuilder.setTimeouts(cspRequest.getTimeouts());
		if (cspRequest.getData() != null) {
			cspRequestBuilder.setData(ByteString.copyFrom(cspRequest.getData()));
		}
		
		//ChannelFuture writeFuture = channel.writeAndFlush(cspRequest);
		ChannelFuture writeFuture = channel.writeAndFlush(cspRequestBuilder.build());//TODO:为什么要复制一下？
		writeFuture.addListener(new GenericFutureListener<ChannelFuture>() {
			private SrvFuture<T> future;
			public GenericFutureListener<ChannelFuture> setFuture(SrvFuture<T> future) {
				this.future = future;
				return this;
			}
			@Override
			public void operationComplete(ChannelFuture channelFuture) throws Exception {
				//不管消息发送成功与否，都可以释放这个channel了
				ChannelPool.getInstance().put(this.future.getHost(), this.future.getPort(), this.future.getChannel());
				//如果发送失败，则从异步队列中取出future，做异常处理。如果成功则等着SrvProxyIoHandler做回调就可以了
				if (!channelFuture.isSuccess()) {
					//找到异步队列里等待的future
					SrvFuture<T> future = SrvFutureQueue.getInstance().pull(this.future.getSrvName(), this.future.getCspId());
					//没找到future，可能是异步超时了，直接丢弃就行
					if (future == null) {
						return;
					}
					future.setException(new SrvException(EResultCode.CLIENT_ERROR, "send request error.", channelFuture.cause())).complete();
					future.setChannel(null);
				}
			}
		}.setFuture(future));
		
		return future;
	}
	
	private CspRequest buildRequestPackage(long cspId, String funName, Object... parameters) throws SrvException {
		CspRequest cspRequest = new CspRequest();
		cspRequest.setCspId(cspId);
		cspRequest.setSrvName(this.getSrvName());
		cspRequest.setFunName(funName);
		cspRequest.setTime(DateTimeUtil.getNowMilliSeconds());
		int timeouts = 0;
		Integer localTimeoutMilliseconds = this.localTimeoutMilliseconds.get();
		if (localTimeoutMilliseconds != null) {
			timeouts = localTimeoutMilliseconds.intValue();
		}
		else {
			timeouts = Communicator.getInstance().getRpcTimeoutMilliseconds();
		}
		cspRequest.setTimeouts(timeouts);
		cspRequest.encodeToData(parameters);
		return cspRequest;
	}
}
