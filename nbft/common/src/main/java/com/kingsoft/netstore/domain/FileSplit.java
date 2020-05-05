package com.kingsoft.netstore.domain;

import java.io.Serializable;

/**
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月3日
 */
public class FileSplit implements Serializable {

	private static final long serialVersionUID = 1L;

	// 所属文件的标识
	private String fileId;

	// 分片顺序
	private int index;

	// 分片起始位置
	private long beginPos;

	// 分片结束位置
	private long endPos;

	// 已经传输入的长度，用于断点续传
	private long transed;

	public FileSplit() {
		super();
	}

	/**
	 * @param beginPos
	 *            开始位置
	 * @param endPos
	 *            结束位置
	 */
	public FileSplit(String fileId, int index, long beginPos, long endPos) {
		super();
		this.fileId = fileId;
		this.index = index;
		this.beginPos = beginPos;
		this.endPos = endPos;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
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

	public long getTransed() {
		return transed;
	}

	public void setTransed(long transed) {
		this.transed = transed;
	}

}
