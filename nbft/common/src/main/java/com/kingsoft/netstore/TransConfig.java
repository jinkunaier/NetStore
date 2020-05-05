package com.kingsoft.netstore;

/**
 * 文件传输配置信息
 * 
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月1日
 */
public class TransConfig {

	// 接收文件临时存放目录
	private String inDir = "D:\\nbft_dir\\inFiles";

	// 连接池数,默认为5
	private int channelPoolSize = 5;

	// 单个分片大小,默认20M
	private int splitSize = 1024 * 1024 * 2;

	// 每次传输的最大分片数据,默认100K
	private int transSize = 1024 * 1000;

	// 传输过程的回调
	private TransReport transReport;

	// 文件Id解析器
	private FileIdParser fileIdParser;

	public TransConfig() {
		super();
	}

	/**
	 * @param inDir
	 *            输入文件路径
	 * @param fileIdParser
	 *            文件ID解析器
	 */
	public TransConfig(String inDir, FileIdParser fileIdParser) {
		super();
		this.inDir = inDir;
		this.fileIdParser = fileIdParser;
	}

	public int getSplitSize() {
		return splitSize;
	}

	public void setSplitSize(int splitSize) {
		this.splitSize = splitSize;
	}

	public int getTransSize() {
		return transSize;
	}

	public void setTransSize(int transSize) {
		this.transSize = transSize;
	}

	public String getInDir() {
		return inDir;
	}

	public void setInDir(String inDir) {
		this.inDir = inDir;
	}

	public int getChannelPoolSize() {
		return channelPoolSize;
	}

	public void setChannelPoolSize(int channelPoolSize) {
		this.channelPoolSize = channelPoolSize;
	}

	public TransReport getTransReport() {
		return transReport;
	}

	public void setTransReport(TransReport transReport) {
		this.transReport = transReport;
	}

	public FileIdParser getFileIdParser() {
		return fileIdParser;
	}

	public void setFileIdParser(FileIdParser fileIdParser) {
		this.fileIdParser = fileIdParser;
	}
}
