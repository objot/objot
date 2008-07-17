//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Library General Public License version 2
//
package objot.service;

import java.io.InputStream;
import java.lang.reflect.Method;

import objot.codec.Codec;
import objot.util.Array2;
import objot.util.Class2;


public class ServiceInfo
{
	public final String name;
	public final Class<?> cla;
	public final Method meth;
	public final Class<?>[] reqClas;
	public final Class<?>[] reqBoxClas;
	public final boolean reqUpload;

	public ServiceInfo(Codec c, String name_, Method m) throws Exception
	{
		name = name_;
		cla = m.getDeclaringClass();
		meth = m;
		Class<?>[] qs = m.getParameterTypes();
		if (qs.length == 0)
		{
			reqClas = qs;
			reqBoxClas = reqClas;
			reqUpload = false;
		}
		else
		{
			reqUpload = InputStream.class.isAssignableFrom(qs[qs.length - 1]);
			if (reqUpload)
				reqClas = Array2.subClone(qs, 0, qs.length - 1);
			else
				reqClas = qs;
			reqBoxClas = new Class[reqClas.length];
			for (int i = 0; i < reqClas.length; i++)
				reqBoxClas[i] = Class2.boxTry(reqClas[i], true);
		}
	}
}
