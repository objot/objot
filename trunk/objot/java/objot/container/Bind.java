//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Library General Public License version 2
//
package objot.container;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import objot.util.Class2;


public class Bind
{
	/** null for static object */
	public Class<?> cla;
	/** box class, just for convenience */
	public Class<?> box;
	public Class<? extends Annotation> mode;
	/** the static object */
	public Object obj;
	/**
	 * actual bind.
	 * <dd>if != this, mode ignored.
	 * <dd>if == ({@link Clazz})this, null mode if static object after bind.
	 * <dd>if == ({@link Bind})this, static object for fields and parameters.
	 */
	Bind b;

	Bind()
	{
	}

	/** bind to a class */
	public Bind cla(Class<?> c)
	{
		cla = c;
		box = Class2.boxTry(cla, true);
		return this;
	}

	/** bind in a mode, ignored if bind to other classes or bind fields or parameters */
	public Bind mode(Class<? extends Annotation> m)
	{
		mode = m;
		return this;
	}

	/** bind to a static object */
	public Bind obj(Object o)
	{
		cla = null;
		obj = o;
		return this;
	}

	static final class Clazz
		extends Bind
	{
		/** iif actual bind && ({@link Inject.New} || {@link Inject.Single}) */
		T t;
		FM[] fms;

		/** array of {@link #obj} */
		Object[] os;
		int maxParamN;

		@Override
		public String toString()
		{
			return "binding of " + cla;
		}
	}

	/** actual binds for {@link Factoring} */
	static final class T
	{
		Constructor<?> t;
		/** actual binds for {@link Factoring} */
		Bind[] ps;
	}

	/** actual binds for {@link Factoring} */
	static final class FM
		extends Bind
	{
		Field f;
		Method m;
		/** actual binds for {@link Factoring} */
		Bind[] ps;
	}
}
