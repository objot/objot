//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import objot.Err;
import objot.ErrThrow;
import objot.Objot;
import objot.Setting;
import objot.servlet.ObjotServlet;
import objot.servlet.Servicing;


public final class Servlet
	extends ObjotServlet
{
	@Override
	public void init()
	{
		objot = new Objot()
		{
			@Override
			protected Class<?> classByName(String name) throws Exception
			{
				return Class.forName("chat.model.".concat(name));
			}

			@Override
			protected String className(Class<?> c)
			{
				if (c == Err.class)
					return "Err";
				return c.getName().substring("chat.model.".length());
			}
		};
		synchronized (Do.class)
		{
			if (Do.sessionFactory == null)
				; // @todo initialize Hibernate SessionFactory
		}
	}

	@Override
	protected Servicing serviceConfig(String name, HttpServletRequest req,
		HttpServletResponse res) throws Exception
	{
		return new Ing().init(objot, name);
	}

	static class Ing
		extends Servicing
	{
		boolean sign;
		boolean tran;
		boolean tranRead;
		boolean tranSerial;

		@Override
		public Ing init(String claName, String methName) throws Exception
		{
			super.init("chat.service.".concat(claName), methName);
			Signed s = meth.getAnnotation(Signed.class);
			sign = s == null || s.need();
			Transac t = meth.getAnnotation(Transac.class);
			tran = t == null || t.need();
			tranRead = t != null && t.readOnly();
			tranSerial = t != null && t.serial();
			return this;
		}

		@Override
		public CharSequence Do(char[] Q, HttpServletRequest req, HttpServletResponse res)
			throws ErrThrow, Exception
		{
			boolean ok = false;
			synchronized (Servlet.class) // @todo get Hibernate session
			{
				try
				{
					Do $ = new Do();
					$.$ = null; // @todo Hibernate session
					$.http = req.getSession();
					if (sign)
						$.me = DoSign.me($);
					if (tran)
					{
						; // @todo start transaction
						if (tranRead)
							; // @todo read only transaction
						else if (tranSerial)
							; // @todo serializable isolation level
						else
							; // @todo repeatable-read isolation level for most cases }
					}
					CharSequence S;
					if (Q == null)
						S = Do(null, req, res, $);
					else
						S = Do(null, req, res, Setting.go(objot, reqClas[0], cla, Q), $);
					ok = true;
					return S;
				}
				finally
				{
					if (tran)
						try
						{
							if (ok)
								; // @todo commit transaction
							else
								; // @todo abort transaction
						}
						catch (Throwable e)
						{
						}
					; // @todo close Hibernate session
				}
			}
		}
	}
}
