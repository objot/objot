//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.servlet;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
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
import objot.Objot;


public class ObjotServlet
	extends GenericServlet
{
	protected Objot objot;

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
	protected Servicing serviceConfig(String name, HttpServletRequest req,
		HttpServletResponse res) throws Exception
	{
		return new Servicing().init(objot, name);
	}

	private static final long serialVersionUID = 1L;

	private ConcurrentHashMap<String, Servicing> cs //
	= new ConcurrentHashMap<String, Servicing>(128, 0.8f, 32);

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
		res.setContentType("application/octet-stream");
		res.setHeader("Cache-Control", "no-cache");

		String uri = req.getRequestURI();
		Servicing conf = null;
		byte[] bs = null;
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
				bs = new byte[len];
				for (int from = 0, done; from < len; from += done)
					if ((done = in.read(bs, from, len - from)) < 0)
						throw new EOFException();
			}
			try
			{
				bs = conf.Do(bs, req, res);
			}
			catch (ErrThrow e)
			{
				log(e);
				bs = conf.get(e.err, req, res);
			}
			catch (Exception e)
			{
				log(e);
				bs = conf.get(new Err(e), req, res);
			}
			if (bs == null)
				res.setContentLength(0);
			else
				res.setContentLength(bs.length);
			res.getOutputStream().write(bs);
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
