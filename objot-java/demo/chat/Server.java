//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU General Public License version 2
//
package chat;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import objot.container.Container;
import objot.service.CodecServlet;
import objot.service.ServiceInfo;
import objot.util.Class2;
import objot.util.Errs;

import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.cache.Cache;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.validator.InvalidStateException;

import chat.service.Data;
import chat.service.Do;
import chat.service.Session;


public final class Server
	extends CodecServlet
{
	boolean dataTest;
	SessionFactory dataFactory;
	/** service container, parent is session container */
	Container container;
	CharSequence ok;

	@Override
	public void init() throws Exception
	{
		Locale.setDefault(Locale.ENGLISH);

		ServletLog.logger = context;
		if ( !(LogFactory.getLog(Server.class) instanceof ServletLog))
		{
			String s = "\n\n**************** WARNING ****************\n"
				+ " org.apache.commons.logging.Log = " + ServletLog.class.getName()
				+ " should be in commons-logging.properties\n\n";
			System.err.println(s);
			context.log(s);
		}

		dataTest = System.getProperty("data.test") != null;
		String test = config.getInitParameter("data.test");
		dataTest |= test != null && Boolean.parseBoolean(test);
		if (dataTest)
		{
			context.log("\n================ for test ================\n");
			new ModelsCreate(true).create(true, -1);
		}
		codec = Models.CODEC;
		dataFactory = Models.build(dataTest).buildSessionFactory();
		container = Services.build(codec, dataFactory);
		ok = codec.enc(new String[] { "ok" }, Object.class);
	}

	@Override
	protected ServiceInfo getServiceInfo(String n, String cla, String m) throws Exception
	{
		if (dataTest && "test".equals(cla))
			return new ServiceInfo(codec, n, ModelsCreate.CREATE);
		ServiceInfo inf = super.getServiceInfo(n, //
			Class2.packageName(Do.class) + '.' + cla, m);
		return inf.meth.isAnnotationPresent(Do.Service.class) ? inf : null;
	}

	@Override
	protected CharSequence service(HttpServletRequest hq, HttpServletResponse hp,
		ServiceInfo inf, Object... reqs) throws Exception
	{
		if (inf.cla == ModelsCreate.class) // test
			synchronized (dataFactory)
			{
				dataFactory.evictQueries();
				for (Object c: ((SessionFactoryImpl)dataFactory) //
				.getAllSecondLevelCacheRegions().values())
					((Cache)c).clear();
				new ModelsCreate(true).create(true, 1);
				return ok;
			}

		Container sess = (Container)hq.getSession().getAttribute("container");
		if (sess == null)
			synchronized (hq.getSession()) // double check
			{
				sess = (Container)hq.getSession().getAttribute("container");
				if (sess == null)
					hq.getSession().setAttribute("container",
						sess = container.parent().create());
			}
		try
		{
			Container con = container.createBubble(container.parent(), sess);
			inf.invoke(con.get(inf.cla), reqs);
			return inf.meth.getReturnType() != void.class ? con.get(Data.class).enc : ok;
		}
		catch (InvalidStateException e)
		{
			throw Do.err(new Errs(e.getInvalidValues()));
		}
		finally
		{
			if (sess.get(Session.class).close)
				hq.getSession().invalidate();
		}
	}
}
