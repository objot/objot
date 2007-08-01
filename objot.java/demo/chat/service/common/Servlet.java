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
import chat.service.Do;


public final class Servlet
	extends ObjotServlet
{
	int verbose = 1;
	SessionFactory sessionFactory;

	@Override
	public void init() throws Exception
	{
		String verb = config.getInitParameter("verbose");
		verbose = verb != null ? Integer.parseInt(verb) : verbose;
		Locale.setDefault(Locale.ENGLISH);

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

		sessionFactory = Model.init().buildSessionFactory();
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
		public CharSequence go(char[] req, HttpServletRequest hReq, HttpServletResponse hRes)
			throws ErrThrow, Exception
		{
			if (Servlet.this.verbose > 0)
				System.out.println(nameVerbose);
			Do $ = new Do();

			Integer me = (Integer)hReq.getSession().getAttribute("signed");
			$.me = me;
			if (sign && me == null)
				throw Do.err(new ErrUnsigned("not signed in"));

			try
			{
				if (tran > 0)
				{
					$.data = sessionFactory.openSession();
					((SessionImpl)$.data).getJDBCContext().borrowConnection() //
						.setReadOnly(tranRead);
					((SessionImpl)$.data).getJDBCContext().borrowConnection() //
						.setTransactionIsolation(tran);
					$.data.beginTransaction();
				}
				boolean ok = false;
				try
				{
					CharSequence res;
					if (req == null)
						res = go(null, hReq, hRes, $);
					else
						res = go(null, hReq, hRes, objot.set(req, reqClas[0], cla), $);
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
								$.data.getTransaction().commit();
							else
								$.data.getTransaction().rollback();
						}
						catch (Throwable e)
						{
							Servlet.this.log(e);
						}
				}
			}
			finally
			{
				if (me != null && $.me == null)
					hReq.getSession().invalidate();
				else if ($.me != me)
					hReq.getSession().setAttribute("signed", $.me);

				if ($.data != null && $.data.isOpen())
					try
					{
						$.data.close();
					}
					catch (Throwable e)
					{
						Servlet.this.log(e);
					}
			}
		}
	}
}
