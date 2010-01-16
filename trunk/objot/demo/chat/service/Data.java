//
// Copyright 2007-2010 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package chat.service;

import java.io.Serializable;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.ReplicationMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.TransientObjectException;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;


public class Data
{
	/** Delegated {@link org.hibernate.Session} */
	public Session hib;
	public boolean rollbackOnly;
	/** encoded service result or byte[] or InputStream */
	public Object result;

	/** @see Hibernate#initialize */
	public <T>T fetch(T o)
	{
		Hibernate.initialize(o);
		return o;
	}

	/** {@link #flush}, {@link #evict} and {@link #refresh} */
	public <T>T flushRefresh(T o)
	{
		hib.flush();
		hib.evict(o);
		hib.refresh(o);
		return o;
	}

	public Integer count(Criteria<?> c)
	{
		int n = (Integer)c.setProjection(Projections.rowCount()).uniqueResult();
		c.setProjection(null);
		return n;
	}

	public <T>T find1(Class<T> clazz, String prop, Object eq)
	{
		return criteria(clazz).add(Restrictions.eq(prop, eq)).uniqueResult();
	}

	// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

	/**
	 * Force this session to flush. Must be called at the end of a unit of work, before
	 * commiting the transaction and closing the session (depending on
	 * {@link Session#setFlushMode flush-mode}, {@link Transaction#commit()} calls this
	 * method). <p/> <i>Flushing</i> is the process of synchronizing the underlying
	 * persistent store with persistable state held in memory.
	 * 
	 * @throws HibernateException Indicates problems flushing the session or talking to
	 *             the database.
	 */
	public void flush() throws HibernateException
	{
		hib.flush();
	}

	/**
	 * Return the identifier value of the given entity as associated with this session. An
	 * exception is thrown if the given entity instance is transient or detached in
	 * relation to this session.
	 * 
	 * @param object a persistent instance
	 * @return the identifier
	 * @throws TransientObjectException if the instance is transient or associated with a
	 *             different session
	 */
	public Serializable getIdentifier(Object object) throws HibernateException
	{
		return hib.getIdentifier(object);
	}

	/**
	 * Remove this instance from the session cache. Changes to the instance will not be
	 * synchronized with the database. This operation cascades to associated instances if
	 * the association is mapped with <tt>cascade="evict"</tt>.
	 * 
	 * @param object a persistent instance
	 * @throws HibernateException
	 */
	public <T>T evict(T object) throws HibernateException
	{
		hib.evict(object);
		return object;
	}

	/**
	 * Return the persistent instance of the given entity class with the given identifier,
	 * assuming that the instance exists. <br>
	 * <br>
	 * You should not use this method to determine if an instance exists (use
	 * <tt>get()</tt> instead). Use this only to retrieve an instance that you assume
	 * exists, where non-existence would be an actual error.
	 * 
	 * @param theClass a persistent class
	 * @param id a valid identifier of an existing persistent instance of the class
	 * @return the persistent instance or proxy
	 * @throws HibernateException
	 */
	@SuppressWarnings("unchecked")
	public <T>T load(Class<T> theClass, Serializable id) throws HibernateException
	{
		return (T)hib.load(theClass, id);
	}

	/**
	 * Read the persistent state associated with the given identifier into the given
	 * transient instance.
	 * 
	 * @param object an "empty" instance of the persistent class
	 * @param id a valid identifier of an existing persistent instance of the class
	 * @throws HibernateException
	 */
	public <T>T load(T object, Serializable id) throws HibernateException
	{
		hib.load(object, id);
		return object;
	}

	/**
	 * Persist the state of the given detached instance, reusing the current identifier
	 * value. This operation cascades to associated instances if the association is mapped
	 * with <tt>cascade="replicate"</tt>.
	 * 
	 * @param object a detached instance of a persistent class
	 */
	public <T>T replicate(T object, ReplicationMode replicationMode)
		throws HibernateException
	{
		hib.replicate(object, replicationMode);
		return object;
	}

	/**
	 * Persist the given transient instance, first assigning a generated identifier. (Or
	 * using the current value of the identifier property if the <tt>assigned</tt>
	 * generator is used.) This operation cascades to associated instances if the
	 * association is mapped with <tt>cascade="save-update"</tt>.
	 * 
	 * @param object a transient instance of a persistent class
	 * @return the generated identifier
	 * @throws HibernateException
	 */
	public <T>T save(T object) throws HibernateException
	{
		hib.save(object);
		return object;
	}

	/**
	 * Either {@link #save(Object)} or {@link #update(Object)} the given instance,
	 * depending upon resolution of the unsaved-value checks (see the manual for
	 * discussion of unsaved-value checking). <p/> This operation cascades to associated
	 * instances if the association is mapped with <tt>cascade="save-update"</tt>.
	 * 
	 * @see Session#save(Object)
	 * @see Session#update(Object)
	 * @param object a transient or detached instance containing new or updated state
	 * @throws HibernateException
	 */
	public <T>T saveOrUpdate(T object) throws HibernateException
	{
		hib.saveOrUpdate(object);
		return object;
	}

