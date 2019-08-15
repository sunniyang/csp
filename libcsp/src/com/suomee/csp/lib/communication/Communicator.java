package com.suomee.csp.lib.communication;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.suomee.csp.lib.info.EProtocal;
import com.suomee.csp.lib.info.ServerInfo;
import com.suomee.csp.lib.info.ServiceInfo;
import com.suomee.csp.lib.proto.Csp;
import com.suomee.csp.lib.proxy.HttpSrvProxyIoHandler;
import com.suomee.csp.lib.proxy.SrvProxyIoHandler;
import com.suomee.csp.lib.server.HttpSrvIoHandler;
import com.suomee.csp.lib.server.SrvIoHandler;

/**
 * 通信器
 * 通信器包含了RPC调用的客户端和服务端
 * @author sunniyang
 *
 */
public final class Communicator {
	private static final int RPC_TIMEOUT_MILLISECONDS = 3000;
	
	//单例
	private static Communicator instance = null;
	private static Object mutex = new Object();
	public static Communicator getInstance() {
		if (instance == null) {
			synchronized (mutex) {
				if (instance == null) {
					instance = new Communicator();
				}
			}
		}
		return instance;
	}
	
	//客户端和服务端
	private ClientSide clientSide;
	private ServerSide serverSide;
	private int rpcTimeoutMilliseconds;
	
	private Communicator() {
		this.clientSide = new ClientSide();
		this.serverSide = new ServerSide();
		this.rpcTimeoutMilliseconds = RPC_TIMEOUT_MILLISECONDS;
	}
	
	//初始化方法
	public void initClient(Map<Integer, Integer> threads, int rpcTimeoutMilliseconds) {
		if (rpcTimeoutMilliseconds <= 0) {
			rpcTimeoutMilliseconds = RPC_TIMEOUT_MILLISECONDS;
		}
		this.clientSide.init(threads, rpcTimeoutMilliseconds);
		this.rpcTimeoutMilliseconds = rpcTimeoutMilliseconds;
	}
	public void initClient(Map<Integer, Integer> threads, int rpcTimeoutMilliseconds, Class<? extends SrvProxyIoHandler> srvProxyIoHandler, Class<? extends HttpSrvProxyIoHandler> httpSrvProxyIoHandler) {
		if (rpcTimeoutMilliseconds <= 0) {
			rpcTimeoutMilliseconds = RPC_TIMEOUT_MILLISECONDS;
		}
		this.clientSide.srvProxyIoHandler = srvProxyIoHandler;
		this.clientSide.httpSrvProxyIoHandler = httpSrvProxyIoHandler;
		this.clientSide.init(threads, rpcTimeoutMilliseconds);
		this.rpcTimeoutMilliseconds = rpcTimeoutMilliseconds;
	}
	public void initServer(ServerInfo serverInfo, List<ServiceInfo> serviceInfos) throws Exception {
		this.serverSide.init(serverInfo, serviceInfos);
	}
	public void initServer(ServerInfo serverInfo, List<ServiceInfo> serviceInfos, Class<? extends SrvIoHandler> srvIoHandler, Class<? extends HttpSrvIoHandler> httpSrvIoHandler) throws Exception {
		this.serverSide.srvIoHandler = srvIoHandler;
		this.serverSide.httpSrvIoHandler = httpSrvIoHandler;
		this.serverSide.init(serverInfo, serviceInfos);
	}
	
	//关闭通信器
	public void shutdownClient() {
		this.clientSide.shutdown();
	}
	public void shutdownServer() {
		this.serverSide.shutdown();
	}
	public void shutdown() {
		this.serverSide.shutdown();
		this.clientSide.shutdown();
	}
	
	public Channel connectDomain(String host, int port) {
		ChannelFuture future = this.clientSide.domainBoot.connect(host, port);
		future.awaitUninterruptibly(this.rpcTimeoutMilliseconds, TimeUnit.MILLISECONDS);
		if (future.isDone() && future.isSuccess()) {
			return future.channel();
		}
		return null;
	}
	
	public Channel connect(String host, int port, int protocal) {
		Bootstrap boot = this.clientSide.boots.get(protocal);
		if (boot == null) {
			return null;
		}
		ChannelFuture future = boot.connect(host, port);
		future.awaitUninterruptibly(this.rpcTimeoutMilliseconds, TimeUnit.MILLISECONDS);
		if (future.isDone() && future.isSuccess()) {
			return future.channel();
		}
		return null;
	}
	
	public int getRpcTimeoutMilliseconds() {
		return this.rpcTimeoutMilliseconds;
	}
	
