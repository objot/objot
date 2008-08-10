//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package chat;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;

import objot.aspect.Aspect;
import objot.container.Inject;
import objot.util.Class2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.impl.SessionImpl;
import static java.sql.Connection.TRANSACTION_READ_COMMITTED;
import static java.sql.Connection.TRANSACTION_REPEATABLE_READ;
import static java.sql.Connection.TRANSACTION_SERIALIZABLE;

import chat.service.Data;


public @interface Transac
{
	/** no transaction or any transaction, {@link Data#hib} still available */
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

		public Config(AnnotatedElement o)
		{
			Annotation a = Class2.annoExclusive(o, Transac.class);
			if (a instanceof Commit)
			{
				iso = TRANSACTION_READ_COMMITTED;
				read = ((Commit)a).readonly();
			}
			else if (a instanceof Serial)
			{
				iso = TRANSACTION_SERIALIZABLE;
				read = ((Serial)a).readonly();
			}
			else if ( !(a instanceof Any))
			{
				iso = TRANSACTION_REPEATABLE_READ;
				read = a == null ? false : a instanceof Readonly ? true
					: ((Repeat)a).readonly();
			}
		}
	}

	public static class As
		extends Aspect
	{
		private static final Log LOG = LogFactory.getLog(As.class);

		@Inject
		public SessionFactory factory;
		@Inject
		public Data data;

		/** open hibernate session, begin transaction */
		@Override
		protected void aspect() throws Throwable
		{
			if (LOG.isDebugEnabled())
				if (data.depth == 0)
					LOG.debug("================ " + Target.clazz().getName() + "-"
						+ Target.name() + " ================");
				else
					LOG.debug("---------------- " + Target.clazz().getName() + "-"
						+ Target.name() + " ----------------");
			if (data.depth == 0)
				data.hib = factory.openSession();
			boolean ok = false;
			data.depth++;
			try
			{
				begin((SessionImpl)data.hib, Target.<Config>data(), Target.target());
				Target.invoke();
				data.flush();
				ok = true;
			}
			finally
			{
				data.depth--;
				if (data.depth <= 0)
				{
					if (data.hib.getTransaction().isActive())
						try
						{
							if (ok)
								data.hib.getTransaction().commit();
							else
								data.hib.getTransaction().rollback();
						}
						catch (Throwable e)
						{
							LOG.warn(e);
						}
					if (data.hib.isOpen())
						try
						{
							data.hib.close();
						}
						catch (Throwable e)
						{
							LOG.warn(e);
						}
					data.hib = null;
				}
			}
		}

		private static void begin(SessionImpl hib, Config con, String target)
			throws Exception
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
				throw new Exception(target + ": transaction must be writable");
			else if (con.iso > hib.getJDBCContext().borrowConnection().getTransactionIsolation())
				throw new Exception(target + ": isolation must be at least "
					+ (con.iso == TRANSACTION_SERIALIZABLE ? "serializable" //
						: con.iso == TRANSACTION_REPEATABLE_READ ? "repeatable read" //
							: "read committed"));
		}
	}
}
