//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.container;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import objot.bytecode.Element;
import objot.util.Bytes;
import objot.util.Class2;


@Scope.Private
public abstract class Container
{
	static final Field F_outer = Class2.declaredField(Container.class, "outer");
	Container outer;

	static final Field F_objss = Class2.declaredField(Container.class, "objss");
	Object[][] objss;

	public final Container outer()
	{
		return outer;
	}

	/** not thread-safe */
	@SuppressWarnings("unchecked")
	public final <T>T get(Class<T> c) throws Exception
	{
		int i = index(c);
		if (i < 0)
			throw new ClassCastException(c + " unbound");
		return (T)get0(i);
	}

	/** not thread-safe */
	@SuppressWarnings("unchecked")
	public final <T>T create(Class<T> c) throws Exception
	{
		int i = index(c);
		if (i < 0)
			throw new ClassCastException(c + " unbound");
		return (T)create0(index(c), false);
	}

	static final Bytes NAME_index = Element.utf("index");
	static final Bytes DESC_index = Element.utf(Class2.descript( //
		Class2.declaredMethod1(Container.class, "index")));

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

	static final Method M_get0 = Class2.declaredMethod1(Container.class, "get0");

	/**
	 * Example:
	 * 
	 * <pre>
	 * switch(i) {
	 *   0: return objss[0][0]; // bind to object
	 *   1: return this; // {@link Container}
	 *   2: return create0(i, false); // @{@link Scope.None}
	 *   3: return o3 != null ? o3 : create0(i, true); // @{@link Scope.Private}
	 *   4: for (Container123 c = this; ; c = (Container123)c.outer)
	 *      	if (c.o4 != null) return o4 = c.o4;
	 *        else if (c.outer == null) break;
	 *      return create0(i, true); // @{@link Scope.Spread}
	 *   5: ... // like 4
	 *      return o5 = (Abc5)c.create0(i, true); // @{@link Scope.SpreadCreate}
	 *   default: return this; // never happen
	 * }</pre>
	 */
	abstract Object get0(int index) throws Exception;

	static final Method M_create0 = Class2.declaredMethod1(Container.class, "create0");

	/**
	 * Example:
	 * 
	 * <pre>
	 * switch(i) {
	 *   1: Container123 o = new Container123();
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
	abstract Object create0(int index, boolean save) throws Exception;
}