	public final class ClientSide {
		private Class<? extends SrvProxyIoHandler> srvProxyIoHandler;
		private Class<? extends HttpSrvProxyIoHandler> httpSrvProxyIoHandler;
		private Bootstrap domainBoot;
		private Map<Integer, Bootstrap> boots;
		private boolean inited;
		ClientSide() {
			this.srvProxyIoHandler = SrvProxyIoHandler.class;
			this.httpSrvProxyIoHandler = HttpSrvProxyIoHandler.class;
			this.domainBoot = this.doInit(EProtocal.NATIVE, 10, RPC_TIMEOUT_MILLISECONDS);
			this.boots = new HashMap<Integer, Bootstrap>();
			this.inited = false;
		}
		public void init(Map<Integer, Integer> threads, int rpcTimeoutMilliseconds) {
			if (!this.inited) {
				synchronized (this) {
					if (!this.inited) {
						for (Map.Entry<Integer, Integer> entry : threads.entrySet()) {
							int protocal = entry.getKey();
							this.boots.put(protocal, this.doInit(protocal, entry.getValue(), rpcTimeoutMilliseconds));
						}
						this.inited = true;
					}
				}
			}
		}
		private Bootstrap doInit(int protocal, int threads, int rpcTimeoutMilliseconds) {
			if (threads < 0) {
				threads = 0;
			}
			EventLoopGroup group = new NioEventLoopGroup(threads);
			Bootstrap boot = new Bootstrap();
			boot
				.group(group)
				.channel(NioSocketChannel.class)
				.option(ChannelOption.TCP_NODELAY, true)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, rpcTimeoutMilliseconds);
			if (protocal == EProtocal.NATIVE) {
				boot.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel channel) throws Exception {
						channel.pipeline()
							.addLast(new ProtobufVarint32FrameDecoder())
							.addLast(new ProtobufDecoder(Csp.CspResponse.getDefaultInstance()))
							.addLast(new ProtobufVarint32LengthFieldPrepender())
							.addLast(new ProtobufEncoder())
							.addLast(ClientSide.this.srvProxyIoHandler.newInstance());
					}
				});
			}
			else if (protocal == EProtocal.HTTP) {
				boot.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel channel) throws Exception {
						channel.pipeline()
							.addLast(new HttpClientCodec())
							.addLast(new HttpObjectAggregator(Integer.MAX_VALUE))
							.addLast(ClientSide.this.httpSrvProxyIoHandler.newInstance());
					}
				});
			}
			return boot;
		}
		public void shutdown() {
			for (Bootstrap boot : this.boots.values()) {
				boot.config().group().shutdownGracefully();
			}
			if (this.domainBoot != null) {
				this.domainBoot.config().group().shutdownGracefully();
			}
		}
	}
	
	public final class ServerSide {
		private Class<? extends SrvIoHandler> srvIoHandler;
		private Class<? extends HttpSrvIoHandler> httpSrvIoHandler;
		private Map<String, ServerBootstrap> boots;
		private boolean inited;
		ServerSide() {
			this.srvIoHandler = SrvIoHandler.class;
			this.httpSrvIoHandler = HttpSrvIoHandler.class;
			this.boots = new HashMap<String, ServerBootstrap>();
			this.inited = false;
		}
		public void init(ServerInfo serverInfo, List<ServiceInfo> serviceInfos) throws Exception {
			if (!this.inited) {
				synchronized (this) {
					if (!this.inited) {
						for (ServiceInfo serviceInfo : serviceInfos) {
							ServerBootstrap boot = this.doInit(serverInfo, serviceInfo);
							this.boots.put(serviceInfo.getFullName(), boot);
						}
						this.inited = true;
					}
				}
			}
		}
		private ServerBootstrap doInit(ServerInfo serverInfo, ServiceInfo serviceInfo) throws Exception {
			int threads = serviceInfo.getThreads();
			if (threads <= 0) {
				threads = 0;
			}
			EventLoopGroup mainGroup = new NioEventLoopGroup(1);
			EventLoopGroup workGroup = new NioEventLoopGroup(threads);
			ServerBootstrap boot = new ServerBootstrap();
			boot
				.group(mainGroup, workGroup)
				.channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 1024)
				.option(ChannelOption.SO_LINGER, 0);
			if (serviceInfo.getProtocal() == EProtocal.NATIVE) {
				boot.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel channel) throws Exception {
						channel.pipeline()
							.addLast(new ProtobufVarint32FrameDecoder())
							.addLast(new ProtobufDecoder(Csp.CspRequest.getDefaultInstance()))
							.addLast(new ProtobufVarint32LengthFieldPrepender())
							.addLast(new ProtobufEncoder())
							.addLast(ServerSide.this.srvIoHandler.newInstance());
					}
				});
			}
			else if (serviceInfo.getProtocal() == EProtocal.HTTP) {
				boot.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel channel) throws Exception {
						channel.pipeline()
							.addLast(new HttpServerCodec())
							.addLast(new HttpObjectAggregator(Integer.MAX_VALUE))
							.addLast(ServerSide.this.httpSrvIoHandler.newInstance());
					}
				});
			}
			boot.bind(serverInfo.getIp(), serviceInfo.getPort()).sync();
			return boot;
		}
		public void shutdown() {
			for (ServerBootstrap boot : this.boots.values()) {
				boot.config().group().shutdownGracefully();
				boot.config().childGroup().shutdownGracefully();
			}
		}
	}
}
