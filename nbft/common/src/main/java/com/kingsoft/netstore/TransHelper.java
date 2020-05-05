package com.kingsoft.netstore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.kingsoft.netstore.domain.FileInfo;
import com.kingsoft.netstore.domain.FileSplit;
import com.kingsoft.netstore.domain.TransData;
import com.kingsoft.netstore.domain.TransInfo;
import com.kingsoft.netstore.domain.TransLog;
import com.kingsoft.netstore.domain.TransResult;
import com.kingsoft.netstore.domain.TransSplit;
import com.kingsoft.netstore.utils.LockUtil;
import com.kingsoft.netstore.utils.SerializeUtil;

/**
 * 文件传输帮助类
 * 
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月1日
 */
public class TransHelper {

	// 文件传输配置
	private TransConfig config;

	private Logger LOG = Logger.getLogger(getClass());

	public TransConfig getConfig() {
		return config;
	}

	public TransHelper(TransConfig config) {
		this.config = config;
	}

	/**
	 * 根据文件ID获取输入文件
	 * 
	 * @param fileId
	 * @return
	 * @throws Exception
	 */
	public File getInFile(String fileId) throws Exception {
		String filePath = getInFilePath(fileId);
		if (StringUtils.isEmpty(filePath)) {
			return null;
		}
		return new File(filePath);
	}

