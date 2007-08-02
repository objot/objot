package chat;

import java.sql.Connection;

import objot.servlet.ObjotServlet;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.SessionFactory;
import org.hibernate.impl.SessionImpl;

import chat.service.Data;
import chat.service.Do;


public class AspectTransac
	implements MethodInterceptor
{
	SessionFactory dataFactory;
	/** 0 for readonly */
	int isolation;

	public AspectTransac(SessionFactory data, boolean readonly, boolean repeat, boolean serial)
	{
		dataFactory = data;
		isolation = serial ? Connection.TRANSACTION_SERIALIZABLE //
			: repeat ? Connection.TRANSACTION_REPEATABLE_READ : readonly ? 0
				: Connection.TRANSACTION_READ_COMMITTED;
	}

	/** open hibernate session, begin transaction */
	public Object invoke(MethodInvocation meth) throws Throwable
	{
		Data data = ((Do)meth.getThis()).data;
		SessionImpl hib = (SessionImpl)data.data;
		if (hib == null)
			data.data = hib = (SessionImpl)dataFactory.openSession();
		if (hib.getTransaction().isActive())
		{
			if (isolation > 0 && hib.getJDBCContext().borrowConnection().isReadOnly())
				throw new Exception("transaction of " + meth + " must be writable");
			if (isolation > hib.getJDBCContext().borrowConnection().getTransactionIsolation())
				throw new Exception("transaction of " + meth
					+ " must be at least " //
					+ (isolation == Connection.TRANSACTION_SERIALIZABLE ? "serializable"
						: isolation == Connection.TRANSACTION_REPEATABLE_READ
							? "repeatable read" : "read committed") + " isolation");
			return meth.proceed();
		}
		hib.getJDBCContext().borrowConnection().setReadOnly(isolation == 0);
		if (isolation > 0)
			hib.getJDBCContext().borrowConnection().setTransactionIsolation(isolation);
		hib.beginTransaction();
		return meth.proceed();
	}

	/** commit or cancel transaction, close hibernate session. like open session in view */
	public static void invokeFinally(Data data, boolean commit, ObjotServlet log)
	{
		if (data.data == null)
			return;
		if (data.data.getTransaction().isActive())
			try
			{
				if (commit)
					data.data.getTransaction().commit();
				else
					data.data.getTransaction().rollback();
			}
			catch (Throwable e)
			{
				if (log != null)
					log.log(e);
			}
		if (data.data.isOpen())
			try
			{
				data.data.close();
			}
			catch (Throwable e)
			{
				if (log != null)
					log.log(e);
			}
	}
}
