package com.kingsoft.netstore.codec;

import java.util.List;

import com.kingsoft.netstore.utils.SerializeUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月1日
 */
public class ObjDecoder extends ByteToMessageDecoder {

	private Class<?> genericClass;

	public ObjDecoder(Class<?> genericClass) {
		this.genericClass = genericClass;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
		if (in.readableBytes() < 4) {
			return;
		}
		// 记录一下当前读取位置
		in.markReaderIndex();
		int dataLength = in.readInt();
		if (in.readableBytes() < dataLength) {
			// 重置到上次记录的读取位置，与markReaderIndex联合使用的
			in.resetReaderIndex();
			return;
		}
		byte[] data = new byte[dataLength];
		in.readBytes(data);
		out.add(SerializeUtil.deserialize(data, genericClass));
	}

}