	public File createInFile(FileInfo fileInfo) throws Exception {
		RandomAccessFile randomAccessFile = null;
		try {
			writeLog(fileInfo);
			TransLog.Log fileLog = getFileLog(fileInfo.getFileId());
			File file = new File(fileLog.getFilePath());
			if (file.exists()) {
				throw new Exception("待下载的文件" + fileInfo.getTitle() + "已经存在！");
			}
			// 新建临时文件
			randomAccessFile = new RandomAccessFile(file, "rw");
			// 日志文件写到实际数据的后面
			byte[] fileInfoByte = SerializeUtil.serialize(fileInfo);
			randomAccessFile.setLength(fileInfo.getSize() + fileInfoByte.length);
			randomAccessFile.seek(fileInfo.getSize());
			randomAccessFile.write(fileInfoByte);
			return file;
		} finally {// 确保流关闭
			if (randomAccessFile != null) {
				try {
					randomAccessFile.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private TransLog writeLog(FileInfo fileInfo) throws Exception {
		synchronized (getClass()) {
			TransLog.Log log = fileInfo2Log(fileInfo);
			TransLog transLog = null;
			String logPath = config.getInDir() + File.separator + "trans.log";
			File file = new File(logPath);
			if (!file.exists()) {
				List<TransLog.Log> logs = new ArrayList<>();
				logs.add(log);
				transLog = new TransLog(logs);
			} else {
				FileInputStream fileInput = new FileInputStream(logPath);
				ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				IOUtils.copy(fileInput, byteOut);
				fileInput.close();
				transLog = SerializeUtil.deserialize(byteOut.toByteArray(), TransLog.class);
				for (TransLog.Log lg : transLog.getFileLogs()) {
					if (lg.getFileId().equals(fileInfo.getFileId())) {
						return transLog;
					}
				}
				transLog.getFileLogs().add(log);
			}
			// 重新生成日志文件
			FileOutputStream fileOut = new FileOutputStream(logPath);
			byte[] data = SerializeUtil.serialize(transLog);
			fileOut.write(data);
			fileOut.flush();
			fileOut.close();
			return transLog;
		}
	}

	/**
	 * 根据ID，删除日志中的记录
	 * 
	 * @param fileId
	 * @throws Exception
	 */
	public void removeLog(String fileId) throws Exception {
		synchronized (getClass()) {
			String logPath = config.getInDir() + File.separator + "trans.log";
			File file = new File(logPath);
			if (file.exists()) {
				FileInputStream fileInput = new FileInputStream(logPath);
				ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				IOUtils.copy(fileInput, byteOut);
				fileInput.close();
				TransLog transLog = SerializeUtil.deserialize(byteOut.toByteArray(), TransLog.class);
				for (TransLog.Log lg : transLog.getFileLogs()) {
					if (lg.getFileId().equals(fileId)) {
						transLog.getFileLogs().remove(lg);
						break;
					}
				}
				// 重新生成日志文件
				FileOutputStream fileOut = new FileOutputStream(logPath);
				byte[] data = SerializeUtil.serialize(transLog);
				fileOut.write(data);
				fileOut.flush();
				fileOut.close();
			}
		}
	}

	/**
	 * 根据ID获取日志
	 * 
	 * @param fileId
	 * @return
	 * @throws Exception
	 */
	public TransLog.Log getFileLog(String fileId) throws Exception {
		String logPath = config.getInDir() + File.separator + "trans.log";
		File file = new File(logPath);
		if (file.exists()) {
			FileInputStream fileInput = new FileInputStream(logPath);
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			IOUtils.copy(fileInput, byteOut);
			fileInput.close();
			TransLog transLog = SerializeUtil.deserialize(byteOut.toByteArray(), TransLog.class);
			for (TransLog.Log lg : transLog.getFileLogs()) {
				if (lg.getFileId().equals(fileId)) {
					return lg;
				}
			}
		}
		return null;
	}

	private TransLog.Log fileInfo2Log(FileInfo fileInfo) {
		TransLog.Log log = new TransLog.Log();
		log.setFileId(fileInfo.getFileId());
		log.setFileTitle(fileInfo.getTitle());
		log.setFileSize(fileInfo.getSize());
		log.setMd5(fileInfo.getMd5());
		String filePath = config.getInDir() + File.separator + fileInfo.getTitle() + ".tmp";
		log.setFilePath(filePath);
		return log;
	}

	/**
	 * 写入分片数据，并计算出下一个分片请求,此方法线程安全
	 * 
	 * @param transData
	 * @param callBack
	 * @return
	 * @throws Exception
	 */
	public TransResult writeSplit(TransData transData) throws Exception {
		RandomAccessFile randomAccessFile = null;
		try {
			LockUtil.instance().lock(transData.getFileId());
			File file = getInFile(transData.getFileId());
			if (file == null) {
				throw new Exception("未找到接收文件:" + transData.getFileId());
			}
			randomAccessFile = new RandomAccessFile(file, "rw");
			randomAccessFile.seek(transData.getBeginPos());
			randomAccessFile.write(transData.getData());

			long endPos = transData.getBeginPos() + transData.getData().length;
			LOG.debug("写文件[" + transData.getBeginPos() + "," + endPos + "),分片末位为:" + transData.getEndPos());
			// 当前的文件分片是否已经传送完成
			boolean splitComplete = (endPos == transData.getEndPos() + 1);
			TransSplit require = null;
			if (!splitComplete) {
				require = new TransSplit();
				require.setTransId(transData.getTransId());
				require.setFileId(transData.getFileId());
				require.setBeginPos(endPos);
				require.setEndPos(transData.getEndPos());
				LOG.debug("请求文件[" + require.getBeginPos() + "," + require.getEndPos() + "]");
			} else {
				LOG.debug("分片传输完成！");
			}

			TransLog.Log log = getFileLog(transData.getFileId());
			randomAccessFile.seek(log.getFileSize());
			byte[] fileInfoByte = new byte[(int) (file.length() - log.getFileSize())];
			randomAccessFile.read(fileInfoByte);
			FileInfo fileInfo = SerializeUtil.deserialize(fileInfoByte, FileInfo.class);

			// 重新计算传输入信息
			for (FileSplit split : fileInfo.getSplits()) {
				if (split.getBeginPos() <= transData.getBeginPos() && split.getEndPos() >= transData.getEndPos()) {
					split.setTransed(split.getTransed() + transData.getData().length);
				}
			}

			// 重新写入文件
			fileInfoByte = SerializeUtil.serialize(fileInfo);
			randomAccessFile.seek(log.getFileSize());
			randomAccessFile.write(fileInfoByte);
			randomAccessFile.setLength(log.getFileSize() + fileInfoByte.length);

			boolean complete = true;
			for (FileSplit split : fileInfo.getSplits()) {
				if ((split.getEndPos() - split.getBeginPos() + 1) != split.getTransed()) {
					complete = false;
				}
			}
			// 所有分片传输完成
			if (complete) {
				randomAccessFile.setLength(log.getFileSize());
				removeLog(transData.getFileId());
				// 关闭后才能重命名成功！
				randomAccessFile.close();
				String newPath = log.getFilePath().substring(0, log.getFilePath().length() - 4);
				new File(log.getFilePath()).renameTo(new File(newPath));
				LOG.info("###############文件" + transData.getFileId() + "传输完成！###############");
			}

			TransResult result = new TransResult(transData.getFileId(), fileInfo, require, complete);
			if (config.getTransReport() != null) {
				config.getTransReport().report(fileInfo, result);
			}
			return result;
		} finally {
			if (randomAccessFile != null) {
				try {
					randomAccessFile.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			LockUtil.instance().unlock(transData.getFileId());
		}
	}

	/**
	 * 根据传入FileInfo计算需要断续下载的TransInfo,可用于断点续传
	 * 
	 * @param fileInfo
	 * @return
	 * @throws Exception
	 */
	public TransInfo getTransInfo(FileInfo fileInfo) throws Exception {
		File inFile = getInFile(fileInfo.getFileId());
		if (inFile == null) {
			createInFile(fileInfo);
			TransInfo transInfo = new TransInfo();
			transInfo.setTransId(fileInfo.getTransId());
			transInfo.setFileId(fileInfo.getFileId());
			List<TransSplit> splits = new ArrayList<>();
			for (FileSplit split : fileInfo.getSplits()) {
				splits.add(new TransSplit(fileInfo.getTransId(), fileInfo.getFileId(), split.getBeginPos(),
						split.getEndPos()));
			}
			transInfo.setSplits(splits);
			return transInfo;
		} else {
			RandomAccessFile randomAccessFile = new RandomAccessFile(inFile, "r");
			TransLog.Log log = getFileLog(fileInfo.getFileId());
			randomAccessFile.seek(log.getFileSize());
			byte[] fileInfoByte = new byte[(int) (inFile.length() - log.getFileSize())];
			randomAccessFile.read(fileInfoByte);
			randomAccessFile.close();

			fileInfo = SerializeUtil.deserialize(fileInfoByte, FileInfo.class);
			TransInfo transInfo = new TransInfo();
			transInfo.setTransId(fileInfo.getTransId());
			transInfo.setFileId(fileInfo.getFileId());
			List<TransSplit> splits = new ArrayList<>();
			for (FileSplit split : fileInfo.getSplits()) {
				long beginPos = split.getBeginPos() + split.getTransed();
				// 传输未完成
				if (beginPos < split.getEndPos()) {
					TransSplit transSplit = new TransSplit(fileInfo.getTransId(), fileInfo.getFileId(), beginPos,
							split.getEndPos());
					// 重新计算起点
					splits.add(transSplit);
				}
			}
			if (splits.size() == 0) {
				return null;
			}
			transInfo.setSplits(splits);
			return transInfo;
		}
	}

	public String getInFilePath(String fileId) throws Exception {
		String transPath = config.getInDir() + File.separator + "trans.log";
		File transFile = new File(transPath);
		if (transFile.exists()) {
			FileInputStream fileInput = new FileInputStream(transFile);
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			IOUtils.copy(fileInput, byteOut);
			fileInput.close();
			TransLog fileInfo = SerializeUtil.deserialize(byteOut.toByteArray(), TransLog.class);
			for (TransLog.Log log : fileInfo.getFileLogs()) {
				if (log.getFileId().equals(fileId)) {
					return log.getFilePath();
				}
			}
		}
		return null;
	}

	public TransData readSplit(TransSplit split) throws Exception {
		File outFile = getOutFile(split.getFileId());
		if (outFile != null) {
			TransData transData = new TransData();
			transData.setTransId(split.getTransId());
			transData.setFileId(split.getFileId());
			RandomAccessFile randomAccessFile = new RandomAccessFile(outFile, "r");
			randomAccessFile.seek(split.getBeginPos());

			// 计算实际要读取的长度
			long readLength = config.getTransSize();
			// 需要取的长度小于固定长度时，取实际长度，注意下标相减需要加1
			if ((split.getEndPos() - split.getBeginPos() + 1) < config.getTransSize()) {
				readLength = split.getEndPos() - split.getBeginPos() + 1;
			}
			byte[] bytes = new byte[(int) readLength];
			int readSize = randomAccessFile.read(bytes);
			if (readSize <= 0) {
				randomAccessFile.close();
				return null;
			}
			transData.setBeginPos(split.getBeginPos());
			transData.setEndPos(split.getEndPos());
			transData.setData(bytes);
			randomAccessFile.close();
			return transData;
		}
		return null;
	}

	/**
	 * 根据fileId获取传出文件,fileId代表文件标识，不一定为文件路径<br/>
	 * 
	 * @param fileId 文件ID
	 * @return
	 * @throws Exception 
	 */
	public File getOutFile(String fileId) throws Exception {
		if (config.getFileIdParser() == null) {
			throw new Exception("文件ID解析器未实现");
		}
		return config.getFileIdParser().getFile(fileId);
	}

	public FileInfo getOutFileInfo(String fileId) throws Exception {
		File file = getOutFile(fileId);
		if (file == null) {
			throw new Exception("fileId:" + fileId + " 对应的文件不存在！");
		}
		FileInfo fileInfo = getFileInfo(file);
		fileInfo.setFileId(fileId);
		return fileInfo;
	}

	/**
	 * 根据URL计算文件分片及MD5信息
	 * 
	 * @return
	 * @throws Exception
	 */
	public FileInfo getFileInfo(File file) throws Exception {
		FileInfo fileInfo = new FileInfo();
		fileInfo.setTitle(file.getName());
		fileInfo.setMd5(calcMD5(file));
		fileInfo.setTransId(fileInfo.getMd5());
		fileInfo.setFileId(file.getName());
		fileInfo.setSize(file.length());
		int splitSize = config.getSplitSize();
		int count = (int) file.length() % splitSize;
		if (count == 0) {
			count = (int) file.length() / splitSize;
		} else {
			count = (int) file.length() / splitSize + 1;
		}
		List<FileSplit> splits = new ArrayList<>();
		for (int index = 0; index < count; index++) {
			FileSplit split = new FileSplit();
			split.setFileId(fileInfo.getFileId());
			split.setTransed(0);
			split.setIndex(index);
			split.setBeginPos(index * splitSize);
			if ((index + 1) * splitSize > file.length()) {
				split.setEndPos(file.length() - 1);
			} else {
				split.setEndPos((index + 1) * splitSize - 1);
			}
			splits.add(split);
		}
		fileInfo.setSplits(splits);
		return fileInfo;
	}

	/**
	 * 计算文件的MD5信息
	 * 
	 * @return
	 * @throws Exception
	 */
	public static String calcMD5(File file) throws Exception {
		FileInputStream inputStream = new FileInputStream(file);
		String md5 = DigestUtils.md5Hex(inputStream);
		inputStream.close();
		return md5;
	}

	public static String getMD5(String src) {
		return DigestUtils.md5Hex(src.getBytes());
	}

}
