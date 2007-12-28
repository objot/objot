//
// Copyright 2007 Qianyan Cai
// Under the terms of the GNU General Public License version 2
//
package objot.servlet;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import objot.codec.Codec;
import objot.codec.Err;
import objot.codec.ErrThrow;
import objot.util.Mod2;
import objot.util.String2;


public class CodecServlet
	implements Servlet
{
	protected ServletContext context;
	protected ServletConfig config;
	protected Codec codec;
	protected String serviceMethodNameDefault = "service";
	protected ConcurrentHashMap<String, ServiceInfo> infos //
	= new ConcurrentHashMap<String, ServiceInfo>(128, 0.8f, 32);

	/** set {@link #codec} */
	protected void init() throws Exception
	{
		codec = new Codec();
	}

	/** must be thread safe, will be cached, service not found if null or exception */
	protected ServiceInfo getServiceInfo(String name) throws Exception
	{
		int s = name.lastIndexOf('-');
		return getServiceInfo(name, s < 0 ? name : name.substring(0, s), s < 0
			|| s >= name.length() - 1 ? serviceMethodNameDefault : name.substring(s + 1));
	}

	/** must be thread safe, will be cached, service not found if null or exception */
	protected ServiceInfo getServiceInfo(String name, String cla, String method)
		throws Exception
	{
		Class<?> c = Class.forName(cla);
		if (Mod2.match(c, Mod2.PUBLIC))
			for (Method m: c.getMethods())
				if (m.getName().equals(method))
					return new ServiceInfo(codec, name, m);
		return null;
	}

	protected CharSequence service(HttpServletRequest hReq, ServiceInfo inf, Object... reqs)
		throws Exception
	{
		return codec.enc(inf.invoke(null, reqs), inf.cla);
	}

	// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

	public final void init(ServletConfig c) throws ServletException
	{
		config = c;
		context = config.getServletContext();
		context.log("\n\n================################ " + getClass().getName()
			+ " %%%%%%%%%%%%%%%%$$$$$$$$$$$$$$$$\n\n");
		try
		{
			init();
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

	public void destroy()
	{
		context.log("\n\n$$$$$$$$$$$$$$$$%%%%%%%%%%%%%%%% " + getClass().getName()
			+ " ################================\n\n");
	}

	public ServletConfig getServletConfig()
	{
		return config;
	}

	public String getServletInfo()
	{
		return getClass().getCanonicalName();
	}

	public void service(ServletRequest hReq, ServletResponse hResp)
		throws ServletException, IOException
	{
		HttpServletRequest hq = (HttpServletRequest)hReq;
		HttpServletResponse hp = (HttpServletResponse)hResp;
		hp.setContentType("text/plain; charset=UTF-8");
		hp.setHeader("Cache-Control", "no-cache");

		String uri = hq.getRequestURI();
		ServiceInfo inf = null;
		char[] q = null;
		CharSequence p = null;
		try
		{
			String name = uri.substring(uri.lastIndexOf('/') + 1);
			inf = infos.get(name);
			if (inf == null)
			{
				try
				{
					inf = getServiceInfo(name);
				}
				catch (Exception e)
				{
					throw new ServletException("service not found : ".concat(name), e);
				}
				if (inf == null)
					throw new ServletException("service not found : ".concat(name));
				infos.put(name, inf);
			}
			int len = hq.getContentLength();
			if (len > 0)
			{
				InputStream in = hq.getInputStream();
				byte[] s = new byte[len];
				for (int begin = 0, done; begin < len; begin += done)
					if ((done = in.read(s, begin, len - begin)) < 0)
						throw new EOFException();
				q = String2.utf(s);
			}
			try
			{
				if (q == null)
					p = service(hq, inf);
				else
					p = service(hq, inf, codec.dec(q, inf.reqCla, inf.cla));
			}
			catch (ErrThrow e)
			{
				if (e.log)
					context.log("", e);
				p = error(inf, e.err);
			}
			catch (Exception e)
			{
				context.log("", e);
				p = error(inf, new Err(e));
			}
			if (p == null)
				hp.setContentLength(0);
			else
			{
				byte[] bs = String2.utf(p);
				hp.setContentLength(bs.length);
				hp.getOutputStream().write(bs);
			}
		}
		catch (RuntimeException e)
		{
			throw e;
		}
		catch (IOException e)
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

	protected CharSequence error(ServiceInfo inf, Err e) throws Exception
	{
		return codec.enc(e, inf.cla);
	}
}
