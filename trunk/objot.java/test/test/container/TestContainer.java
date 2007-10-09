//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
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
import test.container.X.Inner;
import test.container.X.New;
import test.container.X.New2;
import test.container.X.OuterNew;
import test.container.X.OuterSingle;
import test.container.X.Single;
import test.container.X.Single2;


public class TestContainer
	extends Assert
{
	static Container con0;

	@BeforeClass
	public static void init() throws Exception
	{
		final Container outest = new Factory()
		{
			{
				bind(OuterNew.class);
				bind(OuterSingle.class);
			}

			@Override
			protected Object doBind(Class<?> c, Bind b) throws Exception
			{
				return b.cla(c == X.class ? OuterSingle.class : b.cla);
			}
		}.create(null);
		con0 = new Factory()
		{
			{
				bind(Object.class);
				bind(New2.class);
				bind(Single2.class);
				bind(Inner.class);
			}

			@Override
			protected Object doBind(Class<?> c, Bind b) throws Exception
			{
				return b.mode(outest.bound(c) ? null : b.mode);
			}

			@Override
			protected Object doBind(Class<?> cc, AccessibleObject fp, Class<?> c,
				Type generic, Bind b) throws Exception
			{
				if (c == Integer.class)
					return b.obj( -1);
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
		}.create(outest);
	}

	Container con = con0.createAll();
	Container con2 = con.create(Container.class);

	@Test(expected = ClassCastException.class)
	public void unbound() throws Exception
	{
		con.get(TestContainer.class);
	}

	@Test
	public void new_()
	{
		New o = con.get(New.class);
		assertSame(New.class, o.getClass());
		assertSame(con, o.con);
		assertSame(Deprecated.class.getName(), o.name);
		assertEquals( -1, o.new_);
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
	public void outer()
	{
		assertNull(con.create().outer());
		assertSame(con, con.get(Container.class));
		assertSame(con0.getClass(), con.getClass());
		assertSame(con0.outer().getClass(), con.outer().getClass());
		assertSame(con.outer(), con.outest());

		Inner o = con.get(Inner.class);
		assertNotNull(o.on);
		assertNotSame(con.outer().get(OuterNew.class), o.on);
		assertSame(con.outer().get(OuterSingle.class), o.os);
		assertSame(o.os, o.on.x);
		assertSame(o.os, o.os.x);

		Inner o2 = con.create(Inner.class);
		assertNotSame(o.on, o2.on);
		assertSame(o.os, o2.os);
		Inner o3 = con2.get(Inner.class);
		assertNotSame(o.on, o3.on);
		assertNotSame(o2.on, o3.on);
		assertSame(o.os, o3.os);
	}
}
