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
		Bind.Clazz c = new Bind.Clazz(this, Container.class);
		c.b = c;
		(classes = new HashMap<Class<?>, Bind.Clazz>()).put(c.c, c);
	}

	public final synchronized Bind.Clazz bind(Class<?> cla) throws Exception
	{
		Bind.Clazz c = classes.get(cla);
		if (c != null)
			return c;
		classes.put(cla, c = new Bind.Clazz(this, cla));
		c.b = check(cla, doBind(cla));
		if (c.b == c && Mod2.match(cla, Mod2.ABSTRACT))
			throw new IllegalArgumentException("binding to abstract " + cla.getName()
				+ " forbidden");

		c.tbs = Array2.OBJECTS0;
		if (c.b == c)
			for (Constructor<?> t: cla.getDeclaredConstructors())
				if (inject(t, true) != null)
				{
					if (c.t != null)
						throw new IllegalArgumentException("binding constructors " + c.t
							+ " and " + t + " forbidden");
					c.t = t;
					Parameter[] ps = Parameter.gets(t);
					c.tbs = new Object[ps.length];
					for (int i = 0; i < ps.length; i++)
						c.tbs[i] = check(ps[i].cla, doBind(cla, ps[i], ps[i].cla,
							ps[i].generic));
				}
		if (c.b == c && c.t == null)
			try
			{
				c.t = inject(cla.getDeclaredConstructor(), false);
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException(cla
					+ ": exact one constructor() or @Inject constructor expected", e);
			}

		ArrayList<Field> fs = new ArrayList<Field>();
		ArrayList<Object> fbs = new ArrayList<Object>();
		if (c.b == c)
			for (Field f: Class2.fields(cla, 0, 0, 0))
				if (inject(f, true) != null)
				{
					fs.add(f);
					fbs.add(check(f.getType(), //
						doBind(cla, f, f.getType(), f.getGenericType())));
				}
		c.fs = Array2.from(fs, Field.class);
		c.fbs = fbs.toArray();

		ArrayList<Method> ms = new ArrayList<Method>();
		ArrayList<Object[]> mbs = new ArrayList<Object[]>();
		if (c.b == c)
			for (Method m: Class2.methods(cla, 0, 0, 0))
				if (inject(m, true) != null)
				{
					Parameter[] ps = Parameter.gets(m);
					Object[] bs = new Object[ps.length];
					for (int j = 0; j < ps.length; j++)
						bs[j] = check(ps[j].cla, doBind(cla, ps[j], ps[j].cla, ps[j].generic));
					ms.add(m);
					mbs.add(bs);
				}
		c.ms = Array2.from(ms, Method.class);
		c.mbs = Array2.from(mbs, Object[].class);
		return c;
	}

	public static final Object BIND_OUTER = new Object();

	/** @return {@link Bind}, or {@link #BIND_OUTER} or object, or null */
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

	public final synchronized Container create(Container outer) throws Exception
	{
		if (con != null && classes.size() == bindN)
			return con.create(outer);
		Bind.Clazz[] cs = Array2.from(classes.values(), Bind.Clazz.class);
		for (boolean ok = true; !(ok = !ok);)
			for (Bind.Clazz c: cs)
				if (c.b != c && c.b != null && c.b.b != c.b)
				{
					c.b = c.b.b;
					ok = true;
				}
		ArrayList<Object> os = new ArrayList<Object>();
		for (Bind.Clazz c: cs)
			if (c.os == null)
			{
				os.add(c.o);
				if (c.t != null)
					for (int i = 0; i < c.tbs.length; i++)
						c.tbs[i] = bound(c.tbs[i], os);
				for (int i = 0; i < c.fbs.length; i++)
					c.fbs[i] = bound(c.fbs[i], os);
				c.maxParamN = Math.max(c.tbs.length, c.fbs.length > 0 ? 1 : 0);
				for (int i = 0; i < c.mbs.length; i++)
				{
					for (int j = 0; j < c.mbs[i].length; j++)
						c.mbs[i][j] = bound(c.mbs[i][j], os);
					c.maxParamN = Math.max(c.maxParamN, c.mbs[i].length);
				}
				c.os = os.toArray();
				os.clear();
			}
		con = new Factoring().create(cs);
		bindN = cs.length;
		return con.create(outer);
	}

	private HashMap<Class<?>, Bind.Clazz> classes;
	private int bindN;
	private Container con;

	private Object check(Class<?> c, Object o)
	{
		if (c.isPrimitive())
			c = Class2.box(c, true);
		if (o instanceof Bind && ((Bind)o).fact != this)
			throw new IllegalArgumentException("bind in other " + getClass().getName()
				+ " forbidden");
		Class<?> oc = o instanceof Bind ? oc = ((Bind)o).c : o != null ? o.getClass() : c;
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
