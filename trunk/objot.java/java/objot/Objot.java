//
// Objot 11a
//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;


public class Objot
{
	public static final char S = '\20';

	/** multi thread, may be cached */
	protected Class<?> classByName(String name) throws Exception
	{
		return Class.forName(name);
	}

	/** multi thread, may be cached */
	protected String className(Class<?> c)
	{
		return c.getName();
	}

	private final ConcurrentHashMap<Class<?>, HashMap<String, Property>> gets //
	= new ConcurrentHashMap<Class<?>, HashMap<String, Property>>(64, 0.8f, 32);
	private final ConcurrentHashMap<Class<?>, HashMap<String, Property>> sets //
	= new ConcurrentHashMap<Class<?>, HashMap<String, Property>>(64, 0.8f, 32);

	final HashMap<String, Property> gets(Class<?> c)
	{
		HashMap<String, Property> inf = gets.get(c);
		if (inf == null)
		{
			inf = new HashMap<String, Property>();
			for (Field f: c.getFields())
				if ((f.getModifiers() & Modifier.STATIC) == 0)
				{
					Get g = f.getAnnotation(Get.class);
					GetSet gs = f.getAnnotation(GetSet.class);
					if (g != null || gs != null)
						new Property(f, g, gs).into(inf);
				}
			gets.put(c, inf);
		}
		return inf;
	}

	final HashMap<String, Property> sets(Class<?> c)
	{
		HashMap<String, Property> inf = sets.get(c);
		if (inf == null)
		{
			inf = new HashMap<String, Property>();
			for (Field f: c.getFields())
				if ((f.getModifiers() & Modifier.STATIC) == 0)
				{
					Set s = f.getAnnotation(Set.class);
					GetSet gs = f.getAnnotation(GetSet.class);
					if (s != null || gs != null)
						new Property(f, s, gs).into(inf);
				}
			sets.put(c, inf);
		}
		return inf;
	}

	static Object[] ensureN(Object[] s, int n)
	{
		if (n <= s.length)
			return s;
		Object[] _ = new Object[Math.max(n, s.length + (s.length >> 1) + 5)];
		System.arraycopy(s, 0, _, 0, s.length);
		return _;
	}

	static Class<?>[] concat(Class<?>[] a, Class<?>[] b)
	{
		if (a == null || a.length == 0)
			return b;
		if (b == null || b.length == 0)
			return a;
		Class<?>[] _ = new Class<?>[a.length + b.length];
		System.arraycopy(b, 0, _, a.length, b.length);
		return _;
	}
}
