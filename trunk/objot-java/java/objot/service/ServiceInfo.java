//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU General Public License version 2
//
package objot.service;

import java.lang.reflect.Method;

import objot.codec.Codec;
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
}
