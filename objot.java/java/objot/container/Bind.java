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
	implements Cloneable
{
	Class<?> c;
	/** {@link Bind} of {@link #c}, or object of {@link #c} */
	Object b;
	/** null if {@link #b} != this */
	Class<? extends Annotation> scope;

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

	@Override
	public String toString()
	{
		return "binding of " + c;
	}
}
