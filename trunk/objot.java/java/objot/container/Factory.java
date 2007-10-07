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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import objot.util.Array2;
import objot.util.Class2;
import objot.util.Mod2;
import objot.util.Parameter;


public class Factory
{
	{
		Bind b = new Bind(Container.class);
		b.b = b;
		(binds = new HashMap<Class<?>, Bind>()).put(b.c, b);
	}

	public final synchronized Bind bind(Class<?> c) throws Exception
	{
		Bind b = binds.get(c);
		if (b != null)
			return b;
		binds.put(c, b = new Bind(c));
		b.b = check(c, doBind(c));
		if (b.b == b && Mod2.match(c, Mod2.ABSTRACT))
			throw new IllegalArgumentException("binding to abstract " + c.getName()
				+ " forbidden");

		b.tbs = Array2.OBJECTS0;
		if (b.b == b)
			for (Constructor<?> t: c.getDeclaredConstructors())
				if (inject(t, true) != null)
				{
					if (b.t != null)
						throw new IllegalArgumentException("binding constructors " + b.t
							+ " and " + t + " forbidden");
					b.t = t;
					Parameter[] ps = Parameter.gets(t);
					b.tbs = new Object[ps.length];
					for (int i = 0; i < ps.length; i++)
						b.tbs[i] = check(ps[i].cla,
							doBind(c, ps[i], ps[i].cla, ps[i].generic));
				}
		if (b.b == b && b.t == null)
			try
			{
				b.t = inject(c.getDeclaredConstructor(), false);
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException(c
					+ ": exact one constructor() or @Inject constructor expected", e);
			}

		ArrayList<Field> fs = new ArrayList<Field>();
		ArrayList<Object> fbs = new ArrayList<Object>();
		for (Field f: Class2.fields(c, 0, 0, 0))
			if (inject(f, true) != null)
			{
				fs.add(f);
				fbs.add(check(f.getType(), doBind(c, f, f.getType(), f.getGenericType())));
			}
		b.fs = Array2.from(fs, Field.class);
		b.fbs = fbs.toArray();

		ArrayList<Method> ms = new ArrayList<Method>();
		ArrayList<Object[]> mbs = new ArrayList<Object[]>();
		for (Method m: Class2.methods(c, 0, 0, 0))
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

	public final synchronized Container createOutest(Container upper) throws Exception
	{
		if (con != null && binds.size() == bindN)
			return con.createOutest(upper);
		Bind[] bs = Array2.from(binds.values(), Bind.class);
		// let b.b = b or object if b is Bind
		for (boolean ok = true; !(ok = !ok);)
			for (Bind b: bs)
				if (b.b != b && b.b instanceof Bind && b.b != ((Bind)b.b).b)
				{
					b.b = ((Bind)b.b).b;
					ok = true;
				}
		ArrayList<Object> os = new ArrayList<Object>();
		for (Bind b: bs)
			if (b.os == null && b.tbs != null)
			{
				os.add(b.b instanceof Bind ? null : b.b);
				for (int i = 0; i < b.tbs.length; i++)
					b.tbs[i] = bound(b.tbs[i], os);
				for (int i = 0; i < b.fbs.length; i++)
					b.fbs[i] = bound(b.fbs[i], os);
				b.maxParamN = Math.max(b.tbs.length, b.fbs.length > 0 ? 1 : 0);
				for (int i = 0; i < b.mbs.length; i++)
				{
					for (int j = 0; j < b.mbs[i].length; j++)
						b.mbs[i][j] = bound(b.mbs[i][j], os);
					b.maxParamN = Math.max(b.maxParamN, b.mbs[i].length);
				}
				b.os = os.toArray();
				os.clear();
			}
		con = new Factoring().create(bs);
		bindN = bs.length;
		return con.createOutest(upper);
	}

	private HashMap<Class<?>, Bind> binds;
	private int bindN;
	private Container con;

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
		if ( !Mod2.match(o, Mod2.PUBLIC, Mod2.STATIC))
			throw new IllegalArgumentException("injecting " + Mod2.toString(o) + o
				+ " forbidden");
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
