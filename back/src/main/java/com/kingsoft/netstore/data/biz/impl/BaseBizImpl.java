package com.kingsoft.netstore.data.biz.impl;

import cn.hutool.core.lang.UUID;
import com.kingsoft.netstore.data.biz.BaseBiz;
import com.kingsoft.netstore.data.dao.BaseDao;
import com.kingsoft.netstore.data.dto.PageResult;
import com.kingsoft.netstore.utils.ReflectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@SuppressWarnings("deprecation")
@Transactional
public class BaseBizImpl<T, DAO extends BaseDao<T>> implements BaseBiz<DAO, T> {

	protected Logger LOG = LoggerFactory.getLogger(getClass());

	@Autowired(required = true)
	protected DAO baseDao;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public T createEntity(T entity) {
		validateId(entity);
		return baseDao.save(entity);
	}

	@Override
	public List<T> createEntityAll(List<T> entityList) {
		return baseDao.saveAll(entityList);
	}

	/**
	 * 如果是手动设置ID的，这里判断没有ID就自动设置一个。<br/>
	 * 
	 * @param entity
	 */
	private void validateId(Object entity) {
		Class<T> clazz = ReflectionUtils.getSuperClassGenericType(getClass());
		try {
			Field field = clazz.getDeclaredField("id");
			if (field != null) {
				GeneratedValue anno = field.getAnnotation(GeneratedValue.class);
				if (anno == null) {// 表明此时需要手动设置ID
					ReflectionUtils.makeAccessible(field);
					String id = (String) field.get(entity);
					if (StringUtils.isEmpty(id)) {
						field.set(entity, UUID.randomUUID().toString());
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("实体类" + clazz.getName() + "的ID辅助生成失败，检查ID映射是否符合规范.");
		}
	}

	@Override
	public T readEntity(Serializable id) {
		Optional<T> optional = baseDao.findById(id);
		return optional.isPresent() ? optional.get() : null;
	}

	@Override
	public boolean deleteEntity(T entity) {
		baseDao.delete(entity);
		return true;
	}

	@Override
	public boolean updateEntity(T entity) {
		baseDao.save(entity);
		return true;
	}

	@Override
	public boolean deleteEntity(Serializable id) {
		T entity = readEntity(id);
		if (entity != null) {
			deleteEntity(entity);
		}
		return true;
	}

	@Override
	public T getEntityByJpql(String jpql, Object... args) {
		List<T> result = getEntitiesByJpql(jpql, args);
		if (result.size() > 0) {
			return result.get(0);
		}
		return null;
	}

	@Override
	public List<T> getEntitiesByJpql(String jpql, Object... args) {
		Class<T> clazz = ReflectionUtils.getSuperClassGenericType(getClass());
		TypedQuery<T> query = entityManager.createQuery(jpql, clazz);
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				query.setParameter(i, args[i]);
			}
		}
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> getEntitiesBySql(String sql, Object... args) {
		Class<T> clazz = ReflectionUtils.getSuperClassGenericType(getClass());
		Query query = entityManager.createNativeQuery(sql, clazz);
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				query.setParameter(i, args[i]);
			}
		}
		return query.getResultList();
	}

	@Override
	public T getEntityBySql(String sql, Object... args) {
		List<T> result = getEntitiesBySql(sql, args);
		if (result.size() > 0) {
			return result.get(0);
		}
		return null;
	}

	@Override
	public void doEntityByJpql(String jpql, Object... args) {
		Query query = entityManager.createQuery(jpql);
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				query.setParameter(i, args[i]);
			}
		}
		query.executeUpdate();
	}

	@Override
	public PageResult<T> getPageResultByJpql(String jpql, int pageNO, int pageSize, Object... args) {
		throw new RuntimeException("暂未实现");
	}

	@SuppressWarnings("unchecked")
	private <B> PageResult<B> getPageResultBySql(String sql, Class<B> clazz, int pageNO, int pageSize, Object... args) {
		Query query = entityManager.createNativeQuery(sql, clazz);
		String countSql = "SELECT COUNT(1) AS CT FROM (" + sql + ") T_";
		Query countQuery = entityManager.createNativeQuery(countSql);
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				query.setParameter(i, args[i]);
				countQuery.setParameter(i, args[i]);
			}
		}
		query.setFirstResult((pageNO - 1) * pageSize);
		query.setMaxResults(pageSize);
		PageResult<B> pageResult = new PageResult<B>();
		String result = countQuery.getSingleResult().toString();
		pageResult.setTotal(Long.parseLong(result));
		pageResult.setPageNO(pageNO);
		pageResult.setPageSize(pageSize);
		String rt = countQuery.getSingleResult().toString();
		pageResult.setTotalPage((Long.parseLong(rt) + pageSize - 1) / pageSize);
		pageResult.setData(query.getResultList());
		return pageResult;
	}

	@Override
	public PageResult<T> getPageResultBySql(String sql, int pageNO, int pageSize, Object... args) {
		Class<T> clazz = ReflectionUtils.getSuperClassGenericType(getClass());
		PageResult<T> pageResult = getPageResultBySql(sql, clazz, pageNO, pageSize, args);
		return pageResult;
	}

	@SuppressWarnings("unchecked")
	@Override
	public PageResult<Map<String, Object>> getMapPageResultBySql(String sql, int pageNO, int pageSize, Object... args) {
		Query query = entityManager.createNativeQuery(sql);
		query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		String countSql = "SELECT COUNT(1) AS CT FROM (" + sql + ") T_";
		Query countQuery = entityManager.createNativeQuery(countSql);
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				query.setParameter(i, args[i]);
				countQuery.setParameter(i, args[i]);
			}
		}
		query.setFirstResult((pageNO - 1) * pageSize);
		query.setMaxResults(pageSize);
		PageResult<Map<String, Object>> pageResult = new PageResult<>();
		pageResult.setTotal(((BigDecimal) countQuery.getSingleResult()).longValue());
		pageResult.setPageNO(pageNO);
		pageResult.setPageSize(pageSize);
		pageResult.setData(query.getResultList());
		return pageResult;
	}

	@Override
	public void doEntityBySql(String sql, Object... args) {
		Query query = entityManager.createNativeQuery(sql);
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				query.setParameter(i, args[i]);
			}
		}
		query.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, Object>> getMapsBySql(String sql, Object... args) {
		Query query = entityManager.createNativeQuery(sql);
		query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				query.setParameter(i, args[i]);
			}
		}
		return query.getResultList();
	}

	@Override
	public List<T> listEntities() {
		Class<T> clazz = ReflectionUtils.getSuperClassGenericType(getClass());
		String jpql = "from " + clazz.getSimpleName();
		TypedQuery<T> query = entityManager.createQuery(jpql, clazz);
		return query.getResultList();
	}
}
