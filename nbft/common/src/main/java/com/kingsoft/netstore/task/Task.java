package com.kingsoft.netstore.task;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月12日
 */
public class Task {

	private Lock lock;

	private Condition condition;

	private volatile IBack back;

	/**
	 * 是否被唤醒
	 */
	private volatile boolean isNotify = false;

	/**
	 * 是否执行等待
	 */
	private volatile boolean isAwait = false;

	/**
	 * 唯一标示key
	 */
	private String key;

	/**
	 * 是否被唤醒
	 * 
	 * @return true 是，false，否
	 */
	public boolean isNotify() {
		return isNotify;
	}

	/**
	 * 是否被移除 true 是 false 否
	 * 
	 * @return
	 */
	public boolean isRemove() {
		return !TaskUtils.getInstance().hasKey(getKey());
	}

	public boolean isAwait() {
		return isAwait;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public IBack getBack() {
		return back;
	}

	public void setBack(IBack back) {
		this.back = back;
	}

	protected Task() {
		lock = new ReentrantLock();
		condition = lock.newCondition();
	}

	public void remove() {
		TaskUtils.getInstance().removeKey(getKey());
	}

	public void signalTask() {
		while (!isAwait && !Thread.interrupted()) {
			try {
				TimeUnit.MILLISECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			lock.lock();
			isNotify = true;
			condition.signal();
		} finally {
			lock.unlock();
		}
	}

	public void awaitTask() {
		try {
			lock.lock();
			isAwait = true;
			condition.await();
		} catch (Throwable e) {
		} finally {
			lock.unlock();
		}
	}
}
