//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.servlet;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import objot.codec.Codec;
import objot.codec.ErrThrow;


public class Serve
{
	protected String methodNameDefault = "service";
	protected Class<? extends Annotation> serviceAnno = Service.class;

	public Codec objot;
	public String name;
	public Class<?> cla;
	public Method meth;
	public Class<?>[] reqClas;

	public Serve init(Codec o, String name_) throws Exception
	{
		objot = o;
		name = name_;
		int _ = name.lastIndexOf('-');
		return init(_ < 0 ? name : name.substring(0, _), _ < 0 || _ >= name.length() - 1
			? methodNameDefault : name.substring(_ + 1));
	}

	public Serve init(String claName, String methName) throws Exception
	{
		cla = Class.forName(claName);
		for (Method m: cla.getMethods())
			if (m.getName().equals(methName) && m.isAnnotationPresent(serviceAnno))
			{
				meth = m;
				reqClas = m.getParameterTypes();
				return this;
			}
		throw new Exception("service not found : ".concat(name));
	}

	public CharSequence get(Object o, HttpServletRequest hReq, HttpServletResponse hRes)
		throws Exception
	{
		return objot.get(o, cla);
	}

	public CharSequence serve(char[] req, HttpServletRequest hReq, HttpServletResponse hRes)
		throws ErrThrow, Exception
	{
		if (req == null)
			return serve(null, hReq, hRes);
		return serve(null, hReq, hRes, objot.set(req, reqClas[0], cla));
	}

	public CharSequence serve(Object service, HttpServletRequest hReq,
		HttpServletResponse hRes, Object... reqs) throws ErrThrow, Exception
	{
		try
		{
			return get(meth.invoke(service, reqs), hReq, hRes);
		}
		catch (IllegalArgumentException e)
		{
			StringBuilder s = new StringBuilder("can not apply (");
			for (int p = 0; p < reqs.length; p++)
				s.append(p == 0 ? "" : ", ").append(
					reqs[p] == null ? "null" : reqs[p].getClass().getCanonicalName());
			s.append(") to ").append(name).append(e.getMessage() != null ? " : " : "")
				.append(e.getMessage() != null ? e.getMessage() : "");
			throw new ErrThrow(null, s.toString());
		}
		catch (InvocationTargetException e)
		{
			Throwable _ = e.getCause();
			if (_ instanceof ErrThrow)
				throw (ErrThrow)_;
			if (_ instanceof Exception)
				throw (Exception)_;
			if (_ instanceof Error)
				throw (Error)_;
			throw new Exception(_);
		}
	}
}
