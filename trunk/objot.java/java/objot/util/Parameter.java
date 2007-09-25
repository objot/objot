package objot.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;


public class Parameter
	extends AccessibleObject
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
		ctor = (Constructor<?>)p;
		method = (Method)p;
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
			if (annos[i].getClass() == annoClass)
				return (T)annos[i];
		return null;
	}
}