	/**
	 * Update the persistent instance with the identifier of the given detached instance.
	 * If there is a persistent instance with the same identifier, an exception is thrown.
	 * This operation cascades to associated instances if the association is mapped with
	 * <tt>cascade="save-update"</tt>.
	 * 
	 * @param object a detached instance containing updated state
	 * @throws HibernateException
	 */
	public <T>T update(T object) throws HibernateException
	{
		hib.update(object);
		return object;
	}

	/**
	 * Copy the state of the given object onto the persistent object with the same
	 * identifier. If there is no persistent instance currently associated with the
	 * session, it will be loaded. Return the persistent instance. If the given instance
	 * is unsaved, save a copy of and return it as a newly persistent instance. The given
	 * instance does not become associated with the session. This operation cascades to
	 * associated instances if the association is mapped with <tt>cascade="merge"</tt>.<br>
	 * <br>
	 * The semantics of this method are defined by JSR-220.
	 * 
	 * @param object a detached instance with state to be copied
	 * @return an updated persistent instance
	 */
	@SuppressWarnings("unchecked")
	public <T>T merge(T object) throws HibernateException
	{
		return (T)hib.merge(object);
	}

	/**
	 * Make a transient instance persistent. This operation cascades to associated
	 * instances if the association is mapped with <tt>cascade="persist"</tt>.<br>
	 * <br>
	 * The semantics of this method are defined by JSR-220.
	 * 
	 * @param object a transient instance to be made persistent
	 */
	public <T>T persist(T object) throws HibernateException
	{
		hib.persist(object);
		return object;
	}

	/**
	 * Remove a persistent instance from the datastore. The argument may be an instance
	 * associated with the receiving <tt>Session</tt> or a transient instance with an
	 * identifier associated with existing persistent state. This operation cascades to
	 * associated instances if the association is mapped with <tt>cascade="delete"</tt>.
	 * 
	 * @param object the instance to be removed
	 * @throws HibernateException
	 */
	public <T>T delete(T object) throws HibernateException
	{
		hib.delete(object);
		return object;
	}

	/**
	 * Re-read the state of the given instance from the underlying database. It is
	 * inadvisable to use this to implement long-running sessions that span many business
	 * tasks. This method is, however, useful in certain special circumstances. For
	 * example
	 * <ul>
	 * <li>where a database trigger alters the object state upon insert or update
	 * <li>after executing direct SQL (eg. a mass update) in the same session
	 * <li>after inserting a <tt>Blob</tt> or <tt>Clob</tt>
	 * </ul>
	 * 
	 * @param object a persistent or detached instance
	 * @throws HibernateException
	 */
	public <T>T refresh(T object) throws HibernateException
	{
		hib.refresh(object);
		return object;
	}

	/**
	 * Create a new <tt>Criteria</tt> instance, for the given entity class, or a
	 * superclass of an entity class.
	 * 
	 * @param persistentClass a class, which is persistent, or has persistent subclasses
	 * @return Criteria
	 */
	@SuppressWarnings("unchecked")
	public <T>Criteria<T> criteria(Class<T> persistentClass)
	{
		return hib.createCriteria(persistentClass);
	}

	/**
	 * Create a new <tt>Criteria</tt> instance, for the given entity class, or a
	 * superclass of an entity class, with the given alias.
	 * 
	 * @param persistentClass a class, which is persistent, or has persistent subclasses
	 * @return Criteria
	 */
	@SuppressWarnings("unchecked")
	public <T>Criteria<T> criteria(Class<T> persistentClass, String alias)
	{
		return hib.createCriteria(persistentClass, alias);
	}

	/**
	 * Create a new instance of <tt>Query</tt> for the given HQL query string.
	 * 
	 * @param queryString a HQL query
	 * @return Query
	 * @throws HibernateException
	 */
	public Query query(String queryString) throws HibernateException
	{
		return hib.createQuery(queryString);
	}

	/**
	 * Create a new instance of <tt>SQLQuery</tt> for the given SQL query string.
	 * 
	 * @param queryString a SQL query
	 * @return SQLQuery
	 * @throws HibernateException
	 */
	public SQLQuery sql(String queryString) throws HibernateException
	{
		return hib.createSQLQuery(queryString);
	}

	/**
	 * Return the persistent instance of the given entity class with the given identifier,
	 * or null if there is no such persistent instance. (If the instance, or a proxy for
	 * the instance, is already associated with the session, return that instance or
	 * proxy.)
	 * 
	 * @param clazz a persistent class
	 * @param id an identifier
	 * @return a persistent instance or null
	 * @throws HibernateException
	 */
	@SuppressWarnings("unchecked")
	public <T>T get(Class<T> clazz, Serializable id) throws HibernateException
	{
		return (T)hib.get(clazz, id);
	}

	/**
	 * Return the entity name for a persistent entity
	 * 
	 * @param object a persistent entity
	 * @return the entity name
	 * @throws HibernateException
	 */
	public String getEntityName(Object object) throws HibernateException
	{
		return hib.getEntityName(object);
	}
}
