package com.kingsoft.netstore.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.Future;

import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.kingsoft.netstore.TransHelper;
import com.kingsoft.netstore.domain.Cmd;
import com.kingsoft.netstore.domain.FileInfo;
import com.kingsoft.netstore.domain.Protocol;
import com.kingsoft.netstore.domain.TransData;
import com.kingsoft.netstore.domain.TransEnd;
import com.kingsoft.netstore.domain.TransInfo;
import com.kingsoft.netstore.domain.TransObj;
import com.kingsoft.netstore.domain.TransResult;
import com.kingsoft.netstore.domain.TransSplit;
import com.kingsoft.netstore.task.IBack;
import com.kingsoft.netstore.task.Task;
import com.kingsoft.netstore.task.TaskUtils;

/**
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月8日
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {

	// 处理心跳检测
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			ctx.writeAndFlush(new Protocol(Cmd.HEART_BEAT, new TransObj("hearteat", null)));
			return;
		}
		super.userEventTriggered(ctx, evt);
	}

	private Logger LOG = Logger.getLogger(getClass());

	private TransHelper helper;

	public ClientHandler(TransHelper helper) {
		this.helper = helper;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		SocketChannel channel = (SocketChannel) ctx.channel();
		LOG.info("连接报告开始");
		LOG.info("连接报告信息：本客户端连接到服务端。channelId：" + channel.id());
		LOG.info("连接报告IP:" + channel.localAddress().getHostString());
		LOG.info("连接报告Port:" + channel.localAddress().getPort());
		LOG.info("连接报告完毕");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		String msg = "断开连接:" + ctx.channel().localAddress().toString();
		LOG.warn(msg);
		TransException exception = new TransException(msg, null);
		notifyError(ctx, exception);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// 数据格式验证
		if (!(msg instanceof Protocol)) {
			return;
		}
		Protocol protocol = (Protocol) msg;
		TransObj transObj = (TransObj) protocol.getData();
		if (StringUtils.isNotEmpty(transObj.getTransId())) {
			ctx.channel().attr(NbftClient.TRANSKEY).set(transObj.getTransId());
		}
		switch (protocol.getCmd()) {
			case FILEINFO: {// 下载
				FileInfo fileInfo = (FileInfo) protocol.getData();
				LOG.info("文件" + fileInfo.getTitle() + "共有" + fileInfo.getSplits().size() + "个分片需要传输！");
				TransInfo transInfo = helper.getTransInfo(fileInfo);
				transInfo.setTransId(transObj.getTransId());
				processDownload(ctx, transInfo);
				break;
			}
			case TRANS_INFO: {// 上传
				TransInfo transInfo = (TransInfo) protocol.getData();
				transInfo.setTransId(transObj.getTransId());
				processUpload(ctx, transInfo);
				break;
			}
			case TRANS_SPLIT: {
				TransSplit split = (TransSplit) protocol.getData();
				TransData transData = helper.readSplit(split);
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
					if (result.isFileComplete()) {
						notifyDownload(transData.getTransId(), result);
					}
				}
				break;
			}
			case TRANS_END: {
				ctx.flush();
				TransEnd transEnd = (TransEnd) protocol.getData();
				if (transEnd.getEndType() == TransEnd.FILE) {
					notifyUpload(transEnd.getTransId());
				} else if (transEnd.getEndType() == TransEnd.ERROR) {
					TransException exception = new TransException("传输" + transEnd.getTransId() + "失败", transEnd);
					notifyError(ctx, exception);
				}
				// 不能关闭，使用了连接池
				// ctx.close();
				break;
			}
			case HEART_BEAT: {
				LOG.info("服务器返回心跳验证消息！");
			}
			default:
				break;
		}
	}

	private void processDownload(ChannelHandlerContext ctx, TransInfo transInfo) {
		// 只有一块，直接使用当前连接进行数据交换
		if (transInfo.getSplits().size() == 1) {
			TransSplit split = transInfo.getSplits().get(0);
			split.setTransId(transInfo.getTransId());
			ctx.writeAndFlush(new Protocol(Cmd.TRANS_SPLIT, split));
		} else {
			// 第一个分片还是由当前连接进行交换
			TransSplit firstSplit = transInfo.getSplits().get(0);
			firstSplit.setTransId(transInfo.getTransId());
			ctx.writeAndFlush(new Protocol(Cmd.TRANS_SPLIT, firstSplit));

			// 其它分片在线程中执行
			ExecutorService service = ExecutorHolder.instance().getService();
			for (int index = 1; index < transInfo.getSplits().size(); index++) {
				TransSplit split = transInfo.getSplits().get(index);
				split.setTransId(transInfo.getTransId());
				service.execute(new TransSplitCall(ctx, split, Cmd.TRANS_SPLIT));
			}
		}
	}

	private void processUpload(ChannelHandlerContext ctx, TransInfo transInfo) throws Exception {
		// 只有一块，直接使用当前连接进行数据交换
		if (transInfo.getSplits().size() == 1) {
			TransSplit split = transInfo.getSplits().get(0);
			TransData transData = helper.readSplit(split);
			split.setTransId(transInfo.getTransId());
			ctx.writeAndFlush(new Protocol(Cmd.TRANS_DATA, transData));
		} else {
			// 第一个分片还是由当前连接进行交换
			TransSplit firstSplit = transInfo.getSplits().get(0);
			TransData transData = helper.readSplit(firstSplit);
			transData.setTransId(transInfo.getTransId());
			ctx.writeAndFlush(new Protocol(Cmd.TRANS_DATA, transData));

			// 其它分片在线程中执行
			ExecutorService service = ExecutorHolder.instance().getService();
			for (int index = 1; index < transInfo.getSplits().size(); index++) {
				TransSplit split = transInfo.getSplits().get(index);
				split.setTransId(transInfo.getTransId());
				service.execute(new TransSplitCall(ctx, split, Cmd.TRANS_DATA));
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (LOG.isDebugEnabled()) {
			cause.printStackTrace();
		}
		TransException exception = new TransException(cause.getMessage(), null);
		notifyError(ctx, exception);
		LOG.error("异常信息：" + cause.getMessage());
	}

	class TransSplitCall implements Runnable {

		private TransSplit split;

		private ChannelHandlerContext ctx;

		private Cmd cmd;

		public TransSplitCall(ChannelHandlerContext ctx, TransSplit split, Cmd cmd) {
			super();
			this.split = split;
			this.ctx = ctx;
			this.cmd = cmd;
		}

		@Override
		public void run() {
			Future<Channel> channelFuture = null;
			ChannelPool pool = null;
			Channel channel = null;
			try {
				pool = ctx.channel().attr(NbftClient.POOLKEY).get();
				String transId = ctx.channel().attr(NbftClient.TRANSKEY).get();
				channelFuture = pool.acquire();
				channel = channelFuture.get();
				channel.attr(NbftClient.TRANSKEY).set(transId);
				channel.attr(NbftClient.POOLKEY).set(pool);
				switch (cmd) {
					case TRANS_DATA: {
						TransData transData = helper.readSplit(split);
						channel.writeAndFlush(new Protocol(cmd, transData));
						break;
					}
					case TRANS_SPLIT: {
						channel.writeAndFlush(new Protocol(cmd, split));
						break;
					}
					default:
						break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				// 用完后一定要放回连接池
				if (pool != null && channel != null) {
					pool.release(channel);
				}
				if (null != channelFuture && channelFuture.isSuccess()) {
					LOG.info("nbft channel start done.");
				} else {
					LOG.error("nbft channel start error.");
				}
			}
		}
	}

	private void notifyError(ChannelHandlerContext ctx, TransException exception) {
		String transId = ctx.channel().attr(NbftClient.TRANSKEY).get();
		if (StringUtils.isNotEmpty(transId)) {
			// 发生异常，结束线程等待
			Task task = TaskUtils.getInstance().getTask(transId);
			if (task != null && !task.isNotify()) {
				task.setBack(new IBack() {
					@Override
					public Object doing(Object... objs) throws Exception {
						throw exception;
					}
				});
				task.signalTask();
			}
		}
	}

	private void notifyUpload(String transId) {
		Task task = TaskUtils.getInstance().getTask(transId);
		if (task != null && !task.isNotify()) {
			task.setBack(new IBack() {
				@Override
				public Object doing(Object... objs) throws Exception {
					return transId;
				}
			});
			task.signalTask();
		}
	}

	private void notifyDownload(String transId, TransResult result) {
		Task task = TaskUtils.getInstance().getTask(transId);
		if (task != null && !task.isNotify()) {
			task.setBack(new IBack() {
				@Override
				public Object doing(Object... objs) throws Exception {
					return result.getFileInfo().getTitle();
				}
			});
			task.signalTask();
		}
	}

	public static class TransException extends Exception {
		private static final long serialVersionUID = 1L;

		private String msg;
		private TransObj transObj;

		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}

		public TransObj getTransObj() {
			return transObj;
		}

		public void setTransObj(TransObj transObj) {
			this.transObj = transObj;
		}

		public TransException(String msg, TransObj transObj) {
			super(msg);
			this.msg = msg;
			this.transObj = transObj;
		}

	}
}
