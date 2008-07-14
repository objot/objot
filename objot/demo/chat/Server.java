//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Library General Public License version 2
//
package chat;

import java.util.Locale;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import objot.container.Container;
import objot.service.RequestException;
import objot.service.ServiceHandler;
import objot.service.ServiceInfo;
import objot.util.Class2;
import objot.util.ErrThrow;

import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.cache.Cache;
import org.hibernate.impl.SessionFactoryImpl;

import chat.service.Data;
import chat.service.Do;
import chat.service.Session;


public final class Server
	extends ServiceHandler
{
	boolean dataTest;
	SessionFactory dataFactory;
	/** service container, parent is session container */
	Container container;

	@Override
	public Server init(Container context) throws Exception
	{
		Locale.setDefault(Locale.ENGLISH);

		ServletLog.logger = context.get(ServletContext.class);
		log = LogFactory.getLog(toString());
		if ( !(log instanceof ServletLog))
		{
			String s = "\n\n**************** WARNING ****************\n"
				+ " org.apache.commons.logging.Log = " + ServletLog.class.getName()
				+ " should be in commons-logging.properties\n\n";
			System.err.println(s);
			log.warn(s);
		}

		dataTest = System.getProperty("data.test") != null;
		if (dataTest)
		{
			log.warn("\n================ for test ================\n");
			new ModelsCreate(true).create(true, -1);
		}
		codec = Models.CODEC;
		dataFactory = Models.build(dataTest).buildSessionFactory();
		container = Services.build(codec, dataFactory);
		return this;
	}

	@Override
	protected ServiceInfo getInfo(String n, String cla, String m) throws Exception
	{
		if (dataTest && "test".equals(cla))
			return new ServiceInfo(codec, n, ModelsCreate.CREATE);
		ServiceInfo inf = super.getInfo(n, Class2.packageName(Do.class) + '.' + cla, m);
		return inf != null && inf.meth.isAnnotationPresent(Do.Service.class) ? inf : null;
	}

	@Override
	public Object handle(Container context, ServiceInfo inf, char[] req, int begin, int end1,
		Object[] extraReqs) throws Exception
	{
		try
		{
			if (inf.cla == ModelsCreate.class) // test
				synchronized (dataFactory)
				{
					dataFactory.evictQueries();
					for (Object c: ((SessionFactoryImpl)dataFactory) //
					.getAllSecondLevelCacheRegions().values())
						((Cache)c).clear();
					new ModelsCreate(true).create(true, 1);
					return "ok";
				}

			HttpSession hse = context.get(HttpSession.class);
			Container sess = (Container)hse.getAttribute("container");
			if (sess == null)
				synchronized (hse) // double check
				{
					sess = (Container)hse.getAttribute("container");
					if (sess == null)
						hse.setAttribute("container", sess = container.parent().create());
				}
			Container con = container.create(sess);
			Object ser = con.get(inf.cla);
			invoke(inf, ser, req, begin, end1, extraReqs);
			if (sess.get(Session.class).close)
				hse.invalidate();
			if (ser instanceof Do && ((Do)ser).respType != null)
				context.set(String.class, ((Do)ser).respType);
			return con.get(Data.class).result;
		}
		catch (RequestException e)
		{
			if (log.isTraceEnabled())
				log.trace(e);
			return error(e);
		}
		catch (ErrThrow e)
		{
			if (log.isTraceEnabled())
				log.trace(e);
			return error(e);
		}
		catch (Error e)
		{
			log.error(e);
			return error(e);
		}
		catch (Throwable e)
		{
			if (log.isDebugEnabled())
				log.debug(e);
			return error(e);
		}
	}
}
