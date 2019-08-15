package com.suomee.csp.lib.proxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.ReferenceCountUtil;

import java.nio.charset.Charset;

import org.json.JSONObject;

import com.suomee.csp.lib.communication.SrvException;
import com.suomee.csp.lib.future.SrvFuture;
import com.suomee.csp.lib.log.LogBuilder;
import com.suomee.csp.lib.log.Logger;
import com.suomee.csp.lib.proto.EResultCode;
import com.suomee.csp.lib.proto.HttpResponse;
import com.suomee.csp.lib.server.Server;
import com.suomee.csp.lib.util.DateTimeUtil;
import com.suomee.csp.log.util.LogClient;
import com.suomee.csp.log.util.LogName;

public class HttpSrvProxyIoHandler extends ChannelInboundHandlerAdapter {
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
		FullHttpResponse fullHttpResponse = (FullHttpResponse)msg;
		
		String content = fullHttpResponse.content().toString(Charset.forName("UTF-8"));
		ReferenceCountUtil.release(msg);
		
		//转换协议
		HttpResponse httpResponse = new HttpResponse();
		try {
			JSONObject jsonRsp = new JSONObject(content);
			JSONObject jsonRspHead = jsonRsp.getJSONObject("head");
			httpResponse.setCspId(jsonRspHead.getLong("cspId"));
			httpResponse.setSrvName(jsonRspHead.getString("srvName"));
			httpResponse.setCmd(jsonRspHead.getString("cmd"));
			httpResponse.setReqTime(jsonRspHead.getLong("reqTime"));
			httpResponse.setRspTime(jsonRspHead.getLong("rspTime"));
			httpResponse.setCode(jsonRspHead.optInt("result", 0));
			httpResponse.setMsg(jsonRspHead.optString("msg", ""));
			httpResponse.setContent(jsonRsp);
			jsonRspHead.remove("cspId");
			jsonRspHead.remove("reqTime");
			jsonRspHead.remove("rspTime");
		}
		catch (Exception e) {}
		
		//找到异步队列里等待的future
		SrvFuture<Object> future = SrvFutureQueue.getInstance().pull(httpResponse.getSrvName(), httpResponse.getCspId());
		//没找到future，可能是单向调用，也可能是超时了，直接丢弃就行
		if (future == null) {
			return;
		}
		
		//上报调用情况
		if (Server.getInstance() != null && Server.getInstance().getServerInfo().getFullName() != null) {
			long now = DateTimeUtil.getNowMilliSeconds();
			LogClient.csplog(LogName.CSP_INVOKE, LogBuilder.getBuilder()
					.append(now)
					.append(httpResponse.getCspId())
					.append(Server.getInstance().getServerInfo().getFullName())
					.append(Server.getInstance().getServerInfo().getIp())
					.append(Server.parseServerName(httpResponse.getSrvName()))
					.append(httpResponse.getSrvName())
					.append(httpResponse.getCmd())
					.append(future.getHost())
					.append(future.getPort())
					.append(now - future.getTime())
					.append(httpResponse.getRspTime() - httpResponse.getReqTime())
					.append(httpResponse.getCode())
					.getLog(), Logger.ROLLOVER_MINUTE);
		}
		
		if (httpResponse.getCode() != EResultCode.OK) {
			future.setException(new SrvException(httpResponse.getCode(), httpResponse.getMsg())).complete();
		}
		else {
			future.setResult(httpResponse.getContent()).complete();
		}
		future.setChannel(null);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		Logger.getLogger("csp_error").error("HttpSrvProxyIoHandler.exceptionCaught exception.", cause);
	}
}
