package com.kingsoft.netstore.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 线程同步锁
 * 
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月9日
 */
public final class LockUtil {

	private ConcurrentMap<String, Lock> locks = new ConcurrentHashMap<String, Lock>();

	private static LockUtil instance = null;

	private LockUtil() {

	}

	public static synchronized LockUtil instance() {
		if (instance == null) {
			instance = new LockUtil();
		}
		return instance;
	}

	private synchronized Lock getLock(String id) {
		if (!locks.containsKey(id)) {
			// 公平锁，按请求的先后顺序获取锁
			locks.put(id, new ReentrantLock(true));
		}
		return locks.get(id);
	}

	public void lock(String id) {
		Lock lock = getLock(id);
		lock.lock();
	}

	public void unlock(String id) {
		if (locks.containsKey(id)) {
			Lock lock = locks.get(id);
			lock.unlock();
		}
	}

	public void remove(String id) {
		if (locks.containsKey(id)) {
			locks.remove(id);
		}
	}
}
