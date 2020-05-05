package com.kingsoft.netstore;

import com.kingsoft.netstore.domain.FileInfo;
import com.kingsoft.netstore.domain.TransResult;

/**
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月14日
 */
public interface TransReport {

	public void report(FileInfo fileInfo, TransResult result);

}
