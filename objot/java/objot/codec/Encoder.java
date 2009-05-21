//
// Copyright 2007-2009 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.codec;

import java.lang.reflect.Method;
import java.sql.Clob;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import objot.util.Class2;


public final class Encoder
{
	private Codec codec;
	private Object ruleKey;
	/** data as key in data graph */
	private Object[] objs;
	private int objn;
	private int mask;
	private int threshold;
	/**
	 * reference number as value in data graph.
	 * <dl>
	 * <dd>0: be refered only once, no reference number
	 * <dd>&lt;0: be refered many times, need a reference number
	 * <dd>&gt;0: reference number
	 * </dl>
	 */
	private int[] refs;
	/** the number of used reference numbers */
	private int refn;
	private StringBuilder str;
	private boolean split;

	/** @param ruleKey_ null is Object.class */
	Encoder(Codec o, Object ruleKey_, StringBuilder s)
	{
		codec = o;
		ruleKey = ruleKey_;
		str = s != null ? s : new StringBuilder(2000);
	}

	StringBuilder go(Object o) throws Exception
	{
		value(null, o); // including initializtion
		return str;
	}

	private StringBuilder split()
	{
		if (split)
			str.append(Codec.S);
		else
			split = true;
		return str;
	}

	private StringBuilder split(StringBuilder s)
	{
		return str.append(Codec.S);
	}

	static final Method M_valueObject = Class2.declaredMethod(Encoder.class, "value",
		String.class, Object.class);

	public void value(String name, Object v) throws Exception
	{
		if (name != null)
			split().append(name);
		int ref;
		if (v == null)
			split().append(',');
		else if (v instanceof CharSequence)
		{
			CharSequence s = (CharSequence)v;
			for (int l = s.length(), i = 0; i < l; i++)
				if (s.charAt(i) == Codec.S)
					throw new RuntimeException("String must not contain the split char");
			split(split()).append(s);
		}
		else if (v instanceof Clob)
		{
			String s = ((Clob)v).getSubString(1, (int)Math.min(((Clob)v).length(),
				Integer.MAX_VALUE));
			for (int l = s.length(), i = 0; i < l; i++)
				if (s.charAt(i) == Codec.S)
					throw new RuntimeException("String must not contain the split char");
			split(split()).append(s);
		}
		else if (v instanceof Boolean)
			split().append((Boolean)v ? '>' : '<');
		else if (v instanceof Double)
			split().append((double)(Double)v);
		else if (v instanceof Float)
			split().append((float)(Float)v);
		else if (v instanceof Long)
			split().append(codec.beLong((Long)v));
		else if (v instanceof Number)
			split().append(((Number)v).longValue()); // original value
		else if (v instanceof Date)
			split(split().append('*')).append(codec.beLong(((Date)v).getTime()));
		else if (v instanceof Calendar)
			split(split().append('*')).append(codec.beLong(((Calendar)v).getTimeInMillis()));
		else
		{
			if (objs == null)
			{
				objs = new Object[256];
				refs = new int[256];
				mask = 255;
				threshold = 192;
				if (ref(v, -1) >= 0)
					refsDo(v); // first reference
			}
			else if ((ref = ref(v, 0)) > 0)
			{
				split(split().append('=')).append(ref);
				return;
			}
			if (v instanceof Collection || v.getClass().isArray())
				list(v);
			else
				object(v);
		}
	}

