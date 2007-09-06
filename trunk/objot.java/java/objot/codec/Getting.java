//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.codec;

import java.sql.Clob;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;


final class Getting
{
	private static final char S = Codec.S;
	private static final int HASH_MASK = 255;
	private Codec objot;
	private Class<?> forClass;
	/** for object graph, as keys */
	private Object[][] objs;
	/**
	 * for object graph, reference numbers, as values.
	 * <dd>0: the object is referenced only once, no reference number
	 * <dd><0: the object is referenced many times, need a reference number
	 * <dd>>0: reference number
	 */
	private int[][] refs;
	/** for object graph, the number of used (multi) reference numbers */
	private int refn;

	Getting(Codec o, Class<?> for_)
	{
		objot = o;
		forClass = for_;
		objs = new Object[HASH_MASK + 1][32];
		refs = new int[HASH_MASK + 1][32];
		refn = 0;
	}

	CharSequence go(Object o) throws Exception
	{
		refs(o);
		StringBuilder s = new StringBuilder(1000);
		if (o instanceof List || o.getClass().isArray())
			list(o, s.append('['));
		else
			object(o, s.append('{'));
		return s;
	}

	@SuppressWarnings("unchecked")
	/** visit the object graph */
	private void refs(Object o) throws Exception
	{
		if (o instanceof String && ((String)o).indexOf(S) >= 0)
			throw new RuntimeException("String must not contain \20 \\20");
		if (o == null || ref(o, -1) < 0 /* multi references */)
			return;
		if (o instanceof Map)
			for (Map.Entry<String, Object> pv: ((Map<String, Object>)o).entrySet())
			{
				if (pv.getValue() != null && !pv.getValue().getClass().isPrimitive())
					refs(pv.getValue());
			}
		else if (o instanceof List)
			for (Object v: (List<?>)o)
				refs(v);
		else if (o instanceof Set)
			for (Object v: (Set<?>)o)
				refs(v);
		else if ( !o.getClass().isArray()) // other
			for (Map.Entry<String, Property> pv: objot.gets(o.getClass()).entrySet())
			{
				Class<?> c = pv.getValue().cla;
				if ( !c.isPrimitive() && !Number.class.isAssignableFrom(c)
					&& c != Boolean.class && pv.getValue().allow(forClass))
					refs(pv.getValue().get(o));
			}
		else if ( !o.getClass().getComponentType().isPrimitive()) // array
			for (Object v: (Object[])o)
				refs(v);
	}

	/**
	 * get reference number of the object.
	 * 
	 * @param r 0: return the number, <0: refer the object, >0: assign a number if need
	 * @return 0: no number, <0: need a number, >0: the number
	 */
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
					return refs[h][x] = -1;
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
			List<?> l = (List<?>)o;
			s.append(S).append(l.size());
			ref(o, s);
			for (Object v: l)
				value(v, s);
		}
		else if (o instanceof Set)
		{
			Set<?> l = (Set<?>)o;
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
		else if (o instanceof long[])
		{
			long[] l = (long[])o;
			s.append(S).append(l.length);
			ref(o, s);
			for (long v: l)
				s.append(S).append(objot.getLong(v));
		}
		else
		{
			Object[] l = (Object[])o;
			s.append(S).append(l.length);
			ref(o, s);
			for (Object v: l)
				value(v, s);
		}
		s.append(S).append(']');
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
				if (pv.getValue().allow(forClass))
				{
					s.append(S).append(pv.getKey());
					Property p = pv.getValue();
					Class<?> c = p.cla;
					if (c == double.class)
						s.append(S).append(p.getDouble(o));
					else if (c == float.class)
						s.append(S).append(p.getFloat(o));
					else if (c == int.class)
						s.append(S).append(p.getInt(o));
					else if (c == long.class)
						s.append(S).append(objot.getLong(p.getLong(o)));
					else if (c == boolean.class)
						s.append(S).append(p.getBoolean(o) ? '>' : '<');
					else
						value(p.get(o), s);
				}
		}
		s.append(S).append('}');
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
		else if (v instanceof Clob)
			s.append(S).append(S).append(((Clob)v).getSubString(1, //
				(int)Math.min(((Clob)v).length(), Integer.MAX_VALUE)));
		else if (v instanceof Boolean)
			s.append(S).append((Boolean)v ? '>' : '<');
		else if (v instanceof Double)
			s.append(S).append((double)(Double)v);
		else if (v instanceof Float)
			s.append(S).append((float)(Float)v);
		else if (v instanceof Long)
			s.append(S).append(objot.getLong((Long)v));
		else if (v instanceof Number)
			s.append(S).append(((Number)v).intValue());
		else if (v instanceof Date)
			s.append(S).append('*').append(S).append(((Date)v).getTime());
		else if (v instanceof Calendar)
			s.append(S).append('*').append(S).append(((Calendar)v).getTimeInMillis());
		else if ((ref = ref(v, 0)) > 0)
			s.append(S).append('+').append(S).append(ref);
		else if (v instanceof List || v instanceof Set || v.getClass().isArray())
			list(v, s.append(S).append('['));
		else
			object(v, s.append(S).append('{'));
	}
}
