//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.container;

import java.lang.reflect.Field;

import objot.bytecode.Element;
import objot.util.Array2;
import objot.util.Bytes;
import objot.util.Class2;


@Scope.Private
public abstract class Container
{
	public final Container outer;

	static final Field F_objss = Class2.declaredField(Container.class, "objss");
	final Object[][] objss;

	Container(Container out, Object[][] ss)
	{
		outer = out;
		objss = ss;
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

	static final Bytes NAME_createTop = Element.utf("createTop");
	static final Bytes DESC_createTop = Element.utf(Class2.descript( //
		Class2.declaredMethod1(Container.class, "createTop")));

	/** Example: <code>return new Container123(null, {@link #objss});</code> */
	abstract Container createTop();

	static final Bytes NAME_index = Element.utf("index");
	static final Bytes DESC_index = Element.utf(Class2.descript( //
		Class2.declaredMethod1(Container.class, "index")));

	/**
	 * Example:
	 * 
	 * <pre>
	 * switch(c.hashCode()) {
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

	static final Bytes NAME_get0 = Element.utf("get0");
	static final Bytes DESC_get0 = Element.utf(Class2.descript( //
		Class2.declaredMethod1(Container.class, "get0")));

	/**
	 * Example:
	 * 
	 * <pre>
	 * switch(i) {
	 *     0: return objss[0][0]; // bind to object
	 *     1: return this; // {@link Container}
	 *     2: return create0(i, false); // {@link Scope.None}
	 *     3: return o3 != null ? o3 : create0(i, true); // {@link Scope.Private}
	 *     4: for (Container123 c = this; ; c = (Container123)c.outer)
	 *        	if (c.o4 != null) return o4 = c.o4;
	 *          else if (c.outer == null) break;
	 *        return create0(i, true); // {@link Scope.Spread}
	 *     5: ... // like 4
	 *        return o5 = c.create0(i, true); // {@link Scope.SpreadCreate}
	 *     6: ... other.package.Create123.create0(this, i, true) ... // not-public
	 *     default: return null;
	 *   }
	 * }</pre>
	 */
	abstract Object get0(int index) throws Exception;

	static final Bytes NAME_create0 = Element.utf("create0");
	static final Bytes DESC_create0 = Element.utf(Class2.descript( //
		Class2.declaredMethod1(Container.class, "create0")));

	Object create0(int index, boolean save) throws Exception
	{
		Object[] ps = Array2.newObjects(b.cbs.length);
		for (int i = 0; i < ps.length; i++)
			ps[i] = get(b.cbs[i]);
		Object o = b.ct.newInstance(ps);
		if (save)
			os.put(b.c, o);
		for (int i = 0; i < b.fs.length; i++)
			b.fs[i].set(o, get(b.fbs[i]));
		for (int i = 0; i < b.ms.length; i++)
		{
			ps = Array2.newObjects(b.mbs[i].length);
			for (int j = 0; j < ps.length; j++)
				ps[j] = get(b.mbs[i][j]);
			b.ms[i].invoke(o, ps);
		}
		return o;
	}
}
