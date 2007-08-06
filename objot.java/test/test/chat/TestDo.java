package test.chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;

import chat.Models;
import chat.ModelsCreate;
import chat.Scopes;
import chat.Services;
import chat.Transac;
import chat.model.Id;
import chat.service.Data;
import chat.service.Session;

import com.google.inject.Injector;


/** every test cases in its own service session and service request */
public class TestDo
	extends Assert
{
	private static SessionFactory dataFactory;
	protected static Injector container;
	protected Session sess;
	protected Data data;

	@BeforeClass
	public static void beforeAll() throws Exception
	{
		Locale.setDefault(Locale.ENGLISH);
		new ModelsCreate(true, true, true);
		dataFactory = Models.build(true).buildSessionFactory();
		container = Services.build(dataFactory, true, 1);
	}

	{
		sess = Scopes.session(null);
		Scopes.request();
		data = container.getInstance(Data.class);
		System.out.println("\n************************************************");
	}

	@After
	public void afterTest() throws Exception
	{
		Transac.Aspect.invokeFinally(data, false, null);
	}

	// ********************************************************************************

	public <T>Set<T> copy(Set<T> s)
	{
		return new HashSet<T>(s);
	}

	public <T>List<T> copy(List<T> s)
	{
		return new ArrayList<T>(s);
	}

	public static void asser(boolean x)
	{
		assertTrue(x);
	}

	public static void assertEquals(Object expect, Object o)
	{
		if (expect instanceof Id && o instanceof Id)
		{
			expect = ((Id)expect).id();
			o = ((Id)o).id();
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
						return b == null ? 0 : - 1;
					// no a.id - b.id to avoid arithmetic overflow
					int c = b == null ? 1 : a.id() < b.id() ? - 1 : a.id() > b.id() ? 1 : 0;
					if (c == 0)
					{
						int aa = System.identityHashCode(a);
						int bb = System.identityHashCode(b);
						c = aa < bb ? - 1 : aa > bb ? 1 : 0;
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
						return - 1;
					// no a.id - b.id to avoid arithmetic overflow
					int c = b == null ? 1 : a.id() < b.id() ? - 1 : a.id() > b.id() ? 1 : 0;
					if (c == 0)
						fail("set unexpected same element id: " + a.id());
					return c;
				}
			});
		return s;
	}
}
