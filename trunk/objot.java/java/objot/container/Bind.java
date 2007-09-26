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
	/** null */
	Class<?> c;
	/**
	 * {@link Bind} which {@link #c} is subclass of this {@link #c}, or instance of
	 * {@link #c}
	 */
	Object b;
	/** null if {@link #b} != this */
	Class<? extends Annotation> scope;

	/** null iif {@link #b} != this */
	Constructor<?> ct;
	/** [param index], null iif {@link #b} != this */
	Object[] cb;

	Field[] fs;
	Object[] fbs;

	Method[] ms;
	/** [][param index] */
	Object[][] mbs;

	@Override
	public String toString()
	{
		return "binding of " + c;
	}
}
