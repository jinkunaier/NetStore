package com.kingsoft.netstore.client;

import com.kingsoft.netstore.TransHelper;
import com.kingsoft.netstore.codec.ObjDecoder;
import com.kingsoft.netstore.codec.ObjEncoder;
import com.kingsoft.netstore.domain.Protocol;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月8日
 */
public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {

	// 所有连接共用
	private TransHelper helper;

	public ClientChannelInitializer(TransHelper helper) {
		this.helper = helper;
	}

	@Override
	protected void initChannel(SocketChannel channel) throws Exception {
		// 对象传输处理
		channel.pipeline().addLast(new ObjDecoder(Protocol.class));
		channel.pipeline().addLast(new ObjEncoder(Protocol.class));
		// 在管道中添加我们自己的接收数据实现方法
		channel.pipeline().addLast(new ClientHandler(helper));
	}

}
