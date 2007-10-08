//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.container;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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
		Bind.Clazz c = new Bind.Clazz();
		c.set(Container.class, c.cla.getAnnotation(Inject.Single.class).annotationType());
		(classes = new HashMap<Class<?>, Bind.Clazz>()).put(c.cla, c);
	}

	public final synchronized void bind(Class<?> cla) throws Exception
	{
		if (cla.isPrimitive())
			cla = Class2.box(cla, false);
		Bind.Clazz c = classes.get(cla);
		if (c != null)
			return;
		if (cla != Container.class && Container.class.isAssignableFrom(cla))
			throw new IllegalArgumentException("binding " + cla + " forbidden");
		if ( !Mod2.match(cla, Mod2.PUBLIC))
			throw new IllegalArgumentException("binding not-public " + cla + " forbidden");
		try
		{
			classes.put(cla, c = new Bind.Clazz());
			Annotation a = Class2.annoExclusive(cla, Inject.class);
			c.set(cla, a != null ? a.annotationType() : Inject.Single.class);
			Bind to = new Bind().set(c.cla, c.mode);
			doBind(cla, to);
			to(c, to);
			if (c.b == c && Mod2.match(cla, Mod2.ABSTRACT))
				throw new IllegalArgumentException("binding to abstract " + cla.getName()
					+ " forbidden");

			if (c.b == c && c.mode != null)
			{
				c.t = new Bind.T();
				try
				{
					c.t.t = doBind(cla, cla.getDeclaredConstructors());
					if (c.t.t.getDeclaringClass() != cla)
						throw new IllegalArgumentException(c.t.t + " in another class");
					if ( !Mod2.match(c.t.t, Mod2.PUBLIC, Mod2.STATIC))
						throw new IllegalArgumentException(c.t.t);
				}
				catch (Exception e)
				{
					throw new IllegalArgumentException("binding " + cla
						+ " constructor forbidden", e);
				}
				Parameter[] ps = Parameter.gets(c.t.t);
				c.t.ps = new Bind[ps.length];
				for (int i = 0; i < ps.length; i++)
					c.tbs[i] = check(ps[i].cla, doBind(cla, ps[i], ps[i].cla, ps[i].generic));
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
							bs[j] = check(ps[j].cla, doBind(cla, ps[j], ps[j].cla,
								ps[j].generic));
						ms.add(m);
						mbs.add(bs);
					}
			c.ms = Array2.from(ms, Method.class);
			c.mbs = Array2.from(mbs, Object[].class);
		}
		catch (Throwable e)
		{
			classes.remove(cla);
			throw Class2.exception(e);
		}
	}

	/** @return ignored, just for convenience */
	protected Object doBind(Class<?> c, Bind b) throws Exception
	{
		return null;
	}

	protected Constructor<?> doBind(Class<?> c, Constructor<?>[] ts) throws Exception
	{
		for (Constructor<?> t: ts)
			if (t.isAnnotationPresent(Inject.class))
				return t;
		return c.getDeclaredConstructor();
	}

	/** @return array of (field or method or null) */
	protected AccessibleObject[] doBind(Class<?> c, AccessibleObject[] fms) throws Exception
	{
		for (int i = 0; i < fms.length; i++)
			if ( !fms[i].isAnnotationPresent(Inject.class))
				fms[i] = null;
		return fms;
	}

	/**
	 * @param fp {@link Field} or {@link Parameter}
	 * @param generic {@link Field#getGenericType()}, {@link Parameter#getGenericType()}
	 * @return ignored, just for convenience
	 */
	protected Object doBind(Class<?> c, AccessibleObject fp, Type generic, Bind b)
		throws Exception
	{
		return null;
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
				os.add(c.obj);
				if (c.t != null)
				{
					for (Bind p: c.t.ps)
						if (p.b == null || (p.b = p.b.b) == null)
							os.add(p.obj);
					c.maxParamN = c.t.ps.length;
				}
				for (Bind f: c.fs)
					if (f.b == null || (f.b = f.b.b) == null)
						os.add(f.obj);
				c.maxParamN = Math.max(c.maxParamN, c.fs.length > 0 ? 1 : 0);
				for (Bind.M m: c.ms)
				{
					for (Bind p: m.ps)
						if (p.b == null || (p.b = p.b.b) == null)
							os.add(p.obj);
					c.maxParamN = Math.max(c.maxParamN, m.ps.length);
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

	private void to(Bind b, Bind to) throws Exception
	{
		if (to.cla == null)
		{
			if (to.obj == null || b.cla.isAssignableFrom(to.obj.getClass())
				|| b.cla.isArray() && to.obj instanceof Integer)
				b.obj = to.obj;
			throw new ClassCastException("binding " + b.cla + " to " + to.obj + " forbidden");
		}
		if ( !b.cla.isAssignableFrom(to.cla))
			throw new ClassCastException("binding " + b.cla + " to " + to.cla + " forbidden");
		if (to.mode != null && to.mode.getDeclaringClass() != Inject.class)
			throw new IllegalArgumentException("bindding " + b.cla + " to mode " + to.mode
				+ " forbidden");
		bind(to.cla);
		b.b = classes.get(to.cla);
		b.mode = to.mode;
	}
}
