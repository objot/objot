//
// Objot 11a
//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.servlet;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.GenericServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import objot.Err;
import objot.Getting;
import objot.Objot;
import objot.Setting;


public class ObjotServlet
	extends GenericServlet
{
	protected Objot objot;
	protected String classNamePrefix = null;
	protected String methodNameDefault = "index";

	protected void log(Throwable e)
	{
		log("", e);
	}

	/** set {@link #objot} */
	@SuppressWarnings("unused")
	@Override
	public void init() throws ServletException
	{
		objot = new Objot();
	}

	/** multi thread, will be cached */
	protected Class<?> serviceClass(String name, HttpServletRequest req,
		HttpServletResponse res) throws Exception
	{
		return Class.forName(classNamePrefix == null ? name : classNamePrefix + name);
	}

	/** multi thread, will be cached */
	protected Method serviceMethod(Class<?> c, String name, HttpServletRequest req,
		HttpServletResponse res) throws Exception
	{
		for (Method m: c.getMethods())
			if (m.getName().equals(name) && m.isAnnotationPresent(Service.class))
				return m;
		throw new ServletException("no service found : " + c.getName() + "." + name);
	}

	protected Object serviceDo(Class<?> c, Method m, Object o, HttpServletRequest req,
		HttpServletResponse res) throws Exception
	{
		return m.invoke(null, o);
	}

	private static final long serialVersionUID = 1L;

	private ConcurrentHashMap<String, Class<?>> clas //
	= new ConcurrentHashMap<String, Class<?>>(128, 0.8f, 32);
	private ConcurrentHashMap<String, Method> meths //
	= new ConcurrentHashMap<String, Method>(128, 0.8f, 32);

	@Override
	public final void init(ServletConfig c) throws ServletException
	{
		super.init(c);
		log("========########@@@@@@@@$$$$$$$$ " + ObjotServlet.class.getName()
			+ " started $$$$$$$$@@@@@@@@########========");
	}

	@Override
	public void service(ServletRequest req_, ServletResponse res_)
		throws ServletException, IOException
	{
		HttpServletRequest req = (HttpServletRequest)req_;
		HttpServletResponse res = (HttpServletResponse)res_;
		String uri = req.getRequestURI();
		Class<?> sc = null;
		Method sm;
		byte[] bs;
		Object o = null;
		try
		{
			String name = uri.substring(uri.lastIndexOf('/') + 1);
			sc = clas.get(name);
			if (sc != null)
				sm = meths.get(name);
			else
			{
				int dot = name.lastIndexOf('-');
				sc = serviceClass(name.substring(0, dot < 0 ? name.length() : dot), req, res);
				sm = serviceMethod(sc, dot < 0 || dot == name.length() ? methodNameDefault
					: name.substring(dot + 1), req, res);
				clas.put(name, sc);
				meths.put(name, sm);
			}
			int len = req.getContentLength();
			if (len > 0)
			{
				InputStream in = req.getInputStream();
				bs = new byte[len];
				for (int from = 0, done; from < len; from += done)
					if ((done = in.read(bs, from, len - from)) < 0)
						throw new EOFException();
				o = Setting.go(objot, sc, bs);
			}
			try
			{
				o = serviceDo(sc, sm, o, req, res);
			}
			catch (IllegalArgumentException e)
			{
				String _ = "can not apply " + (o == null ? "null" : o.getClass().getName())
					+ " to " + sc.getName() + "-" + sm.getName() + " : " + e.getMessage();
				log(_, e);
				o = new Err(_);
			}
			catch (InvocationTargetException e)
			{
				log(e.getCause());
				o = new Err(e.getCause());
			}
		}
		catch (Exception e)
		{
			log(e);
			o = new Err(e);
		}

		res.setContentType("application/octet-stream");
		res.setHeader("Cache-Control", "no-cache");
		if (o == null)
			res.setContentLength(0);
		else
		{
			try
			{
				bs = Getting.go(objot, sc != null ? sc : getClass(), o);
			}
			catch (RuntimeException e)
			{
				throw e;
			}
			catch (Exception e)
			{
				throw new ServletException(e);
			}
			res.setContentLength(bs.length);
			res.getOutputStream().write(bs);
		}
	}
}
