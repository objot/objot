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

import objot.codec.Codec;
import objot.codec.Err;
import objot.codec.ErrThrow;


public class ObjotServlet
	implements Servlet
{
	protected ServletContext context;
	protected ServletConfig config;
	protected Codec codec;

	/** set {@link #codec} */
	protected void init() throws Exception
	{
		codec = new Codec();
	}

	/** multi thread, will be cached */
	protected Serve getServe(String name, HttpServletRequest req, HttpServletResponse res)
		throws Exception
	{
		return new Serve().init(codec, name);
	}

	/** @see Servlet#destroy() */
	public void destroy()
	{
		context.log("\n\n$$$$$$$$$$$$$$$$%%%%%%%%%%%%%%%% " + getClass().getName()
			+ " ################================\n\n");
	}

	// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

	private ConcurrentHashMap<String, Serve> serves //
	= new ConcurrentHashMap<String, Serve>(128, 0.8f, 32);

	public void init(ServletConfig c) throws ServletException
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

	public ServletConfig getServletConfig()
	{
		return config;
	}

	/** @return {@link #getClass()} {@link Class#getCanonicalName()} */
	public String getServletInfo()
	{
		return getClass().getCanonicalName();
	}

	public void service(ServletRequest hReq_, ServletResponse hRes_)
		throws ServletException, IOException
	{
		HttpServletRequest hReq = (HttpServletRequest)hReq_;
		HttpServletResponse hRes = (HttpServletResponse)hRes_;
		hRes.setContentType("text/plain; charset=UTF-8");
		hRes.setHeader("Cache-Control", "no-cache");

		String uri = hReq.getRequestURI();
		Serve s = null;
		char[] req = null;
		CharSequence res = null;
		try
		{
			String name = uri.substring(uri.lastIndexOf('/') + 1);
			s = serves.get(name);
			if (s == null)
				serves.put(name, s = getServe(name, hReq, hRes));
			int len = hReq.getContentLength();
			if (len > 0)
			{
				InputStream in = hReq.getInputStream();
				byte[] bs = new byte[len];
				for (int from = 0, done; from < len; from += done)
					if ((done = in.read(bs, from, len - from)) < 0)
						throw new EOFException();
				req = Codec.utf(bs);
			}
			try
			{
				res = s.serve(req, hReq, hRes);
			}
			catch (ErrThrow e)
			{
				if (e.log)
					context.log("", e);
				res = s.get(e.err, hReq, hRes);
			}
			catch (Exception e)
			{
				context.log("", e);
				res = s.get(new Err(e), hReq, hRes);
			}
			if (res == null)
				hRes.setContentLength(0);
			else
			{
				byte[] bs = Codec.utf(res);
				hRes.setContentLength(bs.length);
				hRes.getOutputStream().write(bs);
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
