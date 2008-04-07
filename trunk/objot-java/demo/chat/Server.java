//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU General Public License version 2
//
package chat;

import java.util.Locale;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import objot.container.Container;
import objot.service.ServiceHandler;
import objot.service.ServiceInfo;
import objot.util.Class2;
import objot.util.Err;
import objot.util.Errs;

import org.hibernate.SessionFactory;
import org.hibernate.cache.Cache;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.validator.InvalidStateException;

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
	CharSequence ok;

	@Override
	public ServiceHandler init(Container context) throws Exception
	{
		Locale.setDefault(Locale.ENGLISH);

		ServletLog.logger = context.get(ServletContext.class);
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
		ok = codec.enc(new String[] { "ok" }, Object.class);
		return this;
	}

	@Override
	protected ServiceInfo getInfo(String n, String cla, String m) throws Exception
	{
		if (dataTest && "test".equals(cla))
			return new ServiceInfo(codec, n, ModelsCreate.CREATE);
		ServiceInfo inf = super.getInfo(n, //
			Class2.packageName(Do.class) + '.' + cla, m);
		return inf.meth.isAnnotationPresent(Do.Service.class) ? inf : null;
	}

	@Override
	public CharSequence handle(Container context, ServiceInfo inf, char[] req, int begin,
		int end1) throws Exception
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

		HttpSession hse = context.get(HttpSession.class);
		Container sess = (Container)hse.getAttribute("container");
		if (sess == null)
			synchronized (hse) // double check
			{
				sess = (Container)hse.getAttribute("container");
				if (sess == null)
					hse.setAttribute("container", sess = container.parent().create());
			}
		try
		{
			Container con = container.createBubble(container.parent(), sess);
			Object p = invoke(inf, con.get(inf.cla), req, begin, end1);
			return p instanceof Err ? codec.enc(p, null)
				: inf.meth.getReturnType() != void.class ? con.get(Data.class).enc : ok;
		}
		finally
		{
			if (sess.get(Session.class).close)
				hse.invalidate();
		}
	}

	@Override
	protected Object error(Throwable e) throws Exception
	{
		if (e instanceof InvalidStateException)
			return new Errs(((InvalidStateException)e).getInvalidValues());
		return super.error(e);
	}
}
