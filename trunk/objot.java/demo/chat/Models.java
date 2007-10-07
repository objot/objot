//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat;

import java.sql.Connection;
import java.sql.Statement;

import objot.util.Class2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Environment;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.dialect.Dialect;
import org.hibernate.impl.SessionImpl;
import org.hibernate.pretty.DDLFormatter;
import org.hibernate.util.StringHelper;

import chat.model.Id;


public class Models
{
	static final Log LOG = LogFactory.getLog(Models.class);

	/** @param test whether use the testing database */
	public static AnnotationConfiguration build(boolean test) throws Exception
	{
		AnnotationConfiguration conf = new AnnotationConfiguration()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public SessionFactory buildSessionFactory() throws HibernateException
			{
				if ( !"org.hsqldb.jdbcDriver".equals(getProperty(Environment.DRIVER)))
					return super.buildSessionFactory();
				// fix the issue of hsqldb write delay stupid default value
				SessionFactory fac = super.buildSessionFactory();
				try
				{
					SessionImpl hib = (SessionImpl)fac.openSession();
					hib.beginTransaction();
					Statement stat = hib.getJDBCContext().borrowConnection()
						.createStatement();
					stat.executeUpdate("SET WRITE_DELAY FALSE");
					hib.getTransaction().commit();
					stat.close();
					hib.close();
					LOG.info("SET WRITE_DELAY FALSE");
				}
				catch (Exception e)
				{
					throw new Error(e);
				}
				return fac;
			}
		};

		conf.setNamingStrategy(new NamingStrategy()
		{
			public String classToTableName(String entity)
			{
				return StringHelper.unqualify(entity);
			}

			public String propertyToColumnName(String property)
			{
				return StringHelper.unqualify(property);
			}

			public String tableName(String table)
			{
				return table;
			}

			public String columnName(String column)
			{
				return column;
			}

			public String collectionTableName(String ownerEntity, String ownerTable,
				String associatedEntity, String associatedTable, String property)
			{
				return ownerTable + "_" + StringHelper.unqualify(property);
			}

			public String joinKeyColumnName(String joinedColumn, String joinedTable)
			{
				return joinedColumn;
			}

			public String foreignKeyColumnName(String property, String propertyEntity,
				String propertyTable, String referencedColumn)
			{
				return property != null ? StringHelper.unqualify(property) : propertyTable;
			}

			public String logicalColumnName(String column, String property)
			{
				return StringHelper.isEmpty(column) ? StringHelper.unqualify(property)
					: column;
			}

			public String logicalCollectionTableName(String table, String ownerTable,
				String associatedTable, String property)
			{
				if (table != null)
					return table;
				return ownerTable + "_" + StringHelper.unqualify(property);
			}

			public String logicalCollectionColumnName(String column, String property,
				String referencedColumn)
			{
				return StringHelper.isEmpty(column) ? property + "_" + referencedColumn
					: column;
			}
		});

		for (Class<?> c: Class2.packageClasses(Id.class))
			conf.addAnnotatedClass(c);

		if (test)
			conf.setProperty(Environment.URL, conf.getProperty(Environment.URL + ".test"));
		return conf;
	}

	protected AnnotationConfiguration conf;
	protected Dialect dialect;
	protected SessionImpl hib;
	protected Connection conn;
	protected Statement stat;

	protected Models()
	{
	}

	/** for database create and update */
	protected void start(boolean execute, boolean test) throws Exception
	{
		conf = build(test);
		conf.setProperty(Environment.HBM2DDL_AUTO, "false");
		conf.setProperty(Environment.USE_SECOND_LEVEL_CACHE, "false");
		conf.setProperty(Environment.USE_QUERY_CACHE, "false");
		conf.setProperty(Environment.SHOW_SQL, "false");
		conf.setProperty(Environment.FORMAT_SQL, "false");
		hib = (SessionImpl)conf.buildSessionFactory().openSession();
		dialect = hib.getFactory().getDialect();
		conn = hib.getJDBCContext().borrowConnection();
		conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
		hib.beginTransaction();
		if (execute)
			stat = conn.createStatement();
	}

	protected void sql(String s, boolean format) throws Exception
	{
		System.out.println(format ? new DDLFormatter(s).format() : s);
		if (stat != null)
			stat.executeUpdate(s);
	}

	protected void close() throws Exception
	{
		if (stat != null)
			try
			{
				stat.close();
			}
			catch (Throwable e)
			{
			}
		if (hib != null && hib.getTransaction().isActive())
			try
			{
				hib.getTransaction().rollback();
			}
			catch (Throwable e)
			{
			}
		if (hib != null)
			try
			{
				hib.close();
			}
			catch (Throwable e)
			{
			}
	}
}
