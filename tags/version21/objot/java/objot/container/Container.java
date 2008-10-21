//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.container;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
		Container n = this;
		while (n.parent != NULL)
			n = n.parent;
		return n;
	}

	/** create container with same parent, thread-safe. */
	public final Container create()
	{
		return create(parent);
	}

	/** create container with specified parent, thread-safe. */
	public abstract Container create(Container parent_);

	/** create container with parents created recursively until null, thread-safe */
	public final Container createBubble()
	{
		return create(parent == NULL ? NULL : parent.createBubble());
	}

	/**
	 * create container with parents created recursively until the specified one shared as
	 * parent, thread-safe
	 * 
	 * @throws RuntimeException if the until is not null or one of parents
	 */
	public final Container createBubble(Container until)
	{
		if (parent == until)
			return create(parent);
		if (parent == null)
			throw new RuntimeException(until + " is not one of parents");
		return create(parent.createBubble(until));
	}

	/**
	 * create container with parents created recursively until the specified one replaced
	 * by the another one as parent, thread-safe
	 * 
	 * @throws RuntimeException if the until is not null or one of parents
	 */
	public final Container createBubble(Container until, Container to)
	{
		if (parent == until)
			return create(to);
		if (parent == null)
			throw new RuntimeException(until + " is not one of parents");
		return create(parent.createBubble(until));
	}

	/** @return whether class is bound in this container */
	public boolean bound(Class<?> c)
	{
		return index(c) != 0;
	}

	/** @return self or parent container which binds the class, null if no binding */
	public Container contain(Class<?> c)
	{
		Container n = this;
		while (n.index(c) == 0)
			if ((n = n.parent) == NULL)
				return null;
		return n;
	}

	/**
	 * new a {@link Inject.New} instance, or get the {@link Inject.Single} instance, or
	 * get the {@link Inject.Set} instance, or get this container, or get the static
	 * object, or try in parent.
	 * 
	 * @see Factory#create(Container, boolean)
	 * @throws ClassCastException if {@link #contain} false, or maybe any exception while
	 *             new an instance
	 */
	public final <T>T get(Class<T> c)
	{
		Container n = this;
		int i;
		while ((i = n.index(c)) == 0)
			n = n.parent;
		return n.<T>get0(i);
	}

	/**
	 * similar to {@link #get} except new a {@link Inject.Single} instance, or create
	 * container with same parent.
	 * 
	 * @see Factory#create(Container, boolean)
	 * @throws ClassCastException if {@link #contain} false, or maybe any exception while
	 *             new an instance
	 */
	@SuppressWarnings("unchecked")
	public final <T>T getNew(Class<T> c)
	{
		if (c == Container.class)
			return (T)create(parent);
		Container n = this;
		int i;
		while ((i = n.index(c)) == 0)
			n = n.parent;
		return n.<T>get0( -i);
	}

	/**
	 * set an instance of the class in {@link Inject.Single} mode, or try in parent.
	 * 
	 * @see Factory#create(Container, boolean)
	 * @throws ClassCastException if {@link #contain} false, or maybe any exception while
	 *             new an instance
	 * @throws UnsupportedOperationException if the class is not {@link Inject.Set} mode
	 */
	public final <T>T set(Class<T> c, T o)
	{
		Container n = this;
		int i;
		while ((i = n.index(c)) == 0)
			n = n.parent;
		if (n.set0(i, o))
			return o;
		throw new UnsupportedOperationException();
	}

	Container parent;

	static final Field F_parent = Class2.declaredField(Container.class, "parent");

	Container()
	{
	}

	/**
	 * Eager example:
	 * 
	 * <pre>
	 * Container123 o = new Container123();
	 * o.parent = parent_ != null ? parent_ : NULL;
	 * o.get0(3);
	 * o.get0(9);
	 * ... // create only {@link Inject.Single} eagerly
	 * return o;
	 * </pre>
	 * 
	 * Lazy example:
	 * 
	 * <pre>
	 * Container123 o = new Container123();
	 * o.parent = parent_ != null ? parent_ : NULL;
	 * return o;
	 * </pre>
	 */
	static final Method M_create = Class2.declaredMethod(Container.class, "create",
		Container.class);

	/**
	 * Example:
	 * 
	 * <pre>
	 * switch(c.hashCode() % 31) {
	 *   2: if (c == Container.class) return 1; // special {@link Inject.Single}
	 *      if (c == A.class) return -4; // {@link Inject.New}
	 *      if (c == B.class) return 3; // {@link Inject.Single}
	 *      if (c == BB.class) return 3; // BB bound to B
	 *      return 0; // no bind
	 *   7: if (c == D.class) return -5; // {@link Inject.Set}
	 *      if (c == E.class) return 2; // static object
	 *      if (c == F.class) return -6; // {@link Inject.Parent}, may cache
	 *      return 0; // no bind
	 *   default: return 0; // no bind
	 * }</pre>
	 * 
	 * @return >0: cachable, <0: creation or not cachable, 0: no bind
	 */
	abstract int index(Class<?> c);

	static final Method M_index = Class2.declaredMethod1(Container.class, "index");

	/**
	 * Example:
	 * 
	 * <pre>
	 * switch(i) {
	 *    1: return this; // {@link Container}
	 *   -2:
	 *    2: return oss[2][0]; // static object
	 *    5:
	 *   -5: return o5; // {@link Inject.Set}
	 *
	 *    3: if (o3 != null) return o3; // {@link Inject.Single}
	 *   -3: B o = new B(...);
	 *       if (i > 0)
	 *         if (o3 == null) o3 = o;
	 *         else // multi {@link Inject.Single} by circular injection
	 *           throw new {@link ClassCircularityError}("B");
	 *       ...
	 *    4:
	 *   -4: A o = new A((A1)get0(41), get0(-42), (A3)oss[4][1]);
	 *      o.x = (A4)get0(44);
	 *      o.y = (A5)oss[4][2];
	 *      o.p((A6)o46);
	 *      o.q((A7)
	 *      o.q((int)(Integer)o5);
	 *      return o; // {@link Inject.New}
	 *
	 *   -6: if (o6 != null) return o6; // cache, degraded if get null from parent
	 *    6: Container n = parent; int j; // {@link Inject.Parent}
	 *       while ((j = n.index(X26.class)) == 0) // less stack usage than recursive
	 *         n = n.parent;
	 *       if (i > 0) return n.get0(-j);
	 *       Object o = n.get0(j);
	 *       return j > 0 ? o6 = o : o; 
	 *   default: return null; // never happen
	 * }</pre>
	 * 
	 * @param index -{@link #index} for {@link #getNew}
	 */
	abstract <T>T get0(int index);

	static final Method M_get0 = Class2.declaredMethod1(Container.class, "get0");

	/**
	 * Example:
	 * 
	 * <pre>
	 * switch(i) {
	 *   -5: o3 = (A)o; return true; // {@link Inject.Set}
	 *   -6: Container n = parent; int j; // {@link Inject.Parent}
	 *       while ((j = n.index(X26.class)) == 0) // less stack usage than recursive
	 *         n = n.parent;
	 *       return n.set0(j, o);
	 *   default: return false; // others
	 * }</pre>
	 */
	boolean set0(int index, Object o)
	{
		return false;
	}

	static final Method M_set0 = Class2.declaredMethod1(Container.class, "set0");

	static final Container NULL = new Container()
	{
		@Override
		public Container create(Container p)
		{
			return this;
		}

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
	};
	static final Field F_null = Class2.declaredField(Container.class, "NULL");
}
