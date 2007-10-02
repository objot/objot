//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.container;

import objot.util.Class2;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class Bind
{
	/** not primitive */
	final Class<?> c;
	Class<? extends Annotation> scope;
	/** {@link Bind} of {@link #c}, or object of {@link #c} */
	Object b;

	/** null iif {@link #b} != this */
	Constructor<?> ct;
	/** [param index], empty if {@link #ct} null */
	Object[] cbs;

	Field[] fs;
	Object[] fbs;

	Method[] ms;
	/** [][param index] */
	Object[][] mbs;

	/** array of object or null(for {@link Bind}) */
	Object[] os;
	int maxParamN;

	private static final Class<?>[] SCOPES = Scope.class.getDeclaredClasses();

	Bind(Class<?> c_)
	{
		c = c_;
		if (c.isPrimitive())
			throw new ClassCastException("primitive " + c + " forbidden");
		Annotation a = Class2.annoExclusive(c, SCOPES);
		scope = a != null ? a.annotationType() : Scope.Private.class;
	}

	@Override
	public String toString()
	{
		return "binding of " + c;
	}
}
