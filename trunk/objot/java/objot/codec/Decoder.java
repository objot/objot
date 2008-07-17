//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Library General Public License version 2
//
package objot.codec;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import objot.util.Array2;
import objot.util.Class2;
import objot.util.Math2;


final class Decoder
{
	private Codec codec;
	private Class<?> ruleKey;
	private char[] bs;
	private int bBegin;
	private int bEnd1;
	private int bx;
	private int by;
	private Object[] refs;
	private long numl;
	private double numd;
	private boolean arrayForList;

	/** @param ruleKey_ null is Object.class */
	Decoder(Codec o, Class<?> ruleKey_, char[] s, int sBegin, int sEnd1)
	{
		codec = o;
		ruleKey = ruleKey_ != null ? ruleKey_ : Object.class;
		bs = s;
		Math2.range(sBegin, sEnd1, s.length);
		bBegin = sBegin;
		bEnd1 = sEnd1;
	}

	/** @param cla null is Object.class */
	Object go(Class<?> cla) throws Exception
	{
		arrayForList = codec.arrayForList();
		refs = Array2.OBJECTS0;
		by = bBegin - 1;
		bxy();
		Object o = value(chr(), cla != null ? cla : Object.class);
		if (by < bEnd1)
			throw new RuntimeException("termination expected but " + (char)(bs[by] & 0xFF)
				+ " at " + by);
		return o;
	}

	private int bxy()
	{
		bx = ++by;
		if (bx >= bEnd1)
			throw new RuntimeException("termination unexpected");
		while (by < bEnd1 && bs[by] != Codec.S)
			by++;
		return bx;
	}

	private char chr()
	{
		return bx == by ? 0 : bx == by - 1 ? (char)(bs[bx] & 0xFF) : 65535;
	}

	private String str()
	{
		return bx == by ? "" : new String(bs, bx, by - bx);
	}

	/** @return immutable */
	private Clob clob()
	{
		final String s = bx == by ? "" : new String(bs, bx, by - bx);
		return new Clob()
		{
			public InputStream getAsciiStream()
			{
				throw new UnsupportedOperationException();
			}

			public Reader getCharacterStream()
			{
				return new StringReader(s);
			}

			@SuppressWarnings("unused")
			public Reader getCharacterStream(long pos, long length)
			{
				throw new UnsupportedOperationException();
			}

			public String getSubString(long pos, int length)
			{
				int x = (int)Math.min(pos - 1, Integer.MAX_VALUE);
				return s.substring(x, x + length);
			}

			public long length()
			{
				return s.length();
			}

			public long position(String search, long start)
			{
				return s.indexOf(search, (int)Math.min(start - 1, Integer.MAX_VALUE));
			}

			public long position(Clob search, long start)
			{
				throw new UnsupportedOperationException();
			}

			public OutputStream setAsciiStream(long pos)
			{
				throw new UnsupportedOperationException();
			}

			public Writer setCharacterStream(long pos)
			{
				throw new UnsupportedOperationException();
			}

			public int setString(long pos, String str)
			{
				throw new UnsupportedOperationException();
			}

			public int setString(long pos, String str, int offset, int len)
			{
				throw new UnsupportedOperationException();
			}

			public void truncate(long len)
			{
				throw new UnsupportedOperationException();
			}

			@SuppressWarnings("unused")
			public void free()
			{
			}
		};
	}

	/** @return 0 int >0 long <0 double */
	private int num()
	{
		if (bx >= by)
			throw new NumberFormatException("illegal number");
		long v = 0, vv; // negative
		for (int x = bs[bx] == '-' || bs[bx] == '=' ? bx + 1 : bx; x < by; x++)
			if (bs[x] >= '0' && bs[x] <= '9' && (vv = v * 10 - (bs[x] - '0')) <= v)
				v = vv;
			else
				return -1 | (int)(numd = Double.parseDouble(str()));
		if (bs[bx] != '-' && (v = -v) < 0)
			return -1 | (int)(numd = Double.parseDouble(str()));
		numl = v;
		return (v << 32 >> 32) == v ? 0 : 1;
	}

	private int numi(int type)
	{
		if (type == 0)
			return (int)numl;
		throw new NumberFormatException("invalid int ".concat(type > 0 ? String.valueOf(numl)
			: String.valueOf(numd)));
	}

	private long numl(int type)
	{
		if (type >= 0)
			return numl;
		throw new NumberFormatException("invalid long " + numd);
	}

	private double numd(int type)
	{
		return type >= 0 ? numl : numd;
	}

	private Number Num(int type, Class<?> c) throws Exception
	{
		if (c == Integer.class)
			return numi(type);
		else if (c == Long.class)
			return numl(type);
		else if (c == Double.class)
			return numd(type);
		else if (c == Float.class)
			return (float)numd(type);
		return type == 0 ? Integer.valueOf((int)numl) : type > 0 ? Long.valueOf(numl)
			: (Number)Double.valueOf(numd);
	}

	private Object value(char c, Class<?> cla) throws Exception
	{
		if (c == 0 || c == '[' || c == '{' || c == '=' || c == '*' || c == '/')
			bxy();
		if (c == 0)
			return Class2.cast(Clob.class.isAssignableFrom(cla) ? clob() : str(), cla);
		else if (c == '[')
			return list(cla, Object.class);
		else if (c == '{')
			return object(cla);
		else if (c == '=')
			return Class2.cast(ref(), cla);
		else if (c == '*')
			return Class2.cast(new Date(numl(num())), cla);
		else if (c == ',')
			return null;
		else if (c == '<')
			return Class2.cast(false, cla);
		else if (c == '>')
			return Class2.cast(true, cla);
		return Class2.cast(Num(num(), Class2.boxTry(cla, true)), cla);
	}

