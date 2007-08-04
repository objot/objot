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

import objot.servlet.ObjotServlet;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.SessionFactory;
import org.hibernate.impl.SessionImpl;

import chat.service.Data;
import chat.service.Do;


/** service in transaction */
public class Transac
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
		boolean read;
		int isolation;
		boolean sub;
		SessionFactory dataFactory;
		int verbose;

		/** @param subRequest sequent sub requests in a request */
		public Aspect(boolean readonly, boolean commit, boolean serial, boolean subRequest,
			SessionFactory d, int verbose_)
		{
			read = readonly && ! subRequest;
			isolation = serial ? Connection.TRANSACTION_SERIALIZABLE //
				: commit ? Connection.TRANSACTION_READ_COMMITTED
					: Connection.TRANSACTION_REPEATABLE_READ;
			sub = subRequest;
			dataFactory = d;
			verbose = verbose_;
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
				if (verbose > 0)
					if (data.times == 1)
						System.out.println("\n================ "
							+ meth.getMethod().getDeclaringClass().getName() + "-"
							+ meth.getMethod().getName() + " ================");
					else
						System.out.println("---------------- "
							+ meth.getMethod().getDeclaringClass().getName() + "-"
							+ meth.getMethod().getName() + " ----------------");
				if (! hib.getTransaction().isActive())
				{
					hib.getJDBCContext().borrowConnection().setReadOnly(read);
					if (isolation > 0)
						hib.getJDBCContext().borrowConnection().setTransactionIsolation(
							isolation);
					hib.beginTransaction();
				}
				else if (! read && hib.getJDBCContext().borrowConnection().isReadOnly())
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
		public static void invokeFinally(Data data, boolean commit, ObjotServlet log)
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
					if (log != null)
						log.log(e);
				}
			if (data.hib.isOpen())
				try
				{
					data.hib.close();
				}
				catch (Throwable e)
				{
					if (log != null)
						log.log(e);
				}
		}
	}
}
