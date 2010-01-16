//
// Copyright 2007-2010 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;


public class Parameter
	extends AccessibleObject
	implements Member
{
	public static Parameter[] gets(Member ctorMethod)
	{
		Constructor<?> c = ctorMethod instanceof Method ? null : (Constructor<?>)ctorMethod;
		Method m = ctorMethod instanceof Method ? (Method)ctorMethod : null;
		Class<?>[] cs = c != null ? c.getParameterTypes() : m.getParameterTypes();
		Type[] gs = c != null ? c.getGenericParameterTypes() : m.getGenericParameterTypes();
		Annotation[][] ass = c != null ? c.getParameterAnnotations() //
			: m.getParameterAnnotations();
		Parameter[] ps = new Parameter[cs.length];
		for (int i = 0; i < ps.length; i++)
			ps[i] = new Parameter(ctorMethod, c, m, i, cs[i], gs[i], ass[i]);
		return ps;
	}

	public static Parameter get(Member ctorMethod, int index)
	{
		Constructor<?> c = ctorMethod instanceof Method ? null : (Constructor<?>)ctorMethod;
		Method m = ctorMethod instanceof Method ? (Method)ctorMethod : null;
		Class<?>[] cs = c != null ? c.getParameterTypes() : m.getParameterTypes();
		Type[] gs = c != null ? c.getGenericParameterTypes() : m.getGenericParameterTypes();
		Annotation[][] ass = c != null ? c.getParameterAnnotations() //
			: m.getParameterAnnotations();
		return new Parameter(ctorMethod, c, m, index, cs[index], gs[index], ass[index]);
	}

	/** {@link #ctor} or {@link #method} */
	public final Member proc;
	public final Constructor<?> ctor;
	public final Method method;
	public final int index;
	public final Class<?> cla;
	public final Type generic;
	protected final Annotation[] annos;

	protected Parameter(Member p, Constructor<?> c, Method m, int x, Class<?> z, Type g,
		Annotation[] as)
	{
		proc = p;
		ctor = c;
		method = m;
		index = x;
		cla = z;
		generic = g;
		annos = as;
	}

	@Override
	public Annotation[] getDeclaredAnnotations()
	{
		return annos.clone();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Annotation>T getAnnotation(Class<T> annoClass)
	{
		for (int i = 0; i < annos.length; i++)
			if (annos[i].annotationType() == annoClass)
				return (T)annos[i];
		return null;
	}

	public Class<?> getDeclaringClass()
	{
		return proc.getDeclaringClass();
	}

	public int getModifiers()
	{
		return 0;
	}

	public String getName()
	{
		return "param" + index;
	}

	public boolean isSynthetic()
	{
		return false;
	}

	@Override
	public boolean equals(Object o)
	{
		if (o instanceof Parameter)
		{
			Parameter p = (Parameter)o;
			return (proc == p.proc || proc.equals(p.proc)) && index == p.index;
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return proc.hashCode() ^ index;
	}

	@Override
	public String toString()
	{
		return proc.toString() + '[' + index + ']';
	}
}
