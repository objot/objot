//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU General Public License version 2
//
package objot.service;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import objot.container.Container;
import objot.util.Array2;
import objot.util.String2;


public class CodecServlet
	implements Servlet
{
	protected ServletConfig config;
	protected ServiceHandler handler;
	protected Container con;

	public final void init(ServletConfig c) throws ServletException
	{
		config = c;
		config.getServletContext().log(
			"\n\n================################ " + getClass().getName()
				+ " %%%%%%%%%%%%%%%%$$$$$$$$$$$$$$$$\n\n");
		try
		{
			handler = (ServiceHandler)Class.forName(config.getInitParameter("handler")).newInstance();
		}
		catch (Exception e)
		{
			throw new ServletException(e);
		}
	}

	public void destroy()
	{
		config.getServletContext().log(
			"\n\n$$$$$$$$$$$$$$$$%%%%%%%%%%%%%%%% " + getClass().getName()
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
		try
		{
			String uri = hq.getRequestURI();
			ServiceInfo inf = null;
			String name = uri.substring(uri.lastIndexOf('/') + 1);
			inf = handler.getInfo(name);
			char[] q = Array2.CHARS0;
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
			Object p = handler.handle(null, inf, q, 0, q.length);
			if (p == null || !(p instanceof CharSequence))
				hp.setContentLength(0);
			else
			{
				byte[] bs = String2.utf((CharSequence)p);
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
}