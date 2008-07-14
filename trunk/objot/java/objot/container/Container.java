//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Library General Public License version 2
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
		Container n = this;
		while (n.parent != NULL)
			n = n.parent;
		return n;
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

	/** create container with parents created recursively until null, thread-safe */
	public final Container createBubble()
	{
		return create0(index(Container.class), parent == NULL ? NULL : parent.createBubble());
	}

	/**
	 * create container with parents created recursively until the specified one shared as
	 * parent, thread-safe
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
	 * create container with parents created recursively until the specified one replaced
	 * by the another one as parent, thread-safe
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
	 * create a {@link Inject.New} instance, or get the {@link Inject.Single} instance, or
	 * get this container, or get the static object, or get the {@link Inject.Set}
	 * instance, or get in parent.
	 * 
	 * @see Factory#create(Container, boolean)
	 * @throws ClassCastException if the class is not found in this and parents, or a
	 *             bound class throws.
	 */
	public final <T>T get(Class<T> c)
	{
		for (Container n = this;; n = n.parent)
		{
			int i = n.index(c);
			if (i > 0)
				return n.<T>get0(i);
			if (i < 0)
				return n.<T>create0(i, parent);
		}
	}

	/**
	 * create a {@link Inject.New} instance, or create a new {@link Inject.Single}
	 * instance, or create container with same parent, or get the static object, or get
	 * the {@link Inject.Set} instance, or create in parent.
	 * 
	 * @see Factory#create(Container, boolean)
	 * @throws ClassCastException if the class is not found in this and parents, or a
	 *             bound class throws.
	 */
	public final <T>T create(Class<T> c)
	{
		for (Container n = this;; n = n.parent)
		{
			int i = n.index(c);
			if (i != 0)
				return n.<T>create0(i, parent);
		}
	}

	/**
	 * set an instance of the class in {@link Inject.Set} or {@link Inject.Single} mode,
	 * or set in parent.
	 * 
	 * @see Factory#create(Container, boolean)
	 * @throws ClassCastException if the class is not found in this or parents, or the
	 *             instance is not of the bound class
	 * @throws UnsupportedOperationException if the class is not {@link Inject.Set} or
	 *             {@link Inject.Single} mode
	 */
	public final <T>T set(Class<T> c, T o)
	{
		for (Container n = this;; n = n.parent)
		{
			int i = n.index(c);
			if (i != 0)
				if (n.set0(i, o))
					return o;
				else
					throw new UnsupportedOperationException();
		}
	}

	/** @return whether class is bound in this container */
	public boolean bound(Class<?> c)
	{
		return index(c) != 0;
	}

	/** @return self or parent container which binds the class, null if no binding */
	public Container contain(Class<?> c)
	{
		for (Container n = this;; n = n.parent)
		{
			int i = n.index(c);
			if (i != 0)
				return n;
		}
	}

	Container parent;
	static final Field F_parent = Class2.declaredField(Container.class, "parent");

	/**
	 * Example:
	 * 
	 * <pre>
	 * switch(c.hashCode() % 31) {
	 *   2: if (c == Container.class) return 1;
	 *      if (c == A.class) return -2; // {@link Inject.New}
	 *      if (c == B.class) return 3; // {@link Inject.Single}
	 *      if (c == BB.class) return 3; // BB bound to B
	 *      return 0; // not found
	 *   7: if (c == D.class) return -4; // {@link Inject.Set}
	 *      if (c == E.class) return 5; // bind to static object
	 *      if (c == F.class) return 6; // bind to parent, could be cached
	 *      return 0; // not found
	 *   default: return 0; // not found
	 * }</pre>
	 * 
	 * @return >0 for cachable, <0 for creation or not cachable
	 */
	abstract int index(Class<?> c);

	static final Bytes NAME_index = Bytecode.utf("index");
	static final Bytes DESC_index = Bytecode.utf(Class2.descript(Class2.declaredMethod1(
		Container.class, "index")));

	/**
	 * Example:
	 * 
	 * <pre>
	 * switch(i) {
	 *   1: return this; // {@link Container}
	 *   2: return oss[2][0]; // bind to static object
	 *   3: return o3 != null ? o3 : create0(i, null); // {@link Inject.Single}
	 *   4: if (o4 != null) return o4; // cache, degraded if bind to null in parents
	 *      for (Container n = parent; ; n = n.parent) { // less stack usage than recursive  
	 *        int j = n.index(X.class); // never be {@link Container}
	 *        if (j > 0) return o4 = n.get0(j); 
	 *        if (j < 0) return n.create0(j, this); // include {@link Inject.Set} in parent
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
	 *      o.parent = parentOrSingle;
	 *      if (o.o3 == null) o.create0(3, null);
	 *      if (o.o6 == null) o.create0(6, null);
	 *      ... // only create {@link Inject.Single} eagerly
	 *      return o;
	 *   2: return oss[2][0]; // bind to static object
	 *   -3: return o3; // {@link Inject.Set}
	 *   -4: A o = new A((A1)get0(5), (Object)create0(-7, this), (A4)oss[4][1]);
	 *      o.x = (Ax)get0(3);
	 *      o.y = (Ay)oss[4][2];
	 *      o.p((Ap)oss[4][3]);
	 *      o.q((int)(Integer)get0(9));
	 *      return o;
	 *   5: B o = new B(...);
	 *      if (parentOrSingle == null)
	 *        o4 = o; // save {@link Inject.Single}
	 *      ...
	 *   6: for (Container n = parent; ; n = n.parent) { // less stack usage than recursive  
	 *        int j = n.index(X.class); // never be {@link Container}
	 *        if (j != 0) return n.create0(j, this); // include {@link Inject.Set} in parent
	 *      } // bind to parent
	 *   default: return null; // never happen
	 * }</pre>
	 * 
	 * Lazy example:
	 * 
	 * <pre>
	 * switch(i) {
	 *   1: Container123 o = new Container123();
	 *      o.parent = parentOrSingle;
	 *      return o;
	 *   ...
	 * }</pre>
	 * 
	 * @param parentOrSingle parent when create container, null to save
	 *            {@link Inject.Single}
	 */
	abstract <T>T create0(int index, Container parentOrSingle);

	static final Method M_create0 = Class2.declaredMethod1(Container.class, "create0");

	/**
	 * Example:
	 * 
	 * <pre>
	 * switch(i) {
	 *   -3: o3 = (A)o; return true; // {@link Inject.Single}
	 *   6: for (Container n = parent; ; n = n.parent) { // less stack usage than recursive  
	 *        int j = n.index(X.class); // never be {@link Container}
	 *        if (j != 0) return n.set0(j, o);
	 *      } // bind to parent
	 *   default: return false; // others
	 * }</pre>
	 */
	boolean set0(int index, Object o)
	{
		return false;
	}

	static final Method M_set0 = Class2.declaredMethod1(Container.class, "set0");

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
		public Container contain(Class<?> c)
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
