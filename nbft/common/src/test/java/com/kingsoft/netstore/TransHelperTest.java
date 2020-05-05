package com.kingsoft.netstore;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;

import com.kingsoft.netstore.domain.FileInfo;
import com.kingsoft.netstore.domain.TransData;
import com.kingsoft.netstore.domain.TransInfo;
import com.kingsoft.netstore.domain.TransResult;
import com.kingsoft.netstore.domain.TransSplit;
import com.kingsoft.netstore.impl.SampleFileIdParser;

/**
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月6日
 */
public class TransHelperTest {

	private TransHelper helper;

	@Before
	public void initHelper() {
		String inDir = "D:\\nbft_dir\\inFiles";
		String outDir = "D:\\nbft_dir\\outFiles";
		TransConfig config = new TransConfig(inDir, new SampleFileIdParser(outDir));
		helper = new TransHelper(config);
	}

	/**
	 * 模拟下载
	 * 
	 * @throws Exception
	 */
	@Test
	public void download() throws Exception {
		// 客户端发送需要下载的文件ID
		String fileId = "Protege-5.5.0-win.zip";

		// 服务器端生成fileInfo
		FileInfo fileInfo = helper.getOutFileInfo(fileId);

		// 客户端根据接收服务器发回的fileInfo判断是否已经存在未下载完成的文件
		File inFile = helper.getInFile(fileInfo.getFileId());
		if (inFile == null) {
			// 生成临时上传文件
			inFile = helper.createInFile(fileInfo);
		}

		// 客户端根据fileInfo及本地情况生成待下载传的分片信息
		TransInfo transInfo = helper.getTransInfo(fileInfo);

		// 客户端生成线程池请求服务器数据
		ExecutorService service = Executors.newFixedThreadPool(1);
		for (TransSplit split : transInfo.getSplits()) {
			service.execute(new SplitCall(split));
		}
		System.in.read();
		service.shutdown();
	}

	/**
	 * 模拟上传<br/>
	 * 注意：该方法与下载代码一模一样，主要看注释，每行代码执行方不同
	 * 
	 * @throws Exception
	 */
	@Test
	public void upload() throws Exception {

		// 客户端发送需要下载的文件ID
		String fileId = "Protege-5.5.0-win.zip";

		// 客户端生成fileInfo
		FileInfo fileInfo = helper.getOutFileInfo(fileId);

		// 服务器判断是否已经存在未上传完成的文件
		File inFile = helper.getInFile(fileInfo.getFileId());
		if (inFile == null) {
			// 生成临时上传文件
			inFile = helper.createInFile(fileInfo);
		}

		// 服务器根据服务器上已经存的文件及fileInfo生成待上传的分片信息
		TransInfo transInfo = helper.getTransInfo(fileInfo);

		// 客户端生成线程池请求服务器数据
		ExecutorService service = Executors.newFixedThreadPool(8);
		for (TransSplit split : transInfo.getSplits()) {
			service.execute(new SplitCall(split));
		}
		System.in.read();
		service.shutdown();
	}

	class SplitCall implements Runnable {

		private TransSplit split;

		public SplitCall(TransSplit split) {
			super();
			this.split = split;
		}

		@Override
		public void run() {

			try {
				// 发送方根据require生成相应的数据分片
				TransData transData = helper.readSplit(split);

				// 接收方接收splitData并生成的分片请求
				TransResult result = helper.writeSplit(transData);

				split = result.getRequire();

				while (split != null) {
					// 发送方根据require生成相应的数据分片
					transData = helper.readSplit(split);

					if (transData != null) {
						// 接收方接收splitData并生成的分片请求
						result = helper.writeSplit(transData);

						split = result.getRequire();
					} else {
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
