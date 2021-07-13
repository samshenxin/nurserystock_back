package com.ssh.rfidprint.service;
import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.DetachedCriteria;

import com.ssh.rfidprint.common.Pager;
import com.ssh.rfidprint.dto.BaseDao;

import kd.bos.db.tx.Transactional;
/**
 * Service实现类 - Service实现类基类
 */
@Transactional
public class BaseServiceImpl<T, PK extends Serializable> implements BaseService<T, PK> {
    protected Log          logger = LogFactory.getLog(this.getClass());
    private BaseDao<T, PK> baseDao;

    /**
     * 返回dao基类对象.
     * 
     * @return dao基类对象
     */
    public BaseDao<T, PK> getBaseDao() {
        return baseDao;
    }

    /**
     * 设置dao基类对象.
     * 
     * @param baseDao
     *            dao基类对象
     */
    public void setBaseDao(BaseDao<T, PK> baseDao) {
        this.baseDao = baseDao;
    }

    /**
     * 根据ID获取实体对象.
     * 
     * @param id
     *            记录ID
     * @return 实体对象
     */
    public T get(PK id) {
        return baseDao.get(id);
    }

    /**
     * 根据ID获取实体对象.
     * 
     * @param id
     *            记录ID
     * @return 实体对象
     */
    public T load(PK id) {
        return baseDao.load(id);
    }

    /**
     * 根据ID数组获取实体对象集合.
     * 
     * @param ids
     *            ID对象数组
     * @return 实体对象集合
     */
    public List<T> get(PK[] ids) {
        return baseDao.get(ids);
    }

    /**
     * 根据属性名和属性值获取实体对象.
     * 
     * @param propertyName
     *            属性名称
     * @param value
     *            属性值
     * @return 实体对象
     */
    public T get(String propertyName, Object value) {
        return baseDao.get(propertyName, value);
    }

    /**
     * 根据属性名和属性值获取实体对象集合.
     * 
     * @param propertyName
     *            属性名称
     * @param value
     *            属性值
     * @return 实体对象集合
     */
    public List<T> getList(String propertyName, Object value) {
        return baseDao.getList(propertyName, value);
    }

    /**
     * 获取所有实体对象集合.
     * 
     * @return 实体对象集合
     */
    public List<T> getAll() {
        try {
            return baseDao.getAll();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * 获取所有实体对象总数.
     * 
     * @return 实体对象总数
     */
    public Long getTotalCount() {
        // System.out.println("********************************");
        return baseDao.getTotalCount();
    }

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
    public boolean isUnique(String propertyName, Object oldValue, Object newValue) {
        return baseDao.isUnique(propertyName, oldValue, newValue);
    }

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
    public boolean isExist(String propertyName, Object value) {
        return baseDao.isExist(propertyName, value);
    }

    /**
     * 保存实体对象，并返回主键ID值.
     * 
     * @param entity
     *            对象
     * @return ID
     */
    public PK save(T entity) {
        return baseDao.save(entity);
    }

    /**
     * 保存并更新实体对象.
     * 
     * @param entity
     *            对象
     */
    public void saveOrUpdate(T entity) {
        baseDao.saveOrUpdate(entity);
    }

    /**
     * 删除实体对象.
     * 
     * @param entity
     *            对象
     */
    public void delete(T entity) {
        baseDao.delete(entity);
    }

    /**
     * 根据ID删除实体对象.
     * 
     * @param id
     *            记录ID
     */
    public void delete(PK id) {
        baseDao.delete(id);
    }

    /**
     * 根据ID数组删除实体对象.
     * 
     * @param ids
     *            ID数组
     */
    public void delete(PK[] ids) {
        baseDao.delete(ids);
    }

    /**
     * 刷新session.
     */
    public void flush() {
        baseDao.flush();
    }

    /**
     * 清除Session.
     */
    public void clear() {
        baseDao.clear();
    }

    /**
     * 清除某一对象.
     * 
     * @param object
     *            需要清除的对象
     */
    public void evict(Object object) {
        baseDao.evict(object);
    }

    /**
     * 根据Pager对象进行查询(提供分页、查找、排序功能).
     * 
     * @param pager
     *            Pager对象
     * @return Pager对象
     */
    public Pager findByPager(Pager pager) {
        return baseDao.findByPager(pager);
    }

    /**
     * 根据Pager和DetachedCriteria对象进行查询(提供分页、查找、排序功能).
     * 
     * @param pager
     *            Pager对象
     * @return Pager对象
     */
    public Pager findByPager(Pager pager, DetachedCriteria detachedCriteria) {
        return baseDao.findByPager(pager, detachedCriteria);
    }

    /**
     * 根据DetachedCriteria对象进行查询
     * 
     * @param detachedCriteria
     *            相关查询条件
     * @return 列表对象
     */
    public List<T> getList(DetachedCriteria detachedCriteria) {
        return baseDao.getList(detachedCriteria);
    }

    /**
     * <p>
     * Title: getAll
     * </p>
     * <p>
     * Description: TODO(获取全部对象, 支持按属性行序.)
     * </p>
     * 
     * @param orderByProperty
     * @param orderByType
     * @return List<T>
     */
    public List<T> getAll(String orderByProperty, String orderByType) {
        return baseDao.getAll(orderByProperty, orderByType);
    }

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
    public boolean isDelete(Class<?> model, String propertyName, Object value) {
        return baseDao.isDelete(model, propertyName, value);
    }

    public List<?> getList(String hsql, Object[] params, int maxResults) {
        return baseDao.getList(hsql, params, maxResults);
    }

    public List<?> getList(String hsql, Object[] values) {
        return baseDao.getList(hsql, values);
    }

    public List<T> getList(DetachedCriteria detachedCriteria, int maxResults) {
        return baseDao.getList(detachedCriteria, maxResults);
    }

    /**
     * 根据SQL语句分页查询
     * 
     * @param string
     * @param pager
     * @return
     */
    public Pager getListForPager(String hql, Pager pager) {
        // TODO Auto-generated method stub
        return baseDao.getListForPager(hql, pager);
    }

    @Override
    public void deleteBySql(String hql) {
        baseDao.deleteBySql(hql);
        
    }
}
