//
// Copyright 2007 Qianyan Cai
// Under the terms of the GNU General Public License version 2
//
package objot.container;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import objot.util.Class2;


public class Bind
{
	public Object obj;
	/** primitive boxed */
	public Class<?> cla;
	public Class<? extends Annotation> mode;
	Clazz b;

	/** bind to a specified object */
	public Bind obj(Object o)
	{
		cla = null;
		obj = o;
		return this;
	}

	/**
	 * bind to a specified class
	 * 
	 * @param c primitives will be boxed
	 */
	public Bind cla(Class<?> c)
	{
		cla = c.isPrimitive() ? Class2.box(c, false) : c;
		return this;
	}

	/**
	 * If {@link #cla} unchanged, bind class in a specified mode (e.g. {@link Inject.New},
	 * or bind to parent container if null
	 */
	public Bind mode(Class<? extends Annotation> m)
	{
		mode = m;
		return this;
	}

	static final class Clazz
		extends Bind
	{
		/** null iif {@link #b} != this */
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

	static final class T
	{
		Constructor<?> t;
		/** {@link #cla} should be original one for {@link Factoring} */
		Bind[] ps;
	}

	/** {@link #cla} should be original one for {@link Factoring} */
	static final class FM
		extends Bind
	{
		Field f;
		Method m;
		/** {@link #cla} should be original one for {@link Factoring} */
		Bind[] ps;
	}
}
