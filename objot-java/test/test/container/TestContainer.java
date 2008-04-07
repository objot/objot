//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU General Public License version 2
//
package test.container;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Type;

import objot.container.Bind;
import objot.container.Container;
import objot.container.Factory;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import test.container.X.ChildSingle;
import test.container.X.New;
import test.container.X.New2;
import test.container.X.ParentNew;
import test.container.X.ParentSet;
import test.container.X.ParentSingle;
import test.container.X.Set;
import test.container.X.Single;
import test.container.X.Single2;


public class TestContainer
	extends Assert
{
	static Container con0;
	static Container con0lazy;

	@BeforeClass
	public static void init() throws Exception
	{
		final Container parent = new Factory()
		{
			{
				bind(ParentNew.class);
				bind(ParentSingle.class);
				bind(ParentSet.class);
			}

			@Override
			protected Object doBind(Class<?> c, Bind b) throws Exception
			{
				return b.cla(c == X.class ? ParentSingle.class : b.cla);
			}
		}.create(null);
		Factory f = new Factory()
		{
			{
				bind(Object.class);
				bind(New2.class);
				bind(Single2.class);
				bind(Set.class);
				bind(ChildSingle.class);
				bind(ParentSingle.class);
			}

			@Override
			protected Object doBind(Class<?> c, Bind b) throws Exception
			{
				return c == Object.class ? b.obj(parent) : c == Long.class ? b.obj(9L)
					: b.mode(parent.bound(c) ? null : b.mode);
			}

			@Override
			protected Object doBind(Class<?> cc, AccessibleObject fp, Class<?> c,
				Type generic, Bind b) throws Exception
			{
				if (c == Integer.class)
					return b.obj( -1);
				if (c == Long.class)
					return b;
				if (c == String.class && fp.isAnnotationPresent(Deprecated.class))
					return b.obj(Deprecated.class.getName());
				if (c == int[].class)
					return b.obj(null);
				if (c == long[].class)
					return b.obj(65536);

				if (cc == Single2.class)
					if (c == Single.class)
						return b.cla(Single2.class);
					else if (((Member)fp).getName().equals("n"))
						return b.cla(New2.class);
				return b;
			}
		};
		con0 = f.create(parent);
		con0lazy = f.create(parent, true);
	}

	Container con = con0.createBubble();
	Container con2 = con.create(Container.class);
	Container conLazy = con0lazy.createBubble();

	@Test(expected = ClassCastException.class)
	public void unbound() throws Exception
	{
		con.get(TestContainer.class);
	}

	@Test(expected = ClassCastException.class)
	public void eager()
	{
		con.create();
	}

	@Test
	public void staticObject()
	{
		assertSame(con0.rootParent(), con.get(Object.class));
	}

	@Test
	public void lazy()
	{
		Single.created = false;
		conLazy.createBubble();
		assertFalse(Single.created);
		conLazy.get(Single.class);
		assertTrue(Single.created);

		Single.created = false;
		con.createBubble();
		assertTrue(Single.created);
	}

	@Test
	public void new_()
	{
		New o = con.get(New.class);
		assertSame(New.class, o.getClass());
		assertSame(con, o.con);
		assertSame(Deprecated.class.getName(), o.name);
		assertEquals( -1, o.new_);
		assertEquals(9, o.obj);
		assertSame(null, o.ints);
		assertEquals(65536, o.longs.length);

		New o1 = con.get(New.class);
		assertNotSame(o, o1);
		assertSame(o.con, o1.con);
		assertSame(o.name, o1.name);
		assertEquals(o.new_, o1.new_);
		assertNotSame(o.longs, o1.longs);

		New o2 = con.create(New.class);
		assertNotSame(o, o2);
		assertNotSame(o1, o2);
		assertSame(o.con, o2.con);
		assertSame(o.name, o2.name);
		assertEquals(o.new_, o2.new_);

		Single p = con.get(Single.class);
		New2 o3 = con.get(New2.class);
		assertSame(o.con, o3.con);
		assertSame(o.name, o3.name);
		assertEquals(o.new_, o3.new_);
		assertSame(p, o3.p);
		New2 o4 = con.get(New2.class);
		assertNotSame(o3, o4);
	}

	@Test
	public void single()
	{
		Single o0 = con.create(Single.class);
		assertNotSame(o0, o0.s);

		Single o = con.get(Single.class);
		assertSame(Single.class, o.getClass());
		assertSame(o0.s, o);
		assertSame(o, o.s);
		assertNotNull(o.n);

		Single o1 = con.get(Single.class);
		assertSame(o, o1);
		assertNotSame(o.n, con.get(New.class));
		assertNotSame(o1, con.create(Single.class));

		Single o2 = con2.get(Single.class);
		assertNotSame(o0, o2);
		assertNotSame(o, o2);
		assertNotSame(o.n, o2.n);

		Single2 oo = con.get(Single2.class);
		assertSame(oo, oo.s);
		assertSame(New2.class, oo.n.getClass());
		assertSame(New.class, oo.n0.getClass());
		assertNotSame(o.n, oo.n0);
	}

	@Test
	public void set()
	{
		New2 o0 = con.get(New2.class);
		assertNull(o0.t);
		Single o1 = con.get(Single.class);
		assertNull(o1.t);

		Set o2 = con.get(Set.class);
		assertNull(o2);
		o2 = new Set();
		assertSame(o2, con.set(Set.class, o2));
		assertSame(o2, con.get(Set.class));
		assertSame(o2, con.create(Set.class));
		assertNull(o2.noInject);

		New2 o3 = con.get(New2.class);
		assertSame(o2, o3.t);
		Single o4 = con.get(Single.class);
		assertNull(o4.t);

		conLazy.set(Set.class, o2);
		assertSame(o2, conLazy.get(Single.class).t);
	}

	@Test
	public void parent()
	{
		assertNull(con.parent().create().parent());
		assertSame(con, con.get(Container.class));
		assertSame(con0.getClass(), con.getClass());
		assertSame(con0.parent().getClass(), con.parent().getClass());
		assertSame(con.parent(), con.rootParent());

		ChildSingle o = con.get(ChildSingle.class);
		assertNotNull(o.on);
		assertNotSame(con.parent().get(ParentNew.class), o.on);
		assertSame(con.parent().get(ParentSingle.class), o.os);
		assertSame(o.os, o.on.x);
		assertSame(o.os, o.os.x);

		ChildSingle o2 = con.create(ChildSingle.class);
		assertNotSame(o.on, o2.on);
		assertSame(o.os, o2.os);
		ChildSingle o3 = con2.get(ChildSingle.class);
		assertNotSame(o.on, o3.on);
		assertNotSame(o2.on, o3.on);
		assertSame(o.os, o3.os);

		ParentSingle o4 = con.create(ParentSingle.class);
		assertNotNull(o4);
		assertNotSame(o.os, o4);
		assertNotSame(o4, con.create(ParentSingle.class));
	}
}
