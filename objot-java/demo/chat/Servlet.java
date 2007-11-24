//
// Copyright 2007 Qianyan Cai
// Under the terms of the GNU General Public License version 2
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

import chat.service.Do;
import chat.service.Session;
import chat.service.Do.Service;


public final class Servlet
	extends CodecServlet
{
	static final String[] OK = { "ok" };

	boolean dataTest;
	SessionFactory dataFactory;
	/** parent is service container, parent.parent is session container */
	Container conInvoke;

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
		if (dataTest)
		{
			context.log("\n================ for test ================\n");
			new ModelsCreate(true).create(true, -1);
		}
		codec = Models.CODEC;
		dataFactory = Models.build(dataTest).buildSessionFactory();

		final Container conServ = Services.build(dataFactory, null);
		final Class<?> weavedInvoke = new Weaver(Transac.As.class)
		{
			@Override
			protected Object doWeave(Class<? extends Aspect> ac, Method m) throws Exception
			{
				return m.isAnnotationPresent(Service.class) ? new Transac.Config(m) : this;
			}
		}.weave(Invoke.class);
		conInvoke = new Factory(Invoke.class)
		{
			@Override
			protected Object doBind(Class<?> c, Bind b) throws Exception
			{
				if (conServ.bound(c))
					return b.mode(null);
				return c == Invoke.class ? b.cla(weavedInvoke) : b;
			}
		}.create(conServ);
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
	protected CharSequence service(HttpServletRequest hq, ServiceInfo inf, Object... reqs)
		throws Exception
	{
		if (inf.cla == ModelsCreate.class) // test
			synchronized (dataFactory)
			{
				dataFactory.evictQueries();
				for (Object c: ((SessionFactoryImpl)dataFactory) //
				.getAllSecondLevelCacheRegions().values())
					((Cache)c).clear();
				new ModelsCreate(true).create(true, 1);
				return codec.enc(OK, null);
			}

		Container sess = (Container)hq.getSession().getAttribute("container");
		if (sess == null)
			synchronized (hq.getSession()) // double check
			{
				sess = (Container)hq.getSession().getAttribute("container");
				if (sess == null)
					hq.getSession().setAttribute("container",
						sess = conInvoke.parent().parent().create());
			}
		try
		{
			return conInvoke.createBubble(conInvoke.parent().parent(), sess) //
			.get(Invoke.class).serve(inf, reqs);
		}
		catch (InvalidStateException e)
		{
			throw Do.err(new Errs(e.getInvalidValues()));
		}
		finally
		{
			if (sess.get(Session.class).close)
			{
				hq.getSession().invalidate();
				hq.getSession().invalidate();
			}
		}
	}

	@Inject.New
	public static class Invoke
	{
		@Inject
		public Container con;

		@Service
		@Transac.Any
		protected CharSequence serve(ServiceInfo inf, Object... reqs) throws Exception
		{
			Object o = inf.invoke(con.get(inf.cla), reqs);
			return inf.resp(inf.meth.getReturnType() != void.class ? o : OK);
		}
	}
}