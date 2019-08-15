package com.suomee.csp.lib.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.ReferenceCountUtil;

import java.nio.charset.Charset;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.suomee.csp.lib.communication.SrvException;
import com.suomee.csp.lib.future.FutureHandler;
import com.suomee.csp.lib.future.SrvFuture;
import com.suomee.csp.lib.log.Logger;
import com.suomee.csp.lib.proto.EResultCode;
import com.suomee.csp.lib.proto.HttpRequest;
import com.suomee.csp.lib.proto.HttpResponse;
import com.suomee.csp.lib.util.DateTimeUtil;

public class HttpSrvIoHandler extends ChannelInboundHandlerAdapter {
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		Logger.getLogger("channel").info("channel active: " + ctx.channel().id().asLongText());
		ChannelHolder.getInstance().collect(ctx.channel().id().asLongText(), ctx.channel());
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		Logger.getLogger("channel").info("channel inactive: " + ctx.channel().id().asLongText());
		ChannelHolder.getInstance().release(ctx.channel().id().asLongText());
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		//服务端收到消息，直接将FullHttpRequest请求包提交给SrvExecutor，由用户线程去执行，同时返回future。
		//该future上注册的FutureHandler会在任务执行完毕后，会被通知到，通知逻辑也是在用户线程执行。
		FullHttpRequest fullHttpRequest = (FullHttpRequest)msg;
		long time = DateTimeUtil.getNowMilliSeconds();
		
		DecoderResult decoderResult = fullHttpRequest.decoderResult();
		if (!decoderResult.isSuccess()) {
			ReferenceCountUtil.release(msg);
			HttpSrvIoHandler.sendErrorResponse(ctx.channel(), null, 
					0L, "", "", time, time, EResultCode.BAD_REQUEST, "bad request.");
			return;
		}
		ByteBuf buf = fullHttpRequest.content();
		if (!buf.isReadable()) {
			ReferenceCountUtil.release(msg);
			HttpSrvIoHandler.sendErrorResponse(ctx.channel(), null, 
					0L, "", "", time, time, EResultCode.REQUEST_FORMAT_ERROR, "request body empty.");
			return;
		}
		
		String content = buf.toString(Charset.forName("UTF-8"));
		ReferenceCountUtil.release(msg);
		
		//转换协议
		HttpRequest httpRequest = new HttpRequest();
		try {
			JSONObject jsonReq = null;
			JSONObject jsonContent = new JSONObject(content);
			if (jsonContent.has("reqs")) {
				JSONArray jsonReqs = jsonContent.getJSONArray("reqs");
				if (jsonReqs.length() == 0 || jsonReqs.isNull(0)) {
					HttpSrvIoHandler.sendErrorResponse(ctx.channel(), null, 
							0L, "", "", time, time, EResultCode.REQUEST_FORMAT_ERROR, "requests has no item.");
					return;
				}
				jsonReq = jsonReqs.getJSONObject(0);
				JSONArray names = jsonContent.names();
				for (int i = 0; i < names.length(); i++) {
					String name = names.getString(i);
					if (name.equals("reqs")) {
						continue;
					}
					if (!jsonReq.has(name)) {
						jsonReq.put(name, jsonContent.get(name));
					}
				}
			}
			else {
				jsonReq = jsonContent;
			}
			if (!jsonReq.has("head") || !jsonReq.has("body")) {
				HttpSrvIoHandler.sendErrorResponse(ctx.channel(), null, 
						0L, "", "", time, time, EResultCode.REQUEST_FORMAT_ERROR, "request has no head or body.");
				return;
			}
			JSONObject jsonHead = jsonReq.getJSONObject("head");
			httpRequest.setCspId(jsonHead.getLong("cspId"));
			httpRequest.setSrvName(jsonHead.getString("srvName"));
			httpRequest.setCmd(jsonHead.getString("cmd"));
			httpRequest.setTime(jsonHead.getLong("time"));
			httpRequest.setTimeouts(jsonHead.optInt("timeouts"));
			httpRequest.setContent(jsonReq);
		}
		catch (Exception e) {
			Logger.getLogger("csp_error").error("HttpSrvIoHandler.channelRead exception.", e);
			HttpSrvIoHandler.sendErrorResponse(ctx.channel(), null, 
					0L, "", "", time, time, EResultCode.REQUEST_FORMAT_ERROR, "request format error.");
			return;
		}
		Logger.getLogger("cmd_req").info(httpRequest.getContent().toString());
		
