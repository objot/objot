//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.servlet;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import objot.Err;
import objot.ErrThrow;
import objot.Objot;


public class ObjotServlet
	implements Servlet
{
	protected ServletConfig config;
	protected Objot objot;

	/** @set {@link #objot} */
	protected void init() throws Exception
	{
		objot = new Objot();
	}

	/** multi thread, will be cached */
	protected Servicing serviceConfig(String name, HttpServletRequest req,
		HttpServletResponse res) throws Exception
	{
		return new Servicing().init(objot, name);
	}

	/** @see Servlet#destroy() */
	public void destroy()
	{
	}

	/** @see ServletContext#log(String) */
	public void log(String hint)
	{
		config.getServletContext().log(hint);
	}

	/** @see ServletContext#log(String, Throwable) */
	public void log(Throwable e)
	{
		config.getServletContext().log("", e);
	}

	/** @see ServletContext#log(String, Throwable) */
	public void log(String hint, Throwable t)
	{
		config.getServletContext().log(hint, t);
	}

	// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

	private ConcurrentHashMap<String, Servicing> cs //
	= new ConcurrentHashMap<String, Servicing>(128, 0.8f, 32);

	public void init(ServletConfig c) throws ServletException
	{
		config = c;
		log("\n\n========########@@@@@@@@$$$$$$$$ " + ObjotServlet.class.getName()
			+ " started $$$$$$$$@@@@@@@@########========\n\n");
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

	public ServletConfig getServletConfig()
	{
		return config;
	}

	/** @return {@link #getClass()} {@link Class#getCanonicalName()} */
	public String getServletInfo()
	{
		return getClass().getCanonicalName();
	}

	public void service(ServletRequest req_, ServletResponse res_)
		throws ServletException, IOException
	{
		HttpServletRequest req = (HttpServletRequest)req_;
		HttpServletResponse res = (HttpServletResponse)res_;
		res.setContentType("text/plain; charset=UTF-8");
		res.setHeader("Cache-Control", "no-cache");

		String uri = req.getRequestURI();
		Servicing conf = null;
		char[] Q = null;
		CharSequence S = null;
		try
		{
			String name = uri.substring(uri.lastIndexOf('/') + 1);
			conf = cs.get(name);
			if (conf == null)
				cs.put(name, conf = serviceConfig(name, req, res));
			int len = req.getContentLength();
			if (len > 0)
			{
				InputStream in = req.getInputStream();
				byte[] bs = new byte[len];
				for (int from = 0, done; from < len; from += done)
					if ((done = in.read(bs, from, len - from)) < 0)
						throw new EOFException();
				Q = Objot.utf(bs);
			}
			try
			{
				S = conf.go(Q, req, res);
			}
			catch (ErrThrow e)
			{
				if (e.log)
					log(e);
				S = conf.get(e.err, req, res);
			}
			catch (Exception e)
			{
				log(e);
				S = conf.get(new Err(e), req, res);
			}
			if (S == null)
				res.setContentLength(0);
			else
			{
				byte[] bs = Objot.utf(S);
				res.setContentLength(bs.length);
				res.getOutputStream().write(bs);
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
