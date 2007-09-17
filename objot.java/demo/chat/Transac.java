//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Connection;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.impl.SessionImpl;

import chat.service.Data;
import chat.service.Do;


/** service in transaction */
@Target(ElementType.PACKAGE /* just for eclipse auto completion */)
public @interface Transac
{
	/** no transaction or any transaction, and suitable transaction if no this annotation */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Any
	{
	}

	/** readonly or writable transaction, and writable transaction if no this annotation */
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
	}

	/** repeatable read isolation or higher, by default */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Repeat
	{
	}

	/** serializable isolation */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Serial
	{
	}

	public static class Aspect
		implements MethodInterceptor
	{
		static final Log LOG = LogFactory.getLog(Aspect.class);

		boolean read;
		int isolation;
		boolean sub;
		SessionFactory dataFactory;

		/** @param subRequest sequent sub requests in a request */
		public Aspect(boolean readonly, boolean commit, boolean serial, boolean subRequest,
			SessionFactory d)
		{
			read = readonly && !subRequest;
			isolation = serial ? Connection.TRANSACTION_SERIALIZABLE //
				: commit ? Connection.TRANSACTION_READ_COMMITTED
					: Connection.TRANSACTION_REPEATABLE_READ;
			sub = subRequest;
			dataFactory = d;
		}

		/** open hibernate session, begin transaction */
		public Object invoke(MethodInvocation meth) throws Throwable
		{
			Data data = ((Do)meth.getThis()).data;
			SessionImpl hib = (SessionImpl)data.hib;
			if (hib == null)
				data.hib = hib = (SessionImpl)dataFactory.openSession();

			data.times++;
			Object o;
			try
			{
				if (LOG.isDebugEnabled())
					if (data.times == 1)
						LOG.debug("================ "
							+ meth.getMethod().getDeclaringClass().getName() + "-"
							+ meth.getMethod().getName() + " ================");
					else
						LOG.debug("---------------- "
							+ meth.getMethod().getDeclaringClass().getName() + "-"
							+ meth.getMethod().getName() + " ----------------");
				if ( !hib.getTransaction().isActive())
				{
					hib.getJDBCContext().borrowConnection().setReadOnly(read);
					if (isolation > 0)
						hib.getJDBCContext().borrowConnection().setTransactionIsolation(
							isolation);
					hib.beginTransaction();
				}
				else if ( !read && hib.getJDBCContext().borrowConnection().isReadOnly())
					throw new Exception("transaction of " + meth + " must be writable");
				else if (isolation > hib.getJDBCContext().borrowConnection()
					.getTransactionIsolation())
					throw new Exception("transaction of " + meth
						+ " must be at least " //
						+ (isolation == Connection.TRANSACTION_SERIALIZABLE ? "serializable"
							: isolation == Connection.TRANSACTION_REPEATABLE_READ
								? "repeatable read" : "read committed") + " isolation");
				if (sub)
					hib.clear();
				o = meth.proceed();
			}
			finally
			{
				data.times--;
			}
			if (data.times == 0)
				hib.flush();
			return o;
		}

		/**
		 * commit or cancel transaction, close hibernate session. like open session in
		 * view
		 */
		public static void invokeFinally(Data data, boolean commit)
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
		}
	}
}