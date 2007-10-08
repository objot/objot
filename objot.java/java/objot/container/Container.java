//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.container;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import objot.bytecode.Bytecode;
import objot.util.Bytes;
import objot.util.Class2;


@Inject.Single
public abstract class Container
{
	/** thread-safe */
	public final Container outer()
	{
		return outer == NULL ? null : outer;
	}

	/** thread-safe */
	public final Container outest()
	{
		Container c = this;
		while (c.outer != NULL)
			c = c.outer;
		return c;
	}

	/** create container of specified outer, thread-safe. */
	public final Container create(Container outer_)
	{
		Container c = create0(index(Container.class), false);
		c.outer = outer_ != null ? outer_ : NULL;
		return c;
	}

	/**
	 * create container of same outer, same as <code>create(Container.class)</code>,
	 * thread-safe.
	 */
	public final Container create()
	{
		return create0(index(Container.class), false);
	}

	/** mostly not thread-safe except for some class */
	public final <T>T get(Class<T> c)
	{
		int i = index(c);
		return i > 0 ? this.<T>get0(i) : i < 0 ? this.<T>create0(i, false) : outer.get(c);
	}

	/** mostly not thread-safe except for some class */
	public final <T>T create(Class<T> c)
	{
		int i = index(c);
		return i != 0 ? this.<T>create0(i, false) : outer.create(c);
	}

	Container outer;
	Object[][] objss;
	static final Field F_outer = Class2.declaredField(Container.class, "outer");
	static final Field F_objss = Class2.declaredField(Container.class, "objss");

	/**
	 * Example:
	 * 
	 * <pre>
	 * switch(c.hashCode() % 15) {
	 *     2: if (c == Container.class) return 1;
	 *        if (c == A.class) return 3;
	 *        return 0;
	 *     6: if (c == B.class) return -2; // {@link Inject.New}
	 *        if (c == D.class) return 5; // bind to outer
	 *        return 0;
	 *     default: return 0;
	 *   }
	 * }</pre>
	 */
	abstract int index(Class<?> c);

	static final Bytes NAME_index = Bytecode.utf("index");
	static final Bytes DESC_index = Bytecode.utf(Class2.descript( //
		Class2.declaredMethod1(Container.class, "index")));

	/**
	 * Example:
	 * 
	 * <pre>
	 * switch(i) {
	 *   1: return this; // {@link Container}
	 *   2: return objss[0][0]; // bind to object
	 *   3: return o3 != null ? o3 : create0(i, true); // @{@link Inject.Single}
	 *   4: if (o4 != null) return o4; // catch, degraded if bind to null in outers
	 *      for (Container c = outer; ; c = c.outer) {
	 *        int j = c.index(X.class);
	 *        if (j > 0) return o4 = c.get0(j);
	 *        if (j < 0) return c.create0(j, false);
	 *      } // bind to outer
	 *   default: return null; // never happen
	 * }</pre>
	 */
	abstract <T>T get0(int index);

	static final Method M_get0 = Class2.declaredMethod1(Container.class, "get0");

	/**
	 * Example:
	 * 
	 * <pre>
	 * switch(i) {
	 *   1: Container123 o = new Container123();
	 *      o.outer = outer;
	 *      o.objss = objss;
	 *      return o;
	 *   -2: A o = new A((A1)get0(5), (Object)create0(-7, false), (A4)objss[0][1]);
	 *      if (save)
	 *        o0 = o;
	 *      o.x = (Ax)get0(3); // bind to index 3
	 *      o.y = (Ay)objss[0][2]; // bind to object
	 *      o.p((Ap)objss[0][3]);
	 *      o.q((int)(Integer)get0(1));
	 *      return o;
	 *   3: ...
	 *   default: return null; // never happen
	 * }</pre>
	 */
	abstract <T>T create0(int index, boolean save);

	static final Method M_create0 = Class2.declaredMethod1(Container.class, "create0");

	private static final Container NULL = new Container()
	{
		@Override
		int index(Class<?> c)
		{
			throw new ClassCastException(c + " unbound");
		}

		@Override
		<T>T get0(int index)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		<T>T create0(int index, boolean save)
		{
			throw new UnsupportedOperationException();
		}
	};
}
