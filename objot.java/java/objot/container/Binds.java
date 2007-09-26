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

	{
		Bind b = new Bind();
		b.c = Container.class;
		binds.put(b.c, b);
	}

	public final Bind bind(Class<?> c) throws Exception
	{
		Bind b = binds.get(c);
		if (b != null)
			return b;
		binds.put(c, b = new Bind());
		b.c = c;
		b.b = check(c, doBind(c));
		if (b.b == b)
			b.scope = Class2.annoExclusive(c, SCOPES).annotationType();

		if (b.b == b)
			for (Constructor<?> ct: c.getDeclaredConstructors())
				if (inject(ct, true) != null)
				{
					if (b.ct != null)
						throw new UnsupportedOperationException("binding constructors "
							+ b.ct + " and " + ct + " forbidden");
					Parameter[] ps = Parameter.gets(ct);
					Object[] s = new Object[ps.length];
					for (int i = 0; i < ps.length; i++)
						s[i] = check(ps[i].cla, doBind(ps[i], ps[i].cla, ps[i].generic));
					b.ct = ct;
					b.cb = s;
				}
		if (b.b == b && b.ct == null)
			try
			{
				b.cb = Array2.OBJECTS0;
				b.ct = inject(c.getDeclaredConstructor(), false);
			}
			catch (Exception e)
			{
				throw new UnsupportedOperationException(
					"one and only one constructor() or @Inject constructor expected", e);
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
			if (inject(f, true) != null)
			{
				fs.add(f);
				fbs.add(check(f.getType(), doBind(f, f.getType(), f.getGenericType())));
			}
		int supMn = ms.size();
		M: for (Method m: c.getDeclaredMethods())
			if (inject(m, true) != null)
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

	/** @return {@link Bind}, or instance, or null */
	protected Object doBind(Class<?> c) throws Exception
	{
		return bind(c);
	}

	/**
	 * @param anno field or {@link Parameter}
	 * @param c field class, or parameter class
	 * @param t field generic type, or parameter generic type
	 * @return {@link Bind}, or instance, or null
	 */
	protected Object doBind(AnnotatedElement anno, Class<?> c, Type t) throws Exception
	{
		return bind(c);
	}

	private static final Class<?>[] SCOPES = Scope.class.getDeclaredClasses();

	private Object check(Class<?> c, Object o)
	{
		Class<?> oc = o instanceof Bind ? ((Bind)o).c : o != null ? o.getClass() : c;
		if ( !c.isAssignableFrom(oc))
			throw new ClassCastException("binding " + c + " to " + o + " forbidden");
		return o;
	}

	private <O extends AccessibleObject & Member>O inject(O o, boolean needAnno)
	{
		if (needAnno && !o.isAnnotationPresent(Inject.class))
			return null;
		if ((o.getModifiers() & (Modifier.STATIC | Modifier.PRIVATE)) != 0)
			throw new UnsupportedOperationException("injecting " + o + " forbidden");
		return o;
	}
}
