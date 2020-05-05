package com.kingsoft.netstore.domain;

import java.io.Serializable;
import java.util.Map;

/**
 * @author jinkun
 * @email 465110675@qq.com
 * @date 2020年3月14日
 */
public class TransObj implements Serializable {

	private static final long serialVersionUID = 1L;

	protected String transId;

	protected Map<String, Object> transMeta;
	
	public TransObj() {
		super();
	}

	public TransObj(String transId, Map<String, Object> transMeta) {
		super();
		this.transId = transId;
		this.transMeta = transMeta;
	}

	public String getTransId() {
		return transId;
	}

	public void setTransId(String transId) {
		this.transId = transId;
	}

	public Map<String, Object> getTransMeta() {
		return transMeta;
	}

	public void setTransMeta(Map<String, Object> transMeta) {
		this.transMeta = transMeta;
	}
}
