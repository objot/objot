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
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import objot.Getting;
import objot.Objot;
import objot.Setting;


public class ObjotServlet
	extends GenericServlet
{
	protected Objot objot;
	protected String serviceMethodDefaultName = "index";

	/** set {@link #objot} */
	protected void doInit() throws Exception
	{
		objot = new Objot();
	}

	/** multi thread, may be cached */
	protected Class<?> serviceClass(String name, HttpServletRequest req,
		HttpServletResponse res) throws Exception
	{
		return Class.forName(name);
	}

	/** multi thread, may be cached */
	protected Method serviceMethod(Class<?> c, String name, HttpServletRequest req,
		HttpServletResponse res) throws Exception
	{
		for (Method m: c.getMethods())
			if (m.getName().equals(name) && m.isAnnotationPresent(Service.class))
				return m;
		throw new ServletException("no service found : " + c.getName() + "." + name);
	}

	protected Object serviceObject(Class<?> c, Method m, HttpServletRequest req,
		HttpServletResponse res) throws Exception
	{
		return c.newInstance();
	}

	private static final long serialVersionUID = 1L;

	private ConcurrentHashMap<String, Class<?>> clas //
	= new ConcurrentHashMap<String, Class<?>>(128, 0.8f, 32);
	private ConcurrentHashMap<String, Method> meths //
	= new ConcurrentHashMap<String, Method>(128, 0.8f, 32);

	@Override
	public final void init() throws ServletException
	{
		try
		{
			log("-------- " + ObjotServlet.class.getName() + " initializing --------");
			doInit();
			log("-------- " + ObjotServlet.class.getName() + " initialized --------");
		}
		catch (RuntimeException e)
		{
			throw e;
		}
		catch (ServletException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new ServletException(e);
		}
	}

	@Override
	public final void service(ServletRequest req_, ServletResponse res_)
		throws ServletException, IOException
	{
		HttpServletRequest req = (HttpServletRequest)req_;
		HttpServletResponse res = (HttpServletResponse)res_;
		String uri = req.getRequestURI();
		try
		{
			res.setContentType("application/octet-stream");
			res.setHeader("Cache-Control", "no-cache");

			String name = uri.substring(uri.lastIndexOf('/') + 1);
			Class<?> sc = clas.get(name);
			Method sm;
			if (sc != null)
				sm = meths.get(name);
			else
			{
				int dot = name.lastIndexOf('-');
				sc = serviceClass(name.substring(0, dot < 0 ? name.length() : dot), req, res);
				sm = serviceMethod(sc, dot < 0 || dot == name.length()
					? serviceMethodDefaultName : name.substring(dot + 1), req, res);
				clas.put(name, sc);
				meths.put(name, sm);
			}
			Object s = serviceObject(sc, sm, req, res);

			Object o = null;
			int len = req.getContentLength();
			if (len > 0)
			{
				InputStream in = req.getInputStream();
				byte[] b = new byte[len];
				for (int from = 0, done; from < len; from += done)
					if ((done = in.read(b, from, len - from)) < 0)
						throw new EOFException();
				Setting.go(objot, sc, b);
			}
			try
			{
				o = sm.invoke(s, o);
			}
			catch (InvocationTargetException e)
			{
				throw e.getCause();
			}
			if (o != null)
				res.getOutputStream().write(Getting.go(objot, sc, o));
		}
		catch (RuntimeException e)
		{
			throw e;
		}
		catch (ServletException e)
		{
			throw e;
		}
		catch (IOException e)
		{
			throw e;
		}
		catch (Error e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			throw new ServletException(e);
		}
	}
}
