package com.kingsoft.netstore.domain;

import java.io.Serializable;

/**
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月14日
 */
public class Download extends TransObj implements Serializable {

	private static final long serialVersionUID = 1L;

	private String fileId;

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
}
