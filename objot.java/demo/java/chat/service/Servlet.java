package chat.service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import objot.Objot;
import objot.servlet.ObjotServlet;
import objot.servlet.Service;


public final class Servlet
	extends ObjotServlet
{
	private static final long serialVersionUID = 1L;

	// cache needless for this simple demo
	// final ConcurrentHashMap<String, Class<?>> clas //
	// = new ConcurrentHashMap<String, Class<?>>(64, 0.8f, 32);
	// final ConcurrentHashMap<Class<?>, String> claNames //
	// = new ConcurrentHashMap<Class<?>, String>(64, 0.8f, 32);

	@Override
	public void init()
	{
		classNamePrefix = "chat.service.";
		objot = new Objot()
		{
			@Override
			protected Class<?> classByName(String name) throws Exception
			{
				Class<?> c;
				// cache needless for this simple demo
				// c = clas.get(name);
				// if (c == null)
				if (name.startsWith("chat."))
				{
					c = Class.forName("chat.model".concat(name.substring("chat".length())));
					// clas.put(name, c);
				}
				else
					throw new ClassNotFoundException(name);
				return c;
			}

			@Override
			protected String className(Class<?> c)
			{
				String n;
				// cache needless for this simple demo
				// n = claNames.get(c);
				// if (n == null)
				// {
				n = "chat".concat(c.getName().substring("chat.model".length()));
				// claNames.put(c, n);
				// }
				return n;
			}
		};
	}

	@Override
	protected Object serviceDo(Class<?> c, Method m, Object o, HttpServletRequest req,
		HttpServletResponse res) throws Throwable
	{
		try
		{
			return m.invoke(null, o, req.getSession());
		}
		catch (InvocationTargetException e)
		{
			throw e.getCause();
		}
	}

	/**
	 * syncrhonized with {@link Servlet}.class since no transaction no thread safe data
	 * session in this simple demo (see {@link Service}), this is unnecessary for
	 * Hibernate session
	 */
	@Override
	public void service(ServletRequest req_, ServletResponse res_)
		throws ServletException, IOException
	{
		synchronized (Servlet.class)
		{
			super.service(req_, res_);
		}
	}
}
