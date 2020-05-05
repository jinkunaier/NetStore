package com.kingsoft.netstore.data.biz;

import com.kingsoft.netstore.data.dao.TypeDao;
import com.kingsoft.netstore.data.entity.Type;

import java.util.List;

public interface TypeBiz extends BaseBiz<TypeDao, Type> {

	List<Type> getChildrenClassifyAndId(String classify, String string);

}
