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

import objot.util.Array2;
import objot.util.Chars;
import objot.util.Class2;


public final class Encoder
{
	private final Codec codec;
	private final Object ruleKey;
	private final boolean fast;
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
	private final StringBuilder str;
	private final Chars chs;
	private char[] cs;
	private int cx;
	private boolean split;

	/** @param ruleKey_ null is Object.class */
	Encoder(Codec o, Object ruleKey_, boolean fast_, StringBuilder s, Chars chs_)
	{
		codec = o;
		ruleKey = ruleKey_;
		fast = fast_;
		if (fast)
			chs = chs_ != (Object)(str = null) ? chs_ : new Chars().ensureN(2000);
		else
			str = s != (Object)(chs = null) ? s : new StringBuilder(2000);
	}

	StringBuilder go(Object o) throws Exception
	{
		valueNormal(null, o);
		return str;
	}

	Chars goFast(Object o) throws Exception
	{
		cs = chs.chars;
		cx = chs.beginI;
		valueFast(o);
		chs.chars = cs;
		chs.end1I = cx;
		return chs;
	}

	private StringBuilder split()
	{
		if (split)
			str.append(Codec.S);
		else
			split = true;
		return str;
	}

	private StringBuilder split(StringBuilder dummy)
	{
		return str.append(Codec.S);
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
		for (; objs[x] != null; x = x + 1 & mask)
			if (o == objs[x])
				if (r == 0)
					return refs[x];
				else if (r < 0)
					return refs[x] < 0 ? -1 : (refs[x] = ++refn | -1); // = -1
				else
					return refs[x] >= 0 ? refs[x] : (refs[x] = refn--);
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

	static final Method M_valueInt = Class2.declaredMethod(Encoder.class, "value",
		String.class, int.class);

	public void value(String name, int v)
	{
		if (fast)
		{
			int l = name.length();
			cs = Array2.ensureN(cs, cx + 3 + l);
			short v2;
			if ((v2 = (short)v) == v)
			{
				cs[cx++] = (char)('\u6000' | l);
				name.getChars(0, l, cs, (cx += l) - l);
				cx = Chars.writeS2(cs, cx, v2);
			}
			else
			{
				cs[cx++] = (char)('\u8000' | l);
				name.getChars(0, l, cs, (cx += l) - l);
				cx = Chars.writeS4(cs, cx, v);
			}
		}
		else
			split(split().append(name)).append(v);
	}

	static final Method M_valueLong = Class2.declaredMethod(Encoder.class, "value",
		String.class, long.class);

	public void value(String name, long v) throws Exception
	{
		if (fast)
		{
			int l = name.length(), v4;
			cs = Array2.ensureN(cs, cx + 5 + l);
			short v2;
			if ((v2 = (short)v) == v)
			{
				cs[cx++] = (char)('\u6000' | l);
				name.getChars(0, l, cs, (cx += l) - l);
				cx = Chars.writeS2(cs, cx, v2);
			}
			else if ((v4 = (int)v) == v)
			{
				cs[cx++] = (char)('\u8000' | l);
				name.getChars(0, l, cs, (cx += l) - l);
				cx = Chars.writeS4(cs, cx, v4);
			}
			else
			{
				cs[cx++] = (char)('\uA000' | l);
				name.getChars(0, l, cs, (cx += l) - l);
				cx = Chars.writeS8(cs, cx, v);
			}
		}
		else
			split(split().append(name)).append(codec.beLong(v));
	}

	static final Method M_valueBool = Class2.declaredMethod(Encoder.class, "value",
		String.class, boolean.class);

	public void value(String name, boolean v)
	{
		if (fast)
		{
			int l = name.length();
			cs = Array2.ensureN(cs, cx + 1 + l);
			cs[cx++] = (char)(v ? '\u4000' | l : '\u2000' | l);
			name.getChars(0, l, cs, (cx += l) - l);
		}
		else
			split(split().append(name)).append(v ? '>' : '<');
	}

	static final Method M_valueFloat = Class2.declaredMethod(Encoder.class, "value",
		String.class, float.class);

	public void value(String name, float v)
	{
		if (fast)
			value(name, (double)v);
		else
		{
			int v4 = (int)v;
			split().append(name);
			if (v4 == v)
				split().append(v4);
			else
				split().append(v);
		}
	}

	static final Method M_valueDouble = Class2.declaredMethod(Encoder.class, "value",
		String.class, double.class);

	public void value(String name, double v)
	{
		if (fast)
		{
			int l = name.length(), v4;
			cs = Array2.ensureN(cs, cx + 5 + l);
			short v2;
			if ((v4 = (int)v) == v)
				if ((v2 = (short)v4) == v4)
				{
					cs[cx++] = (char)('\u6000' | l);
					name.getChars(0, l, cs, (cx += l) - l);
					cx = Chars.writeS2(cs, cx, v2);
				}
				else
				{
					cs[cx++] = (char)('\u8000' | l);
					name.getChars(0, l, cs, (cx += l) - l);
					cx = Chars.writeS4(cs, cx, v4);
				}
			else
			{
				cs[cx++] = (char)('\uC000' | l);
				name.getChars(0, l, cs, (cx += l) - l);
				cx = Chars.writeS8(cs, cx, Double.doubleToRawLongBits(v));
			}
		}
		else
		{
			int v4 = (int)v;
			split().append(name);
			if (v4 == v)
				split().append(v4);
			else
				split().append(v);
		}
	}

	static final Method M_valueObject = Class2.declaredMethod(Encoder.class, "value",
		String.class, Object.class);

	public void value(String name, Object v) throws Exception
	{
		if ( !fast)
		{
			valueNormal(name, v);
			return;
		}
		int cx0 = cx, n = name.length();
		cs = Array2.ensureN(cs, cx += 1 + n);
		name.getChars(0, n, cs, cx - n);
		int v4;
		short v2;

		if (v == null)
			cs[cx0] = (char)n;
		else if (v instanceof Boolean)
			cs[cx0] = (char)((Boolean)v ? '\u4000' | n : '\u2000' | n);
		else if (v instanceof Integer)
		{
			cs = Array2.ensureN(cs, cx + 2);
			v4 = (Integer)v;
			if ((v2 = (short)v4) == v4)
			{
				cs[cx0] = (char)('\u6000' | n);
				cx = Chars.writeS2(cs, cx, v2);
			}
			else
			{
				cs[cx0] = (char)('\u8000' | n);
				cx = Chars.writeS4(cs, cx, v4);
			}
		}
		else if (v instanceof Double || v instanceof Float)
		{
			cs = Array2.ensureN(cs, cx + 4);
			double d = ((Number)v).doubleValue();
			if ((v4 = (int)d) == d)
				if ((v2 = (short)v4) == v4)
				{
					cs[cx0] = (char)('\u6000' | n);
					cx = Chars.writeS2(cs, cx, v2);
				}
				else
				{
					cs[cx0] = (char)('\u8000' | n);
					cx = Chars.writeS4(cs, cx, v4);
				}
			else
			{
				cs[cx0] = (char)('\uC000' | n);
				cx = Chars.writeS8(cs, cx, Double.doubleToRawLongBits(d));
			}
		}
		else if (v instanceof Number)
		{
			cs = Array2.ensureN(cs, cx + 4);
			long v8 = ((Number)v).longValue();
			if ((v2 = (short)v8) == v8)
			{
				cs[cx0] = (char)('\u6000' | n);
				cx = Chars.writeS2(cs, cx, v2);
			}
			else if ((v4 = (int)v8) == v8)
			{
				cs[cx0] = (char)('\u8000' | n);
				cx = Chars.writeS4(cs, cx, v4);
			}
			else
			{
				cs[cx0] = (char)('\uA000' | n);
				cx = Chars.writeS8(cs, cx, v8);
			}
		}
		else
		{
			cs[cx0] = (char)('\uE000' | n);
			valueFast0(v);
		}
	}

	private void valueNormal(String name, Object v) throws Exception
	{
		if (name != null)
			split().append(name);
		if (v == null)
		{
			split().append(',');
			return;
		}
		float f;
		double d;
		int v4;
		if (v instanceof Clob)
			v = ((Clob)v).getSubString(1,
				(int)Math.min(((Clob)v).length(), Integer.MAX_VALUE));
		if (v instanceof CharSequence)
		{
			CharSequence s = (CharSequence)v;
			for (int l = s.length(), i = 0; i < l; i++)
				if (s.charAt(i) == Codec.S)
					throw new RuntimeException("String must not contain the split char");
			split(split()).append(s);
		}
		else if (v instanceof Boolean)
			split().append((Boolean)v ? '>' : '<');
		else if (v instanceof Double)
			if ((d = (Double)v) == (v4 = (int)d))
				split().append(v4);
			else
				split().append(d);
		else if (v instanceof Float)
			if ((f = (Float)v) == (v4 = (int)f))
				split().append(v4);
			else
				split().append(f);
		else if (v instanceof Long)
			split().append(codec.beLong((Long)v));
		else if (v instanceof Number)
			split().append(codec.beLong(((Number)v).longValue()));
		else if (v instanceof Date)
			split(split().append('*')).append(codec.beLong(((Date)v).getTime()));
		else if (v instanceof Calendar)
			split(split().append('*')).append(codec.beLong(((Calendar)v).getTimeInMillis()));
		else
		{
			int ref;
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
				listNormal(v);
			else
				objectNormal(v);
		}
	}

	private void listNormal(Object o) throws Exception
	{
		split().append('[');
		if (o instanceof Collection)
		{
			Collection<?> l = (Collection<?>)o;
			split().append(l.size());
			ref(o);
			for (Object v: l)
				valueNormal(null, v);
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
				valueNormal(null, v);
		}
		split().append(']');
	}

	@SuppressWarnings("unchecked")
	private void objectNormal(Object o) throws Exception
	{
		split().append('{');
		split().append(codec.name(o, o.getClass(), ruleKey));
		ref(o);
		codec.clazz(o.getClass()).encode(this, o, ruleKey);
		if (o instanceof Map)
			for (Map.Entry<String, Object> pv: ((Map<String, Object>)o).entrySet())
				if (pv.getKey() != null)
					valueNormal(pv.getKey(), pv.getValue());
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

	private void valueFast(Object v) throws Exception
	{
		if (v == null)
		{
			cs = Array2.ensureN(cs, cx + 1);
			cs[cx++] = '\u0000';
		}
		else if (v instanceof Boolean)
		{
			cs = Array2.ensureN(cs, cx + 1);
			cs[cx++] = (Boolean)v ? '\u0002' : '\u0001';
		}
		else if (v instanceof Integer)
		{
			cs = Array2.ensureN(cs, cx + 3);
			int v4 = (Integer)v, b12, b28;
			if ((b12 = v4 << 20 >> 20) == v4)
				cs[cx++] = (char)('\u1000' | b12 & 4095); // 12s
			else if ((b28 = v4 << 4 >> 4) == v4)
			{
				cs[cx++] = (char)('\u2000' | b28 & 4095); // 12l
				cx = Chars.writeS2(cs, cx, (short)(b28 >> 12)); // 16sh
			}
			else
			{
				cs[cx++] = '\u0004';
				cx = Chars.writeS4(cs, cx, v4);
			}
		}
		else if (v instanceof Double || v instanceof Float)
		{
			cs = Array2.ensureN(cs, cx + 5);
			double d = ((Number)v).doubleValue();
			int v4, b12, b28;
			if ((v4 = (int)d) == d)
				if ((b12 = v4 << 20 >> 20) == v4)
					cs[cx++] = (char)('\u1000' | b12 & 4095); // 12s
				else if ((b28 = v4 << 4 >> 4) == v4)
				{
					cs[cx++] = (char)('\u2000' | b28 & 4095); // 12l
					cx = Chars.writeS2(cs, cx, (short)(b28 >> 12)); // 16sh
				}
				else
				{
					cs[cx++] = '\u0004';
					cx = Chars.writeS4(cs, cx, v4);
				}
			else
			{
				cs[cx++] = '\u0006';
				cx = Chars.writeS8(cs, cx, Double.doubleToRawLongBits(d));
			}
		}
		else if (v instanceof Number)
		{
			cs = Array2.ensureN(cs, cx + 5);
			long v8 = ((Number)v).longValue(), b12, b28;
			int v4;
			if ((b12 = v8 << 52 >> 52) == v8)
				cs[cx++] = (char)('\u1000' | (int)b12 & 4095); // 12s
			else if ((b28 = v8 << 36 >> 36) == v8)
			{
				cs[cx++] = (char)('\u2000' | (int)b28 & 4095); // 12l
				cx = Chars.writeS2(cs, cx, (short)(b28 >> 12)); // 16sh
			}
			else if ((v4 = (int)v8) == v8)
			{
				cs[cx++] = '\u0004';
				cx = Chars.writeS4(cs, cx, v4);
			}
			else
			{
				cs[cx++] = '\u0005';
				cx = Chars.writeS8(cs, cx, v8);
			}
		}
		else
			valueFast0(v);
	}

	/** excludes null, bool, num */
	private void valueFast0(Object v) throws Exception
	{
		if (v instanceof Clob)
			v = ((Clob)v).getSubString(1,
				(int)Math.min(((Clob)v).length(), Integer.MAX_VALUE));
		if (v instanceof String)
		{
			String s = (String)v;
			int n = s.length();
			cs = Array2.ensureN(cs, cx + 3 + n);
			if (n < 1024)
				cs[cx++] = (char)('\u0C00' | n); // 10u
			else
			{
				cs[cx++] = '\u0003';
				cx = Chars.writeS4(cs, cx, n);
			}
			s.getChars(0, n, cs, cx);
			cx += n;
		}
		else if (v instanceof CharSequence)
		{
			CharSequence s = (CharSequence)v;
			int n = s.length();
			cs = Array2.ensureN(cs, cx + 3 + n);
			if (n < 1024)
				cs[cx++] = (char)('\u0C00' | n); // 10u
			else
			{
				cs[cx++] = '\u0003';
				cx = Chars.writeS4(cs, cx, n);
			}
			for (int i = 0; i < n; i++)
				cs[cx++] = s.charAt(i);
		}
		else if (v instanceof Date)
		{
			long t = ((Date)v).getTime();
			cs = Array2.ensureN(cs, cx + 4);
			cs[cx++] = (char)('\u0800' | t & 1023); // 10l
			cx = Chars.writeS4(cs, cx, (int)(t >> 10)); // 32sh
		}
		else if (v instanceof Calendar)
		{
			long t = ((Calendar)v).getTimeInMillis();
			cs = Array2.ensureN(cs, cx + 4);
			cs[cx++] = (char)('\u0800' | t & 1023); // 10l
			cx = Chars.writeS4(cs, cx, (int)(t >> 10)); // 32sh
		}
		else
		{
			int ref;
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
				cs = Array2.ensureN(cs, cx + 1);
				cs[cx++] = (char)('\u4000' | ref); // 14u
				return;
			}
			if (v instanceof Collection || v.getClass().isArray())
				listFast(v);
			else
				objectFast(v);
		}
	}

	private void listFast(Object o) throws Exception
	{
		if (o instanceof Collection)
		{
			Collection<?> l = (Collection<?>)o;
			int n = l.size();
			cs = Array2.ensureN(cs, cx + 3);
			listFast0(n, l);
			for (Object v: l)
				valueFast(v);
		}
		else if (o instanceof boolean[])
		{
			boolean[] l = (boolean[])o;
			int n = l.length;
			cs = Array2.ensureN(cs, cx + 3 + n);
			listFast0(n, l);
			for (boolean v: l)
				cs[cx++] = v ? '\u0002' : '\u0001';
		}
		else if (o instanceof int[])
		{
			int[] l = (int[])o;
			int n = l.length;
			cs = Array2.ensureN(cs, cx + 3 + n + n + n);
			listFast0(n, l);
			int b12, b28;
			for (int v: l)
				if ((b12 = v << 20 >> 20) == v)
					cs[cx++] = (char)('\u1000' | b12 & 4095); // 12s
				else if ((b28 = v << 4 >> 4) == v)
				{
					cs[cx++] = (char)('\u2000' | b28 & 4095); // 12l
					cx = Chars.writeS2(cs, cx, (short)(b28 >> 12)); // 16sh
				}
				else
				{
					cs[cx++] = '\u0004';
					cx = Chars.writeS4(cs, cx, v);
				}
		}
		else if (o instanceof long[])
		{
			long[] l = (long[])o;
			int n = l.length;
			cs = Array2.ensureN(cs, cx + 3 + (n << 2) + n);
			listFast0(n, l);
			long b12, b28;
			int v4;
			for (long v: l)
				if ((b12 = v << 52 >> 52) == v)
					cs[cx++] = (char)('\u1000' | (int)b12 & 4095); // 12s
				else if ((b28 = v << 36 >> 36) == v)
				{
					cs[cx++] = (char)('\u2000' | (int)b28 & 4095); // 12l
					cx = Chars.writeS2(cs, cx, (short)(b28 >> 12)); // 16sh
				}
				else if ((v4 = (int)v) == v)
				{
					cs[cx++] = '\u0004';
					cx = Chars.writeS4(cs, cx, v4);
				}
				else
				{
					cs[cx++] = '\u0005';
					cx = Chars.writeS8(cs, cx, v);
				}
		}
		else
		{
			Object[] l = (Object[])o;
			int n = l.length;
			cs = Array2.ensureN(cs, cx + 3);
			listFast0(n, l);
			for (Object v: l)
				valueFast(v);
		}
	}

	private void listFast0(int n, Object o)
	{
		int ref = ref(o, 1);
		if (ref < 512 && n < 32)
			cs[cx++] = (char)('\uC000' | ref << 5 | n); // 9u + 5u
		else if (n < 1024)
		{
			cs[cx++] = (char)('\u0700' | ref >> 6); // 8uh
			cs[cx++] = (char)(ref << 10 | n); // 6l + 10u
		}
		else
		{
			cs[cx++] = (char)('\u3000' | ref >> 2); // 12uh
			cs[cx++] = (char)(ref << 14 | n >> 16); // 2l + 14uh
			cs[cx++] = (char)n; // 16l
		}
	}

	@SuppressWarnings("unchecked")
	private void objectFast(Object o) throws Exception
	{
		String nm = codec.name(o, o.getClass(), ruleKey);
		int ref = ref(o, 1), n = nm.length();
		cs = Array2.ensureN(cs, cx + 2 + n);
		if (ref < 512 && n < 32)
			cs[cx++] = (char)('\u8000' | ref << 5 | n); // 9u + 5u
		else
		{
			cs[cx++] = (char)('\u0600' | ref >> 6); // 8uh
			cs[cx++] = (char)(ref << 10 | n); // 6l + 10u
		}
		nm.getChars(0, n, cs, cx);
		cx += n;
		codec.clazz(o.getClass()).encode(this, o, ruleKey);
		if (o instanceof Map)
			for (Map.Entry<String, Object> pv: ((Map<String, Object>)o).entrySet())
				if (pv.getKey() != null)
					value(pv.getKey(), pv.getValue());
				else
					throw new RuntimeException("property name is null");
		cs[cx++] = '\uFFFF';
	}
}
