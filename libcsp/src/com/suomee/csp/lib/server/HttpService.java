package com.suomee.csp.lib.server;

import org.json.JSONObject;

import com.suomee.csp.lib.communication.SrvException;
import com.suomee.csp.lib.future.Future;
import com.suomee.csp.lib.future.FutureHandler;
import com.suomee.csp.lib.future.SrvFuture;
import com.suomee.csp.lib.proto.EResultCode;
import com.suomee.csp.lib.proto.HttpRequest;
import com.suomee.csp.lib.proto.HttpResponse;
import com.suomee.csp.lib.util.DateTimeUtil;

public abstract class HttpService implements Runnable {
	//最小服务执行时间，也就是说一般service执行至少需要的毫秒数
	private static final int MIN_SERVICE_EXECUTE_MILLI_SECONDS = 10;
	private HttpRequest httpRequest;
	private SrvFuture<HttpResponse> httpResponseFuture;
	void setHttpRequest(HttpRequest httpRequest) {
		this.httpRequest = httpRequest;
	}
	void setHttpResponseFuture(SrvFuture<HttpResponse> httpResponseFuture) {
		this.httpResponseFuture = httpResponseFuture;
	}
	
	public static Future<JSONObject> buildErrorRsp(Future<JSONObject> rspFuture, int result, String msg) {
		try {
			JSONObject jsonRspHead = rspFuture.getResult().getJSONObject("head");
			jsonRspHead.put("result", result);
			jsonRspHead.put("msg", msg);
		}
		catch (Exception e) {}
		return rspFuture;
	}
	
	@Override
	public void run() {
		//先判断从队列里出来，是否已经超时了，如果已超时则直接超时返回。（防雪崩）
		if (this.httpRequest.getTimeouts() > 0) {
			if (DateTimeUtil.getNowMilliSeconds() - this.httpRequest.getTime() >= this.httpRequest.getTimeouts() - MIN_SERVICE_EXECUTE_MILLI_SECONDS) {
				this.httpResponseFuture.setException(new SrvException(EResultCode.TIMEOUT, "server timeout."));
				this.httpResponseFuture.complete();
				return;
			}
		}
		try {
			Future<JSONObject> resultFuture = this.dispatchSrv(this.httpRequest);
			resultFuture.ready(new FutureHandler<JSONObject>() {
				SrvFuture<HttpResponse> httpResponseFuture;
				private FutureHandler<JSONObject> setHttpResponseFuture(SrvFuture<HttpResponse> httpResponseFuture) {
					this.httpResponseFuture = httpResponseFuture;
					return this;
				}
				@Override
				public void result(JSONObject result) {
					//TODO: 单向调用，这里要判断如果result为null，则为单向调用
					if (result.has("head")) {
						try {
							JSONObject jsonHead = result.getJSONObject("head");
							if (!jsonHead.has("result")) {
								jsonHead.put("result", 0);
							}
							if (!jsonHead.has("msg")) {
								jsonHead.put("msg", "");
							}
						}
						catch (Exception e) {}
					}
					HttpResponse httpResponse = new HttpResponse();
					httpResponse.setCode(0);
					httpResponse.setContent(result);
					this.httpResponseFuture.setResult(httpResponse);
					this.httpResponseFuture.complete();
				}
				@Override
				public void resultException(SrvException e) {
					this.httpResponseFuture.setException(e).complete();
				}
			}.setHttpResponseFuture(this.httpResponseFuture));
		}
		catch (Exception e) {
			this.httpResponseFuture.setException(new SrvException(EResultCode.SERVER_ERROR, "server error.", e));
			this.httpResponseFuture.complete();
			return;
		}
	}
	
	protected abstract Future<JSONObject> dispatchSrv(HttpRequest httpRequest);
}
