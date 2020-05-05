package com.kingsoft.netstore.domain;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * 待交换的文件信息
 * 
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月1日
 */
public class FileInfo extends TransObj implements Serializable {

	private static final long serialVersionUID = 1L;

	// 文件统一标识
	private String fileId;

	// 文件名称
	private String title;

	// 文件md5值
	private String md5;

	// 文件大小
	private long size;

	// 文件分片信息
	private List<FileSplit> splits;

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public List<FileSplit> getSplits() {
		return splits;
	}

	public void setSplits(List<FileSplit> splits) {
		this.splits = splits;
	}
}
