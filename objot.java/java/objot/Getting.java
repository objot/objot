//
// Objot 11a
//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;


public final class Getting
{
	/**
	 * @param o the whole gettable object graph must keep unchanged since the references
	 *            detection is not thread safe
	 */
	public static byte[] go(Objot objot, Class<?> for_, Object o) throws Exception
	{
		return new Getting(objot, for_).go(o);
	}

	private static final char S = Objot.S;
	private static final int HASH_MASK = 255;
	private Objot objot;
	private Class<?> forClass;
	private Object[][] objs;
	private int[][] refs;
	private int refn;

	private Getting(Objot o, Class<?> for_)
	{
		objot = o;
		forClass = for_;
		objs = new Object[HASH_MASK + 1][32];
		refs = new int[HASH_MASK + 1][32];
		refn = 0;
	}

	private byte[] go(Object o) throws Exception
	{
		refs(o);
		StringBuilder s = new StringBuilder(1000);
		if (o instanceof List || o.getClass().isArray())
			list(o, s.append('['));
		else
			object(o, s.append('/'));
		return utf(s);
	}

	@SuppressWarnings("unchecked")
	private void refs(Object o) throws Exception
	{
		if (o instanceof String && ((String)o).indexOf(S) >= 0)
			throw new RuntimeException("String must not contain \20 \\20");
		if (o == null || ref(o, - 1) < 0)
			return;
		if (o instanceof Map)
		{
			for (Map.Entry<String, Object> pv: ((Map<String, Object>)o).entrySet())
				if (pv.getValue() != null && ! pv.getValue().getClass().isPrimitive())
					refs(pv.getValue());
		}
		else if (o instanceof List)
		{
			for (Object v: (List)o)
				refs(v);
		}
		else if (! o.getClass().isArray())
		{
			for (Map.Entry<String, Property> pv: objot.gets(o.getClass()).entrySet())
			{
				Class<?> c = pv.getValue().f.getType();
				if (! c.isPrimitive() && ! Number.class.isAssignableFrom(c)
					&& c != Boolean.class && pv.getValue().in(forClass))
					refs(pv.getValue().f.get(o));
			}
		}
		else if (! o.getClass().getComponentType().isPrimitive())
			for (Object v: (Object[])o)
				refs(v);
	}

	private int ref(Object o, int r)
	{
		int h = (System.identityHashCode(o) >> 3) & HASH_MASK;
		Object[] s = objs[h];
		int x;
		for (x = 0; s[x] != null; x++)
			if (o == s[x])
				if (r == 0)
					return refs[h][x];
				else if (r < 0)
					return refs[h][x] = - 1;
				else
					return refs[h][x] >= 0 ? refs[h][x] : (refs[h][x] = ++refn);
		if (r < 0)
		{
			s[x] = o;
			objs[h] = ensureN(s, x + 2);
			refs[h] = ensureN(refs[h], x + 2);
		}
		return 0;
	}

	private static Object[] ensureN(Object[] s, int n)
	{
		if (n <= s.length)
			return s;
		Object[] _ = new Object[Math.max(n, s.length + (s.length >> 2) + 5)];
		System.arraycopy(s, 0, _, 0, s.length);
		return _;
	}

	private static int[] ensureN(int[] s, int n)
	{
		if (n <= s.length)
			return s;
		int[] _ = new int[Math.max(n, s.length + (s.length >> 2) + 5)];
		System.arraycopy(s, 0, _, 0, s.length);
		return _;
	}

	private void list(Object o, StringBuilder s) throws Exception
	{
		if (o instanceof List)
		{
			List<?> l = (List)o;
			s.append(S).append(l.size());
			ref(o, s);
			for (Object v: l)
				value(v, s);
		}
		else if (o instanceof boolean[])
		{
			boolean[] l = (boolean[])o;
			s.append(S).append(l.length);
			ref(o, s);
			for (boolean v: l)
				s.append(S).append(v ? '>' : '<');
		}
		else if (o instanceof int[])
		{
			int[] l = (int[])o;
			s.append(S).append(l.length);
			ref(o, s);
			for (int v: l)
				s.append(S).append(v);
		}
		else
		{
			Object[] l = (Object[])o;
			s.append(S).append(l.length);
			ref(o, s);
			for (Object v: l)
				value(v, s);
		}
		s.append(S).append(';');
	}

	@SuppressWarnings("unchecked")
	private void object(Object o, StringBuilder s) throws Exception
	{
		if (o instanceof Map)
		{
			s.append(S);
			ref(o, s);
			for (Map.Entry<String, Object> pv: ((Map<String, Object>)o).entrySet())
				value(pv.getValue(), s.append(S).append(pv.getKey()));
		}
		else
		{
			s.append(S).append(objot.className(o.getClass()));
			ref(o, s);
			for (Map.Entry<String, Property> pv: objot.gets(o.getClass()).entrySet())
				if (pv.getValue().in(forClass))
				{
					s.append(S).append(pv.getKey());
					Field f = pv.getValue().f;
					Class<?> c = f.getType();
					if (c == double.class)
						s.append(S).append(f.getDouble(o));
					else if (c == float.class)
						s.append(S).append(f.getFloat(o));
					else if (c == int.class)
						s.append(S).append(f.getInt(o));
					else if (c == boolean.class)
						s.append(S).append(f.getBoolean(o) ? '>' : '<');
					else
						value(f.get(o), s);
				}
		}
		s.append(S).append(';');
	}

	private void ref(Object o, StringBuilder s)
	{
		int ref = ref(o, 1);
		if (ref > 0)
			s.append(S).append('=').append(S).append(ref);
	}

	private void value(Object v, StringBuilder s) throws Exception
	{
		int ref;
		if (v == null)
			s.append(S).append('.');
		else if (v instanceof String)
			s.append(S).append(S).append((String)v);
		else if (v instanceof Boolean)
			s.append(S).append((Boolean)v ? '>' : '<');
		else if (v instanceof Double)
			s.append(S).append((double)(Double)v);
		else if (v instanceof Float)
			s.append(S).append((float)(Float)v);
		else if (v instanceof Number)
			s.append(S).append((int)(Integer)v);
		else if ((ref = ref(v, 0)) > 0)
			s.append(S).append('+').append(S).append(ref);
		else if (v instanceof List || v.getClass().isArray())
			list(v, s.append(S).append('['));
		else
			object(v, s.append(S).append('/'));
	}

	private static byte[] utf(StringBuilder s)
	{
		char c;
		int len = s.length();
		int ulen = 0;
		for (int i = 0; i < len; i++)
			if ((c = s.charAt(i)) < 0x80)
				ulen++;
			else if (c < 0x800)
				ulen += 2;
			else
				ulen += 3;
		byte[] utf = new byte[ulen];
		int ui = 0;
		for (int i = 0; i < len; i++)
			if ((c = s.charAt(i)) < 0x80)
				utf[ui++] = (byte)c;
			else if (c < 0x800)
			{
				utf[ui++] = (byte)(0xC0 | (c >>> 6) & 0x1F);
				utf[ui++] = (byte)(0x80 | c & 0x3F);
			}
			else
			{
				utf[ui++] = (byte)(0xE0 | (c >>> 12) & 0x0F);
				utf[ui++] = (byte)(0x80 | (c >>> 6) & 0x3F);
				utf[ui++] = (byte)(0x80 | c & 0x3F);
			}
		return utf;
	}
}
