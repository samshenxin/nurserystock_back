package com.ssh.rfidprint.dto;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.ssh.rfidprint.common.Pager;
import com.ssh.rfidprint.common.Pager.OrderType;

import oadd.org.apache.commons.lang3.StringUtils;

/**
 * 类<code>BaseDaoImpl</code> Dao实现类，提供基本操作方法
 * 
 * @since jdk 1.6
 */
@Repository
public class BaseDaoImpl<T, PK extends Serializable> implements BaseDao<T, PK> {
    private Class<T>          entityClass;
    protected SessionFactory  sessionFactory;
    private HibernateTemplate hibernateTemplate;
    protected Log             logger = LogFactory.getLog(this.getClass());

    /**
     * 获取HibernateTemplate
     * 
     * @return HibernateTemplate
     */
    public HibernateTemplate getHibernateTemplate() {
        if (hibernateTemplate == null) {
            hibernateTemplate = new HibernateTemplate(this.sessionFactory);
        }
        return hibernateTemplate;
    }

    /**
     * 设置HibernateTemplate
     * 
     * @param hibernateTemplate
     */
    public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
        this.hibernateTemplate = hibernateTemplate;
    }

    /**
     * 构造函数，初始化对应值
     */
    @SuppressWarnings("unchecked")
    public BaseDaoImpl() {
        this.entityClass = null;
        Class<?> c = getClass();
        Type type = c.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type[] parameterizedType = ((ParameterizedType) type).getActualTypeArguments();
            this.entityClass = (Class<T>) parameterizedType[0];
        }
    }

    /**
     * Spring 注入<tt>sessionFactory</tt>
     * 
     * @param sessionFactory
     */
    @Resource
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * 返回<tt>Session</tt>对象
     * 
     * @return <tt>Session</tt>对象
     */
    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    /**
     * 根据ID获取实体对象.
     * 
     * @param id
     *            记录ID
     * @return 实体对象
     */
    @SuppressWarnings("unchecked")
    public T get(PK id) {
        Assert.notNull(id, "id is required");
        return (T) getSession().get(entityClass, id);
    }

    /**
     * 根据ID获取实体对象.
     * 
     * @param id
     *            记录ID
     * @return 实体对象
     */
    @SuppressWarnings("unchecked")
    public T load(PK id) {
        Assert.notNull(id, "id is required");
        return (T) getSession().load(entityClass, id);
    }

    /**
     * 根据ID数组获取实体对象集合.
     * 
     * @param ids
     *            ID对象数组
     * @return 实体对象集合
     */
    @SuppressWarnings("unchecked")
    public List<T> get(PK[] ids) {
        Assert.notEmpty(ids, "ids must not be empty");
        String hql = "FROM " + entityClass.getName() + " as model where model.id in(:ids)";
        return getSession().createQuery(hql).setParameterList("ids", ids).list();
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
    @SuppressWarnings("unchecked")
    public T get(String propertyName, Object value) {
        Assert.hasText(propertyName, "propertyName must not be empty");
        Assert.notNull(value, "value is required");
        String hql = "FROM " + entityClass.getName() + " as model where model." + propertyName + " = ?";
        return (T) getSession().createQuery(hql).setParameter(0, value).uniqueResult();
    }
    
    /**
     * 根据属性名获取最大实体对象.
     * 
     * @param propertyName
     *            属性名称
     *
     * @return 实体对象
     */
    @SuppressWarnings("unchecked")
    public T getMax(String propertyName){
        String hql = "SELECT model FROM " +entityClass.getName() + " as model where model." + propertyName +"= (SELECT MAX(model2."+ propertyName +") FROM " +entityClass.getName() +" as model2)";
        return (T) getSession().createQuery(hql).uniqueResult();
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
    @SuppressWarnings("unchecked")
    public List<T> getList(String propertyName, Object value) {
        Assert.hasText(propertyName, "propertyName must not be empty");
        Assert.notNull(value, "value is required");
        String hql = "FROM " + entityClass.getName() + " as model where model." + propertyName + " = ?";
        return getSession().createQuery(hql).setParameter(0, value).list();
    }

    /**
     * 获取所有实体对象集合.
     * 
     * @return 实体对象集合
     */
    @SuppressWarnings("unchecked")
    public List<T> getAll() {
        String hql = "FROM " + entityClass.getName();
        return getSession().createQuery(hql).list();
    }

    /**
     * 获取所有实体对象总数.
     * 
     * @return 实体对象总数
     */
    public Long getTotalCount() {
        String hql = "SELECT COUNT(*) FROM " + entityClass.getName();
        return (Long) getSession().createQuery(hql).uniqueResult();
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
        Assert.hasText(propertyName, "propertyName must not be empty");
        Assert.notNull(newValue, "newValue is required");
        if (newValue == oldValue || newValue.equals(oldValue)) {
            return true;
        }
        if (newValue instanceof String) {
            if (oldValue != null && StringUtils.equalsIgnoreCase((String) oldValue, (String) newValue)) {
                return true;
            }
        }
        T object = get(propertyName, newValue);
        return (object == null);
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
        Assert.hasText(propertyName, "propertyName must not be empty");
        Assert.notNull(value, "value is required");
        T object = get(propertyName, value);
        return (object != null);
    }

    /**
     * 保存实体对象，并返回主键ID值.
     * 
     * @param entity
     *            对象
     * @return ID
     */
    @SuppressWarnings("unchecked")
    public PK save(T entity) {
        Assert.notNull(entity, "entity is required");
        return (PK) getSession().save(entity);
    }

    /**
     * 保存并更新实体对象.
     * 
     * @param entity
     *            对象
     */
    public void saveOrUpdate(T entity) {
        Assert.notNull(entity, "entity is required");
        getSession().saveOrUpdate(entity);
    }

    /**
     * 删除实体对象.
     * 
     * @param entity
     *            对象
     */
    public void delete(T entity) {
        Assert.notNull(entity, "entity is required");
        getSession().delete(entity);
    }

    /**
     * 根据ID删除实体对象.
     * 
     * @param id
     *            记录ID
     */
    public void delete(PK id) {
        Assert.notNull(id, "id is required");
        T entity = load(id);
        getSession().delete(entity);
    }

    /**
     * 根据ID数组删除实体对象.
     * 
     * @param ids
     *            ID数组
     */
    public void delete(PK[] ids) {
        Assert.notEmpty(ids, "ids must not be empty");
        for (PK id : ids) {
            T entity = load(id);
            getSession().delete(entity);
        }
    }

    /**
     * 刷新session.
     */
    public void flush() {
        getSession().flush();
    }

    /**
     * 清除Session.
     */
    public void clear() {
        getSession().clear();
    }

    /**
     * 清除某一对象.
     * 
     * @param object
     *            需要清除的对象
     */
    public void evict(Object object) {
        Assert.notNull(object, "object is required");
        getSession().evict(object);
    }

    /**
     * 根据Pager对象进行查询(提供分页、查找、排序功能).
     * 
     * @param pager
     *            Pager对象
     * @return Pager对象
     */
    public Pager findByPager(Pager pager) {
        if (pager == null) {
            pager = new Pager();
        }
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(entityClass);
        return findByPager(pager, detachedCriteria);
    }

    /**
     * 根据Pager和DetachedCriteria对象进行查询(提供分页、查找、排序功能).
     * 
     * @param pager
     *            Pager对象
     * @return Pager对象
     */
    public Pager findByPager(Pager pager, DetachedCriteria detachedCriteria) {
        if (pager == null) {
            pager = new Pager();
        }
        Integer pageNumber = pager.getPageNumber();
        Integer pageSize = pager.getPageSize();
        String property = pager.getProperty();
        String keyword = pager.getKeyword();
        String orderBy = pager.getOrderBy();
        OrderType orderType = pager.getOrderType();
        Criteria criteria = detachedCriteria.getExecutableCriteria(getSession());
        if (StringUtils.isNotEmpty(property) && StringUtils.isNotEmpty(keyword)) {
            String propertyString = "";
            if (property.contains(".")) {
                String propertyPrefix = StringUtils.substringBefore(property, ".");
                String propertySuffix = StringUtils.substringAfter(property, ".");
                criteria.createAlias(propertyPrefix, "model");
                propertyString = "model." + propertySuffix;
            } else {
                propertyString = property;
            }
            criteria.add(Restrictions.like(propertyString, "%" + keyword + "%"));
        }
        Integer totalCount = (Integer) criteria.setProjection(Projections.rowCount()).uniqueResult();
        criteria.setProjection(null);
        criteria.setResultTransformer(CriteriaSpecification.ROOT_ENTITY);
        criteria.setFirstResult((pageNumber - 1) * pageSize);
        criteria.setMaxResults(pageSize);
        if (StringUtils.isNotEmpty(orderBy) && orderType != null) {
            if (orderType == OrderType.asc) {
                criteria.addOrder(Order.asc(orderBy));
            } else {
                criteria.addOrder(Order.desc(orderBy));
            }
        }
        pager.setTotalCount(totalCount);
        pager.setResultList(criteria.list());
        return pager;
    }

    /**
     * 根据DetachedCriteria对象进行查询
     * 
     * @param detachedCriteria
     *            相关查询条件
     * @return 列表对象
     */
    @SuppressWarnings("unchecked")
    public List<T> getList(DetachedCriteria detachedCriteria) {
        Criteria criteria = detachedCriteria.getExecutableCriteria(getSession());
        criteria.setProjection(null);
        criteria.setResultTransformer(CriteriaSpecification.ROOT_ENTITY);
        return criteria.list();
    }

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
    @SuppressWarnings("unchecked")
    public List<T> getAll(String orderByProperty, String orderByType) {
        String hql = "FROM " + entityClass.getName() + " ORDER BY " + orderByProperty + " " + orderByType;
        return getSession().createQuery(hql).list();
    }

    /**
     * 列表查询接口
     * 
     * @param hsql
     *            统计语句
     * @param Object
     *            [] values 问号占位符的值的数据组
     * @return List
     */
    public List<?> getList(final String hsql, final Object[] values) {
        return this.getList(hsql, values, 0);
    }

    /**
     * 列表查询接口
     * 
     * @param hsql
     *            统计语句
     * @param Map
     *            <String, Object> params 名称占位符的名称与值
     * @return List
     */
    public List<?> getList(final String hsql, final Object[] params, final int maxResults) {
        Query query = getSession().createQuery(hsql.toString());
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                query.setParameter(i, params[i]);
            }
        }
        if (maxResults > 0) {
            query.setMaxResults(maxResults);
        }
        return query.list();
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
        Assert.hasText(propertyName, "propertyName must not be empty");
        Assert.notNull(value, "value is required");
        String hql = "SELECT COUNT(*) FROM " + model.getName() + " as model where model." + propertyName + " = ?";
        Long size = (Long) getSession().createQuery(hql).setParameter(0, value).uniqueResult();
        return (size == null || size == 0);
    }

    @SuppressWarnings("unchecked")
    public List<T> getList(DetachedCriteria detachedCriteria, int maxResults) {
        Criteria criteria = detachedCriteria.getExecutableCriteria(getSession()).setMaxResults(maxResults);
        criteria.setProjection(null);
        criteria.setResultTransformer(CriteriaSpecification.ROOT_ENTITY);
        return criteria.list();
    }

    /**
     * 根据SQL语句分页查询
     * 
     * @param string
     * @param pager
     * @return
     */
    @SuppressWarnings("unchecked")
    public Pager getListForPager(String hql, Pager pager) {
        if (pager == null) {
            pager = new Pager();
        }
        Integer pageNumber = pager.getPageNumber();
        Integer pageSize = pager.getPageSize();
        StatelessSession session = this.getHibernateTemplate().getSessionFactory().openStatelessSession();
        Query query = session.createSQLQuery(hql);
        Integer count = ((Integer) query.list().size()).intValue();
        query.setFirstResult((pageNumber - 1) * pageSize);
        query.setMaxResults(pageSize);
        List<Object> list = (List<Object>) query.list();
        pager.setTotalCount(count);
        pager.setResultList(list);
        session.close();
        return pager;
    }

    @Override
    public void deleteBySql(String hql) {
        StatelessSession session = this.getHibernateTemplate().getSessionFactory().openStatelessSession();
        SQLQuery query = session.createSQLQuery(hql);
        query.executeUpdate();
        session.close();
    }

}