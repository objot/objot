//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package test.chat;

import java.sql.Clob;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.sql.rowset.serial.SerialClob;

import objot.container.Container;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;

import chat.Models;
import chat.ModelsCreate;
import chat.Services;
import chat.model.Id;
import chat.service.Session;


/** every test cases in its own service session and service request */
public class TestDo
	extends Assert
{
	private static Container container0;
	private static ModelsCreate dataInit;
	protected final Container container;
	protected final Session sess;

	@BeforeClass
	public static void beforeAll() throws Exception
	{
		Locale.setDefault(Locale.ENGLISH);
		container0 = Services.build(Models.build(true).buildSessionFactory(), Services.CODEC);
		dataInit = new ModelsCreate(true);
		dataInit.create(true, 1);
		dataInit.print = false;
	}

	{
		container = container0.createBubble(null);
		sess = container.get(Session.class);
		System.err.println("\n\n************************************************\n");
	}

	@After
	public void afterTest() throws Exception
	{
		dataInit.create(true, 1);
	}

	// ********************************************************************************

	public Clob clob(String s) throws Exception
	{
		return new SerialClob(s.toCharArray());
	}

	public String string(Clob s) throws Exception
	{
		return s.getSubString(1, (int)Math.min(s.length(), Integer.MAX_VALUE));
	}

	public static void asser(boolean x)
	{
		assertTrue(x);
	}

	public static void assertEquals(Object expect, Object o)
	{
		if (expect instanceof Id && o instanceof Id)
		{
			expect = ((Id<?>)expect).id();
			o = ((Id<?>)o).id();
		}
		Assert.assertEquals(expect, o);
	}

	public static final int BAG = 0;
	public static final int SERIAL = 1;
	public static final int SET = 2;

	public static void asserts(int mode, Id<?>[] expect, Id<?>[] o)
	{
		if (expect == o)
			return;
		if (expect == null)
			fail("expected array was null");
		if (o == null)
			fail("actual array was null");
		if (o.length != expect.length)
			fail("array lengths differed, expected.length=" + expect.length
				+ " actual.length=" + o.length);

		expect = sort(mode, expect);
		o = sort(mode, o);
		for (int i = 0; i < expect.length; i++)
			if ((expect[i] == null ^ o[i] == null) //
				|| o[i] != null && expect[i].id() != o[i].id())
				fail("array first differed element [" + i + "]; expected:<"
					+ (expect[i] == null ? null : expect[i] + ",id=" + expect[i].id())
					+ "> but was:<" //
					+ (o[i] == null ? null : o[i] + ",id=" + o[i].id()) + ">");
	}

	public static void asserts(int mode, List<? extends Id<?>> expect, Id<?>[] o)
	{
		asserts(mode, expect == null ? null : expect.toArray(new Id[expect.size()]), o);
	}

	public static void asserts(int mode, Id<?>[] expect, List<? extends Id<?>> o)
	{
		asserts(mode, expect, o == null ? null : o.toArray(new Id[o.size()]));
	}

	public static void asserts(int mode, List<? extends Id<?>> expect, List<? extends Id<?>> o)
	{
		asserts(mode, expect == null ? null : expect.toArray(new Id[expect.size()]),
			o == null ? null : o.toArray(new Id[o.size()]));
	}

	public static void asserts(Set<? extends Id<?>> expect, Id<?>[] o)
	{
		asserts(SET, expect == null ? null : expect.toArray(new Id[expect.size()]), o);
	}

	public static void asserts(Id<?>[] expect, Set<? extends Id<?>> o)
	{
		asserts(SET, expect, o == null ? null : o.toArray(new Id[o.size()]));
	}

	public static void asserts(Set<? extends Id<?>> expect, List<? extends Id<?>> o)
	{
		asserts(SET, expect == null ? null : expect.toArray(new Id[expect.size()]), //
			o == null ? null : o.toArray(new Id[o.size()]));
	}

	public static void asserts(List<? extends Id<?>> expect, Set<? extends Id<?>> o)
	{
		asserts(SET, expect == null ? null : expect.toArray(new Id[expect.size()]), //
			o == null ? null : o.toArray(new Id[o.size()]));
	}

	public static void asserts(Set<? extends Id<?>> expect, Set<? extends Id<?>> o)
	{
		asserts(SET, expect == null ? null : expect.toArray(new Id[expect.size()]), //
			o == null ? null : o.toArray(new Id[o.size()]));
	}

	private static Id<?>[] sort(int mode, Id<?>[] s)
	{
		if (mode == BAG)
			Arrays.sort(s, new Comparator<Id<?>>()
			{
				public int compare(Id<?> a, Id<?> b)
				{
					if (a == null)
						return b == null ? 0 : -1;
					// no a.id - b.id to avoid arithmetic overflow
					int c = b == null ? 1 : a.id() < b.id() ? -1 : a.id() > b.id() ? 1 : 0;
					if (c == 0)
					{
						int aa = System.identityHashCode(a);
						int bb = System.identityHashCode(b);
						c = aa < bb ? -1 : aa > bb ? 1 : 0;
					}
					return c;
				}
			});
		else if (mode == SET)
			Arrays.sort(s, new Comparator<Id<?>>()
			{
				public int compare(Id<?> a, Id<?> b)
				{
					if (a == b)
						fail("set unexpected same element: " + a);
					if (a == null)
						return -1;
					// no a.id - b.id to avoid arithmetic overflow
					int c = b == null ? 1 : a.id() < b.id() ? -1 : a.id() > b.id() ? 1 : 0;
					if (c == 0)
						fail("set unexpected same element id: " + a.id());
					return c;
				}
			});
		return s;
	}
}
