package test.container;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Type;

import objot.container.Binder;
import objot.container.Container;
import objot.container.Factory;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestContainer
	extends Assert
{
	static Factory factory;

	@BeforeClass
	public static void init() throws Exception
	{
		Binder b = new Binder()
		{
			@Override
			protected Object doBind(Class<?> c) throws Exception
			{
				return bind(c == S.class ? Spread.class : c);
			}

			@Override
			protected Object doBind(Class<?> out, AccessibleObject a, Class<?> c, Type t)
				throws Exception
			{
				if (c == int.class)
					return -1;
				if (c == String.class && a.isAnnotationPresent(Deprecated.class))
					return Deprecated.class.getName();
				if (out == Private2.class)
					if (c == Private.class)
						return bind(Private2.class);
					else if (((Member)a).getName().equals("n"))
						return bind(None2.class);
				return bind(c);
			}
		};
		b.bind(None2.class);
		b.bind(Private2.class);
		b.bind(Spread.P.class);
		b.bind(Spread2.class);
		b.bind(SpreadC.class);
		b.bind(SpreadC2.class);
		factory = new Factory(b);
	}

	Container con = factory.container();
	Container con2 = factory.container();

	@Test(expected = ClassCastException.class)
	public void unbound() throws Exception
	{
		con.get(TestContainer.class);
	}

	@Test
	public void none() throws Exception
	{
		None o = con.get(None.class);
		assertSame(None.class, o.getClass());
		assertSame(con, o.con);
		assertSame(Deprecated.class.getName(), o.name);
		assertEquals( -1, o.none);

		None o1 = con.get(None.class);
		assertNotSame(o, o1);
		assertSame(o.con, o1.con);
		assertSame(o.name, o1.name);
		assertEquals(o.none, o1.none);

		None o2 = con.create(None.class);
		assertNotSame(o, o2);
		assertNotSame(o1, o2);
		assertSame(o.con, o2.con);
		assertSame(o.name, o2.name);
		assertEquals(o.none, o2.none);

		Private p = con.get(Private.class);
		None2 o3 = con.get(None2.class);
		assertSame(o.con, o3.con);
		assertSame(o.name, o3.name);
		assertEquals(o.none, o3.none);
		assertSame(p, o3.p);
	}

	@Test
	public void priv() throws Exception
	{
		Private p0 = con.create(Private.class);
		assertNotSame(p0, p0.p);

		Private p = con.get(Private.class);
		assertSame(Private.class, p.getClass());
		assertSame(p0.p, p);
		assertSame(p, p.p);
		assertNotNull(p.n);

		Private p1 = con.get(Private.class);
		assertSame(p, p1);
		assertNotSame(p.n, con.get(None.class));

		Private p2 = con2.get(Private.class);
		assertNotSame(p0, p2);
		assertNotSame(p, p2);
		assertNotSame(p.n, p2.n);

		Private2 pp = con.get(Private2.class);
		assertSame(pp, pp.p);
		assertSame(None2.class, pp.n.getClass());
		assertSame(None.class, pp.n0.getClass());
		assertNotSame(p.n, pp.n0);
	}

	@Test
	public void containers() throws Exception
	{
		assertSame(con, con.get(Container.class));
		Container con11 = con.create(Container.class);
		assertNotSame(con, con11);
		assertSame(con, con11.outer);
		Container con12 = con11.create(Container.class);
		assertNotSame(con, con12);
		assertNotSame(con11, con12);
		assertSame(con11, con12.outer);
	}

	@Test
	public void spread() throws Exception
	{
		Spread s0 = con.create(Spread.class);
		assertNotSame(s0, s0.s);

		Spread s = con.get(Spread.class);
		assertSame(s0.s, s);
		assertSame(s, s.s);
		Spread s2 = con.get(Spread.class);
		assertSame(s, s2);

		Container con11 = con.create(Container.class);
		Container con12 = con11.create(Container.class);

		assertSame(s, con11.get(Spread.class));
		assertSame(s, con12.get(Spread.class));

		assertNotSame(con.get(Spread.P.class), con11.get(Spread.P.class));
		assertNotSame(con.get(Spread.P.class), con12.get(Spread.P.class));

		Spread s12 = con12.get(Spread2.class);
		Spread s1 = con.get(Spread2.class);
		Spread s11 = con11.get(Spread2.class);
		assertSame(s1, s11);
		assertNotSame(s1, s12);
	}

	@Test
	public void spreadCreate() throws Exception
	{
		SpreadC c0 = con.create(SpreadC.class);
		assertNotSame(c0, c0.c);

		SpreadC c = con.get(SpreadC.class);
		assertSame(c0.c, c);
		assertSame(c, c.c);
		assertSame(con.get(Spread.class), c.s);
		SpreadC c2 = con.get(SpreadC.class);
		assertSame(c, c2);

		Container con11 = con.create(Container.class);
		Container con12 = con11.create(Container.class);

		assertSame(c, con11.get(SpreadC.class));
		assertSame(c, con12.get(SpreadC.class));

		SpreadC c12 = con12.get(SpreadC2.class);
		SpreadC c1 = con.get(SpreadC2.class);
		SpreadC c11 = con11.get(SpreadC2.class);
		assertSame(c1, c11);
		assertSame(c1, c12);
	}
}
