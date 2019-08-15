package com.suomee.csp.lib.server;

import com.suomee.csp.lib.communication.SrvException;
import com.suomee.csp.lib.future.Future;
import com.suomee.csp.lib.future.FutureHandler;
import com.suomee.csp.lib.future.SrvFuture;
import com.suomee.csp.lib.proto.CspRequest;
import com.suomee.csp.lib.proto.CspResponse;
import com.suomee.csp.lib.proto.EResultCode;
import com.suomee.csp.lib.util.DateTimeUtil;

public abstract class Service implements Runnable {
	//最小服务执行时间，也就是说一般service执行至少需要的毫秒数
	private static final int MIN_SERVICE_EXECUTE_MS = 10;//TODO:这个最好放在请求丢进业务线程之前
	private CspRequest cspRequest;
	private SrvFuture<CspResponse> cspResponseFuture;
	void setCspRequest(CspRequest cspRequest) {
		this.cspRequest = cspRequest;
	}
	void setCspResponseFuture(SrvFuture<CspResponse> cspResponseFuture) {
		this.cspResponseFuture = cspResponseFuture;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		//先判断从队列里出来，是否已经超时了，如果已超时则直接超时返回。（防雪崩）
		if (DateTimeUtil.getNowMilliSeconds() - this.cspRequest.getTime() >= this.cspRequest.getTimeouts() - MIN_SERVICE_EXECUTE_MS) {
			this.cspResponseFuture.setException(new SrvException(EResultCode.TIMEOUT, "server timeout."));
			this.cspResponseFuture.complete();
			return;
		}
		try {
			Future<Object> resultFuture = (Future<Object>)this.dispatchSrv(this.cspRequest);
			resultFuture.ready(new FutureHandler<Object>() {
				private SrvFuture<CspResponse> cspResponseFuture;
				private FutureHandler<Object> setCspResponseFuture(SrvFuture<CspResponse> cspResponseFuture) {
					this.cspResponseFuture = cspResponseFuture;
					return this;
				}
				@Override
				public void result(Object result) {
					//TODO: 单向调用，这里要判断如果result为null，则为单向调用
					CspResponse cspResponse = new CspResponse();
					cspResponse.setCode(0);
					try {
						cspResponse.encodeToData(result);
					}
					catch (SrvException e) {
						this.cspResponseFuture.setException(e);
					}
					this.cspResponseFuture.setResult(cspResponse);
					this.cspResponseFuture.complete();
				}
				@Override
				public void resultException(SrvException e) {
					this.cspResponseFuture.setException(e).complete();
				}
			}.setCspResponseFuture(this.cspResponseFuture));
		}
		catch (Exception e) {
			this.cspResponseFuture.setException(new SrvException(EResultCode.SERVER_ERROR, "server error."));
			this.cspResponseFuture.complete();
			return;
		}
	}
	
	protected abstract Future<?> dispatchSrv(CspRequest cspRequest);
}
