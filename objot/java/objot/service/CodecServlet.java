//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.service;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import objot.container.Container;
import objot.container.Factory;
import objot.container.Inject;
import objot.util.Array2;
import objot.util.Bytes;
import objot.util.Input;
import objot.util.String2;


public class CodecServlet
	implements Servlet
{
	public static final String PROGRESS = "$prog$";

	protected ServletConfig config;
	protected ServiceHandler handler;
	/** round container with global parent */
	protected Container con;

	public final void init(ServletConfig c) throws ServletException
	{
		try
		{
			config = c;
			handler = (ServiceHandler)Class.forName(config.getInitParameter("handler")).newInstance();
			config.getServletContext().log(
				"\n\n================################ " + getClass().getName() + " : "
					+ handler.getClass().getName() + " %%%%%%%%%%%%%%%%$$$$$$$$$$$$$$$$\n\n");

			con = new Factory(Inject.Set.class).bind(ServletConfig.class,
				ServletContext.class).create(null);
			con = new Factory(Inject.Set.class).bind(HttpServletRequest.class,
				HttpServletResponse.class, HttpSession.class, String.class).create(con);
			con.set(ServletConfig.class, c);
			con.set(ServletContext.class, c.getServletContext());

			handler = handler.init(con.parent());
		}
		catch (RuntimeException e)
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
		config.getServletContext().log(
			"\n\n$$$$$$$$$$$$$$$$%%%%%%%%%%%%%%%% " + getClass().getName() + " : "
				+ handler.getClass().getName() + " ################================\n\n");
	}

	public ServletConfig getServletConfig()
	{
		return config;
	}

	public String getServletInfo()
	{
		return getClass().getName();
	}

	public void service(ServletRequest hReq, ServletResponse hResp)
		throws ServletException, IOException
	{
		HttpServletRequest hq = (HttpServletRequest)hReq;
		HttpServletResponse hp = (HttpServletResponse)hResp;
		hp.setHeader("Cache-Control", "no-cache");
		hp.setContentType("text/plain; charset=UTF-8");

		String uri = URLDecoder.decode(hq.getRequestURI(), "UTF-8");
		String name = uri.substring(uri.lastIndexOf('/') + 1);
		if (name.equals(PROGRESS))
		{
			Input.Upload up = (Input.Upload)hq.getSession().getAttribute(
				PROGRESS.concat(String2.maskNull(hq.getQueryString())));
			byte[] bs = up == null ? Array2.BYTES0
				: String2.utf(up.progress(999) / 10f + "%");
			hp.setContentLength(bs.length);
			hp.getOutputStream().write(bs);
			return;
		}
		ServiceInfo inf;
		try
		{
			inf = handler.getInfo(name);
		}
		catch (RequestException e)
		{
			throw e;
		}
		char[] q = Array2.CHARS0;
		Object[] extraQs = null;

		String up = String2.maskNull(hq.getContentType()).startsWith("multipart/form-data")
			? "" : null;
		int len = hq.getContentLength();
		if (up != null)
		{
			Input.Upload in = new Input.Upload(hq.getInputStream());
			in.total = len;
			if (in.next())
			{
				if (in.name().length() > 0)
					hq.getSession().setAttribute(up = PROGRESS.concat(in.name()), in);
				Bytes s = new Bytes(in, false);
				len = s.byteN();
				q = String2.utf(s.bytes, s.beginBi, len);
				in.next();
			}
			if (inf.reqUpload)
				extraQs = new Object[] { in };
		}
		else if (len > 0)
		{
			InputStream in = hq.getInputStream();
			byte[] s = new byte[len];
			for (int begin = 0, done; begin < len; begin += done)
				if ((done = in.read(s, begin, len - begin)) < 0)
					throw new EOFException();
			q = String2.utf(s);
		}
		else if (hq.getQueryString() != null)
			q = URLDecoder.decode(hq.getQueryString(), "UTF-8").toCharArray();

		Container c = con.create();
		c.set(HttpServletRequest.class, hq);
		c.set(HttpServletResponse.class, hp);
		c.set(HttpSession.class, hq.getSession());
		c.set(String.class, "application/octet-stream");
		Object p;
		try
		{
			p = handler.handle(c, inf, q, 0, q.length, extraQs);
		}
		catch (RuntimeException e)
		{
			throw e;
		}
		catch (IOException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new ServletException(e);
		}
		finally
		{
			if ( !String2.empty(up))
				hq.getSession().removeAttribute(up);
		}
		if (p == null)
			throw null;
		if (p instanceof CharSequence)
		{
			if (up != null)
			{
				hp.setContentType("text/html; charset=UTF-8");
				CharSequence r = (CharSequence)p;
				StringBuilder s = new StringBuilder();
				s.append("<pre id=objot>");
				for (int i = 0; i < r.length(); i++)
					if (r.charAt(i) == '&')
						s.append("&amp;");
					else if (r.charAt(i) == '<')
						s.append("&lt;");
					else
						s.append(r.charAt(i));
				p = s.append("</pre>").toString();
			}
			byte[] bs = String2.utf((CharSequence)p);
			hp.setContentLength(bs.length);
			hp.getOutputStream().write(bs);
		}
		else if (p instanceof InputStream)
		{
			hp.setContentType(c.get(String.class));
			Input.readTo((InputStream)p, hp.getOutputStream());
		}
		else
		{
			hp.setContentType(c.get(String.class));
			hp.setContentLength(((byte[])p).length);
			hp.getOutputStream().write((byte[])p);
		}
	}
}
