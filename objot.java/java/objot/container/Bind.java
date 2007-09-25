//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.container;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class Bind
{
	/** null */
	Class<?> c;
	/** subclass of {@link #c}, or instance of {@link #c} */
	Object b;
	Scope s;
	Field[] fs;
	Object[] fbs;
	Constructor<?> ct;
	/** [param index] */
	Object[] cb;
	Method[] ms;
	/** [][param index] */
	Object[][] mbs;

	@Override
	public String toString()
	{
		return "binding of " + c;
	}
}
