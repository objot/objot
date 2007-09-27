//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.container;

import java.util.HashMap;

import objot.util.Array2;


@Scope.Private
public class Container
{
	public final Container out;
	private final HashMap<Class<?>, Bind> bs;
	private final HashMap<Class<?>, Object> os = new HashMap<Class<?>, Object>();

	Container(Container o, HashMap<Class<?>, Bind> bs_)
	{
		out = o;
		bs = bs_;
	}

	@SuppressWarnings("unchecked")
	public <T>T get(Class<T> c) throws Exception
	{
		Bind b = bs.get(c);
		if (b == null)
			throw new ClassCastException(c + " not bound");
		return (T)get(b.b);
	}

	Object get(Object b_) throws Exception
	{
		if ( !(b_ instanceof Bind))
			return b_;
		Bind b = (Bind)b_; // assert b.b == b;
		if (b.c == Container.class)
			return this;
		Object o = os.get(b.c);
		if (o != null)
			return o;
		if (b.scope == Scope.None.class)
			return create(b, false);
		if (b.scope == Scope.Private.class)
			return create(b, true);
		Container t = this;
		while (t.out != null)
		{
			t = t.out;
			o = t.os.get(b.c);
			if (o != null)
				// @todo spread instance to fasten later searchs
				return o;
		}
		return (b.scope == Scope.Spread.class ? this : t).create(b, true);
	}

	@SuppressWarnings("unchecked")
	public <T>T create(Class<T> c) throws Exception
	{
		if (c == Container.class)
			return (T)new Container(this, bs);
		Bind b = bs.get(c);
		if (b == null)
			throw new ClassCastException(c + " not bound");
		return (T)create(b, false);
	}

	Object create(Bind b, boolean put) throws Exception
	{
		Object[] ps = Array2.newObjects(b.cbs.length);
		for (int i = 0; i < ps.length; i++)
			ps[i] = get(b.cbs[i]);
		Object o = b.ct.newInstance(ps);
		if (put)
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
