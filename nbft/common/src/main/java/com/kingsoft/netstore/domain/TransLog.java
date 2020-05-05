package com.kingsoft.netstore.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月15日
 */
public class TransLog implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<Log> fileLogs = new ArrayList<Log>();

	public List<Log> getFileLogs() {
		if (fileLogs == null) {
			fileLogs = new ArrayList<>();
		}
		return fileLogs;
	}

	public void setFileLogs(List<Log> fileLogs) {
		this.fileLogs = fileLogs;
	}

	public TransLog(List<Log> fileLogs) {
		super();
		this.fileLogs = fileLogs;
	}

	public TransLog() {
		super();
	}

	public static class Log {

		String fileId;

		String fileTitle;

		long fileSize;

		String md5;

		String filePath;

		public String getFileId() {
			return fileId;
		}

		public void setFileId(String fileId) {
			this.fileId = fileId;
		}

		public String getFileTitle() {
			return fileTitle;
		}

		public void setFileTitle(String fileTitle) {
			this.fileTitle = fileTitle;
		}

		public String getFilePath() {
			return filePath;
		}

		public void setFilePath(String filePath) {
			this.filePath = filePath;
		}

		public long getFileSize() {
			return fileSize;
		}

		public void setFileSize(long fileSize) {
			this.fileSize = fileSize;
		}

		public String getMd5() {
			return md5;
		}

		public void setMd5(String md5) {
			this.md5 = md5;
		}

		public Log(String fileId, String fileTitle, long fileSize, String md5, String filePath) {
			super();
			this.fileId = fileId;
			this.fileTitle = fileTitle;
			this.fileSize = fileSize;
			this.md5 = md5;
			this.filePath = filePath;
		}

		public Log() {
			super();
		}
	}
}
