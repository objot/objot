//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.container;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import objot.util.Array2;
import objot.util.Class2;
import objot.util.Parameter;


public class Binds
{
	HashMap<Class<?>, Bind> binds = new HashMap<Class<?>, Bind>();

	public final Bind bind(Class<?> c) throws Exception
	{
		Bind b = binds.get(c);
		if (b != null)
			return b;
		binds.put(c, b = new Bind());
		b.c = c;
		b.b = check(c, doBind(c));
		if (b.b instanceof Class)
		{
			b.s = c.getAnnotation(Scope.class);
			bind((Class<?>)b.b);
		}
		ArrayList<Field> fs = new ArrayList<Field>();
		ArrayList<Object> fbs = new ArrayList<Object>();
		ArrayList<Method> ms = new ArrayList<Method>();
		ArrayList<Object[]> mbs = new ArrayList<Object[]>();
		if (c.getSuperclass() != null)
		{
			Bind sup = bind(c.getSuperclass());
			Array2.addTo(sup.fs, fs);
			Array2.addTo(sup.fbs, fbs);
			Array2.addTo(sup.ms, ms);
			Array2.addTo(sup.mbs, mbs);
		}
		for (Field f: c.getDeclaredFields())
			if (inject(f))
			{
				fs.add(f);
				fbs.add(check(f.getType(), doBind(f, f.getType(), f.getGenericType())));
			}
		for (Constructor<?> ct: c.getDeclaredConstructors())
			if (inject(ct))
			{
				if (b.ct != null)
					throw new IllegalAccessException("bind multi constructors " + b.ct
						+ " and " + ct + " forbidden");
				Parameter[] ps = Parameter.gets(ct);
				Object[] s = new Object[ps.length];
				for (int i = 0; i < ps.length; i++)
					s[i] = check(ps[i].cla, doBind(ps[i], ps[i].cla, ps[i].generic));
				b.ct = ct;
				b.cb = s;
			}
		int supMn = ms.size();
		M: for (Method m: c.getDeclaredMethods())
			if (inject(m))
			{
				Parameter[] ps = Parameter.gets(m);
				Object[] bs = new Object[ps.length];
				for (int i = 0; i < ps.length; i++)
					bs[i] = check(ps[i].cla, doBind(ps[i], ps[i].cla, ps[i].generic));
				for (int i = 0; i < supMn; i++)
					if (Class2.override(m, ms.get(i)))
					{
						ms.set(i, m);
						mbs.set(i, bs);
						continue M;
					}
				ms.add(m);
				mbs.add(bs);
			}
		b.fs = Array2.from(fs, Field.class);
		b.fbs = fbs.toArray();
		b.ms = Array2.from(ms, Method.class);
		b.mbs = Array2.from(mbs, Object[].class);
		return b;
	}

	/** @return {@link Bind}, class, or instance, or null */
	protected Object doBind(Class<?> c) throws Exception
	{
		return bind(c);
	}

	/**
	 * @param anno field or {@link Parameter}
	 * @param c field class, or parameter class
	 * @param t field generic type, or parameter generic type
	 * @return {@link Bind}, class, or instance, or null
	 */
	protected Object doBind(AnnotatedElement anno, Class<?> c, Type t) throws Exception
	{
		return bind(c);
	}

	private Object check(Class<?> c, Object o)
	{
		if (o instanceof Bind)
		{
			if ( !c.isAssignableFrom(((Bind)o).c))
				throw new ClassCastException("binding " + c + " to " + o + " forbidden");
		}
		else if (o instanceof Class)
		{
			if ( !c.isAssignableFrom((Class<?>)o))
				throw new ClassCastException("binding " + c + " to " + o + " forbidden");
		}
		else if (o != null && !c.isAssignableFrom(o.getClass()))
			throw new ClassCastException("binding " + c + " to instance of " + o.getClass()
				+ " forbidden");
		return o;
	}

	private <O extends AccessibleObject & Member>boolean inject(O o) throws Exception
	{
		if ( !o.isAnnotationPresent(Inject.class))
			return false;
		if ((o.getModifiers() & (Modifier.STATIC | Modifier.PRIVATE)) != 0)
			throw new IllegalAccessException("inject " + o + " forbidden");
		return true;
	}
}
