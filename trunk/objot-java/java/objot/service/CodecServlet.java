//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Library General Public License version 2
//
package objot.service;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
	protected static final byte[] UPLOAD_RESP_PRE = String2.utf( // for IE
	"<html>                                                          "
		+ "                                                                "
		+ "                                                                "
		+ "                                                          <body>");
	protected static final byte[] UPLOAD_SPLIT = String2.utf("%<br>");

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
		return getClass().getCanonicalName();
	}

	public void service(ServletRequest hReq, ServletResponse hResp)
		throws ServletException, IOException
	{
		HttpServletRequest hq = (HttpServletRequest)hReq;
		HttpServletResponse hp = (HttpServletResponse)hResp;
		OutputStream hpo = hp.getOutputStream();
		hp.setHeader("Cache-Control", "no-cache");
		ServiceInfo inf;
		try
		{
			String uri = URLDecoder.decode(hq.getRequestURI(), "UTF-8");
			inf = handler.getInfo(uri.substring(uri.lastIndexOf('/') + 1));
		}
		catch (RequestException e)
		{
			throw e;
		}
		char[] q = Array2.CHARS0;
		Object[] extraQs = null;

		boolean up = String2.maskNull(hq.getContentType()).startsWith("multipart/form-data");
		int len = hq.getContentLength();
		if (up)
		{
			boolean down = byte[].class.isAssignableFrom(inf.meth.getReturnType())
				|| InputStream.class.isAssignableFrom(inf.meth.getReturnType());
			if ( !down)
			{
				hp.setContentType("text/html; charset=UTF-8");
				hpo.write(UPLOAD_RESP_PRE);
				hpo.flush();
			}
			Input.Upload in = new Input.Upload(down ? hq.getInputStream() //
				: new Upload(hq.getInputStream(), len, hpo));
			if (in.next())
			{
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

		Container c = con.create(con.parent());
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
		if (p == null)
			throw null;
		if (p instanceof CharSequence)
		{
			if (up)
			{
				CharSequence r = (CharSequence)p;
				StringBuilder s = new StringBuilder();
				s.append("<pre class=ob>");
				for (int i = 0; i < r.length(); i++)
					if (r.charAt(i) == '&')
						s.append("&amp;");
					else if (r.charAt(i) == '<')
						s.append("&lt;");
					else
						s.append(r.charAt(i));
				p = s.append("</pre><br></body></html>").toString();
			}
			else
				hp.setContentType("text/plain; charset=UTF-8");
			byte[] bs = String2.utf((CharSequence)p);
			hp.setContentLength(bs.length);
			hpo.write(bs);
		}
		else if (p instanceof InputStream)
		{
			Bytes s = new Bytes((InputStream)p, true);
			hp.setContentType(c.get(String.class));
			hp.setContentLength(s.byteN());
			hpo.write(s.bytes, s.beginBi, s.byteN());
		}
		else
		{
			hp.setContentType(c.get(String.class));
			hp.setContentLength(((byte[])p).length);
			hpo.write((byte[])p);
		}
	}

	static class Upload
		extends InputStream
	{
		InputStream in;
		long n;
		long p;
		int k;
		OutputStream out;

		Upload(InputStream in_, int n_, OutputStream out_)
		{
			in = in_;
			n = n_;
			out = out_;
		}

		void progress(long x) throws IOException
		{
			p += x;
			int kk = (int)(p * 999 / n); // max 99.9%
			if (kk <= k)
				return;
			k = kk;
			if (k >= 100)
				out.write(k / 100 + '0');
			out.write(k / 10 % 10 + '0');
			out.write('.');
			out.write(k % 10 + '0');
			out.write(UPLOAD_SPLIT);
			out.flush();
		}

		@Override
		public int available() throws IOException
		{
			return in.available();
		}

		@Override
		public void close() throws IOException
		{
			in.close();
		}

		@Override
		public int read() throws IOException
		{
			progress(1);
			return in.read();
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException
		{
			int x = in.read(b, off, len);
			if (x > 0)
				progress(x);
			return x;
		}

		@Override
		public long skip(long x) throws IOException
		{
			x = in.skip(x);
			if (x > 0)
				progress(Math.max(x, n));
			return x;
		}
	}
}
