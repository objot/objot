//
// Objot 1
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
import objot.ErrThrow;
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
		return o == null ? m.invoke(null, (Object[])null) : m.invoke(null, o);
	}

	private static final long serialVersionUID = 1L;

	static class S
	{
		Class<?> c;
		Method m;
		Class<?> listC;
	}

	private ConcurrentHashMap<String, S> ss //
	= new ConcurrentHashMap<String, S>(128, 0.8f, 32);

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
		S s = null;
		byte[] bs;
		Object o = null;
		try
		{
			String name = uri.substring(uri.lastIndexOf('/') + 1);
			s = ss.get(name);
			if (s == null)
			{
				s = new S();
				int _ = name.lastIndexOf('-');
				s.c = serviceClass(name.substring(0, _ < 0 ? name.length() : _), req, res);
				s.c.equals(null);
				s.m = serviceMethod(s.c, _ < 0 || _ == name.length() ? methodNameDefault
					: name.substring(_ + 1), req, res);
				Class<?>[] ps = s.m.getParameterTypes();
				s.listC = ps.length > 0 ? ps[0] : null;
				ss.put(name, s);
			}
			int len = req.getContentLength();
			if (len > 0)
			{
				InputStream in = req.getInputStream();
				bs = new byte[len];
				for (int from = 0, done; from < len; from += done)
					if ((done = in.read(bs, from, len - from)) < 0)
						throw new EOFException();
				o = Setting.go(objot, s.c, bs, s.listC);
			}
			try
			{
				o = serviceDo(s.c, s.m, o, req, res);
			}
			catch (IllegalArgumentException e)
			{
				String _ = "can not apply " + (o == null ? "null" : o.getClass().getName())
					+ " to " + s.c.getCanonicalName() + "-" + s.m.getName()
					+ (e.getMessage() != null ? " : " : "")
					+ (e.getMessage() != null ? e.getMessage() : "");
				log(_, e);
				o = new Err().hint(_);
			}
			catch (InvocationTargetException e)
			{
				Throwable _ = e.getCause();
				log(_);
				o = _ instanceof ErrThrow ? ((ErrThrow)_).err : new Err().cause(_);
			}
		}
		catch (Exception e)
		{
			log(e);
			o = new Err().cause(e);
		}

		res.setContentType("application/octet-stream");
		res.setHeader("Cache-Control", "no-cache");
		if (o == null)
			res.setContentLength(0);
		else
		{
			try
			{
				bs = Getting.go(objot, s.c != null ? s.c : getClass(), o);
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
