package com.kingsoft.netstore.server;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.kingsoft.netstore.TransHelper;
import com.kingsoft.netstore.domain.Cmd;
import com.kingsoft.netstore.domain.Download;
import com.kingsoft.netstore.domain.FileInfo;
import com.kingsoft.netstore.domain.Protocol;
import com.kingsoft.netstore.domain.TransData;
import com.kingsoft.netstore.domain.TransEnd;
import com.kingsoft.netstore.domain.TransInfo;
import com.kingsoft.netstore.domain.TransObj;
import com.kingsoft.netstore.domain.TransResult;
import com.kingsoft.netstore.domain.TransSplit;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;

/**
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月1日
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		// 指定时间内未收到客户端数据，则关闭连接
		if (evt instanceof IdleStateEvent) {
			ctx.close();
			return;
		}
		super.userEventTriggered(ctx, evt);
	}

	private AttributeKey<String> transKey = AttributeKey.valueOf("transKey");

	private Logger LOG = Logger.getLogger(getClass());

	private TransHelper helper;
	// 交换过滤器
	private TransFilter filter;

	public ServerHandler(TransHelper helper, TransFilter filter) {
		this.helper = helper;
		this.filter = filter;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		SocketChannel channel = (SocketChannel) ctx.channel();
		LOG.info("连接报告开始");
		LOG.info("连接报告信息：户端连接到本服务端。channelId：" + channel.id());
		LOG.info("连接报告IP:" + channel.localAddress().getHostString());
		LOG.info("连接报告Port:" + channel.localAddress().getPort());
		LOG.info("连接报告完毕");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		LOG.info("客户端断开连接" + ctx.channel().localAddress().toString());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// 数据格式验证
		if (!(msg instanceof Protocol)) {
			return;
		}
		Protocol protocol = (Protocol) msg;
		TransObj transObj = (TransObj) protocol.getData();
		ctx.channel().attr(transKey).set(transObj.getTransId());
		if (!doFilter(ctx, transObj)) {
			return;
		}
		switch (protocol.getCmd()) {
			case DOWNLOAD: {// 下载
				// 传送的是下载 文件的URL
				Download download = (Download) protocol.getData();
				FileInfo fileInfo = helper.getOutFileInfo(download.getFileId());
				fileInfo.setTransId(download.getTransId());
				ctx.writeAndFlush(new Protocol(Cmd.FILEINFO, fileInfo));
				break;
			}
			case FILEINFO: {// 上传
				FileInfo fileInfo = (FileInfo) protocol.getData();
				TransInfo transInfo = helper.getTransInfo(fileInfo);
				LOG.info("文件" + fileInfo.getTitle() + "共有" + transInfo.getSplits().size() + "个分片需要传输！");
				transInfo.setTransId(transObj.getTransId());
				ctx.writeAndFlush(new Protocol(Cmd.TRANS_INFO, transInfo));
				break;
			}
			case TRANS_SPLIT: {
				TransSplit transSplit = (TransSplit) protocol.getData();
				TransData transData = helper.readSplit(transSplit);
				transData.setTransId(transObj.getTransId());
				ctx.writeAndFlush(new Protocol(Cmd.TRANS_DATA, transData));
				break;
			}
			case TRANS_DATA: {
				TransData transData = (TransData) protocol.getData();
				TransResult result = helper.writeSplit(transData);
				if (result.getRequire() != null) {
					result.getRequire().setTransId(transObj.getTransId());
					ctx.writeAndFlush(new Protocol(Cmd.TRANS_SPLIT, result.getRequire()));
				} else {
					int endType = result.isFileComplete() ? TransEnd.FILE : TransEnd.SPLIT;
					TransEnd transEnd = new TransEnd();
					transEnd.setTransId(transObj.getTransId());
					transEnd.setEndType(endType);
					ctx.writeAndFlush(new Protocol(Cmd.TRANS_END, transEnd));
				}
				break;
			}
			case TRANS_END: {
				ctx.flush();
				// 不断开连接
				// ctx.close();
				break;
			}
			case HEART_BEAT: {
				LOG.info("客户端心跳检测包:" + ctx.channel().remoteAddress());
				ctx.writeAndFlush(new Protocol(Cmd.HEART_BEAT, new TransObj("ok", null)));
			}
			default:
				break;
		}
	}

	private boolean doFilter(ChannelHandlerContext ctx, TransObj transObj) {
		if (filter != null) {
			String error = filter.filter(transObj);
			if (StringUtils.isNotEmpty(error)) {
				TransEnd transEnd = new TransEnd();
				transEnd.setTransId(transObj.getTransId());
				transEnd.setEndType(TransEnd.ERROR);
				Map<String, Object> transMeta = new HashMap<>();
				transEnd.setTransMeta(transMeta);
				transMeta.put("error", error);
				transMeta.put("code", "failure");
				ctx.writeAndFlush(new Protocol(Cmd.TRANS_END, transEnd));
				return false;
			}
		}
		return true;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (LOG.isDebugEnabled()) {
			cause.printStackTrace();
		}
		String transId = ctx.channel().attr(transKey).get();
		if (StringUtils.isNotEmpty(transId)) {
			TransEnd transEnd = new TransEnd();
			transEnd.setTransId(transId);
			transEnd.setEndType(TransEnd.ERROR);
			Map<String, Object> transMeta = new HashMap<>();
			transEnd.setTransMeta(transMeta);
			transMeta.put("error", cause.getMessage());
			ctx.writeAndFlush(new Protocol(Cmd.TRANS_END, transEnd));
		}
		LOG.error("异常信息：" + cause.getMessage());
	}

}
