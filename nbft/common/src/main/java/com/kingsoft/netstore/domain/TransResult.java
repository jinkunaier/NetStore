package com.kingsoft.netstore.domain;

import java.io.Serializable;

/**
 * 
 * 数据传输写入结果
 * 
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月13日
 */
public class TransResult implements Serializable {

	private static final long serialVersionUID = 1L;

	// 文件ID
	private String fileId;

	// 文件传输状态保存在这个里面
	private FileInfo fileInfo;

	// 后续分片请求，为空则无请求
	private TransSplit require;

	// 整个文件是否已接收完成
	private boolean fileComplete;

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public FileInfo getFileInfo() {
		return fileInfo;
	}

	public void setFileInfo(FileInfo fileInfo) {
		this.fileInfo = fileInfo;
	}

	public TransSplit getRequire() {
		return require;
	}

	public void setRequire(TransSplit require) {
		this.require = require;
	}

	public boolean isFileComplete() {
		return fileComplete;
	}

	public void setFileComplete(boolean fileComplete) {
		this.fileComplete = fileComplete;
	}

	public TransResult(String fileId, FileInfo fileInfo, TransSplit require, boolean fileComplete) {
		super();
		this.fileId = fileId;
		this.fileInfo = fileInfo;
		this.require = require;
		this.fileComplete = fileComplete;
	}

	public TransResult() {
		super();
	}
}
