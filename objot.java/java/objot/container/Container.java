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
	public final Container upper()
	{
		return upper;
	}

	/** thread-safe */
	public final Container uppest()
	{
		Container c = this;
		while (c.upper != null)
			c = c.upper;
		return c;
	}

	/** thread-safe */
	public final Container outer()
	{
		return outer;
	}

	/** thread-safe */
	public final Container outest()
	{
		Container c = this;
		while (c.outer != null)
			c = c.outer;
		return c;
	}

	/** create outest container of specified upper, thread-safe. */
	public final Container createOutest(Container upper_)
	{
		Container c = create0(index(Container.class), false);
		c.upper = upper_;
		c.outer = null;
		return c;
	}

	/**
	 * create inner container, same as <code>create(Container.class)</code>,
	 * thread-safe.
	 */
	public final Container createInner()
	{
		return create0(index(Container.class), false);
	}

	/** mostly not thread-safe except for some class */
	public final <T>T get(Class<T> c)
	{
		int i = index(c);
		if (i < 0)
			if (upper != null)
				return upper.get(c);
			else
				throw new ClassCastException(c + " unbound");
		return get0(i);
	}

	/** mostly not thread-safe except for some class */
	public final <T>T create(Class<T> c)
	{
		int i = index(c);
		if (i < 0)
			if (upper != null)
				return upper.create(c);
			else
				throw new ClassCastException(c + " unbound");
		return create0(index(c), false);
	}

	Container upper;
	Container outer;
	Object[][] objss;
	static final Field F_upper = Class2.declaredField(Container.class, "upper");
	static final Field F_outer = Class2.declaredField(Container.class, "outer");
	static final Field F_objss = Class2.declaredField(Container.class, "objss");

	/**
	 * Example:
	 * 
	 * <pre>
	 * switch(c.hashCode() % 15) {
	 *     2: if (c == A.class) return 0;
	 *        if (c == D.class) return 2;
	 *        return -1;
	 *     6: if (c == B.class) return 1;
	 *        return -1;
	 *     default: return -1;
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
	 *   0: return objss[0][0]; // bind to object
	 *   1: return this; // {@link Container}
	 *   2: return create0(i, false); // @{@link Inject.New}
	 *   3: return o3 != null ? o3 : create0(i, true); // @{@link Inject.Single}
	 *   4: for (Container123 c = this; ; c = (Container123)c.outer)
	 *      	if (c.o4 != null) return o4 = c.o4;
	 *        else if (c.outer == null) break;
	 *      return create0(i, true); // @{@link Inject.Spread}
	 *   5: ... // like 4
	 *      return o5 = (Abc5)c.create0(i, true); // @{@link Inject.Inherit}
	 *   default: return this; // never happen
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
	 *      o.upper = upper;
	 *      o.outer = this;
	 *      o.objss = objss;
	 *      return o;
	 *   2: A o = new A((A1)get0(5), (A2)get0(7), (Object)objss[0][0], (A4)objss[0][1]);
	 *      if (save)
	 *        o0 = o;
	 *      o.x = (Ax)get0(3); // bind to index 3
	 *      o.y = (Ay)objss[0][2]; // bind to object
	 *      o.p((Ap)objss[0][3]);
	 *      o.q((int)(Integer)get0(1));
	 *      return o;
	 *   default: return null; // never happen
	 * }</pre>
	 */
	abstract <T>T create0(int index, boolean save);

	static final Method M_create0 = Class2.declaredMethod1(Container.class, "create0");
}
