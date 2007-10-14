//
// Copyright 2007 Qianyan Cai
// Under the terms of the GNU General Public License version 2
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
				read = a == null ? false : a instanceof Readonly ? true : ((Repeat)a)
					.readonly();
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
			boolean ok = false;
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
					data.hib = factory.openSession();
				begin((SessionImpl)data.hib, Target.<Config>getData());
				Target.invoke();
				data.flush();
				ok = true;
			}
			finally
			{
				data.deep--;
				if (data.deep <= 0)
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
	}
}
