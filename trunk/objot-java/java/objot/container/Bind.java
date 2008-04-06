//
// Copyright 2007-2008 Qianyan Cai
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
	/** primitive boxed, null iff static object */
	public Class<?> cla;
	/** null for parent */
	public Class<? extends Annotation> mode;
	/** the static object */
	public Object obj;
	/** actual class, null iff static object */
	Clazz bc;

	/**
	 * bind to the class
	 * 
	 * @param c primitives will be boxed
	 */
	public Bind cla(Class<?> c)
	{
		cla = c.isPrimitive() ? Class2.box(c, false) : c;
		return this;
	}

	/**
	 * If {@link #cla} is the original binding class, bind it in the mode (e.g.
	 * {@link Inject.New}, or bind to parent container if null
	 */
	public Bind mode(Class<? extends Annotation> m)
	{
		mode = m;
		return this;
	}

	/** bind to the static object */
	public Bind obj(Object o)
	{
		cla = null;
		obj = o;
		return this;
	}

	static final class Clazz
		extends Bind
	{
		/** iif {@link #bc} == this && {@link Inject.New} || {@link Inject.Single} */
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
