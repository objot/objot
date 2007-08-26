//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import objot.codec.Codec;
import objot.codec.Err;
import objot.codec.ErrThrow;
import objot.codec.Errs;
import objot.servlet.ObjotServlet;
import objot.servlet.Serve;

import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.cache.Cache;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.validator.InvalidStateException;

import chat.model.Id;
import chat.model.Ok;
import chat.service.Do;
import chat.service.Session;

import com.google.inject.Injector;


public final class Servlet
	extends ObjotServlet
{
	boolean dataTest;
	Injector container;
	SessionFactory dataFactory;

	@Override
	public void init() throws Exception
	{
		Locale.setDefault(Locale.ENGLISH);

		ServletLog.logger = context;
		if (! (LogFactory.getLog(Servlet.class) instanceof ServletLog))
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
			new ModelsCreate(true, - 1, true);

		dataFactory = Models.build(dataTest).buildSessionFactory();
		container = Services.build(dataFactory, false);
		codec = new Codec()
		{
			String modelPrefix = Id.class.getPackage().getName() + ".";

			@Override
			protected Class<?> classByName(String name) throws Exception
			{
				return Class.forName(modelPrefix.concat(name));
			}

			/** include {@link Err} and {@link Errs} */
			@Override
			protected String className(Class<?> c) throws Exception
			{
				return c.getName().substring(c.getName().lastIndexOf('.') + 1);
			}
		};
	}

	@Override
	protected Serve getServe(String name, HttpServletRequest hReq, HttpServletResponse hRes)
		throws Exception
	{
		return new S().init(codec, name);
	}

	class S
		extends Serve
	{
		{
			serviceAnno = Do.Service.class;
		}

		@Override
		public Serve init(String claName, String methName) throws Exception
		{
			if (dataTest && "test".equals(claName))
				return this;
			return super.init(Do.class.getPackage().getName() + '.' + claName, methName);
		}

		@Override
		public CharSequence serve(char[] req, HttpServletRequest hReq,
			HttpServletResponse hRes) throws ErrThrow, Exception
		{
			if (cla == null) // test
			{
				dataFactory.evictQueries();
				for (Object c: ((SessionFactoryImpl)dataFactory)
					.getAllSecondLevelCacheRegions().values())
					((Cache)c).clear();
				new ModelsCreate(true, 1, true);
				return codec.get(Ok.OK, Object.class);
			}

			Session sess = (Session)hReq.getSession().getAttribute("scope");
			if (sess != null)
				Scopes.session(sess);
			else
				synchronized (hReq.getSession()) // double check
				{
					sess = (Session)hReq.getSession().getAttribute("scope");
					if (sess != null)
						Scopes.session(sess);
					else
						hReq.getSession().setAttribute("scope", sess = Scopes.session(sess));
				}
			Scopes.request();
			Do s = (Do)container.getInstance(cla);
			int me = sess.me;

			boolean ok = false;
			try
			{
				CharSequence res;
				if (req == null)
					res = serve(s, hReq, hRes);
				else
					res = serve(s, hReq, hRes, codec.set(req, reqClas[0], cla));
				ok = true;
				return res;
			}
			catch (InvalidStateException e)
			{
				throw Do.err(new Errs(e.getInvalidValues()));
			}
			finally
			{
				if (me != 0 && sess.me == 0)
					hReq.getSession().invalidate();
				// like open session in view
				Transac.Aspect.invokeFinally(s.data, ok);
			}
		}
	}
}
