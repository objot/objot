//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.container;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import objot.util.Class2;
import objot.util.Mod2;


public class Bind
{
	/** not primitive */
	final Class<?> c;
	Class<? extends Annotation> mode;
	/** {@link Bind} of {@link #c}, or object of {@link #c} */
	Object b;

	/** null iif {@link #b} != this */
	Constructor<?> t;
	/** [param index], empty if {@link #t} null */
	Object[] tbs;

	Field[] fs;
	Object[] fbs;

	Method[] ms;
	/** [][param index] */
	Object[][] mbs;

	/** array of object or null(for {@link Bind}) */
	Object[] os;
	int maxParamN;

	private static final Class<?>[] MODES = Inject.class.getDeclaredClasses();

	Bind(Class<?> c_)
	{
		c = c_;
		if (c.isPrimitive() || Bind.class.isAssignableFrom(c) //
			|| c != Container.class && Container.class.isAssignableFrom(c))
			throw new IllegalArgumentException("binding " + c + " forbidden");
		if ( !Mod2.match(c, Mod2.PUBLIC))
			throw new IllegalArgumentException("binding not-public " + c + " forbidden");
		Annotation a = Class2.annoExclusive(c, MODES);
		mode = a != null ? a.annotationType() : Inject.Single.class;
	}

	@Override
	public String toString()
	{
		return "binding of " + c;
	}
}