	private Object ref() throws Exception
	{
		int i = numi(num());
		if (i < 0 || i >= refs.length || refs[i] == null)
			throw new RuntimeException("reference " + i + " not found");
		return refs[i];
	}

	@SuppressWarnings("unchecked")
	private Object list(Class<?> cla, Class<?> elem) throws Exception
	{
		final int len = numi(num());
		bxy();
		boolean[] lb = null;
		int[] li = null;
		long[] ll = null;
		Object[] lo = null;
		Object l = null;
		Collection<Object> ls = null;

		if (cla == Object.class)
			cla = arrayForList ? Object[].class : ArrayList.class;
		if (cla.isArray())
		{
			elem = cla.getComponentType();
			if (elem == boolean.class)
				l = lb = new boolean[len];
			else if (elem == int.class)
				l = li = new int[len];
			else if (elem == long.class)
				l = ll = new long[len];
			else
				l = lo = Array2.news(elem, len);
		}
		else
		{
			l = ls = codec.newList(cla, len);
			if ( !cla.isInstance(l))
				throw new ClassCastException(l.getClass() + " forbidden for " + cla);
		}

		int ref = -1;
		if (chr() == ':')
		{
			bxy();
			ref = numi(num());
			refs = Array2.ensureN(refs, ref + 1);
			refs[ref] = l;
			bxy();
		}
		int i = 0;
		if (lb != null)
			for (char c; (c = chr()) != ']'; bxy())
				if (c == '<' || c == '>')
					lb[i++] = c == '>';
				else
					throw new RuntimeException("bool expected for boolean[] but " + c
						+ " at " + bx);
		else if (li != null)
			for (; chr() != ']'; bxy())
				li[i++] = numi(num());
		else if (ll != null)
			for (; chr() != ']'; bxy())
				ll[i++] = numl(num());
		else if (lo != null)
			for (char c; (c = chr()) != ']'; bxy())
				lo[i++] = value(c, elem);
		else
			for (char c; (c = chr()) != ']'; bxy())
				ls.add(value(c, elem));
		return l;
	}

	@SuppressWarnings("unchecked")
	Object object(Class<?> cla0) throws Exception
	{
		String name = str();
		bxy();
		Object o = codec.byName(name);
		Class<?> cla = o instanceof Class ? (Class<?>)o : o.getClass();
		Clazz z = codec.clazz(cla);
		o = cla == o ? z.object() : o;
		if ( !cla0.isAssignableFrom(cla))
			throw new RuntimeException(cla.getCanonicalName() + " forbidden for "
				+ cla0.getCanonicalName());
		Map<String, Object> m = o instanceof Map ? (Map)o : null;

		if (chr() == ':')
		{
			bxy();
			int ref = numi(num());
			refs = Array2.ensureN(refs, ref + 1);
			bxy();
			refs[ref] = o;
		}
		for (char c; chr() != '}'; bxy())
		{
			String n = str();
			bxy();
			c = chr();
			if (c == 0 || c == '[' || c == '{' || c == '=' || c == '*' || c == '/')
				bxy();

			Property p = z.decs.get(n);
			if (p != null)
			{
				if ( !p.allow(ruleKey))
					throw new RuntimeException("decoding " + o.getClass().getCanonicalName()
						+ "." + n + " forbidden for " + ruleKey.getCanonicalName());
				Object v = this;
				try
				{
					if (c == 0)
						z.decode(o, p.index, v = p.clob ? clob() : str());
					else if (c == '[')
						z.decode(o, p.index, v = list(p.cla, p.listElem));
					else if (c == '{')
						z.decode(o, p.index, v = object(p.cla));
					else if (c == '=')
						z.decode(o, p.index, v = ref());
					else if (c == '*')
						z.decode(o, p.index, v = new Date(numl(num())));
					else if (c == ',')
						z.decode(o, p.index, v = null);
					else if (c == '<')
						z.decode(o, p.index, v = false);
					else if (c == '>')
						z.decode(o, p.index, v = true);
					else if (p.cla == int.class)
						z.decode(o, p.index, numi(num()));
					else if (p.cla == long.class)
						z.decode(o, p.index, numl(num()));
					else if (p.cla == double.class || p.cla == float.class)
						z.decode(o, p.index, numd(num()));
					else
						z.decode(o, p.index, v = Num(num(), p.cla));
				}
				catch (ClassCastException e)
				{
					if (v == this)
						v = Num(num(), null);
					throw new RuntimeException(o.getClass().getCanonicalName() + "." + n
						+ " : " + (v != null ? v.getClass().getCanonicalName() : "null")
						+ " forbidden for " + p.cla);
				}
			}
			else if (m != null)
				if (c == 0)
					m.put(n, str());
				else if (c == '[')
					m.put(n, list(Object.class, Object.class));
				else if (c == '{')
					m.put(n, object(Object.class));
				else if (c == '=')
					m.put(n, ref());
				else if (c == '*')
					m.put(n, new Date(numl(num())));
				else if (c == ',')
					m.put(n, null);
				else if (c == '<')
					m.put(n, false);
				else if (c == '>')
					m.put(n, true);
				else
					m.put(n, Num(num(), null));
			// not found
			else if (p == null)
				throw new RuntimeException(o.getClass().getCanonicalName() + "." + n
					+ " not found or not decodable");
		}
		return o;
	}
}
