//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.codec;

import java.io.UTFDataFormatException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;


public class Codec
{
	public static final char S = '\20';

	/**
	 * @param o the whole gettable object graph must keep unchanged since the references
	 *            detection is not thread safe
	 */
	public CharSequence enc(Object o, Class<?> for_) throws Exception
	{
		return new Encoder(this, for_).go(o);
	}

	public Object dec(char[] s, Class<?> clazz, Class<?> for_) throws Exception
	{
		return new Decoder(this, for_, s).go(clazz);
	}

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
		if (l < -4503599627370496L || l > 4503599627370496L) // 2^52, for Javascript
			throw new RuntimeException("getting integer out of range " + l);
		return l;
	}

	/** {@link HashSet} by default but not recommended for ORM */
	protected java.util.Set<Object> newUniques(int len) throws Exception
	{
		return new HashSet<Object>(len);
	}

	static final Class<?>[] CS0 = {};

	private final ConcurrentHashMap<Class<?>, HashMap<String, Property>> encs //
	= new ConcurrentHashMap<Class<?>, HashMap<String, Property>>(64, 0.8f, 32);
	private final ConcurrentHashMap<Class<?>, HashMap<String, Property>> decs //
	= new ConcurrentHashMap<Class<?>, HashMap<String, Property>>(64, 0.8f, 32);

	final HashMap<String, Property> encs(Class<?> c)
	{
		HashMap<String, Property> inf = encs.get(c);
		if (inf == null)
		{
			inf = new HashMap<String, Property>();
			for (Field f: c.getFields())
				if ((f.getModifiers() & Modifier.STATIC) == 0)
				{
					Enc e = f.getAnnotation(Enc.class);
					EncDec gs = f.getAnnotation(EncDec.class);
					if (e != null || gs != null)
						new PropField(f, e, null, gs, true).into(inf);
				}
			for (Method m: c.getMethods())
				if ((m.getModifiers() & Modifier.STATIC) == 0)
				{
					Enc e = m.getAnnotation(Enc.class);
					if (e != null)
						new PropMethod(m, e, null, true).into(inf);
				}
			encs.put(c, inf);
		}
		return inf;
	}

	final HashMap<String, Property> decs(Class<?> c)
	{
		HashMap<String, Property> inf = decs.get(c);
		if (inf == null)
		{
			inf = new HashMap<String, Property>();
			for (Field f: c.getFields())
				if ((f.getModifiers() & Modifier.STATIC) == 0)
				{
					Dec d = f.getAnnotation(Dec.class);
					EncDec gs = f.getAnnotation(EncDec.class);
					if (d != null || gs != null)
						new PropField(f, null, d, gs, false).into(inf);
				}
			for (Method m: c.getMethods())
				if ((m.getModifiers() & Modifier.STATIC) == 0)
				{
					Dec d = m.getAnnotation(Dec.class);
					if (d != null)
						new PropMethod(m, null, d, false).into(inf);
				}
			decs.put(c, inf);
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
