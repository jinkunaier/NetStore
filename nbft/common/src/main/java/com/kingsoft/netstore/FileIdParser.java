package com.kingsoft.netstore;

import java.io.File;

/**
 * 根据fileId返回对应的文件,对于文件发送方fileId可能是个业务主键，这里做个转换
 * 
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月17日
 */
public interface FileIdParser {

	public File getFile(String fileId);

}
