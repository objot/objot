//
// Copyright 2007 Qianyan Cai
// Under the terms of the GNU General Public License version 2
//
package objot.servlet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import objot.codec.Codec;
import objot.codec.ErrThrow;
import objot.util.Class2;


public class ServiceInfo
{
	public final String name;
	public final Class<?> cla;
	public final Method meth;
	public final Class<?>[] reqClas;
	public final Class<?>[] reqBoxClas;

	public ServiceInfo(Codec c, String name_, Method m) throws Exception
	{
		name = name_;
		cla = m.getDeclaringClass();
		meth = m;
		reqClas = m.getParameterTypes();
		if (reqClas.length == 0)
			reqBoxClas = reqClas;
		else
		{
			reqBoxClas = reqClas.clone();
			for (int i = 0; i < reqClas.length; i++)
				if (reqClas[i].isPrimitive())
					reqBoxClas[i] = Class2.box(reqClas[i], true);
		}
	}

	public Object invoke(Object serv, Object... reqs) throws ErrThrow, Exception
	{
		try
		{
			return meth.invoke(serv, reqs);
		}
		catch (IllegalArgumentException e)
		{
			StringBuilder s = new StringBuilder("can not apply (");
			for (int p = 0; p < reqs.length; p++)
				s.append(p == 0 ? "" : ", ").append(
					reqs[p] == null ? "null" : reqs[p].getClass().getCanonicalName());
			s.append(") to ").append(name);
			s.append(e.getMessage() != null ? " : " : "").append(
				e.getMessage() != null ? e.getMessage() : "");
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
