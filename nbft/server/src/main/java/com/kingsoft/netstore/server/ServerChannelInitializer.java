package com.kingsoft.netstore.server;

import com.kingsoft.netstore.TransHelper;
import com.kingsoft.netstore.codec.ObjDecoder;
import com.kingsoft.netstore.codec.ObjEncoder;
import com.kingsoft.netstore.domain.Protocol;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月1日
 */
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {

	// 所有连接共用
	private TransHelper helper;

	// 交换过滤器
	private TransFilter filter;

	public ServerChannelInitializer(TransHelper helper, TransFilter filter) {
		super();
		this.helper = helper;
		this.filter = filter;
	}

	@Override
	protected void initChannel(SocketChannel channel) {
		// 15分钟(3个客户端心跳时间)未收到客户端数据，则关闭连接
		channel.pipeline().addLast(new IdleStateHandler(0, 0, 900));
		// 对象传输处理
		channel.pipeline().addLast(new ObjDecoder(Protocol.class));
		channel.pipeline().addLast(new ObjEncoder(Protocol.class));
		// 在管道中添加我们自己的接收数据实现方法
		channel.pipeline().addLast(new ServerHandler(helper, filter));
	}

}
