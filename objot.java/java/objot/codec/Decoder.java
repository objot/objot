//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.codec;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.sql.Clob;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import objot.util.Array2;


final class Decoder
{
	private Codec codec;
	private Class<?> forClass;
	private char[] bs;
	private int bx;
	private int by;
	private Object[] refs;
	private long numl;
	private double numd;

	Decoder(Codec o, Class<?> for_, char[] s)
	{
		codec = o;
		forClass = for_;
		bs = s;
	}

	Object go(Class<?> clazz) throws Exception
	{
		bx = 0;
		by = -1;
		bxy();
		refs = new Object[28];
		Object o;
		if (bs[0] == '[')
		{
			bxy();
			if (clazz.isArray())
				o = list(null, null, clazz.getComponentType());
			else if (Set.class.isAssignableFrom(clazz))
				o = list(null, Object.class, null);
			else
				o = list(Object.class, null, null);
			if ( !clazz.isAssignableFrom(o.getClass()))
				throw new RuntimeException(o.getClass().getCanonicalName()
					+ " forbidden for " + clazz.getCanonicalName());
		}
		else if (bs[0] == '{')
		{
			bxy();
			o = object(clazz);
		}
		else
			throw new RuntimeException("array or object expected but " + chr() + " at 0");
		if (by < bs.length)
			throw new RuntimeException("termination expected but " + (char)(bs[by] & 0xFF)
				+ " at " + by);
		return o;
	}

	private int bxy()
	{
		bx = ++by;
		if (bx >= bs.length)
			throw new RuntimeException("termination unexpected");
		while (by < bs.length && bs[by] != Codec.S)
			by++;
		return bx;
	}

