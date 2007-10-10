//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import objot.aspect.Aspect;
import objot.aspect.Weaver;
import objot.codec.ErrThrow;
import objot.codec.Errs;
import objot.container.Container;
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
	Container container0;
	SessionFactory data0;
	Constructor<S> ctorS;

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

		data0 = Models.build(dataTest).buildSessionFactory();
		container0 = Services.build(data0, null);
		codec = Services.CODEC;
		ctorS = new Weaver(Transac.As.class)
		{
			@Override
			protected Object doWeave(Class<? extends Aspect> ac, Method m) throws Exception
			{
				if (m.isAnnotationPresent(Service.class))
					return Transac.Config.config(m);
				return this;
			}
		}.weave(S.class).getConstructor(Servlet.class);
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
			synchronized (data0)
			{
				data0.evictQueries();
				for (Object c: ((SessionFactoryImpl)data0).getAllSecondLevelCacheRegions()
					.values())
					((Cache)c).clear();
				new ModelsCreate(true).create(true, 1);
				return codec.enc(Ok.OK, null);
			}

		Container con = (Container)hq.getSession().getAttribute("container");
		if (con == null)
			synchronized (hq.getSession()) // double check
			{
				con = (Container)hq.getSession().getAttribute("container");
				if (con == null)
					hq.getSession().setAttribute("container",
						con = container0.outer().create());
			}
		return serve(con, req, hq, hRes);
	}

	public static class S
	{
		@Service
		@Transac.Any
		protected CharSequence serve(Container con, char[] req, HttpServletRequest hReq,
			HttpServletResponse hRes) throws ErrThrow, Exception
		{
			Do s = (Do)container0.create(con).get(cla);
			try
			{
				CharSequence res;
				if (req == null)
					res = serve(s, hReq, hRes);
				else
					res = serve(s, hReq, hRes, codec.dec(req, reqClas[0], cla));
				return res;
			}
			catch (InvalidStateException e)
			{
				throw Do.err(new Errs(e.getInvalidValues()));
			}
			finally
			{
				if (con.get(Session.class).me < 0)
					hReq.getSession().invalidate();
			}
		}
	}
}
