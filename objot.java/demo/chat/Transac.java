//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import objot.aspect.Aspect;
import objot.container.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.impl.SessionImpl;
import static java.sql.Connection.TRANSACTION_READ_COMMITTED;
import static java.sql.Connection.TRANSACTION_REPEATABLE_READ;
import static java.sql.Connection.TRANSACTION_SERIALIZABLE;

import chat.service.Data;
import chat.service.Do;


public @interface Transac
{
	/** no transaction or any transaction */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Any
	{
	}

	/** readonly transaction, and writable transaction by default */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Readonly
	{
	}

	/** read committed isolation or higher */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Commit
	{
		boolean readonly() default false;
	}

	/** repeatable read isolation or higher, by default */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Repeat
	{
		boolean readonly() default false;
	}

	/** serializable isolation */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Serial
	{
		boolean readonly() default false;
	}

	public static class Config
	{
		boolean read;
		int iso;

		public static Config config(Annotation t)
		{
			if (t instanceof Any)
				return null;
			Config con = new Config();
			if (t instanceof Commit)
			{
				con.iso = TRANSACTION_READ_COMMITTED;
				con.read = ((Commit)t).readonly();
			}
			else if (t instanceof Serial)
			{
				con.iso = TRANSACTION_SERIALIZABLE;
				con.read = ((Serial)t).readonly();
			}
			else
			{
				con.iso = TRANSACTION_REPEATABLE_READ;
				con.read = t == null ? false : t instanceof Readonly ? true : ((Repeat)t)
					.readonly();
			}
			return con;
		}
	}

	public static class As
		extends Aspect
	{
		private static final Log LOG = LogFactory.getLog(As.class);

		@Inject
		public SessionFactory factory;

		/** open hibernate session, begin transaction */
		@Override
		protected void aspect() throws Throwable
		{
			boolean ok = false;
			Config con = Target.getData();
			Data data = Target.<Do>getThis().data;
			data.deep++;
			try
			{
				if (LOG.isDebugEnabled())
					if (data.deep == 1)
						LOG.debug("================ " + Target.getClazz().getName() + "-"
							+ Target.getName() + " ================");
					else
						LOG.debug("---------------- " + Target.getClazz().getName() + "-"
							+ Target.getName() + " ----------------");
				if (data.deep == 1)
					close(data, true);
				if (data.hib == null)
					data.hib = factory.openSession();
				begin((SessionImpl)data.hib, con);
				Target.invoke();
				data.flush();
				ok = true;
			}
			finally
			{
				data.deep--;
				if (data.deep <= 0 && !data.lazyClose)
					close(data, ok);
			}
		}

		private static void begin(SessionImpl hib, Config con) throws Exception
		{
			if (con.iso <= 0)
				return;
			if ( !hib.getTransaction().isActive())
			{
				hib.getJDBCContext().borrowConnection().setReadOnly(con.read);
				hib.getJDBCContext().borrowConnection().setTransactionIsolation(con.iso);
				hib.beginTransaction();
			}
			else if ( !con.read && hib.getJDBCContext().borrowConnection().isReadOnly())
				throw new Exception(Target.getTarget() + ": transaction must be writable");
			else if (con.iso > hib.getJDBCContext().borrowConnection()
				.getTransactionIsolation())
				throw new Exception(Target.getTarget() + ": isolation must be at least "
					+ (con.iso == TRANSACTION_SERIALIZABLE ? "serializable" //
						: con.iso == TRANSACTION_REPEATABLE_READ ? "repeatable read" //
							: "read committed"));
		}

		private static void close(Data data, boolean commit)
		{
			if (data.hib == null)
				return;
			if (data.hib.getTransaction().isActive())
				try
				{
					if (commit)
						data.hib.getTransaction().commit();
					else
						data.hib.getTransaction().rollback();
				}
				catch (Throwable e)
				{
					if (LOG.isWarnEnabled())
						LOG.warn(e);
				}
			if (data.hib.isOpen())
				try
				{
					data.hib.close();
				}
				catch (Throwable e)
				{
					if (LOG.isWarnEnabled())
						LOG.warn(e);
				}
			data.hib = null;
		}
	}
}
