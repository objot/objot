//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import objot.Err;
import objot.ErrThrow;
import objot.Errs;
import objot.Objot;
import objot.servlet.ObjotServlet;
import objot.servlet.Serve;

import org.hibernate.SessionFactory;
import org.hibernate.validator.InvalidStateException;

import chat.service.Do;
import chat.service.Session;

import com.google.inject.Injector;


public final class Servlet
	extends ObjotServlet
{
	int verbose = 1;
	Injector container;
	SessionFactory dataFactory;

	@Override
	public void init() throws Exception
	{
		String verb = config.getInitParameter("verbose");
		verbose = verb != null ? Integer.parseInt(verb) : verbose;
		Locale.setDefault(Locale.ENGLISH);

		dataFactory = Models.init().buildSessionFactory();
		container = Services.init(dataFactory);
		objot = new Objot()
		{
			@Override
			protected Class<?> classByName(String name) throws Exception
			{
				return Class.forName("chat.model.".concat(name));
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
		return new S().init(objot, name);
	}

	class S
		extends Serve
	{
		String nameVerbose;

		@Override
		public S init(String claName, String methName) throws Exception
		{
			super.init("chat.service.".concat(claName), methName);
			if (Servlet.this.verbose > 0)
				nameVerbose = "\n-------------------- " + name + " --------------------";
			return this;
		}

		@Override
		public CharSequence serve(char[] req, HttpServletRequest hReq,
			HttpServletResponse hRes) throws ErrThrow, Exception
		{
			if (Servlet.this.verbose > 0)
				System.out.println(nameVerbose);

			Do s = (Do)container.getInstance(cla);
			Session sess = s.sess;
			Integer me = sess.me;
			boolean ok = false;
			try
			{
				CharSequence res;
				if (req == null)
					res = serve(s, hReq, hRes);
				else
					res = serve(s, hReq, hRes, objot.set(req, reqClas[0], cla));
				ok = true;
				return res;
			}
			catch (InvalidStateException e)
			{
				throw Do.err(new Errs(e.getInvalidValues()));
			}
			finally
			{
				if (me != null && sess.me == null)
					hReq.getSession().invalidate();
				// like open session in view
				AspectTransac.invokeFinally(s.data, ok, Servlet.this);
			}
		}
	}
}
