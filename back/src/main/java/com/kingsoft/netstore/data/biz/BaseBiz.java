package com.kingsoft.netstore.data.biz;


import com.kingsoft.netstore.data.dao.BaseDao;
import com.kingsoft.netstore.data.dto.PageResult;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface BaseBiz<Dao extends BaseDao<T>, T> {
    /**
     * 保存数据对象
     *
     * @param entity
     * @return
     */
    T createEntity(T entity);


    /**
     * 批量保存数据对象
     *
     * @param entityList
     * @return
     */
    List<T> createEntityAll(List<T> entityList);

    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    T readEntity(Serializable id);

    /**
     * 删除数据
     *
     * @param entity
     */
    boolean deleteEntity(T entity);

    /**
     * 根据表的id删除数据
     *
     * @param id
     */
    boolean deleteEntity(Serializable id);

    /**
     * 更新对象
     *
     * @param entity
     * @return
     */
    boolean updateEntity(T entity);

    /**
     * 列出所有的对象
     *
     * @return
     */
    public List<T> listEntities();

    public T getEntityByJpql(String jpql, Object... args);

    public T getEntityBySql(String sql, Object... args);

    public List<T> getEntitiesByJpql(String jpql, Object... args);

    public List<T> getEntitiesBySql(String sql, Object... args);

    public PageResult<T> getPageResultByJpql(String jpql, int pageNO, int pageSize, Object... args);

    public PageResult<T> getPageResultBySql(String sql, int pageNO, int pageSize, Object... args);

    public PageResult<Map<String, Object>> getMapPageResultBySql(String sql, int pageNO, int pageSize, Object... args);

    public void doEntityByJpql(String jpql, Object... args);

    public void doEntityBySql(String sql, Object... args);

    public List<Map<String, Object>> getMapsBySql(String sql, Object... args);

}
