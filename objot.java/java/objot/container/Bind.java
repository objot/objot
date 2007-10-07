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


public final class Bind
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
		if (c.isPrimitive() || c == Bind.class //
			|| c != Container.class && Container.class.isAssignableFrom(c))
			throw new IllegalArgumentException("binding " + c + " forbidden");
		if ( !Mod2.match(c, Mod2.PUBLIC))
			throw new IllegalArgumentException("binding not-public " + c + " forbidden");
		Annotation a = Class2.annoExclusive(c, MODES);
		mode = a != null ? a.annotationType() : Inject.Single.class;
	}

	public final Class<?> clazz()
	{
		return c;
	}

	public final Class<? extends Annotation> injectMode()
	{
		return mode;
	}

	public final Object clazzBind()
	{
		return b;
	}

	/** null if {@link #clazzBind} is not self */
	public final Constructor<?> ctor()
	{
		return t;
	}

	public final Object[] ctorParamBinds()
	{
		return tbs.length == 0 ? tbs : tbs.clone();
	}

	public final Field[] fields()
	{
		return fs.length == 0 ? fs : fs.clone();
	}

	public final Object[] fieldBinds()
	{
		return fbs.length == 0 ? fbs : fbs.clone();
	}

	public final Method[] methods()
	{
		return ms.length == 0 ? ms : ms.clone();
	}

	public final Object[][] methodParamBinds()
	{
		if (mbs.length == 0)
			return mbs;
		Object[][] s = mbs.clone();
		for (int i = 0; i < s.length; i++)
			s[i] = s[i].clone();
		return s;
	}

	@Override
	public String toString()
	{
		return "binding of " + c;
	}
}
