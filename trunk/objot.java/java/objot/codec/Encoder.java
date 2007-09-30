//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.codec;

import java.lang.reflect.Method;
import java.sql.Clob;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import objot.util.Array2;
import objot.util.Class2;


final class Encoder
{
	private static final int HASH_MASK = 255;
	private Codec codec;
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
	private StringBuilder str;

	Encoder(Codec o, Class<?> for_)
	{
		codec = o;
		forClass = for_;
		objs = new Object[HASH_MASK + 1][32];
		refs = new int[HASH_MASK + 1][32];
		refn = 0;
		str = new StringBuilder(1000);
	}

	CharSequence go(Object o) throws Exception
	{
		refs(o);
		if (o instanceof Collection || o.getClass().isArray())
			list(o);
		else
			object(o);
		return str;
	}

	static final Method M_refs = Class2.declaredMethod1(Encoder.class, "refs");

	@SuppressWarnings("unchecked")
	/** visit the object graph */
	void refs(Object o) throws Exception
	{
		if (o instanceof String && ((String)o).indexOf(Codec.S) >= 0)
			throw new RuntimeException("String must not contain \20 \\20");
		if (o == null || ref(o, -1) < 0 /* multi references */)
			return;
		if (o instanceof Map)
			for (Map.Entry<String, Object> pv: ((Map<String, Object>)o).entrySet())
			{
				if (pv.getValue() != null && !pv.getValue().getClass().isPrimitive())
					refs(pv.getValue());
			}
		else if (o instanceof Collection)
			for (Object v: (Collection<?>)o)
				refs(v);
		else if ( !o.getClass().isArray()) // other
			codec.clazz(o.getClass()).encodeRefs(this, o, forClass);
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
			objs[h] = Array2.ensureN(s, x + 2);
			refs[h] = Array2.ensureN(refs[h], x + 2);
		}
		return 0;
	}

	private StringBuilder split()
	{
		if (str.length() > 0)
			str.append(Codec.S);
		return str;
	}

	private StringBuilder split(StringBuilder s)
	{
		return str.append(Codec.S);
	}

	private void list(Object o) throws Exception
	{
		split().append('[');
		if (o instanceof Collection)
		{
			Collection<?> l = (Collection<?>)o;
			split().append(l.size());
			ref(o);
			for (Object v: l)
				value(null, v);
		}
		else if (o instanceof boolean[])
		{
			boolean[] l = (boolean[])o;
			split().append(l.length);
			ref(o);
			for (boolean v: l)
				split().append(v ? '>' : '<');
		}
		else if (o instanceof int[])
		{
			int[] l = (int[])o;
			split().append(l.length);
			ref(o);
			for (int v: l)
				split().append(v);
		}
		else if (o instanceof long[])
		{
			long[] l = (long[])o;
			split().append(l.length);
			ref(o);
			for (long v: l)
				split().append(codec.getLong(v));
		}
		else
		{
			Object[] l = (Object[])o;
			split().append(l.length);
			ref(o);
			for (Object v: l)
				value(null, v);
		}
		split().append(']');
	}

	@SuppressWarnings("unchecked")
	private void object(Object o) throws Exception
	{
		split().append('{');
		if (o instanceof Map)
		{
			split();
			ref(o);
			for (Map.Entry<String, Object> pv: ((Map<String, Object>)o).entrySet())
				value(pv.getKey(), pv.getValue());
		}
		else
		{
			split().append(codec.className(o.getClass()));
			ref(o);
			codec.clazz(o.getClass()).encode(this, o, forClass);
		}
		split().append('}');
	}

	private void ref(Object o)
	{
		int ref = ref(o, 1);
		if (ref > 0)
			split(split().append(':')).append(ref);
	}

	static final Method M_valueInt = Class2.declaredMethod(Encoder.class, "value",
		String.class, int.class);

	void value(String name, int v)
	{
		if (name != null)
			split().append(name);
		split().append(v);
	}

	static final Method M_valueLong = Class2.declaredMethod(Encoder.class, "value",
		String.class, long.class);

	void value(String name, long v) throws Exception
	{
		if (name != null)
			split().append(name);
		split().append(codec.getLong(v));
	}

	static final Method M_valueBool = Class2.declaredMethod(Encoder.class, "value",
		String.class, boolean.class);

	void value(String name, boolean v)
	{
		if (name != null)
			split().append(name);
		split().append(v ? '>' : '<');
	}

	static final Method M_valueFloat = Class2.declaredMethod(Encoder.class, "value",
		String.class, float.class);

	void value(String name, float v)
	{
		if (name != null)
			split().append(name);
		split().append(v);
	}

	static final Method M_valueDouble = Class2.declaredMethod(Encoder.class, "value",
		String.class, double.class);

	void value(String name, double v)
	{
		if (name != null)
			split().append(name);
		split().append(v);
	}

	static final Method M_valueObject = Class2.declaredMethod(Encoder.class, "value",
		String.class, Object.class);

	void value(String name, Object v) throws Exception
	{
		if (name != null)
			split().append(name);
		int ref;
		if (v == null)
			split().append(',');
		else if (v instanceof String)
			split(split()).append((String)v);
		else if (v instanceof Clob)
			split(split()).append(((Clob)v).getSubString(1, //
				(int)Math.min(((Clob)v).length(), Integer.MAX_VALUE)));
		else if (v instanceof Boolean)
			split().append((Boolean)v ? '>' : '<');
		else if (v instanceof Double)
			split().append((double)(Double)v);
		else if (v instanceof Float)
			split().append((float)(Float)v);
		else if (v instanceof Long)
			split().append(codec.getLong((Long)v));
		else if (v instanceof Number)
			split().append(((Number)v).intValue());
		else if (v instanceof Date)
			split(split().append('*')).append(((Date)v).getTime());
		else if (v instanceof Calendar)
			split(split().append('*')).append(((Calendar)v).getTimeInMillis());
		else if ((ref = ref(v, 0)) > 0)
			split(split().append('=')).append(ref);
		else if (v instanceof Collection || v.getClass().isArray())
			list(v);
		else
			object(v);
	}
}
