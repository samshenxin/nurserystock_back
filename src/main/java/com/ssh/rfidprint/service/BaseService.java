package com.ssh.rfidprint.service;
import java.io.Serializable;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;

import com.ssh.rfidprint.common.Pager;
/**
 * Service接口 - Service接口基类
 */
public interface BaseService<T, PK extends Serializable> {
    /**
     * 根据ID获取实体对象.
     * 
     * @param id
     *            记录ID
     * @return 实体对象
     */
    public T get(PK id);

    /**
     * 根据ID获取实体对象.
     * 
     * @param id
     *            记录ID
     * @return 实体对象
     */
    public T load(PK id);

    /**
     * 根据ID数组获取实体对象集合.
     * 
     * @param ids
     *            ID对象数组
     * @return 实体对象集合
     */
    public List<T> get(PK[] ids);

    /**
     * 根据属性名和属性值获取实体对象.
     * 
     * @param propertyName
     *            属性名称
     * @param value
     *            属性值
     * @return 实体对象
     */
    public T get(String propertyName, Object value);

    /**
     * 根据属性名和属性值获取实体对象集合.
     * 
     * @param propertyName
     *            属性名称
     * @param value
     *            属性值
     * @return 实体对象集合
     */
    public List<T> getList(String propertyName, Object value);

    /**
     * 获取所有实体对象集合.
     * 
     * @return 实体对象集合
     */
    public List<T> getAll();

    /**
     * 获取所有实体对象总数.
     * 
     * @return 实体对象总数
     */
    public Long getTotalCount();

    /**
     * 根据属性名、修改前后属性值判断在数据库中是否唯一.
     * 
     * @param propertyName
     *            属性名称
     * @param oldValue
     *            修改前的属性值
     * @param oldValue
     *            修改后的属性值
     * @return <tt>true</tt> 新修改的值与原来值相等<br/>
     *         <tt>false</tt> 新修改的值与原来值不相等<br/>
     */
    public boolean isUnique(String propertyName, Object oldValue, Object newValue);

    /**
     * 根据属性名判断数据是否已存在.
     * 
     * @param propertyName
     *            属性名称
     * @param value
     *            属性值
     * @return boolean <tt>true</tt> 已经存在<br/>
     *         <tt>false</tt> 不存在<br/>
     */
    public boolean isExist(String propertyName, Object value);

    /**
     * 保存实体对象.
     * 
     * @param entity
     *            对象
     * @return ID
     */
    public PK save(T entity);

    /**
     * 更新实体对象.
     * 
     * @param entity
     *            对象
     */
    public void saveOrUpdate(T entity);

    /**
     * 删除实体对象.
     * 
     * @param entity
     *            对象
     */
    public void delete(T entity);

    /**
     * 根据ID删除实体对象.
     * 
     * @param id
     *            记录ID
     */
    public void delete(PK id);

    /**
     * 根据ID数组删除实体对象.
     * 
     * @param ids
     *            ID数组
     */
    public void delete(PK[] ids);

    /**
     * 刷新session.
     */
    public void flush();

    /**
     * 清除Session.
     */
    public void clear();

    /**
     * 清除某一对象.
     * 
     * @param object
     *            需要清除的对象
     */
    public void evict(Object object);

    /**
     * 根据Page对象进行查询(提供分页、查找、排序功能).
     * 
     * @param page
     *            Page对象
     * @return Page对象
     */
    public Pager findByPager(Pager pager);

    /**
     * 根据Pager和DetachedCriteria对象进行查询(提供分页、查找、排序功能).
     * 
     * @param pager
     *            Pager对象
     * @return Pager对象
     */
    public Pager findByPager(Pager pager, DetachedCriteria detachedCriteria);

    /**
     * 根据DetachedCriteria对象进行查询
     * 
     * @param detachedCriteria
     * @return List
     */
    public List<T> getList(DetachedCriteria detachedCriteria);

    /**
     * 列表查询接口
     * 
     * @param hsql
     *            统计语句
     * @param Object
     *            [] values 问号占位符的值的数据组
     * @return List
     */
    public List<?> getList(final String hsql, final Object[] values);

    /**
     * 列表查询接口
     * 
     * @param hsql
     *            统计语句
     * @param Map
     *            <String, Object> params 名称占位符的名称与值
     * @return List
     */
    public List<?> getList(final String hsql, final Object[] params, final int maxResults);

    /**
     * <p>
     * Title: getAll
     * </p>
     * <p>
     * Description: 获取全部对象, 支持按属性行序.
     * </p>
     * 
     * @param orderByProperty
     * @param orderByType
     * @return List<T>
     */
    public List<T> getAll(String orderByProperty, String orderByType);

    /**
     * 根据数据库表对象，判断对应列名中是否存在对应值
     * 
     * @param model
     *            数据库表对象
     * @param propertyName
     *            列名
     * @param value
     *            列名值
     * @return <tt>true</tt> 可以删除<br/>
     *         <tt>false</tt> 不可以删除
     */
    public boolean isDelete(Class<?> model, String propertyName, Object value);

    /**
     * 根据DetachedCriteria对象进行查询
     * 
     * @param detachedCriteria
     *            相关查询条件
     * @param maxResults
     *            返回最大数量
     * @return 列表对象
     */
    public List<T> getList(DetachedCriteria detachedCriteria, int maxResults);

    /**
     * 根据SQL语句分页查询
     * 
     * @param string
     * @param pager
     * @return
     */
    public Pager getListForPager(String string, Pager pager);
    /**
     *根据SQL语句删除表数据
     */
    public void deleteBySql(String hql);
    
}
