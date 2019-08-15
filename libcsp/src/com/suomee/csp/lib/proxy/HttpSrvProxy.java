package com.suomee.csp.lib.proxy;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.concurrent.GenericFutureListener;

import java.nio.charset.Charset;
import java.util.List;

import org.json.JSONObject;

import com.suomee.csp.lib.communication.Communicator;
import com.suomee.csp.lib.communication.SrvException;
import com.suomee.csp.lib.communication.SrvNode;
import com.suomee.csp.lib.future.SrvFuture;
import com.suomee.csp.lib.info.EProtocal;
import com.suomee.csp.lib.proto.EResultCode;
import com.suomee.csp.lib.proto.HttpRequest;
import com.suomee.csp.lib.util.DateTimeUtil;

public class HttpSrvProxy extends AbstractSrvProxy {
	private String srvName;
	
	HttpSrvProxy(String srvName) {
		this.srvName = srvName;
	}
	
	@Override
	protected String getSrvName() {
		return this.srvName;
	}
	
	@Override
	int getProtocal() {
		return EProtocal.HTTP;
	}
	
	public SrvFuture<JSONObject> invoke(String cmd, JSONObject jsonReq) {
		long cspId = this.cspId.getAndIncrement();
		SrvFuture<JSONObject> future = new SrvFuture<JSONObject>(cspId, this.getSrvName(), cmd);
		
		//构造请求包
		HttpRequest httpRequest = this.buildRequestPackage(cspId, cmd, jsonReq);
		
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
		future.setTime(httpRequest.getTime());
		future.setTimeoutMilliseconds(httpRequest.getTimeouts());
		//保持住channel
		future.setChannel(channel);
		future.setHost(selectedSrvNode.getHost());
		future.setPort(selectedSrvNode.getPort());
		
		//发送之前，先把future放入异步队列
		SrvFutureQueue.getInstance().push(future.getSrvName(), future.getCspId(), future);
		
		//转换协议
		FullHttpRequest fullHttpRequest = new DefaultFullHttpRequest(
				HttpVersion.HTTP_1_1, HttpMethod.POST, "http://" + selectedSrvNode.getHost() + ":" + selectedSrvNode.getPort(), 
				Unpooled.wrappedBuffer(httpRequest.getContent().toString().getBytes(Charset.forName("UTF-8"))));
		fullHttpRequest.headers().set(HttpHeaderNames.HOST, selectedSrvNode.getHost());
		fullHttpRequest.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
		fullHttpRequest.headers().set(HttpHeaderNames.CONTENT_LENGTH, fullHttpRequest.content().readableBytes());
		
		//发送数据
		ChannelFuture writeFuture = channel.writeAndFlush(fullHttpRequest);
		writeFuture.addListener(new GenericFutureListener<ChannelFuture>() {
			private SrvFuture<JSONObject> future;
			public GenericFutureListener<ChannelFuture> setFuture(SrvFuture<JSONObject> future) {
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
					SrvFuture<JSONObject> future = SrvFutureQueue.getInstance().pull(this.future.getSrvName(), this.future.getCspId());
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
	
	private HttpRequest buildRequestPackage(long cspId, String cmd, JSONObject jsonReq) {
		long time = DateTimeUtil.getNowMilliSeconds();
		int timeouts = 0;
		Integer localTimeoutMilliseconds = this.localTimeoutMilliseconds.get();
		if (localTimeoutMilliseconds != null) {
			timeouts = localTimeoutMilliseconds.intValue();
		}
		else {
			timeouts = Communicator.getInstance().getRpcTimeoutMilliseconds();
		}
		try {
			JSONObject jsonReqHead = jsonReq.getJSONObject("head");
			jsonReqHead.put("cspId", cspId);
			jsonReqHead.put("time", time);
			jsonReqHead.put("timeouts", timeouts);
		}
		catch (Exception e) {}
		HttpRequest httpRequest = new HttpRequest();
		httpRequest.setCspId(cspId);
		httpRequest.setSrvName(this.getSrvName());
		httpRequest.setCmd(cmd);
		httpRequest.setTime(time);
		httpRequest.setTimeouts(timeouts);
		httpRequest.setContent(jsonReq);
		return httpRequest;
	}
}
