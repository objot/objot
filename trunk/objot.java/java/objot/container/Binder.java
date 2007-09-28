//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.container;

import java.lang.annotation.Annotation;
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
import objot.util.Parameter;


public class Binder
{
	private HashMap<Class<?>, Bind> binds = new HashMap<Class<?>, Bind>();

	{
		Bind b = new Bind();
		b.c = Container.class;
		b.b = b;
		scope(b);
		binds.put(b.c, b);
	}

	public final synchronized Bind bind(Class<?> c) throws Exception
	{
		Bind b = binds.get(c);
		if (b != null)
			return b;
		binds.put(c, b = new Bind());
		b.c = c;
		b.b = b; // bind to self by default, must before doing actual binding
		b.b = check(c, doBind(c));
		if (b.b == b)
			scope(b);

		if (c.getSuperclass() != null)
			bind(c.getSuperclass());

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
						s[i] = check(ps[i].cla, doBind(c, ps[i], ps[i].cla, ps[i].generic));
					b.ct = ct;
					b.cbs = s;
				}
		if (b.b == b && b.ct == null)
			try
			{
				b.cbs = Array2.OBJECTS0;
				b.ct = inject(c.getDeclaredConstructor(), false);
			}
			catch (Exception e)
			{
				throw new UnsupportedOperationException(c + //
					": one and only one constructor() or @Inject constructor expected", e);
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

	/** @return {@link Bind}, or instance, or null */
	protected Object doBind(Class<?> c) throws Exception
	{
		return bind(c);
	}

	/**
	 * @param a field or {@link Parameter}
	 * @param c field class, or parameter class
	 * @param t field generic type, or parameter generic type
	 * @return {@link Bind}, or instance, or null
	 */
	protected Object doBind(Class<?> out, AccessibleObject a, Class<?> c, Type t)
		throws Exception
	{
		return bind(c);
	}

	private static final Class<?>[] SCOPES = Scope.class.getDeclaredClasses();

	private void scope(Bind b)
	{
		Annotation a = Class2.annoExclusive(b.c, SCOPES);
		b.scope = a != null ? a.annotationType() : Scope.Private.class;
	}

	private Object check(Class<?> c, Object o)
	{
		if (c.isPrimitive())
			c = Class2.box(c, true);
		Class<?> oc = o instanceof Bind ? ((Bind)o).c : o != null ? o.getClass() : c;
		if ( !c.isAssignableFrom(oc))
			throw new ClassCastException("binding " + c + " to " + o + " forbidden");
		return o instanceof Bind ? ((Bind)o).b : o;
	}

	private <O extends AccessibleObject & Member>O inject(O o, boolean needAnno)
	{
		if (needAnno && !o.isAnnotationPresent(Inject.class))
			return null;
		if ((o.getModifiers() & (Modifier.STATIC | Modifier.PRIVATE)) != 0)
			throw new UnsupportedOperationException("injecting " + o + " forbidden");
		o.setAccessible(true); // @todo
		return o;
	}

	public final synchronized HashMap<Class<?>, Bind> toMap()
	{
		return new HashMap<Class<?>, Bind>(binds);
	}

	public final synchronized Bind[] toArray()
	{
		return Array2.from(binds.values(), Bind.class);
	}
}
