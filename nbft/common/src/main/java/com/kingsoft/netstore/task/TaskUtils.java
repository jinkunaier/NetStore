package com.kingsoft.netstore.task;

import org.apache.commons.lang3.StringUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月12日
 */
public class TaskUtils {

	private static TaskUtils instance = null;

	private Map<String, Task> taskMap = new ConcurrentHashMap<String, Task>();

	private TaskUtils() {

	}

	public static TaskUtils getInstance() {
		if (instance == null) {
			synchronized (TaskUtils.class) {
				if (instance == null) {
					instance = new TaskUtils();
				}
			}
		}
		return instance;
	}

	public Task createTask(String key) {
		Task task = new Task();
		task.setKey(key);
		taskMap.put(key, task);
		return task;
	}

	public Task getTask(String key) {
		return taskMap.get(key);
	}

	public void removeKey(String key) {
		if (StringUtils.isNotEmpty(key)) {
			taskMap.remove(key);
		}
	}

	public boolean hasKey(String key) {
		return taskMap.containsKey(key);
	}
}