	@SuppressWarnings("unchecked")
	private void refsDo(Object o) throws Exception
	{
		if (o instanceof Collection)
		{
			for (Object v: (Collection<?>)o)
				if ( !(v == null || v instanceof CharSequence || v instanceof Clob
					|| v instanceof Boolean || v instanceof Number || v instanceof Date
					|| v instanceof Calendar || ref(v, -1) < 0))
					refsDo(v); // first reference
			return;
		}
		if (o.getClass().isArray())
		{
			if ( !o.getClass().getComponentType().isPrimitive())
				for (Object v: (Object[])o)
					if ( !(v == null || v instanceof CharSequence || v instanceof Clob
						|| v instanceof Boolean || v instanceof Number || v instanceof Date
						|| v instanceof Calendar || ref(v, -1) < 0))
						refsDo(v); // first reference
			return;
		}
		codec.clazz(o.getClass()).encodeRefs(this, o, ruleKey);
		if (o instanceof Map)
			for (Map.Entry<String, Object> pv: ((Map<String, Object>)o).entrySet())
				if ((o = pv.getValue()) != null && !o.getClass().isPrimitive())
					if ( !(o == null || o instanceof CharSequence || o instanceof Clob
						|| o instanceof Boolean || o instanceof Number || o instanceof Date
						|| o instanceof Calendar || ref(o, -1) < 0))
						refsDo(o); // first reference
	}

	static final Method M_refs = Class2.declaredMethod1(Encoder.class, "refs");

	public void refs(Object o) throws Exception
	{
		if ( !(o == null || o instanceof CharSequence || o instanceof Clob
			|| o instanceof Boolean || o instanceof Number || o instanceof Date
			|| o instanceof Calendar || ref(o, -1) < 0))
			refsDo(o); // first reference
	}

	/**
	 * get reference number of the object.
	 * 
	 * @param r 0: return the number, <0: refer the object, >0: assign a number if need
	 * @return 0: no number, <0: need a number, >0: the number
	 */
	private int ref(Object o, int r)
	{
		int x = System.identityHashCode(o) >> 3 & mask;
		do
			if (o == objs[x])
				if (r == 0)
					return refs[x];
				else if (r < 0)
					return refs[x] < 0 ? -1 : (refs[x] = ++refn | -1); // = -1
				else
					return refs[x] >= 0 ? refs[x] : (refs[x] = refn--);
		while (objs[x = x + 1 & mask] != null);
		if (r < 0)
		{
			objs[x] = o;
			if (++objn == threshold)
			{
				Object[] os = new Object[objs.length << 1];
				int[] rs = new int[os.length];
				mask = os.length - 1;
				threshold = os.length - (os.length >> 2);
				for (int i = objs.length - 1; i >= 0; i--)
					if ((o = objs[i]) != null)
					{
						x = System.identityHashCode(o) >> 4 & mask;
						while (os[x] != null)
							x = x + 1 & mask;
						os[x] = o;
						rs[x] = refs[i];
					}
				objs = os;
				refs = rs;
			}
		}
		return 0;
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
				split().append(codec.beLong(v));
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
		split().append(codec.name(o, o.getClass()));
		ref(o);
		codec.clazz(o.getClass()).encode(this, o, ruleKey);
		if (o instanceof Map)
			for (Map.Entry<String, Object> pv: ((Map<String, Object>)o).entrySet())
				if (pv.getKey() != null)
					value(pv.getKey(), pv.getValue());
				else
					throw new RuntimeException("property name is null");
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

	public void value(String name, int v)
	{
		if (name != null)
			split().append(name);
		split().append(v);
	}

	static final Method M_valueLong = Class2.declaredMethod(Encoder.class, "value",
		String.class, long.class);

	public void value(String name, long v) throws Exception
	{
		if (name != null)
			split().append(name);
		split().append(codec.beLong(v));
	}

	static final Method M_valueBool = Class2.declaredMethod(Encoder.class, "value",
		String.class, boolean.class);

	public void value(String name, boolean v)
	{
		if (name != null)
			split().append(name);
		split().append(v ? '>' : '<');
	}

	static final Method M_valueFloat = Class2.declaredMethod(Encoder.class, "value",
		String.class, float.class);

	public void value(String name, float v)
	{
		if (name != null)
			split().append(name);
		split().append(v);
	}

	static final Method M_valueDouble = Class2.declaredMethod(Encoder.class, "value",
		String.class, double.class);

	public void value(String name, double v)
	{
		if (name != null)
			split().append(name);
		split().append(v);
	}
}
