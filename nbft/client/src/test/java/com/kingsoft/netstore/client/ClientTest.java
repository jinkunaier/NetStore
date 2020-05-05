package com.kingsoft.netstore.client;

import java.util.concurrent.Future;

import com.kingsoft.netstore.TransConfig;
import com.kingsoft.netstore.impl.SampleFileIdParser;

/**
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月1日
 */
public class ClientTest {

	public static void main(String[] args) {
		NbftClient client = null;
		try {
			String fileId = "FSCapture.zip";
			String inDir = "D:\\nbft_dir\\inFiles";
			String outDir = "D:\\nbft_dir\\outFiles";
			TransConfig config = new TransConfig(inDir, new SampleFileIdParser(outDir));
			client = new NbftClient(config);
			client.connect("127.0.0.1", 9527);
			Future<String> future = client.download(fileId, null);
			Object obj = future.get();
			System.out.println(obj);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (client != null) {
				client.destroy();
			}
		}
	}
}
