//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.container;

import objot.util.Parameter;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.HashMap;


public class Binds
{
	HashMap<Class<?>, Bind> bs = new HashMap<Class<?>, Bind>();

	public final void bind(Class<?> c) throws Exception
	{
		if (bs.get(c) != null)
			return;
		Bind b = new Bind();
		bs.put(c, b);
		b.b = doBind(c);
		if (b.b instanceof Class)
		{
			if ( !c.isAssignableFrom((Class<?>)b.b))
				throw new ClassCastException("binding " + c + " to " + b.b + " forbidden");
			b.s = c.getAnnotation(Scope.class);
			bind((Class<?>)b.b);
		}
		else if (b.b != null && !c.isAssignableFrom(b.b.getClass()))
			throw new ClassCastException("binding " + c + " to instance of " + b.b.getClass()
				+ " forbidden");
		if (c.getSuperclass() != null)
			bind(c.getSuperclass());
	}

	/** @return class, or instance, or null */
	protected Object doBind(Class<?> c) throws Exception
	{
		return c;
	}

	/**
	 * @param anno field or {@link Parameter}
	 * @param c field class, or parameter class
	 * @param t field generic type, or parameter generic type
	 * @return class, or instance, or null
	 */
	protected Object doBind(AnnotatedElement anno, Class<?> c, Type t) throws Exception
	{
		return c;
	}
}
