package com.kingsoft.netstore.server;

import com.kingsoft.netstore.domain.TransObj;

/**
 * 文件交换过滤器，可以根据传输对象中存放的元数据保一些认证工作
 * 
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月17日
 */
public interface TransFilter {
	/**
	 * @param transObj
	 * @return 认证成功返回null，否则返回错误信息
	 */
	public String filter(TransObj transObj);
}
