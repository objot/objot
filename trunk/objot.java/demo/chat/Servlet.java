//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat;

import java.lang.reflect.Method;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;

import objot.aspect.Aspect;
import objot.aspect.Weaver;
import objot.codec.Errs;
import objot.container.Bind;
import objot.container.Container;
import objot.container.Factory;
import objot.container.Inject;
import objot.servlet.CodecServlet;
import objot.servlet.ServiceInfo;
import objot.util.Class2;

import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.cache.Cache;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.validator.InvalidStateException;

import chat.model.Ok;
import chat.service.Do;
import chat.service.Session;
import chat.service.Do.Service;


public final class Servlet
	extends CodecServlet
{
	boolean dataTest;
	SessionFactory dataFactory;
	/** outer is services container, outer.outer is session container */
	Container containerS;

	@Override
	public void init() throws Exception
	{
		Locale.setDefault(Locale.ENGLISH);

		ServletLog.logger = context;
		if ( !(LogFactory.getLog(Servlet.class) instanceof ServletLog))
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
		context.log("\n================ for test ================\n");
		if (dataTest)
			new ModelsCreate(true).create(true, -1);
		dataFactory = Models.build(dataTest).buildSessionFactory();

		codec = Services.CODEC;
		final Class<?> claS = new Weaver(Transac.As.class)
		{
			@Override
			protected Object doWeave(Class<? extends Aspect> ac, Method m) throws Exception
			{
				return m.isAnnotationPresent(Service.class) ? new Transac.Config(m) : this;
			}
		}.weave(S.class);
		containerS = Services.build(dataFactory, null);
		containerS = new Factory(S.class)
		{
			@Override
			protected Object doBind(Class<?> c, Bind b) throws Exception
			{
				if (containerS.bound(c)) // here is service container
					return b.mode(null);
				return c == S.class ? b.cla(claS) : b;
			}
		}.create(containerS);
	}

	@Override
	protected ServiceInfo getServiceInfo(String n, String cla, String m) throws Exception
	{
		if (dataTest && "test".equals(cla))
			return new ServiceInfo(codec, n, Class2.method1(ModelsCreate.class, "create"));
		ServiceInfo inf = super.getServiceInfo(n,
			Do.class.getPackage().getName() + '.' + cla, m);
		return inf.meth.isAnnotationPresent(Do.Service.class) ? inf : null;
	}

	@Override
	protected CharSequence service(HttpServletRequest hq, ServiceInfo inf, Object... reqs)
		throws Exception
	{
		if (inf.cla == ModelsCreate.class) // test
			synchronized (dataFactory)
			{
				dataFactory.evictQueries();
				for (Object c: ((SessionFactoryImpl)dataFactory)
					.getAllSecondLevelCacheRegions().values())
					((Cache)c).clear();
				new ModelsCreate(true).create(true, 1);
				return codec.enc(Ok.OK, null);
			}

		Container sess = (Container)hq.getSession().getAttribute("container");
		if (sess == null)
			synchronized (hq.getSession()) // double check
			{
				sess = (Container)hq.getSession().getAttribute("container");
				if (sess == null)
					hq.getSession().setAttribute("container",
						sess = containerS.outer().outer().create());
			}
		try
		{
			return containerS.createBubble(containerS.outer().outer(), sess).get(S.class)
				.serve(inf, reqs);
		}
		catch (InvalidStateException e)
		{
			throw Do.err(new Errs(e.getInvalidValues()));
		}
		finally
		{
			if (sess.get(Session.class).me < 0)
				hq.getSession().invalidate();
		}
	}

	@Inject.New
	public static class S
	{
		@Inject
		public Container con;

		@Service
		@Transac.Any
		protected CharSequence serve(ServiceInfo inf, Object... reqs) throws Exception
		{
			return inf.invoke(con.get(inf.cla), reqs);
		}
	}
}
