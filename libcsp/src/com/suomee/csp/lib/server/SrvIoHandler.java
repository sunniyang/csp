package com.suomee.csp.lib.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import com.google.protobuf.ByteString;
import com.suomee.csp.lib.communication.SrvException;
import com.suomee.csp.lib.future.FutureHandler;
import com.suomee.csp.lib.future.SrvFuture;
import com.suomee.csp.lib.log.Logger;
import com.suomee.csp.lib.proto.Csp;
import com.suomee.csp.lib.proto.CspRequest;
import com.suomee.csp.lib.proto.CspResponse;
import com.suomee.csp.lib.util.DateTimeUtil;

public final class SrvIoHandler extends ChannelInboundHandlerAdapter {
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ChannelHolder.getInstance().collect(ctx.channel().id().asLongText(), ctx.channel());
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ChannelHolder.getInstance().release(ctx.channel().id().asLongText());
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		//服务端收到消息，直接将CspRequest请求包提交给SrvExecutor，由用户线程去执行，同时返回future。
		//该future上注册的FutureHandler会在任务执行完毕后，会被通知到，通知逻辑也是在用户线程执行。
		Csp.CspRequest protoRequest = (Csp.CspRequest)msg;
		
		CspRequest cspRequest = new CspRequest();
		cspRequest.setCspId(protoRequest.getCspId());
		cspRequest.setSrvName(protoRequest.getSrvName());
		cspRequest.setFunName(protoRequest.getFunName());
		cspRequest.setTime(protoRequest.getTime());
		cspRequest.setTimeouts(protoRequest.getTimeouts());
		if (protoRequest.getData() != null) {
			byte[] data = new byte[protoRequest.getData().size()];
			protoRequest.getData().copyTo(data, 0);
			cspRequest.setData(data);
		}
		
		ReferenceCountUtil.release(msg);
		
		SrvFuture<CspResponse> cspResponseFuture = SrvExecutor.getInstance().submit(cspRequest);
		cspResponseFuture.ready(new FutureHandler<CspResponse>() {
			private Channel channel;
			FutureHandler<CspResponse> setChannel(Channel channel) {
				this.channel = channel;
				return this;
			}
			@Override
			public void result(CspResponse result) {
				SrvFuture<CspResponse> future = (SrvFuture<CspResponse>)this.getFuture();
				result.setCspId(future.getCspId());
				result.setSrvName(future.getSrvName());
				result.setFunName(future.getFunName());
				result.setReqTime(future.getReqTime());
				result.setRspTime(DateTimeUtil.getNowMilliSeconds());
				
				Csp.CspResponse.Builder cspResponseBuilder = Csp.CspResponse.newBuilder();
				cspResponseBuilder.setCspId(result.getCspId());
				cspResponseBuilder.setSrvName(result.getSrvName());
				cspResponseBuilder.setFunName(result.getFunName());
				cspResponseBuilder.setReqTime(result.getReqTime());
				cspResponseBuilder.setRspTime(result.getRspTime());
				cspResponseBuilder.setCode(result.getCode());
				if (result.getData() != null) {
					cspResponseBuilder.setData(ByteString.copyFrom(result.getData()));
				}
				
				//this.channel.writeAndFlush(result);
				this.channel.writeAndFlush(cspResponseBuilder.build());//TODO:为什么要复制一下？
			}
			@Override
			public void resultException(SrvException e) {
				Logger.getLogger("csp_error").error("SrvIoHandler$FutureHandler.resultException exception.", e);
				SrvFuture<CspResponse> future = (SrvFuture<CspResponse>)this.getFuture();
				CspResponse result = new CspResponse();
				result.setCspId(future.getCspId());
				result.setSrvName(future.getSrvName());
				result.setFunName(future.getFunName());
				result.setReqTime(future.getReqTime());
				result.setRspTime(DateTimeUtil.getNowMilliSeconds());
				result.setCode(e.getCode());
				
				Csp.CspResponse.Builder cspResponseBuilder = Csp.CspResponse.newBuilder();
				cspResponseBuilder.setCspId(result.getCspId());
				cspResponseBuilder.setSrvName(result.getSrvName());
				cspResponseBuilder.setFunName(result.getFunName());
				cspResponseBuilder.setReqTime(result.getReqTime());
				cspResponseBuilder.setRspTime(result.getRspTime());
				cspResponseBuilder.setCode(result.getCode());
				
				this.channel.writeAndFlush(cspResponseBuilder.build());
			}
		}.setChannel(ctx.channel()));
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
		Logger.getLogger("csp_error").error("SrvIoHandler.exceptionCaught exception.", new Exception("exception in exceptionCaught"));
	}
}
