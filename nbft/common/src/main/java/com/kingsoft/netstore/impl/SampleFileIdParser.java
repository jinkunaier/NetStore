package com.kingsoft.netstore.impl;

import java.io.File;

import com.kingsoft.netstore.FileIdParser;

/**
 * 示例，此时的fileId即为发送文件目录下的文件名
 * 
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月17日
 */
public class SampleFileIdParser implements FileIdParser {

	// 发送文件所在目录
	public String outDir = "";

	public String getOutDir() {
		return outDir;
	}

	public void setOutDir(String outDir) {
		this.outDir = outDir;
	}

	public SampleFileIdParser(String outDir) {
		super();
		this.outDir = outDir;
	}

	@Override
	public File getFile(String fileId) {
		File file = new File(outDir + File.separator + fileId);
		if (file.exists()) {
			return file;
		}
		return null;
	}

}