	private Object ref() throws Exception
	{
		int i = numi(num());
		if (i < 0 || i >= refs.length || refs[i] == null)
			throw new RuntimeException("reference " + i + " not found");
		return refs[i];
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
		for (int x = bs[bx] == '-' || bs[bx] == '+' ? bx + 1 : bx; x < by; x++)
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
		throw new NumberFormatException("invalid int ".concat //
			(type > 0 ? String.valueOf(numl) : String.valueOf(numd)));
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

	Object[] lo_ = null;

	private Object list(Class<?> listClass, Class<?> uniqueClass, Class<?> arrayClass)
		throws Exception
	{
		final int len = numi(num());
		bxy();
		boolean[] lb = null;
		int[] li = null;
		long[] ll = null;
		Object[] lo = null;
		Object l = null; // be boolean[] int[] long[] or Object[] except java.util.Set
		if (listClass != null)
		{
			// ArrayList's field "array" should be "protected"
			l = new ArrayList<Object>(new AbstractCollection<Object>()
			{
				@Override
				public int size()
				{
					return len;
				}

				/** for Java 5 */
				@SuppressWarnings("unchecked")
				@Override
				public Object[] toArray(Object[] a)
				{
					return lo_ = a;
				}

				/** for Java 6 */
				@Override
				public Object[] toArray()
				{
					return lo_ = new Object[len];
				}

				@Override
				public Iterator<Object> iterator()
				{
					return null;
				}
			});
			lo = lo_;
			lo_ = null;
		}
		else if (uniqueClass != null)
			lo = Array2.news(uniqueClass, len);
		else if (arrayClass == boolean.class)
			l = lb = new boolean[len];
		else if (arrayClass == int.class)
			l = li = new int[len];
		else if (arrayClass == long.class)
			l = ll = new long[len];
		else
			l = lo = Array2.news(arrayClass, len);
		int ref = -1;
		if (chr() == '=')
		{
			bxy();
			ref = numi(num());
			refs = Array2.ensureN(refs, ref + 1);
			refs[ref] = l;
			bxy();
		}
		Class<?> cla;
		int i = 0;
		if (listClass != null)
			cla = listClass;
		else if (arrayClass == boolean.class)
		{
			for (char c; (c = chr()) != ']'; bxy())
				if (c != '<' && c != '>')
					throw new RuntimeException("bool expected for boolean[] but " + c
						+ " at " + bx);
				else
					lb[i++] = c == '>';
			return l;
		}
		else if (arrayClass == int.class)
		{
			for (char c; (c = chr()) != ']'; bxy())
				if (c == 0 || c == '[' || c == '{' || c == '+' || c == '.' || c == '*'
					|| c == '<' || c == '>')
					throw new RuntimeException("integer expected for int[] but " + c + " at "
						+ bx);
				else
					li[i++] = numi(num());
			return l;
		}
		else if (arrayClass == long.class)
		{
			for (char c; (c = chr()) != ']'; bxy())
				if (c == 0 || c == '[' || c == '{' || c == '+' || c == '.' || c == '*'
					|| c == '<' || c == '>')
					throw new RuntimeException("long integer expected for int[] but " + c
						+ " at " + bx);
				else
					ll[i++] = numl(num());
			return l;
		}
		else
			cla = Object.class;
		for (char c; (c = chr()) != ']'; bxy())
		{
			if (c == 0 || c == '[' || c == '{' || c == '+' || c == '*')
				bxy();
			if (c == 0)
				set(lo, i++, Clob.class.isAssignableFrom(cla) ? clob() : str(), cla);
			else if (c == '[')
				set(lo, i++, list(Object.class, null, null), cla);
			else if (c == '{')
				set(lo, i++, object(Object.class), cla);
			else if (c == '+')
				set(lo, i++, ref(), cla);
			else if (c == '*')
				set(lo, i++, new Date(numl(num())), cla);
			else if (c == '.')
				lo[i++] = null;
			else if (c == '<')
				set(lo, i++, false, cla);
			else if (c == '>')
				set(lo, i++, true, cla);
			else
				set(lo, i++, Num(num(), cla), cla);
		}
		if (uniqueClass != null)
		{
			Set<Object> ls = codec.newUniques(len);
			for (Object o: lo)
				ls.add(o);
			return ls;
		}
		return l;
	}

	private void set(Object[] l, int i, Object o, Class<?> cla)
	{
		if ( !cla.isAssignableFrom(o.getClass()))
			throw new RuntimeException(o.getClass().getCanonicalName() + " forbidden for "
				+ cla.getCanonicalName());
		l[i] = o;
	}

	@SuppressWarnings("unchecked")
	Object object(Class<?> cla0) throws Exception
	{
		String cName = str();
		Class<?> cla = cName.length() > 0 ? codec.classByName(cName) : HashMap.class;
		bxy();
		if ( !cla0.isAssignableFrom(cla))
			throw new RuntimeException(cla.getCanonicalName() + " forbidden for "
				+ cla0.getCanonicalName());
		int ref = -1;
		if (chr() == '=')
		{
			bxy();
			ref = numi(num());
			refs = Array2.ensureN(refs, ref + 1);
			bxy();
		}
		Clazz z = cla == HashMap.class ? null : codec.clazz(cla);
		HashMap<String, Object> m = cla == HashMap.class ? new HashMap<String, Object>()
			: null;
		Object o = cla == HashMap.class ? m : z.object();
		if (ref >= 0)
			refs[ref] = o;
		for (char c; chr() != '}'; bxy())
		{
			String n = str();
			bxy();
			c = chr();
			if (c == 0 || c == '[' || c == '{' || c == '+' || c == '*')
				bxy();
			if (cla == HashMap.class)
			{
				if (c == 0)
					m.put(n, str());
				else if (c == '[')
					m.put(n, list(Object.class, null, null));
				else if (c == '{')
					m.put(n, object(Object.class));
				else if (c == '+')
					m.put(n, ref());
				else if (c == '*')
					m.put(n, new Date(numl(num())));
				else if (c == '.')
					m.put(n, null);
				else if (c == '<')
					m.put(n, false);
				else if (c == '>')
					m.put(n, true);
				else
					m.put(n, Num(num(), null));
				continue;
			}

			Property p = z.decs.get(n);
			if (p == null)
				throw new RuntimeException(cla.getCanonicalName() + "." + n
					+ " not found or not decodable");
			if ( !p.allow(forClass))
				throw new RuntimeException("decoding " + cla.getCanonicalName() + "." + n
					+ " forbidden for " + forClass.getCanonicalName());
			Object v = Class.class;
			try
			{
				if (c == 0)
					z.decode(o, p.index, v = p.clob ? clob() : str());
				else if (c == '[')
					z.decode(o, p.index, v = list(p.list, p.unique, p.array));
				else if (c == '{')
					z.decode(o, p.index, v = object(p.cla));
				else if (c == '+')
					z.decode(o, p.index, v = ref());
				else if (c == '*')
					z.decode(o, p.index, v = new Date(numl(num())));
				else if (c == '.')
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
				if (v == Class.class)
					v = Num(num(), null);
				throw new ClassCastException(cla.getCanonicalName() + "." + n + " : " //
					+ (v != null ? v.getClass().getCanonicalName() : "null") //
					+ " forbidden for " + p.cla);
			}
		}
		return o;
	}
}
