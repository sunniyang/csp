package com.suomee.csp.lib.proxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import com.suomee.csp.config.ConfigNames;
import com.suomee.csp.domain.DomainNames;
import com.suomee.csp.lib.communication.SrvException;
import com.suomee.csp.lib.future.SrvFuture;
import com.suomee.csp.lib.log.LogBuilder;
import com.suomee.csp.lib.log.Logger;
import com.suomee.csp.lib.proto.Csp;
import com.suomee.csp.lib.proto.CspResponse;
import com.suomee.csp.lib.proto.EResultCode;
import com.suomee.csp.lib.server.Server;
import com.suomee.csp.lib.util.DateTimeUtil;
import com.suomee.csp.log.LogNames;
import com.suomee.csp.log.util.LogClient;
import com.suomee.csp.log.util.LogName;

public final class SrvProxyIoHandler extends ChannelInboundHandlerAdapter {
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ChannelPool.getInstance().collect(ctx.channel().id().asLongText(), ctx.channel());
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ChannelPool.getInstance().release(ctx.channel().id().asLongText());
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		Csp.CspResponse protoResponse = (Csp.CspResponse)msg;
		
		CspResponse cspResponse = new CspResponse();
		cspResponse.setCspId(protoResponse.getCspId());
		cspResponse.setSrvName(protoResponse.getSrvName());
		cspResponse.setFunName(protoResponse.getFunName());
		cspResponse.setReqTime(protoResponse.getReqTime());
		cspResponse.setRspTime(protoResponse.getRspTime());
		cspResponse.setCode(protoResponse.getCode());
		if (protoResponse.getData() != null) {
			byte[] data = new byte[protoResponse.getData().size()];
			protoResponse.getData().copyTo(data, 0);
			cspResponse.setData(data);
		}
		
		ReferenceCountUtil.release(msg);
		
		//找到异步队列里等待的future
		SrvFuture<Object> future = SrvFutureQueue.getInstance().pull(cspResponse.getSrvName(), cspResponse.getCspId());
		//没找到future，可能是单向调用，也可能是超时了，直接丢弃就行
		if (future == null) {
			return;
		}
		
		//上报调用耗时（被调是DomainServer、ConfigServer、LogServer的不上报）
		if (Server.getInstance() != null && Server.getInstance().getServerInfo().getFullName() != null 
				&& !cspResponse.getSrvName().equals(DomainNames.DOMAIN_SRV_NAME) 
				&& !cspResponse.getSrvName().equals(ConfigNames.CONFIG_SRV_NAME) 
				&& !cspResponse.getSrvName().equals(LogNames.LOG_SRV_NAME)) {
			long now = DateTimeUtil.getNowMilliSeconds();
			LogClient.csplog(LogName.CSP_INVOKE, LogBuilder.getBuilder()
					.append(now)
					.append(cspResponse.getCspId())
					.append(Server.getInstance().getServerInfo().getFullName())
					.append(Server.getInstance().getServerInfo().getIp())
					.append(Server.parseServerName(cspResponse.getSrvName()))//TODO:这个可以不报，从srvName里解析
					.append(cspResponse.getSrvName())
					.append(cspResponse.getFunName())
					.append(future.getHost())
					.append(future.getPort())
					.append(now - future.getTime())
					.append(cspResponse.getRspTime() - cspResponse.getReqTime())
					.append(cspResponse.getCode())
					.getLog(), Logger.ROLLOVER_MINUTE);
		}
		
		if (cspResponse.getCode() != EResultCode.OK) {
			future.setException(new SrvException(EResultCode.SERVER_ERROR, "server error, code: " + cspResponse.getCode())).complete();
		}
		else {
			future.setResult(cspResponse.decodeFromData()).complete();
		}
		future.setChannel(null);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		Logger.getLogger("csp_error").error("SrvProxyIoHandler.exceptionCaught exception.", cause);
	}
}
