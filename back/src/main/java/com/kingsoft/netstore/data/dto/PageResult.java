package com.kingsoft.netstore.data.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PageResult<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	private long total;
	private long pageSize;
	private long pageNO;
	private long totalPage;
	private List<T> data = new ArrayList<T>();

	public PageResult(int total, int pageSize, int pageNO, List<T> data) {
		this.total = total;
		this.pageSize = pageSize;
		this.pageNO = pageNO;
		this.data = data;
	}

	public PageResult() {
		super();
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public long getPageSize() {
		return pageSize;
	}

	public void setPageSize(long pageSize) {
		this.pageSize = pageSize;
	}

	public long getPageNO() {
		return pageNO;
	}

	public void setPageNO(long pageNO) {
		this.pageNO = pageNO;
	}

	public List<T> getData() {
		return data;
	}

	public void setData(List<T> data) {
		this.data = data;
	}

	public long getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(long totalPage) {
		this.totalPage = totalPage;
	}
}
