package com.kingsoft.netstore.context;

import com.kingsoft.netstore.data.entity.User;

import java.util.HashMap;
import java.util.Map;

/**
 * 线程上下文绑定类
 * 
 * @author jack
 *
 */
public class ContextHolder {

	public static final ThreadLocal<Map<String, Object>> local = new ThreadLocal<Map<String, Object>>();

	public static void set(String key, Object value) {
		Map<String, Object> map = local.get();
		if (map == null) {
			map = new HashMap<String, Object>();
			local.set(map);
		}
		map.put(key, value);
	}

	public static Object get(String key) {
		Map<String, Object> map = local.get();
		if (map == null) {
			map = new HashMap<String, Object>();
			local.set(map);
		}
		return map.get(key);
	}

	public static User getUser() {
		return (User) get(Constants.LOGIN_USER);
	}

	public static void setUser(User user) {
		set(Constants.LOGIN_USER, user);
	}

	public static void remove() {
		local.remove();
	}
}
