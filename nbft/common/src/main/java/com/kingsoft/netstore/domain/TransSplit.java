package com.kingsoft.netstore.domain;

import java.io.Serializable;

/**
 * 文件分片传输请求
 * 
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月1日
 */
public class TransSplit extends TransObj implements Serializable {

	private static final long serialVersionUID = 1L;

	// 文件ID
	private String fileId;

	// 起始位置
	private long beginPos;

	// 注意是当前分片请求的文件分片的结束位置
	private long endPos;

	public TransSplit() {
	}

	public TransSplit(String transId, String fileId, long beginPos, long endPos) {
		this.transId = transId;
		this.fileId = fileId;
		this.beginPos = beginPos;
		this.endPos = endPos;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public long getBeginPos() {
		return beginPos;
	}

	public void setBeginPos(long beginPos) {
		this.beginPos = beginPos;
	}

	public long getEndPos() {
		return endPos;
	}

	public void setEndPos(long endPos) {
		this.endPos = endPos;
	}

}
