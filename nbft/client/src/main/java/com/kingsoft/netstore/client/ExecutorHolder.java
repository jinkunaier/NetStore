package com.kingsoft.netstore.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 公用的连接池
 * 
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月9日
 */
public final class ExecutorHolder {

	private static ExecutorHolder instance = null;

	private ExecutorService service;

	private ExecutorHolder(int poolSize) {
		service = Executors.newFixedThreadPool(poolSize);
	}

	public static synchronized void init(int poolSize) {
		if (instance == null) {
			instance = new ExecutorHolder(poolSize);
		}
	}

	public static ExecutorHolder instance() {
		if (instance == null) {
			throw new RuntimeException("ExecutorHolder未初始化");
		}
		return instance;
	}

	public ExecutorService getService() {
		return service;
	}

	public void destroy() {
		if (service != null) {
			service.shutdown();
		}
	}
}
