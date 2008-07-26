//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Library General Public License version 2
//
package test.aspect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import objot.aspect.Aspect;
import objot.aspect.Weaver;
import objot.container.Inject;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestAspect
	extends Assert
{
	static Class<X> weaved;

	@BeforeClass
	public static void init() throws Exception
	{
		weaved = new Weaver(A2.class, A1.class)
		{
			@Override
			protected Object forWeave(Class<? extends Aspect> ac, Method m) throws Exception
			{
				if (m.getDeclaringClass() == Object.class)
					return this;
				X.P p = X.P.valueOf(m.getName());
				if (ac == A1.class && p == X.P.Throw2 //
					|| ac == A2.class && p != X.P.Throw2 && p != X.P.Throw3)
					return this;
				return p.a;
			}
		}.weave(X.class);
	}

	A a;

	@After
	public void clear()
	{
		if (a != null)
			a.clear();
	}

	@Test
	public void ctor() throws Exception
	{
		Constructor<X> c = weaved.getConstructor(Object.class);
		X x = c.newInstance("new");
		assertEquals("new", x.result);
		assertTrue(c.isAnnotationPresent(Inject.class));
		assertEquals(2, A.ctor);
	}

	@Test
	public void Void() throws Exception
	{
		a = X.P.Void.a;
		X x = weaved.newInstance();
		x.Void();
		assertEquals(a.name0, a.name);
		assertEquals(a.desc0, a.desc);
		assertEquals(a.target0, a.target);
		assertSame(x, a.thiz);
		assertEquals(X.class, a.clazz);
		assertSame(void.class, a.returnC);
		assertNull(a.returnV);
		assertNull(a.except);
		assertNull(a.Finally);
		assertNull(x.result);
	}

	@Test
	public void Int() throws Exception
	{
		a = X.P.Int.a;
		X x = weaved.newInstance();
		assertEquals(11, x.Int(10));
		assertEquals(a.name0, a.name);
		assertEquals(a.desc0, a.desc);
		assertEquals(a.target0, a.target);
		assertSame(x, a.thiz);
		assertEquals(X.class, a.clazz);
		assertSame(int.class, a.returnC);
		assertNull(a.except);
		assertNull(a.Finally);
		assertEquals(10, x.a);
		assertEquals(x.a, x.result);
	}

	@Test
	public void Long() throws Exception
	{
		a = X.P.Long.a;
		X x = weaved.newInstance();
		assertEquals(Long.MAX_VALUE, x.Long(Long.MAX_VALUE, 20));
		assertEquals(a.name0, a.name);
		assertNull(a.except);
		assertEquals(20, x.a);
		assertEquals(Long.MAX_VALUE, x.b);
		assertEquals(x.b, x.result);
	}

	@Test
	public void Char() throws Exception
	{
		a = X.P.Char.a;
		X x = weaved.newInstance();
		assertEquals('!', x.Char('!', Long.MIN_VALUE, -30));
		assertEquals(a.name0, a.name);
		assertNull(a.except);
		assertEquals( -30, x.a);
		assertEquals(Long.MIN_VALUE, x.b);
		assertEquals('!', x.c);
		assertEquals(x.c, x.result);
	}

	@Test
	public void Double() throws Exception
	{
		a = X.P.Double.a;
		X x = weaved.newInstance();
		assertEquals(2e200, x.Double(2e200, 40));
		assertEquals(a.name0, a.name);
		assertNull(a.except);
		assertEquals(40, x.b);
		assertEquals(2e200, x.d);
		assertEquals(x.d, x.result);
	}

	@Test
	public void Str() throws Exception
	{
		a = X.P.Str.a;
		X x = weaved.newInstance();
		assertEquals("objot", x.Str("objot", Float.NaN, 0));
		assertEquals(a.name0, a.name);
		assertNull(a.except);
		assertEquals(0, x.b);
		assertEquals(Double.NaN, x.d);
		assertEquals("objot", x.e);
		assertEquals(x.e, x.result);
	}

	@Test
	public void Throw1() throws Exception
	{
		a = X.P.Throw1.a;
		X x = weaved.newInstance();
		try
		{
			x.Throw1("faster");
			fail("RuntimeException expected");
		}
		catch (RuntimeException e)
		{
			assertEquals(a.name0, a.name);
			assertNull(a.except);
			assertNull(a.Finally);
			assertEquals("faster", x.e);
			assertEquals(e, x.result);
		}
	}

	@Test
	public void Throw2() throws Exception
	{
		a = X.P.Throw2.a;
		X x = weaved.newInstance();
		try
		{
			x.Throw2("faster");
			fail("RuntimeException expected");
		}
		catch (RuntimeException e)
		{
			assertNull(a.name);
			assertSame(e, a.except);
			assertEquals("finally", a.Finally);
			assertEquals("faster", x.e);
			assertEquals(e, x.result);
		}
	}

	@Test
	public void Throw3() throws Exception
	{
		a = X.P.Throw3.a;
		X x = weaved.newInstance();
		try
		{
			x.Throw3("faster");
			fail("RuntimeException expected");
		}
		catch (RuntimeException e)
		{
			assertEquals(a.name0, a.name);
			assertSame(X.class, a.clazz);
			assertSame(e, a.except);
			assertEquals("finally", a.Finally);
			assertEquals("faster", x.e);
			assertEquals(e, x.result);
		}
	}
}
