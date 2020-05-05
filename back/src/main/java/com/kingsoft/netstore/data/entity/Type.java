package com.kingsoft.netstore.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * 通用分类
 */
@Entity
@Table(name = "ns_t_type")
public class Type implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String text;
    // 类别
    private String classify;

    private Type parent;

    private List<Type> children;

    private boolean leaf;

    private boolean expanded;

    private String icon;

    private int orderNO;

    @Id
    @GenericGenerator(name = "system-uuid", strategy = "assigned")
    @GeneratedValue(generator = "system-uuid")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "classify", unique = false, nullable = true)
    public String getClassify() {
        return classify;
    }

    public void setClassify(String classify) {
        this.classify = classify;
    }

    @ManyToOne(cascade = {CascadeType.REFRESH}, optional = true)
    @JoinColumn(name = "parentId", foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT))
    @NotFound(action = NotFoundAction.IGNORE)
    @JsonIgnore
    public Type getParent() {
        return parent;
    }

    public void setParent(Type parent) {
        this.parent = parent;
    }

    @OneToMany(mappedBy = "parent", cascade = {CascadeType.REFRESH})
    @OrderBy("orderNO ASC")
    public List<Type> getChildren() {
        return children;
    }

    public void setChildren(List<Type> children) {
        this.children = children;
    }


    @Column(name = "text", unique = false, nullable = true)
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Column(name = "leaf", unique = false, nullable = true)
    public boolean isLeaf() {
        return leaf;
    }

    public void setLeaf(boolean leaf) {
        this.leaf = leaf;
    }

    @Column(name = "expanded", unique = false, nullable = true)
    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    @Column(name = "icon", unique = false, nullable = true)
    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Column(name = "orderNO", unique = false, nullable = true)
    public int getOrderNO() {
        return orderNO;
    }

    public void setOrderNO(int orderNO) {
        this.orderNO = orderNO;
    }
}
