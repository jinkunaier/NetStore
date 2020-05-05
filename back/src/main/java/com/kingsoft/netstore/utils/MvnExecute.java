package com.kingsoft.netstore.utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

public class MvnExecute {

	public static void main(String[] args) throws Exception {
		Logger logger = LoggerFactory.getLogger(MvnExecute.class);
		String basePath = args[0];
		logger.info("=============代码压缩=============");
		String jsPath = basePath + "/static";
		jsPath = jsPath.replaceAll("/", Matcher.quoteReplacement(File.separator));
		logger.info("脚本路径为：" + jsPath);
		Compressor compressor = new Compressor(jsPath);
		compressor.compress();
		logger.info("=============压缩完毕=============");
	}
}

class Compressor {

	Logger logger = LoggerFactory.getLogger(getClass());

	private String basePath;

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public Compressor(String basePath) {
		super();
		this.basePath = basePath;
	}

	public void compress() throws Exception {
		List<File> files = new ArrayList<File>();
		listAllFiles(new File(basePath), files);
		int index = 0;
		for (File file : files) {
			compressFile(file);
			logger.info("已经压缩代码:" + (++index) + "/" + files.size());
		}
	}

	private void compressFile(File file) throws Exception {
		Reader in = null;
		FileWriter out = null;
		try {
			in = new FileReader(file);
			JavaScriptCompressor compressor = new JavaScriptCompressor(in, new EReport(file));
			File minFile = new File(file.getAbsolutePath() + ".min");
			out = new FileWriter(minFile);
			compressor.compress(out, -1, true, true, false, false);
			out.flush();
			out.close();
			in.close();
			file.delete();
			minFile.renameTo(file);
		} catch (Exception e) {
			throw e;
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
	}

	private void listAllFiles(File file, List<File> files) {
		if (file.isDirectory()) {
			File[] subFiles = file.listFiles();
			for (File sub : subFiles) {
				if (sub.isDirectory()) {
					listAllFiles(sub, files);
				} else {
					if (sub.getName().endsWith(".js")) {
						files.add(sub);
					}
				}
			}
		} else {
			if (file.getName().endsWith(".js")) {
				files.add(file);
			}
		}
	}

	class EReport implements ErrorReporter {

		private File file;

		public EReport(File file) {
			this.file = file;
		}

		@Override
		public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
			if (logger.isInfoEnabled()) {
				logger.warn("\n[WARNING] in " + file.getAbsolutePath());
			}
			if (line < 0) {
				if (logger.isInfoEnabled()) {
					logger.warn("  " + message);
				}
			} else {
				if (logger.isInfoEnabled()) {
					logger.warn("  " + line + ':' + lineOffset + ':' + message);
				}
			}
		}

		@Override
		public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
			if (logger.isInfoEnabled()) {
				logger.error("[ERROR] in " + file.getAbsolutePath());
			}
			if (line < 0) {
				if (logger.isInfoEnabled()) {
					logger.error("  " + message);
				}
			} else {
				if (logger.isInfoEnabled()) {
					logger.error("  " + line + ':' + lineOffset + ':' + message);
				}
			}
		}

		@Override
		public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource,
				int lineOffset) {
			error(message, sourceName, line, lineSource, lineOffset);
			return new EvaluatorException(message);
		}
	}

}