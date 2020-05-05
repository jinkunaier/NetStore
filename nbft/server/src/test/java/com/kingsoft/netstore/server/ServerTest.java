package com.kingsoft.netstore.server;

import com.kingsoft.netstore.TransConfig;
import com.kingsoft.netstore.impl.SampleFileIdParser;

/**
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月1日
 */
public class ServerTest {

	public static void main(String[] args) {
		String inDir = "D:\\nbft_dir\\inFiles";
		String outDir = "D:\\nbft_dir\\outFiles";
		TransConfig config = new TransConfig(inDir, new SampleFileIdParser(outDir));
		NbftServer server = new NbftServer(config);
		server.bing(9527);
	}
}
