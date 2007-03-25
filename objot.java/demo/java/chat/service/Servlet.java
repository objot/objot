//
// Objot 1
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
import objot.servlet.ObjotServlet;
import objot.servlet.Servicing;


public final class Servlet
	extends ObjotServlet
{
	private static final long serialVersionUID = 1L;

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
	}

	@Override
	protected Servicing serviceConfig(String name, HttpServletRequest req,
		HttpServletResponse res) throws Exception
	{
		return new Ing();
	}

	static class Ing
		extends Servicing
	{
		Ing()
		{
			classNamePrefix = "chat.service.";
		}

		@Override
		public byte[] Do(Object service, HttpServletRequest req, HttpServletResponse res,
			Object... reqOs) throws ErrThrow, Exception
		{
			synchronized (Servlet.class) // serializable transaction
			{
				return reqOs.length == 0 ? super.Do(null, req, res, req.getSession()) //
					: super.Do(null, req, res, reqOs[0], req.getSession());
			}
		}

	}
}
