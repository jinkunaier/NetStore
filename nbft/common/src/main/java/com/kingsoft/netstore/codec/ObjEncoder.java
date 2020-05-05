package com.kingsoft.netstore.codec;

import com.kingsoft.netstore.utils.SerializeUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月1日
 */
public class ObjEncoder extends MessageToByteEncoder<Object> {

	private Class<?> genericClass;

	public ObjEncoder(Class<?> genericClass) {
		this.genericClass = genericClass;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) {
		if (genericClass.isInstance(in)) {
			byte[] data = SerializeUtil.serialize(in);
			out.writeInt(data.length);
			out.writeBytes(data);
		}
	}

}
