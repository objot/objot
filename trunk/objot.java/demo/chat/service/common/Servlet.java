//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat.service.common;

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
import org.hibernate.impl.SessionImpl;
import org.hibernate.validator.InvalidStateException;

import chat.model.ErrUnsigned;
import chat.model.common.Model;
import chat.service.Data;
import chat.service.Do;
import chat.service.DoChat;
import chat.service.DoSign;
import chat.service.DoUser;
import chat.service.Session;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.ServletScopes;


public final class Servlet
	extends ObjotServlet
{
	int verbose = 1;
	SessionFactory dataFactory;
	Injector container;

	@Override
	public void init() throws Exception
	{
		String verb = config.getInitParameter("verbose");
		verbose = verb != null ? Integer.parseInt(verb) : verbose;
		Locale.setDefault(Locale.ENGLISH);

		dataFactory = Model.init().buildSessionFactory();

		container = Guice.createInjector(new AbstractModule()
		{
			@Override
			protected void configure()
			{
				bindScope(ScopeSession.class, ServletScopes.SESSION);
				bindScope(ScopeRequest.class, ServletScopes.REQUEST);
				bind(Session.class);
				bind(Data.class);
				bind(DoSign.class);
				bind(DoUser.class);
				bind(DoChat.class);
			}
		});

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
		boolean sign;
		int tran;
		boolean tranRead;

		@Override
		public S init(String claName, String methName) throws Exception
		{
			super.init("chat.service.".concat(claName), methName);
			if (Servlet.this.verbose > 0)
				nameVerbose = "\n-------------------- " + name + " --------------------";
			Signed s = meth.getAnnotation(Signed.class);
			sign = s == null || s.need();
			Transac t = meth.getAnnotation(Transac.class);
			tran = t == null ? Transac.DEFAULT : t.level();
			tranRead = t != null && t.readOnly();
			return this;
		}

		@Override
		public CharSequence serve(char[] req, HttpServletRequest hReq,
			HttpServletResponse hRes) throws ErrThrow, Exception
		{
			if (Servlet.this.verbose > 0)
				System.out.println(nameVerbose);

			Session sess = container.getInstance(Session.class);
			SessionImpl data = null;
			Do s = (Do)container.getInstance(cla);

			Integer me = sess.me;
			if (sign && me == null)
				throw Do.err(new ErrUnsigned("not signed in"));
			try
			{
				if (tran > 0)
				{
					s.data.data = data = (SessionImpl)dataFactory.openSession();
					data.getJDBCContext().borrowConnection().setReadOnly(tranRead);
					data.getJDBCContext().borrowConnection().setTransactionIsolation(tran);
					data.beginTransaction();
				}
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
					if (tran > 0)
						try
						{
							if (ok && ! tranRead)
								data.getTransaction().commit();
							else
								data.getTransaction().rollback();
						}
						catch (Throwable e)
						{
							Servlet.this.log(e);
						}
				}
			}
			finally
			{
				if (me != null && sess.me == null)
					hReq.getSession().invalidate();
				if (data != null && data.isOpen())
					try
					{
						data.close();
					}
					catch (Throwable e)
					{
						Servlet.this.log(e);
					}
			}
		}
	}
}
