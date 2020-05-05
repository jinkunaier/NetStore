package com.kingsoft.netstore.client;

import com.kingsoft.netstore.TransHelper;
import com.kingsoft.netstore.codec.ObjDecoder;
import com.kingsoft.netstore.codec.ObjEncoder;
import com.kingsoft.netstore.domain.Protocol;

import io.netty.channel.Channel;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月10日
 */
public class ClientChannelPoolHander extends AbstractChannelPoolHandler {

	// 所有连接共用
	private TransHelper helper;

	public ClientChannelPoolHander(TransHelper helper) {
		this.helper = helper;
	}

	@Override
	public void channelCreated(Channel channel) throws Exception {

		// 5分钟发送一个心跳包
		channel.pipeline().addLast(new IdleStateHandler(0, 0, 300));
		// 对象传输处理
		channel.pipeline().addLast(new ObjDecoder(Protocol.class));
		channel.pipeline().addLast(new ObjEncoder(Protocol.class));
		// 在管道中添加我们自己的接收数据实现方法
		channel.pipeline().addLast(new ClientHandler(helper));
	}

}
