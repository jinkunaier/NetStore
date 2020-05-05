package com.kingsoft.netstore.exception;

import com.kingsoft.netstore.domain.FileInfo;

/**
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月15日
 */
public class TransFileExistException extends Exception {

	private static final long serialVersionUID = 1L;

	FileInfo fileInfo = null;

	/**
	 * @param fileInfo
	 */
	public TransFileExistException(FileInfo fileInfo) {
		super("待下载的文件" + fileInfo.getTitle() + "已经存在");
		this.fileInfo = fileInfo;
	}
}
