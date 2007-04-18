//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot;

import java.io.UTFDataFormatException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
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
	protected String className(Class<?> c) throws Exception
	{
		return c.getName();
	}

	/** check long value, not too large for Javascript */
	protected long getLong(long l) throws Exception
	{
		if (l < - 562949953421312L || l > 562949953421312L) // 2^49, for Javascript
			throw new RuntimeException("getting integer out of range " + l);
		return l;
	}

	/** {@link HashSet} by default but not recommended for ORM */
	protected java.util.Set<Object> newUnique(int len) throws Exception
	{
		return new HashSet<Object>(len);
	}

	static final Class<?>[] CS0 = {};

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
						new Property(f, g, null, gs, true).into(inf);
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
						new Property(f, null, s, gs, false).into(inf);
				}
			sets.put(c, inf);
		}
		return inf;
	}

	public static Object[] ensureN(Object[] s, int n)
	{
		if (n <= s.length)
			return s;
		Object[] _ = new Object[Math.max(n, s.length + (s.length >> 1) + 5)];
		System.arraycopy(s, 0, _, 0, s.length);
		return _;
	}

	public static Class<?>[] concat(Class<?>[] a, Class<?>[] b)
	{
		if (a == null || a.length == 0)
			return b;
		if (b == null || b.length == 0)
			return a;
		Class<?>[] _ = new Class<?>[a.length + b.length];
		System.arraycopy(b, 0, _, a.length, b.length);
		return _;
	}

	public static char[] utf(byte[] s) throws UTFDataFormatException
	{
		int ulen = s.length;
		int len = 0;
		try
		{
			for (int x = 0, u; x < ulen; x++)
				if ((u = s[x]) >= 0 || //
					(u & 0xE0) == 0xC0 && (s[++x] & 0xC0) == 0x80 || //
					(u & 0xF0) == 0xE0 && (s[++x] & 0xC0) == 0x80 && (s[++x] & 0xC0) == 0x80)
					len++;
				else
					throw new UTFDataFormatException();
		}
		catch (IndexOutOfBoundsException e)
		{
			throw new UTFDataFormatException();
		}
		char[] cs = new char[len];
		int y = 0;
		for (int x = 0, u; x < ulen; x++)
			if ((u = s[x]) >= 0)
				cs[y++] = (char)u;
			else if ((u & 0xE0) == 0xC0)
				cs[y++] = (char)((u & 0x1F) << 6 | s[++x] & 0x3F);
			else
				cs[y++] = (char)((u & 0xF) << 12 | (s[++x] & 0x3F) << 6 | s[++x] & 0x3F);
		return cs;
	}

	public static byte[] utf(CharSequence s)
	{
		char c;
		int len = s.length();
		int ulen = 0;
		for (int x = 0; x < len; x++)
			if ((c = s.charAt(x)) < 0x80)
				ulen++;
			else if (c < 0x800)
				ulen += 2;
			else
				ulen += 3;
		byte[] utf = new byte[ulen];
		int y = 0;
		for (int x = 0; x < len; x++)
			if ((c = s.charAt(x)) < 0x80)
				utf[y++] = (byte)c;
			else if (c < 0x800)
			{
				utf[y++] = (byte)(0xC0 | (c >>> 6) & 0x1F);
				utf[y++] = (byte)(0x80 | c & 0x3F);
			}
			else
			{
				utf[y++] = (byte)(0xE0 | (c >>> 12) & 0x0F);
				utf[y++] = (byte)(0x80 | (c >>> 6) & 0x3F);
				utf[y++] = (byte)(0x80 | c & 0x3F);
			}
		return utf;
	}
}
