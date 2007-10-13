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
	public final Container parent()
	{
		return parent == NULL ? null : parent;
	}

	/** thread-safe */
	public final Container rootParent()
	{
		Container c = this;
		while (c.parent != NULL)
			c = c.parent;
		return c;
	}

	/** create container with null parent, thread-safe. */
	public final Container create()
	{
		return create0(index(Container.class), NULL);
	}

	/** create container with specified parent, thread-safe. */
	public final Container create(Container parent_)
	{
		return create0(index(Container.class), parent_ != null ? parent_ : NULL);
	}

	/**
	 * create container with parents created recursively until the specified one,
	 * thread-safe
	 * 
	 * @param until must be one of the true parents, or RuntimeException thrown
	 */
	public final Container createBubble(Container until)
	{
		if (parent == (until != null ? until : NULL))
			return create0(index(Container.class), parent);
		if (parent == NULL)
			throw new RuntimeException(until + " is not a true parent");
		return create0(index(Container.class), parent.createBubble(until));
	}

	/**
	 * create container with parents created recursively until the specified one,
	 * thread-safe
	 * 
	 * @param until must be one of the true parents, or RuntimeException thrown
	 */
	public final Container createBubble(Container until, Container to)
	{
		if (parent == (until != null ? until : NULL))
			return create0(index(Container.class), to != null ? to : NULL);
		if (parent == NULL)
			throw new RuntimeException(until + " is not a true parent");
		return create0(index(Container.class), parent.createBubble(until));
	}

	/**
	 * get an instance of the class, or get this container.
	 * 
	 * @see Factory#create(Container, boolean)
	 * @throws ClassCastException if the class is not found in this and parents, or a
	 *             bound class throws.
	 */
	public final <T>T get(Class<T> c)
	{
		int i = index(c);
		return i > 0 ? this.<T>get0(i) : i < 0 ? this.<T>create0(i, parent) : parent.get(c);
	}

	/**
	 * create an instance of the class whatever {@link Inject.Single} or not, or create
	 * container with same parent.
	 * 
	 * @see Factory#create(Container, boolean)
	 * @throws ClassCastException if the class is not found in this and parents, or a
	 *             bound class throws.
	 */
	public final <T>T create(Class<T> c)
	{
		int i = index(c);
		return i != 0 ? this.<T>create0(i, parent) : parent.create(c);
	}

	/** @return whether class is bound in this container */
	public boolean bound(Class<?> c)
	{
		return index(c) != 0;
	}

	/** @return which container bind the class, null if no one */
	public Container boundIn(Class<?> c)
	{
		int i = index(c);
		return i != 0 ? this : parent.boundIn(c);
	}

	Container parent;
	static final Field F_parent = Class2.declaredField(Container.class, "parent");

	/**
	 * Example:
	 * 
	 * <pre>
	 * switch(c.hashCode() % 31) {
	 *     2: if (c == Container.class) return 1;
	 *        if (c == A.class) return 3;
	 *        return 0;
	 *     6: if (c == B.class) return -2; // {@link Inject.New}
	 *        if (c == D.class) return 5; // bind to parent
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
	 *   3: return o3 != null ? o3 : create0(i, null); // {@link Inject.Single}
	 *   4: if (o4 != null) return o4; // catch, degraded if bind to null in parents
	 *      for (Container c = parent; ; c = c.parent) {
	 *        int j = c.index(X.class);
	 *        if (j > 0) return o4 = c.get0(j);
	 *        if (j < 0) return c.create0(j, this);
	 *      } // bind to parent
	 *   default: return null; // never happen
	 * }</pre>
	 */
	abstract <T>T get0(int index);

	static final Method M_get0 = Class2.declaredMethod1(Container.class, "get0");

	/**
	 * Eager example:
	 * 
	 * <pre>
	 * switch(i) {
	 *   1: Container123 o = new Container123();
	 *      o.parent = parentOrSave;
	 *      if (o.o3 == null) o.create0(3, null); // {@link Inject.Single}
	 *      if (o.o6 == null) o.create0(6, null); // {@link Inject.Single}
	 *      ...
	 *      return o;
	 *   -2: A o = new A((A1)get0(5), (Object)create0(-7, this), (A4)objss[2][1]);
	 *      if (parentOrSave != null)
	 *        o0 = o;
	 *      o.x = (Ax)get0(3); // bind to index 3
	 *      o.y = (Ay)objss[2][2]; // bind to object
	 *      o.p((Ap)objss[2][3]);
	 *      o.q((int)(Integer)get0(1));
	 *      return o;
	 *   3: ...
	 *   default: return null; // never happen
	 * }</pre>
	 * 
	 * Lazy example:
	 * 
	 * <pre>
	 * switch(i) {
	 *   1: Container123 o = new Container123();
	 *      o.parent = parent;
	 *      return o;
	 *   ...
	 * }</pre>
	 * 
	 * @param parentOrSave container: parent, others: null to save
	 */
	abstract <T>T create0(int index, Container parentOrSave);

	static final Method M_create0 = Class2.declaredMethod1(Container.class, "create0");

	private static final Container NULL = new Container()
	{
		@Override
		<T>T get0(int index)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		<T>T create0(int index, Container parentOrSave)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean bound(Class<?> c)
		{
			return false;
		}

		@Override
		public Container boundIn(Class<?> c)
		{
			return null;
		}

		@Override
		int index(Class<?> c)
		{
			throw new ClassCastException(c + " unbound");
		}
	};
}
