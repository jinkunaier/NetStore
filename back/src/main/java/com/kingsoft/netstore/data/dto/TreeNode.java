package com.kingsoft.netstore.data.dto;

import java.util.List;

/**
 * 通用树节点
 * 
 */
public class TreeNode {

	private String id;

	private String title;

	private List<TreeNode> children;
	/**
	 * 是否选中
	 */
	private Boolean checked;

	/**
	 * 如果是父亲节点，是事展开
	 */
	private Boolean expanded;
	/**
	 * 节点对应的数据
	 */
	private Object data;

	public TreeNode() {
		super();
	}

	public TreeNode(String id, String title, List<TreeNode> children, Object data) {
		super();
		this.id = id;
		this.title = title;
		this.children = children;
		this.data = data;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<TreeNode> getChildren() {
		return children;
	}

	public void setChildren(List<TreeNode> children) {
		this.children = children;
	}

	public Boolean getChecked() {
		return checked;
	}

	public void setChecked(Boolean checked) {
		this.checked = checked;
	}

	public Boolean getExpanded() {
		return expanded;
	}

	public void setExpanded(Boolean expanded) {
		this.expanded = expanded;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
