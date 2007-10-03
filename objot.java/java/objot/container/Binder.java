//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.container;

import java.lang.reflect.AccessibleObject;
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
import objot.util.Mod2;
import objot.util.Parameter;


public class Binder
{
	private HashMap<Class<?>, Bind> binds = new HashMap<Class<?>, Bind>();

	{
		Bind b = new Bind(Container.class);
		b.b = b;
		binds.put(b.c, b);
	}

	public final synchronized Bind bind(Class<?> c) throws Exception
	{
		Bind b = binds.get(c);
		if (b != null)
			return b;
		binds.put(c, b = new Bind(c));
		b.b = check(c, doBind(c));
		if (c.getSuperclass() != null && c.getSuperclass() != Object.class)
			bind(c.getSuperclass());

		b.cbs = Array2.OBJECTS0;
		if (b.b == b)
			for (Constructor<?> ct: c.getDeclaredConstructors())
				if (inject(ct, true) != null)
				{
					if (b.ct != null)
						throw new UnsupportedOperationException("binding constructors "
							+ b.ct + " and " + ct + " forbidden");
					b.ct = ct;
					Parameter[] ps = Parameter.gets(ct);
					b.cbs = new Object[ps.length];
					for (int i = 0; i < ps.length; i++)
						b.cbs[i] = check(ps[i].cla,
							doBind(c, ps[i], ps[i].cla, ps[i].generic));
				}
		if (b.b == b && b.ct == null)
			try
			{
				b.ct = inject(c.getDeclaredConstructor(), false);
			}
			catch (Exception e)
			{
				throw new UnsupportedOperationException(c
					+ ": exact one constructor() or @Inject constructor expected", e);
			}

		ArrayList<Field> fs = new ArrayList<Field>();
		ArrayList<Object> fbs = new ArrayList<Object>();
		for (Field f: Class2.fields(c, 0, 0))
			if (inject(f, true) != null)
			{
				fs.add(f);
				fbs.add(check(f.getType(), doBind(c, f, f.getType(), f.getGenericType())));
			}
		b.fs = Array2.from(fs, Field.class);
		b.fbs = fbs.toArray();

		ArrayList<Method> ms = new ArrayList<Method>();
		ArrayList<Object[]> mbs = new ArrayList<Object[]>();
		for (Method m: Class2.methods(c, 0, 0))
			if (inject(m, true) != null)
			{
				Parameter[] ps = Parameter.gets(m);
				Object[] bs = new Object[ps.length];
				for (int j = 0; j < ps.length; j++)
					bs[j] = check(ps[j].cla, doBind(c, ps[j], ps[j].cla, ps[j].generic));
				ms.add(m);
				mbs.add(bs);
			}
		b.ms = Array2.from(ms, Method.class);
		b.mbs = Array2.from(mbs, Object[].class);
		return b;
	}

	/** @return {@link Bind}, or object, or null */
	protected Object doBind(Class<?> c) throws Exception
	{
		return bind(c);
	}

	/**
	 * @param a field or {@link Parameter}
	 * @param c field class, or parameter class
	 * @param t field generic type, or parameter generic type
	 * @return {@link Bind}, or object, or null
	 */
	protected Object doBind(Class<?> out, AccessibleObject a, Class<?> c, Type t)
		throws Exception
	{
		return bind(c);
	}

	/**
	 * {@link Bind#b}.{@link Bind#b b} == ({@link Bind#b} | object) if {@link Bind#b}
	 * is {@link Bind}, {@link Bind#os} generated
	 */
	public final synchronized Bind[] toArray()
	{
		Bind[] bs = Array2.from(binds.values(), Bind.class);
		for (boolean ok = true; !(ok = !ok);)
			for (Bind b: bs)
				if (b.b != b && b.b instanceof Bind && b.b != ((Bind)b.b).b)
				{
					b.b = ((Bind)b.b).b;
					ok = true;
				}
		ArrayList<Object> os = new ArrayList<Object>();
		for (Bind b: bs)
			if (b.os == null && b.cbs != null)
			{
				os.add(b.b instanceof Bind ? null : b.b);
				for (int i = 0; i < b.cbs.length; i++)
					b.cbs[i] = bound(b.cbs[i], os);
				for (int i = 0; i < b.fbs.length; i++)
					b.fbs[i] = bound(b.fbs[i], os);
				b.maxParamN = Math.max(b.cbs.length, b.fbs.length > 0 ? 1 : 0);
				for (int i = 0; i < b.mbs.length; i++)
				{
					for (int j = 0; j < b.mbs[i].length; j++)
						b.mbs[i][j] = bound(b.mbs[i][j], os);
					b.maxParamN = Math.max(b.maxParamN, b.mbs[i].length);
				}
				b.os = os.toArray();
				os.clear();
			}
		return bs;
	}

	private Object check(Class<?> c, Object o)
	{
		if (c.isPrimitive())
			c = Class2.box(c, true);
		Class<?> oc = o instanceof Bind ? ((Bind)o).c : o != null ? o.getClass() : c;
		if (c.isAssignableFrom(oc) || c.isArray() && oc == Integer.class)
			return o;
		throw new ClassCastException("binding " + c + " to " + o + " forbidden");
	}

	private <O extends AccessibleObject & Member>O inject(O o, boolean needAnno)
	{
		if (needAnno && !o.isAnnotationPresent(Inject.class))
			return null;
		if ((o.getModifiers() & Modifier.STATIC) != 0
			|| (o.getModifiers() & Modifier.PUBLIC) == 0)
			throw new IllegalArgumentException("injecting "
				+ Mod2.toString(Mod2.get(o.getModifiers(), 0)) + " " + o + " forbidden");
		o.setAccessible(true); // @todo
		return o;
	}

	private Object bound(Object o, ArrayList<Object> os)
	{
		if (o instanceof Bind)
			o = ((Bind)o).b;
		if ( !(o instanceof Bind))
			os.add(o);
		return o;
	}
}
