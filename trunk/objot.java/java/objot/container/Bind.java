//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.container;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class Bind
{
	/** primitive boxed, except for {@link T#ps}, {@link F} and {@link M#ps} */
	public Class<?> cla;
	Clazz b;
	public Object obj;
	/** null to bind class to outer container */
	public Class<? extends Annotation> mode;

	public Bind set(Object o)
	{
		cla = null;
		obj = o;
		return this;
	}

	public Bind set(Class<?> c, Class<? extends Annotation> m)
	{
		cla = c;
		mode = m;
		return this;
	}

	static final class Clazz
		extends Bind
	{
		/** null iif {@link #b} != this */
		T t;
		F[] fs;
		M[] ms;

		/** array of {@link #obj} */
		Object[] os;
		int maxParamN;

		@Override
		public String toString()
		{
			return "binding of " + cla;
		}
	}

	static final class T
	{
		Constructor<?> t;
		Bind[] ps;
	}

	static final class F
		extends Bind
	{
		Field f;
	}

	static final class M
	{
		Method m;
		Bind[] ps;
	}
}
