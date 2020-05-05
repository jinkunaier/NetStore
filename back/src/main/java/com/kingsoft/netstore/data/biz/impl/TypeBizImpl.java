package com.kingsoft.netstore.data.biz.impl;

import java.util.List;

import com.kingsoft.netstore.data.biz.TypeBiz;
import com.kingsoft.netstore.data.dao.TypeDao;
import com.kingsoft.netstore.data.entity.Type;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class TypeBizImpl extends BaseBizImpl<Type, TypeDao> implements TypeBiz {

	@Override
	public List<Type> getChildrenClassifyAndId(String classify, String id) {
		List<Type> children = this.getEntitiesByJpql(
				"from CType c where  c.classify= ?0 and c.parent.id = ?1 order by orderNO asc", classify, id);
		for (Type child : children) {
			child.setChildren(this.getChildrenClassifyAndId(classify, child.getId()));
		}
		return children;
	}

}
