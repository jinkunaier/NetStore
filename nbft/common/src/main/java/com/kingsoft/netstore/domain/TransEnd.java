package com.kingsoft.netstore.domain;

import java.io.Serializable;

/**
 * 传输完成
 * 
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月13日
 */
public class TransEnd extends TransObj implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final int ERROR = 0;// 异常结束
	public static final int SPLIT = 10;// 分片传输完成
	public static final int FILE = 20;// 文件传输完成

	private String fileId;

	// 传输完成类型
	private int endType;

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public int getEndType() {
		return endType;
	}

	public void setEndType(int endType) {
		this.endType = endType;
	}
}
