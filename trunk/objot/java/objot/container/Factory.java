//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
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
		c.b = c;
		c.mode = c.cla.getAnnotation(Inject.Single.class).annotationType();
		(classes = new HashMap<Class<?>, Bind.Clazz>()).put(c.cla, c);
	}

	public Factory()
	{
		defaultMode = Inject.Single.class;
	}

	public Factory(Class<? extends Annotation> defaultMode_)
	{
		defaultMode = defaultMode_;
		if (defaultMode == null || defaultMode.getDeclaringClass() != Inject.class)
			throw new IllegalArgumentException("mode " + defaultMode);
	}

	public final synchronized boolean bound(Class<?> cla)
	{
		return classes.containsKey(cla);
	}

	public final synchronized Factory bind(Class<?>... clas)
	{
		for (Class<?> c: clas)
			bind(c);
		return this;
	}

	/** @param cla {@link Container} subclasses and not public classes forbidden */
	public final synchronized Factory bind(Class<?> cla)
	{
		Bind.Clazz c = classes.get(cla);
		if (c != null)
			return this;
		if (Container.class.isAssignableFrom(cla))
			throw new IllegalArgumentException("concrete container " + cla);
		if ( !Mod2.match(cla, Mod2.PUBLIC))
			throw new IllegalArgumentException("not-public " + cla);
		try
		{
			classes.put(cla, c = new Bind.Clazz());
			Annotation a = Class2.annoExclusive(cla, Inject.class);
			c.cla(cla).mode(a != null ? a.annotationType() : defaultMode);
			Bind to = new Bind().cla(c.cla).mode(c.mode);
			forBind(cla, to);
			to(c, to);
			if (c.b != c || c.mode != Inject.New.class && c.mode != Inject.Single.class)
				return this;

			if (Mod2.match(cla, Mod2.ABSTRACT))
				throw new IllegalArgumentException("abstract");
			c.t = new Bind.T();
			c.t.t = forBind(cla, cla.getConstructors());
			if (c.t.t == null)
				throw new IllegalArgumentException("no constructor");
			if (c.t.t.getDeclaringClass() != cla)
				throw new IllegalArgumentException(c.t.t.getName() + " in another class");
			if ( !Mod2.match(c.t.t, Mod2.PUBLIC, Mod2.STATIC))
				throw new IllegalArgumentException(Mod2.toString(c.t.t) + c.t.t.getName());
			Parameter[] ps = Parameter.gets(c.t.t);
			c.t.ps = new Bind[ps.length];
			for (int i = 0; i < ps.length; i++)
			{
				Bind b = c.t.ps[i] = new Bind().cla(ps[i].cla);
				forBind(cla, ps[i], b.box, ps[i].generic, to = new Bind().cla(b.cla));
				to(b, to);
			}

			@SuppressWarnings("unchecked")
			ArrayList<AccessibleObject> fms0 = (ArrayList)Class2.fields(cla, 0, 0, 0);
			fms0.addAll(Class2.methods(cla, 0, 0, 0));
			AccessibleObject[] fms = forBind(cla, Array2.from(fms0, AccessibleObject.class));
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
					forBind(cla, f.f, f.cla, f.f.getGenericType(), to = new Bind().cla(f.cla));
					to(f, to);
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
						forBind(cla, ps[j], b.cla, ps[j].generic, to = new Bind().cla(b.cla));
						to(b, to);
					}
				}
			return this;
		}
		catch (Error e)
		{
			classes.remove(cla);
			throw e;
		}
		catch (Throwable e)
		{
			classes.remove(cla);
			throw new IllegalArgumentException("bind " + cla + " : "
				+ (e.getMessage() != null ? e.getMessage() : ""), e);
		}
	}

	/**
	 * bind to another class, or an static object, or parent container. circular
	 * dependences from {@link Inject.New} classes must be avoided since it causes stack
	 * overflow.
	 * 
	 * @param c primitive unboxed where {@link Bind#box} boxed
	 * @return ignored, just for convenience
	 */
	protected Object forBind(Class<?> c, Bind b) throws Exception
	{
		return null;
	}

	/** choose a constructor for creation and injection */
	protected Constructor<?> forBind(Class<?> c, Constructor<?>[] ts) throws Exception
	{
		for (Constructor<?> t: ts)
			if (t.isAnnotationPresent(Inject.class))
				return t;
		return c.getDeclaredConstructor(); // nullary ctor
	}

	/**
	 * determine which fields and methods need injection.
	 * 
	 * @param fms array of fields and methods
	 * @return array of field and method, each need injection, or of null to ignore that
	 *         field or method
	 */
	protected AccessibleObject[] forBind(Class<?> c, AccessibleObject[] fms) throws Exception
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
	 * @param c {@link Field#getType()} or {@link Parameter#cla}, same as {@link Bind#cla}
	 *            , primitive unboxed where {@link Bind#box} boxed
	 * @param generic {@link Field#getGenericType()} or {@link Parameter#generic}
	 * @param b {@link Bind#mode} ignored
	 * @return ignored, just for convenience
	 */
	protected Object forBind(Class<?> cc, AccessibleObject fp, Class<?> c, Type generic,
		Bind b) throws Exception
	{
		return null;
	}

	/** eager, see {@link #create(Container, boolean)} */
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
	 * on parent.
	 * <dt>lazy
	 * <dd>all instances in {@link Inject.Single} mode are created at first demand, not
	 * thread safe.
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
				ok |= c != null && c.b != bindSpread(c).b;
		ArrayList<Object> os = new ArrayList<Object>();
		for (Bind.Clazz c: cs)
			if (c != null && c.os == null)
			{
				os.add(c.obj); // even if not static object
				if (c.t != null)
				{
					for (Bind p: c.t.ps)
						if (bindSpread(p).b.mode == null)
							os.add(p.b.obj);
					c.maxParamN = c.t.ps.length;
					for (Bind.FM fm: c.fms)
						if (fm.m != null)
						{
							for (Bind p: fm.ps)
								if (bindSpread(p).b.mode == null)
									os.add(p.b.obj);
							c.maxParamN = Math.max(c.maxParamN, fm.ps.length);
						}
						else if (bindSpread(fm).b.mode == null)
							os.add(fm.obj);
				}
				c.os = os.toArray();
				os.clear();
			}
		con = new Factoring().create(cs, lazy_); // nothing inited
		bindN = cs.length - 1;
		lazy = lazy_;
		return con.create(parent);
	}

	private Class<? extends Annotation> defaultMode;
	private HashMap<Class<?>, Bind.Clazz> classes;
	private int bindN;
	private boolean lazy;
	private Container con;

	private void to(Bind b, Bind to) throws Exception
	{
		if (to.cla == null)
		{
			if ( !Class2.castableBox(to.obj, b.cla))
				// && (!b.cla.isArray() || !to.obj instanceof Integer))
				throw new ClassCastException(b.cla + ": obj " + Class2.systemIdentity(to.obj));
			// b.cla unchanged
			b.mode = null;
			b.obj = to.obj;
			b.b = b;
			return;
		}
		if ( !b.cla.isAssignableFrom(to.cla))
			throw new ClassCastException(b.cla + ": " + to.cla);
		if (b instanceof Bind.Clazz)
			if (to.mode == null || to.mode.getDeclaringClass() != Inject.class)
				throw new IllegalArgumentException(b.cla + ": mode " + to.mode);
		bind(to.cla);
		b.mode = to.mode;
		b.b = classes.get(to.cla);
	}

	private Bind bindSpread(Bind b)
	{
		if (b.b != b)
			b.b = b.b.b; // never circular since must bind to self or subclass
		return b;
	}
}
