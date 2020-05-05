package com.kingsoft.netstore.domain;

import java.io.Serializable;

/**
 * 文件传输分片数据
 * 
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月1日
 */
public class TransData extends TransObj  implements Serializable {

	private static final long serialVersionUID = 1L;

	// 文件ID
	private String fileId;

	// 起始位置
	private long beginPos;

	// 注意是当前数据分片所在的文件分片的结束位置
	private long endPos;

	// 分片的实际数据
	private byte[] data;

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

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
}
