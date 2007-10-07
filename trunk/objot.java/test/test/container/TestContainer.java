//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package test.container;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Type;

import objot.container.Container;
import objot.container.Binder;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import test.container.X.Inherit;
import test.container.X.Inherit2;
import test.container.X.New;
import test.container.X.New2;
import test.container.X.Single;
import test.container.X.Single2;
import test.container.X.Spread;
import test.container.X.Spread2;


public class TestContainer
	extends Assert
{
	static Container con0;

	@BeforeClass
	public static void init() throws Exception
	{
		con0 = new Binder()
		{
			{
				bind(New2.class);
				bind(Single2.class);
				bind(Spread2.class);
				bind(Inherit.class);
				bind(Inherit2.class);
				bind(Object.class);
			}

			@Override
			protected Object doBind(Class<?> c) throws Exception
			{
				return bind(c == X.class ? Spread.class : c);
			}

			@Override
			protected Object doBind(Class<?> out, AccessibleObject a, Class<?> c, Type t)
				throws Exception
			{
				if (c == int.class)
					return -1;
				if (c == String.class && a.isAnnotationPresent(Deprecated.class))
					return Deprecated.class.getName();
				if (c == int[].class)
					return null;
				if (c == long[].class)
					return 65536;

				if (out == Single2.class)
					if (c == Single.class)
						return bind(Single2.class);
					else if (((Member)a).getName().equals("n"))
						return bind(New2.class);

				return bind(c);
			}
		}.createOutest(null);
	}

	Container con = con0.createOutest(null);
	Container con2 = con0.createOutest(null);

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
	public void containers()
	{
		assertSame(con, con.get(Container.class));
		Container con11 = con.create(Container.class);
		assertNotSame(con, con11);
		assertSame(con, con11.outer());
		Container con12 = con11.create(Container.class);
		assertNotSame(con, con12);
		assertNotSame(con11, con12);
		assertSame(con11, con12.outer());
	}

	@Test
	public void spread()
	{
		Spread o0 = con.create(Spread.class);
		assertNotSame(o0, o0.x);

		Spread o = con.get(Spread.class);
		assertSame(o0.x, o);
		assertSame(o, o.x);
		Spread o2 = con.get(Spread.class);
		assertSame(o, o2);

		Container con11 = con.create(Container.class);
		Container con12 = con11.create(Container.class);

		assertSame(o, con11.get(Spread.class));
		assertSame(o, con12.get(Spread.class));

		Spread o12 = con12.get(Spread2.class);
		Spread o1 = con.get(Spread2.class);
		Spread o11 = con11.get(Spread2.class);
		assertSame(o1, o11);
		assertNotSame(o1, o12);
	}

	@Test
	public void spreadCreate()
	{
		Inherit o0 = con.create(Inherit.class);
		assertNotSame(o0, o0.i);

		Inherit o = con.get(Inherit.class);
		assertSame(o0.i, o);
		assertSame(o, o.i);
		assertSame(con.get(Spread.class), o.x);
		Inherit o2 = con.get(Inherit.class);
		assertSame(o, o2);

		Container con11 = con.create(Container.class);
		Container con12 = con11.create(Container.class);

		assertSame(o, con11.get(Inherit.class));
		assertSame(o, con12.get(Inherit.class));

		Inherit o12 = con12.get(Inherit2.class);
		Inherit o1 = con.get(Inherit2.class);
		Inherit o11 = con11.get(Inherit2.class);
		assertSame(o1, o11);
		assertSame(o1, o12);
	}
}