		SrvFuture<HttpResponse> httpResponseFuture = HttpSrvExecutor.getInstance().submit(httpRequest);
		httpResponseFuture.ready(new FutureHandler<HttpResponse>() {
			private Channel channel;
			FutureHandler<HttpResponse> setChannel(Channel channel) {
				this.channel = channel;
				return this;
			}
			@Override
			public void result(HttpResponse result) {
				SrvFuture<HttpResponse> future = (SrvFuture<HttpResponse>)this.getFuture();
				try {
					JSONObject jsonRsp = result.getContent();
					JSONObject jsonRspHead = jsonRsp.getJSONObject("head");
					jsonRspHead.put("cspId", future.getCspId());
					jsonRspHead.put("srvName", future.getSrvName());
					jsonRspHead.put("cmd", future.getFunName());
					jsonRspHead.put("reqTime", future.getReqTime());
					jsonRspHead.put("rspTime", DateTimeUtil.getNowMilliSeconds());
				}
				catch (Exception e) {}
				String content = result.getContent().toString();
				HttpSrvIoHandler.sendResponse(this.channel, null, content);
				Logger.getLogger("cmd_rsp").info(content);
			}
			@Override
			public void resultException(SrvException e) {
				Logger.getLogger("csp_error").error("HttpSrvIoHandler$FutureHandler.resultException exception.", e);
				SrvFuture<HttpResponse> future = (SrvFuture<HttpResponse>)this.getFuture();
				String content = HttpSrvIoHandler.sendErrorResponse(this.channel, null, 
						future.getCspId(), future.getSrvName(), future.getFunName(), 
						future.getReqTime(), DateTimeUtil.getNowMilliSeconds(), e.getCode(), e.getMessage());
				Logger.getLogger("cmd_rsp").info(content);
			}
		}.setChannel(ctx.channel()));
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		Logger.getLogger("csp_error").error("HttpSrvIoHandler.exceptionCaught exception.", cause);
		long time = DateTimeUtil.getNowMilliSeconds();
		HttpSrvIoHandler.sendErrorResponse(ctx.channel(), null, 
				0L, "", "", time, time, EResultCode.UNKNOW_ERROR, "server error.");
	}
	
	protected static String sendErrorResponse(Channel channel, Map<String, String> headers, long cspId, String srvName, String cmd, long reqTime, long rspTime, int result, String msg) {
		JSONObject jsonContent = new JSONObject();
		try {
			JSONObject jsonHead = new JSONObject();
			jsonContent.put("head", jsonHead);
			jsonHead.put("cspId", cspId);
			jsonHead.put("srvName", srvName);
			jsonHead.put("cmd", cmd);
			jsonHead.put("reqTime", reqTime);
			jsonHead.put("rspTime", rspTime);
			jsonHead.put("result", result);
			jsonHead.put("msg", msg);
			jsonContent.put("body", new JSONObject());
		}
		catch (Exception e) {}
		String content = jsonContent.toString();
		sendResponse(channel, headers, content);
		return content;
	}
	
	protected static void sendResponse(Channel channel, Map<String, String> headers, String content) {
		boolean keepAlive = true;//服务端不主动关闭连接
		FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, 
				Unpooled.wrappedBuffer(content.getBytes(Charset.forName("UTF-8"))));
		fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
		fullHttpResponse.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, fullHttpResponse.content().readableBytes());
		if (headers != null) {
			for (Map.Entry<String, String> header : headers.entrySet()) {
				fullHttpResponse.headers().set(header.getKey(), header.getValue());
			}
		}
		if (keepAlive) {
			fullHttpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
			channel.writeAndFlush(fullHttpResponse);
		}
		else {
			channel.writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
		}
	}
}
