//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.servlet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import objot.Err;
import objot.ErrThrow;
import objot.Getting;
import objot.Objot;
import objot.Setting;


public class Servicing
{
	protected String methodNameDefault = "service";

	public Objot objot;
	public String name;
	public Class<?> cla;
	public Method meth;
	public Class<?>[] reqClas;

	public Servicing init(Objot o, String name_) throws Exception
	{
		objot = o;
		name = name_;
		int _ = name.lastIndexOf('-');
		return init(_ < 0 ? name : name.substring(0, _), _ < 0 || _ >= name.length() - 1
			? methodNameDefault : name.substring(_ + 1));
	}

	public Servicing init(String claName, String methName) throws Exception
	{
		cla = Class.forName(claName);
		for (Method m: cla.getMethods())
			if (m.getName().equals(methName) && m.isAnnotationPresent(Service.class))
			{
				meth = m;
				reqClas = m.getParameterTypes();
				return this;
			}
		throw new Exception("no service found : ".concat(name));
	}

	public byte[] get(Object o, HttpServletRequest req, HttpServletResponse res)
		throws Exception
	{
		try
		{
			return Getting.go(objot, cla, o);
		}
		catch (Exception e)
		{
			return Getting.go(objot, cla, new Err(e));
		}
	}

	public byte[] Do(byte[] reqBs, HttpServletRequest req, HttpServletResponse res)
		throws ErrThrow, Exception
	{
		if (reqBs == null)
			return Do(null, req, res);
		return Do(null, req, res, Setting.go(objot, reqClas[0], cla, reqBs));
	}

	public byte[] Do(Object service, HttpServletRequest req, HttpServletResponse res,
		Object... reqOs) throws ErrThrow, Exception
	{
		try
		{
			return get(meth.invoke(service, reqOs), req, res);
		}
		catch (IllegalArgumentException e)
		{
			StringBuilder s = new StringBuilder("can not apply (");
			for (int p = 0; p < reqOs.length; p++)
				s.append(p == 0 ? "" : ", ").append(
					reqOs[p] == null ? "null" : reqOs[p].getClass().getCanonicalName());
			s.append(") to ").append(name).append(e.getMessage() != null ? " : " : "")
				.append(e.getMessage() != null ? e.getMessage() : "");
			throw new ErrThrow(null, s.toString());
		}
		catch (InvocationTargetException e)
		{
			Throwable _ = e.getCause();
			throw _ instanceof ErrThrow ? ((ErrThrow)_) : new ErrThrow(null, _);
		}
	}
}
