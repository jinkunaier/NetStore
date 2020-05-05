package com.kingsoft.netstore.domain;

import java.io.Serializable;
import java.util.List;

/**
 * 分片请求信息，用于上传及下载<br/>
 * 注意：客户端发送文件到服务器端为上传，客户端从服务器取数据为下载<br/>
 * 续传到上传上载时都有需要
 * 
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月1日
 */
public class TransInfo extends TransObj implements Serializable {

	private static final long serialVersionUID = 1L;

	// 对应文件ID
	private String fileId;

	// 需要交换的分片信息
	private List<TransSplit> splits;

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public List<TransSplit> getSplits() {
		return splits;
	}

	public void setSplits(List<TransSplit> splits) {
		this.splits = splits;
	}

}
