package com.kingsoft.netstore.domain;

import java.io.Serializable;

/**
 * 传输协议
 * 
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月1日
 */
public class Protocol implements Serializable {

	private static final long serialVersionUID = 1L;

	private Cmd cmd;

	// 只能是Object类型，否则反序列化时无法向下转型到子类型
	private Object data;

	public Protocol() {
		super();
	}

	public Protocol(Cmd cmd, Object data) {
		super();
		this.cmd = cmd;
		this.data = data;
	}

	public Cmd getCmd() {
		return cmd;
	}

	public void setCmd(Cmd cmd) {
		this.cmd = cmd;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
