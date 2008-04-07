//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU General Public License version 2
//
package objot.container;

import java.lang.annotation.Annotation;
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
		Bind.Clazz c = new Bind.Clazz();
		c.cla = Container.class;
		c.c = c;
		c.mode = c.cla.getAnnotation(Inject.Single.class).annotationType();
		(classes = new HashMap<Class<?>, Bind.Clazz>()).put(c.cla, c);
	}

	public Factory()
	{
	}

	/**
	 * @param abstractSet_ if change abstract {@link Inject.Single} class to
	 *            {@link Inject.Set}
	 */
	public Factory(boolean abstractSet_)
	{
		abstractSet = abstractSet_;
	}

	public Factory(Class<?>... clas)
	{
		for (Class<?> c: clas)
			bind(c);
	}

	/**
	 * @param abstractSet_ if change abstract {@link Inject.Single} class to
	 *            {@link Inject.Set}
	 */
	public Factory(boolean abstractSet_, Class<?>... clas)
	{
		abstractSet = abstractSet_;
		for (Class<?> c: clas)
			bind(c);
	}

	public final synchronized boolean bound(Class<?> cla)
	{
		if (cla.isPrimitive())
			cla = Class2.box(cla, false);
		return classes.containsKey(cla);
	}

	/** @param cla primitive forbidden */
	public final synchronized void bind(Class<?> cla)
	{
		Bind.Clazz c = classes.get(cla);
		if (c != null)
			return;
		if (cla.isPrimitive())
			throw new IllegalArgumentException("binding " + cla + " forbidden: primitive");
		if (cla != Container.class && Container.class.isAssignableFrom(cla))
			throw new IllegalArgumentException("binding " + cla + " forbidden");
		if ( !Mod2.match(cla, Mod2.PUBLIC))
			throw new IllegalArgumentException("binding " + cla + " forbidden: not-public");
		try
		{
			classes.put(cla, c = new Bind.Clazz());
			Annotation a = Class2.annoExclusive(cla, Inject.class);
			c.cla(cla).mode(a != null ? a.annotationType() : Inject.Single.class);
			Bind to = new Bind().cla(c.cla).mode(c.mode);
			doBind(cla, to);
			to(false, c, to);
			if (c.c != c || c.mode == null || c.mode == Inject.Set.class)
				return; // bind to other or set

			if (Mod2.match(cla, Mod2.ABSTRACT))
			{
				if ( !abstractSet || c.mode != Inject.Single.class)
					throw new IllegalArgumentException("abstract");
				c.mode = Inject.Set.class;
				return;
			}
			c.t = new Bind.T();
			c.t.t = doBind(cla, cla.getDeclaredConstructors());
			if (c.t.t.getDeclaringClass() != cla)
				throw new IllegalArgumentException(c.t.t.getName() + " in another class");
			if ( !Mod2.match(c.t.t, Mod2.PUBLIC, Mod2.STATIC))
				throw new IllegalArgumentException(Mod2.toString(c.t.t) + c.t.t.getName());
			Parameter[] ps = Parameter.gets(c.t.t);
			c.t.ps = new Bind[ps.length];
			for (int i = 0; i < ps.length; i++)
			{
				Bind b = c.t.ps[i] = new Bind().cla(ps[i].cla);
				doBind(cla, ps[i], b.cla, ps[i].generic, to = new Bind().cla(b.cla));
				to(ps[i].cla.isPrimitive(), b, to);
			}

			@SuppressWarnings("unchecked")
			ArrayList<AccessibleObject> fms0 = (ArrayList)Class2.fields(cla, 0, 0, 0);
			fms0.addAll(Class2.methods(cla, 0, 0, 0));
			AccessibleObject[] fms = doBind(cla, Array2.from(fms0, AccessibleObject.class));
			fms0.clear();
			for (AccessibleObject fm: fms)
				if (fm != null)
					if ( !((Member)fm).getDeclaringClass().isAssignableFrom(cla))
						throw new IllegalArgumentException(fm + " in another class");
					else if ( !Mod2.match(fm, Mod2.PUBLIC, Mod2.STATIC))
						throw new IllegalArgumentException(Mod2.toString(fm)
							+ ((Member)fm).getName());
					else
						fms0.add(fm);
			fms = Array2.from(fms0, AccessibleObject.class);
			c.fms = new Bind.FM[fms.length];
			for (int i = 0; i < fms.length; i++)
				if (fms[i] instanceof Field)
				{
					Bind.FM f = c.fms[i] = new Bind.FM();
					f.f = (Field)fms[i];
					f.cla(f.f.getType());
					doBind(cla, f.f, f.cla, f.f.getGenericType(), to = new Bind().cla(f.cla));
					to(f.f.getType().isPrimitive(), f, to);
				}
				else
				{
					Bind.FM m = c.fms[i] = new Bind.FM();
					m.m = (Method)fms[i];
					ps = Parameter.gets(m.m);
					m.ps = new Bind[ps.length];
					for (int j = 0; j < ps.length; j++)
					{
						Bind b = m.ps[j] = new Bind().cla(ps[j].cla);
						doBind(cla, ps[j], b.cla, ps[j].generic, to = new Bind().cla(b.cla));
						to(ps[j].cla.isPrimitive(), b, to);
					}
				}
		}
		catch (Error e)
		{
			classes.remove(cla);
			throw e;
		}
		catch (Throwable e)
		{
			classes.remove(cla);
			throw new IllegalArgumentException("binding " + cla + " forbidden: "
				+ (e.getMessage() != null ? e.getMessage() : ""), e);
		}
	}

	/**
	 * bind to another class, or an static object, or parent container. circular
	 * dependences from {@link Inject.New} classes must be avoided since it causes stack
	 * overflow.
	 * 
	 * @return ignored, just for convenience
	 */
	protected Object doBind(Class<?> c, Bind b) throws Exception
	{
		return null;
	}

	/** choose a constructor for creation and injection */
	protected Constructor<?> doBind(Class<?> c, Constructor<?>[] ts) throws Exception
	{
		for (Constructor<?> t: ts)
			if (t.isAnnotationPresent(Inject.class))
				return t;
		return c.getDeclaredConstructor();
	}

	/**
	 * determine which fields and methods need injection.
	 * 
	 * @param fms array of fields and methods
	 * @return array of field and method, each need injection, or of null to ignore that
	 *         field or method
	 */
	protected AccessibleObject[] doBind(Class<?> c, AccessibleObject[] fms) throws Exception
	{
		for (int i = 0; i < fms.length; i++)
			if ( !fms[i].isAnnotationPresent(Inject.class))
				fms[i] = null;
		return fms;
	}

	/**
	 * bind fields and parameters to something. circular dependences from constructor
	 * parameters must be avoided since it causes stack overflow.
	 * 
	 * @param cc the binding class
	 * @param fp {@link Field} or {@link Parameter}
	 * @param c {@link Field#getType()} or {@link Parameter#cla}, same as
	 *            {@link Bind#cla}, primitive boxed
	 * @param generic {@link Field#getGenericType()} or {@link Parameter#generic}
	 * @return ignored, just for convenience
	 */
	protected Object doBind(Class<?> cc, AccessibleObject fp, Class<?> c, Type generic, Bind b)
		throws Exception
	{
		return null;
	}

	/** eager see {@link #create(Container, boolean)} */
	public final Container create(Container parent) throws Exception
	{
		return create(parent, false);
	}

	/**
	 * Create a eager or lazy container with specified parent.
	 * <dl>
	 * <dt>eager, recommended
	 * <dd>all instances in {@link Inject.Single} mode are created while creating
	 * container. thread safe, but for the classes bound to parent container, it depends
	 * on parent.</dd>
	 * <dt>lazy
	 * <dd>all instances in {@link Inject.Single} mode are created at first demand, not
	 * thread safe.</dd>
	 * </dl>
	 * Note that circular dependences from constructor parameters or {@link Inject.New}
	 * classes must be avoided since it causes stack overflow.
	 */
	public final synchronized Container create(Container parent, boolean lazy_)
		throws Exception
	{
		if (con != null && classes.size() == bindN && lazy == lazy_)
			return con.create(parent);
		Bind.Clazz[] cs = new Bind.Clazz[classes.size() + 1];
		int i = 1;
		for (Bind.Clazz c: classes.values())
			cs[i++] = c;
		for (boolean ok = true; !(ok = !ok);)
			for (Bind.Clazz c: cs)
				ok |= c != null && c.c != bindSpread(c).c;
		ArrayList<Object> os = new ArrayList<Object>();
		for (Bind.Clazz c: cs)
			if (c != null && c.os == null)
			{
				os.add(c.obj);
				if (c.t != null)
				{
					for (Bind p: c.t.ps)
						if (bindSpread(p).c == null)
							os.add(p.obj);
					c.maxParamN = c.t.ps.length;
					for (Bind.FM fm: c.fms)
						if (fm.m != null)
						{
							for (Bind p: fm.ps)
								if (bindSpread(p).c == null)
									os.add(p.obj);
							c.maxParamN = Math.max(c.maxParamN, fm.ps.length);
						}
						else if (bindSpread(fm).c == null)
							os.add(fm.obj);
				}
				c.os = os.toArray();
				os.clear();
			}
		con = new Factoring().create(cs, lazy_); // parent not inited
		bindN = cs.length - 1;
		lazy = lazy_;
		return con.create(parent);
	}

	private boolean abstractSet;
	private HashMap<Class<?>, Bind.Clazz> classes;
	private int bindN;
	private boolean lazy;
	private Container con;

	/** @param b its {@link Bind#cla} may be unboxed */
	private void to(boolean primitive, Bind b, Bind to) throws Exception
	{
		if (to.cla == null)
			if (to.obj == null || b.cla.isAssignableFrom(to.obj.getClass())
				|| b.cla.isArray() && to.obj instanceof Integer)
				b.obj = to.obj; // b.cla unchanged
			else
				throw new ClassCastException(b.cla + ": " + Class2.systemIdentity(to.obj));
		else
		{
			if ( !b.cla.isAssignableFrom(to.cla))
				throw new ClassCastException(b.cla + ": " + to.cla);
			if (to.mode != null && to.mode.getDeclaringClass() != Inject.class)
				throw new IllegalArgumentException("mode " + to.mode);
			bind(to.cla);
			b.c = classes.get(to.cla);
			b.mode = to.mode;
		}
		if (primitive)
			b.cla = Class2.unbox(b.cla, false);
	}

	private Bind bindSpread(Bind b)
	{
		if (b.c != b && b.c != null)
		{
			b.obj = b.c.obj;
			b.c = b.c.c;
		}
		return b;
	}
}
