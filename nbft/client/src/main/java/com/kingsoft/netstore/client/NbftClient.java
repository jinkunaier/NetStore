package com.kingsoft.netstore.client;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;

import com.kingsoft.netstore.TransConfig;
import com.kingsoft.netstore.TransHelper;
import com.kingsoft.netstore.domain.Cmd;
import com.kingsoft.netstore.domain.Download;
import com.kingsoft.netstore.domain.FileInfo;
import com.kingsoft.netstore.domain.Protocol;
import com.kingsoft.netstore.task.Task;
import com.kingsoft.netstore.task.TaskUtils;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;

public class NbftClient {

	private Logger LOG = Logger.getLogger(getClass());

	public static final AttributeKey<ChannelPool> POOLKEY = AttributeKey.valueOf("poolKey");

	public static final AttributeKey<String> TRANSKEY = AttributeKey.valueOf("transKey");

	// 传输配置信息
	private TransConfig config;

	private TransHelper helper;

	public TransConfig getConfig() {
		return config;
	}

	// 配置服务端NIO线程组
	private EventLoopGroup workerGroup = new NioEventLoopGroup();

	private Channel channel;

	private ChannelPool channelPool;

	public NbftClient(TransConfig config) {
		this.config = config;
		this.helper = new TransHelper(this.config);
		// 打开线程池
		ExecutorHolder.init(getThreadPoolSize());
	}

	private int getThreadPoolSize() {
		int threadPoolSize = 10;
		// 线程池是连接池的二倍就足够用了，两者没有必然联系
		if (this.config.getChannelPoolSize() * 2 > 10) {
			threadPoolSize = this.config.getChannelPoolSize();
		}
		return threadPoolSize;
	}

	public NbftClient connect(String remoteHost, int remotePort) {
		Future<Channel> channelFuture = null;
		try {
			Bootstrap b = new Bootstrap();
			b.group(workerGroup);
			b.channel(NioSocketChannel.class);
			b.remoteAddress(remoteHost, remotePort);
			b.option(ChannelOption.AUTO_READ, true);
			this.channelPool = new FixedChannelPool(b, new ClientChannelPoolHander(helper),
					config.getChannelPoolSize());
			channelFuture = this.channelPool.acquire();
			this.channel = channelFuture.get();
			channel.attr(NbftClient.POOLKEY).set(this.channelPool);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != channelFuture && channelFuture.isSuccess()) {
				LOG.info("nbft client start done.");
				this.channelPool.release(this.channel);
			} else {
				LOG.info("nbft client start error.");
			}
		}
		return this;
	}

	/**
	 * 下载文件
	 * 
	 * @param fileId
	 *            远程服务器文件id
	 * @param meta
	 *            可以给服务器端传递一些额外信息
	 * @return
	 */
	public java.util.concurrent.Future<String> download(String fileId, Map<String, Object> transMeta) {
		if (this.channel != null) {
			String transId = createTransId();
			this.channel.attr(TRANSKEY).set(transId);
			Download download = new Download();
			download.setTransId(transId);
			download.setFileId(fileId);
			download.setTransMeta(transMeta);
			this.channel.writeAndFlush(new Protocol(Cmd.DOWNLOAD, download));
			ExecutorService service = ExecutorHolder.instance().getService();
			return service.submit(new Callable<String>() {
				@Override
				public String call() throws Exception {
					System.out.println("-----------------------------------------------------------------------------");
					Task task = TaskUtils.getInstance().createTask(transId);
					task.awaitTask();
					task.remove();
					return (String) task.getBack().doing();
				}
			});
		} else {
			throw new RuntimeException("未获取客户端连接");
		}

	}

	/**
	 * 上传文件
	 * 
	 * @param fileId
	 *            本地文件的id
	 * 
	 * @param meta
	 *            可以给服务器端传递一些额外信息
	 * @return
	 * @throws Exception
	 */
	public java.util.concurrent.Future<Boolean> upload(String fileId, Map<String, Object> transMeta) throws Exception {
		if (this.channel != null) {
			String transId = createTransId();
			this.channel.attr(TRANSKEY).set(transId);
			FileInfo fileInfo = helper.getOutFileInfo(fileId);
			fileInfo.setTransId(transId);
			fileInfo.setTransMeta(transMeta);
			this.channel.writeAndFlush(new Protocol(Cmd.FILEINFO, fileInfo));
			ExecutorService service = ExecutorHolder.instance().getService();
			return service.submit(new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					Task task = TaskUtils.getInstance().createTask(transId);
					task.awaitTask();
					task.remove();
					task.getBack().doing();
					return true;
				}
			});
		} else {
			throw new RuntimeException("未获取客户端连接");
		}
	}

	private String createTransId() {
		return UUID.randomUUID().toString();
	}

	public void destroy() {
		if (this.channelPool != null) {
			this.channelPool.close();
			workerGroup.shutdownGracefully();
		}
		// 关闭线程池
		ExecutorHolder.instance().destroy();
	}

}
