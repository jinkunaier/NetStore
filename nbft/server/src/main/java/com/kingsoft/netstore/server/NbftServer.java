package com.kingsoft.netstore.server;

import org.apache.log4j.Logger;

import com.kingsoft.netstore.TransConfig;
import com.kingsoft.netstore.TransHelper;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月1日
 */
public class NbftServer {

	private Logger LOG = Logger.getLogger(getClass());

	// 传输配置信息
	private TransConfig config;

	// 交换过滤器
	private TransFilter filter;

	// 配置服务端NIO线程组
	private EventLoopGroup parentGroup = new NioEventLoopGroup();
	private EventLoopGroup childGroup = new NioEventLoopGroup();
	private Channel channel;

	public TransConfig getConfig() {
		return config;
	}

	public NbftServer(TransConfig config) {
		this(config, null);
	}

	public NbftServer(TransConfig config, TransFilter filter) {
		this.config = config;
		this.filter = filter;
	}

	public ChannelFuture bing(int port) {
		ChannelFuture channelFuture = null;
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(parentGroup, childGroup).channel(NioServerSocketChannel.class) // 非阻塞模式
					.option(ChannelOption.SO_BACKLOG, 128)
					.childHandler(new ServerChannelInitializer(new TransHelper(config), filter));
			channelFuture = b.bind(port).syncUninterruptibly();
			this.channel = channelFuture.channel();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != channelFuture && channelFuture.isSuccess()) {
				LOG.info("nbft server start done.");
			} else {
				LOG.error("nbft server start error.");
			}
		}
		return channelFuture;
	}

	public void destroy() {
		if (channel != null) {
			channel.close();
			parentGroup.shutdownGracefully();
			childGroup.shutdownGracefully();
		}
	}

	public Channel getChannel() {
		return channel;
	}

}
