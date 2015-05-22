//
// Copyright 2007-2015 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.codec;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.sql.Clob;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import objot.util.Array2;
import objot.util.Class2;
import objot.util.Math2;


final class Decoder
{
	private final Codec codec;
	private final Object ruleKey;
	private final char[] bs;
	private int begin;
	private int end1;
	private int bx;
	private int by;
	private Object[] refs;
	private long numl;
	private double numd;

	/** @param ruleKey_ null is Object.class */
	Decoder(Codec o, Object ruleKey_, char[] s, int sBegin, int sEnd1)
	{
		codec = o;
		ruleKey = ruleKey_ != null ? ruleKey_ : Object.class;
		bs = s;
		Math2.range(sBegin, sEnd1, s.length);
		begin = sBegin;
		end1 = sEnd1;
	}

	/** @param cla null is Object.class */
	@SuppressWarnings("unchecked")
	<T>T go(Class<T> cla) throws Exception
	{
		refs = Array2.OBJECTS0;
		by = begin - 1;
		bxy();
		Object o = value(chr(), cla != null ? cla : Object.class);
		if (by < end1)
			throw new RuntimeException("termination expected but " + (char)(bs[by] & 0xFF)
				+ " at " + by);
		return (T)o;
	}

	/** @param cla null is Object.class */
	@SuppressWarnings("unchecked")
	<T>T goFast(Class<T> cla) throws Exception
	{
		refs = Array2.OBJECTS0;
		Object o = valueFast(cla != null ? cla : Object.class, Object.class);
		if (begin < end1)
			throw new RuntimeException("termination expected but \\u"
				+ Integer.toHexString(bs[begin]) + " at " + begin);
		return (T)o;
	}

	private int bxy()
	{
		bx = ++by;
		if (bx >= end1)
			throw new RuntimeException("termination unexpected");
		while (by < end1 && bs[by] != Codec.S)
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
	private Clob clob(final String s)
	{
		return new Clob()
		{
			@Override
			public InputStream getAsciiStream()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public Reader getCharacterStream()
			{
				return new StringReader(s);
			}

			@Override
			public Reader getCharacterStream(long pos, long length)
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public String getSubString(long pos, int length)
			{
				int x = (int)Math.min(pos - 1, Integer.MAX_VALUE);
				return s.substring(x, x + length);
			}

			@Override
			public long length()
			{
				return s.length();
			}

			@Override
			public long position(String search, long start)
			{
				return s.indexOf(search, (int)Math.min(start - 1, Integer.MAX_VALUE));
			}

			@Override
			public long position(Clob search, long start)
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public OutputStream setAsciiStream(long pos)
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public Writer setCharacterStream(long pos)
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public int setString(long pos, String str)
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public int setString(long pos, String str, int offset, int len)
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public void truncate(long len)
			{
				throw new UnsupportedOperationException();
			}

			@Override
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
		if (codec.numCla != null && (c == Number.class || c == Object.class))
			c = codec.numCla;
		if (c == Integer.class)
			return numi(type);
		else if (c == Long.class)
			return numl(type);
		else if (c == Double.class)
			return numd(type);
		else if (c == Float.class)
			return (float)numd(type);
		return type == 0 ? (int)numl : type > 0 ? numl : (Number)numd;
	}

	private Object value(char c, Class<?> cla) throws Exception
	{
		if (c == 0 || c == '[' || c == '{' || c == '=' || c == '*' || c == '/')
			bxy();
		if (c == 0)
			return Class2.cast(Clob.class.isAssignableFrom(cla) ? clob(str()) : str(), cla);
		else if (c == '[')
			return list(cla, Object.class);
		else if (c == '{')
			return object(cla);
		else if (c == '=')
			return Class2.cast(ref(numi(num())), cla);
		else if (c == '*')
			return Class2.cast(new Date(numl(num())), cla);
		else if (c == ',')
			return null;
		else if (c == '<')
			return Class2.cast(false, cla);
		else if (c == '>')
			return Class2.cast(true, cla);
		return Class2.cast(Num(num(), Class2.boxTry(cla, false)), cla);
	}

	private Object ref(int i) throws Exception
	{
		if (i < 0 || i >= refs.length || refs[i] == null)
			throw new RuntimeException("reference " + i + " not found");
		return refs[i];
	}

	@SuppressWarnings("all")
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
			cla = codec.arrayForList ? Object[].class : Collection.class;
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
				throw new RuntimeException(l.getClass() + " forbidden for " + cla);
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
				if (c == '<')
					lb[i++] = false;
				else if (c == '>')
					lb[i++] = true;
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
		Object o = codec.byName(name, ruleKey);
		Class<?> cla = o instanceof Class ? (Class<?>)o : o.getClass();
		Clazz z = codec.clazz(cla);
		o = cla == o ? z.object(codec) : o;
		if ( !cla0.isAssignableFrom(cla))
			throw new RuntimeException(cla.getName() + " forbidden for " + cla0.getName());
		Map<String, Object> m = o instanceof Map ? (Map)o : null;

		if (chr() == ':')
		{
			bxy();
			int ref = numi(num());
			refs = Array2.ensureN(refs, ref + 1);
			bxy();
			refs[ref] = o;
		}
		for (char c; bx != by; bxy())
		{
			String n = str();
			bxy();
			c = chr();
			if (c == 0 || c == '[' || c == '{' || c == '=' || c == '*' || c == '/')
				bxy();

			Object v = null;
			Property p = z.decs.get(n);
			if (p != null)
			{
				if ( !p.decodable(o, ruleKey))
					throw new RuntimeException("decoding " + o.getClass().getName() + "." + n
						+ " forbidden for " + ruleKey);
				try
				{
					if (c == 0)
						z.decode(o, p.index, v = p.clob ? clob(str()) : str());
					else if (c == '[')
						z.decode(o, p.index, v = list(p.cla, p.listElem));
					else if (c == '{')
						z.decode(o, p.index, v = object(p.cla));
					else if (c == '=')
						z.decode(o, p.index, v = ref(numi(num())));
					else if (c == '*')
						z.decode(o, p.index, v = new Date(numl(num())));
					else if (c == ',')
						z.decode(o, p.index, v = null);
					else if (c == '<')
						z.decode(o, p.index, v = false);
					else if (c == '>')
						z.decode(o, p.index, v = true);
					else if ((v = this) != null && p.cla == int.class)
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
					throw new RuntimeException(o.getClass().getName() + "." + n + " : "
						+ (v != null ? v.getClass().getName() : "null") + " forbidden for "
						+ p.cla, e);
				}
			}
			else
			{
				if (m == null) // not found
					codec.undecodable(o, n, ruleKey);
				if (c == 0)
					v = str();
				else if (c == '[')
					v = list(Object.class, Object.class);
				else if (c == '{')
					v = object(Object.class);
				else if (c == '=')
					v = ref(numi(num()));
				else if (c == '*')
					v = new Date(numl(num()));
				else if (c == ',')
					v = null;
				else if (c == '<')
					v = false;
				else if (c == '>')
					v = true;
				else
					v = Num(num(), Object.class);
				if (m == null)
					codec.undecodeValue(o, n, ruleKey, v);
				else
					m.put(n, v);
			}
		}
		bxy();
		if (chr() != '}')
			throw new RuntimeException("termination unexpected");
		return o;
	}

	private char readU2()
	{
		if (begin >= end1)
			throw new RuntimeException("termination unexpected");
		return bs[begin++];
	}

	private int readS2()
	{
		if (begin >= end1)
			throw new RuntimeException("termination unexpected");
		return (short)bs[begin++];
	}

	private int readS4()
	{
		if (begin >= end1 - 1)
			throw new RuntimeException("termination unexpected");
		return bs[begin++] << 16 | bs[begin++];
	}

	private long readS8()
	{
		if (begin >= end1 - 3)
			throw new RuntimeException("termination unexpected");
		return (long)bs[begin++] << 48 | (long)bs[begin++] << 32 | (long)bs[begin++] << 16
			| bs[begin++];
	}

	private int n(int n)
	{
		if (begin >= end1 - n)
			throw new RuntimeException("termination unexpected");
		begin += n;
		return n;
	}

	/** @return 0 int >0 long <0 double */
	private int numFast()
	{
		char c = readU2();
		if (c >> 12 == 1) // \u1000 + 12s
			numl = (long)(short)(c << 4) >> 4;
		else if (c >> 12 == 2) // \u2000 + 12l 16sh
			numl = c & 4095 | readS2() << 12;
		else if (c == 4) // \u0004 32s
			numl = readS4();
		else if (c == 5) // \u0005 64s
		{
			numl = readS8();
			return 1;
		}
		else if (c == 6) // \u0006 64d
		{
			numd = Double.longBitsToDouble(readS8());
			return -1;
		}
		else
			throw new RuntimeException("not number tag \\u".concat(Integer.toHexString(c)));
		return 0;
	}

	private Object valueFast(Class<?> cla, Class<?> elem) throws Exception
	{
		char c = readU2();
		switch (c >> 14)
		{
		case 1: // \u4000 + 14u
			return Class2.cast(ref(c & 16383), cla);
		case 2: // \u8000 + 9u + 5u
			return objectFast(c >> 5 & 511, c & 31, cla);
		case 3: // \uC000 + 9u + 5u
			return listFast(c >> 5 & 511, c & 31, cla, elem);
		}
		switch (c >> 12)
		{
		case 1: // \u1000 + 12s
			numl = (long)(short)(c << 4) >> 4;
			return Class2.cast(Num(0, Class2.boxTry(cla, false)), cla);
		case 2: // \u2000 + 12l 16sh
			numl = c & 4095 | readS2() << 12;
			return Class2.cast(Num(0, Class2.boxTry(cla, false)), cla);
		case 3: // \u3000 + 12uh 2l + 14uh 16l
			int x = readS4();
			return listFast((c & 4095) << 2 | x >>> 30, x << 2 >>> 2, cla, elem);
		}
		switch (c >> 8)
		{
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
			throw new RuntimeException("invalid tag \\u".concat(Integer.toHexString(c)));
		case 6: // \u0600 + 8uh 6l + 10u
			char x = readU2();
			return objectFast((c & 255) << 6 | x >> 10, x & 1023, cla);
		case 7: // \u0700 + 8uh 6l + 10u
			x = readU2();
			return listFast((c & 255) << 6 | x >> 10, x & 1023, cla, elem);
		case 8:
		case 9:
		case 10:
		case 11: // \u0800 + 10l 32sh
			return Class2.cast(new Date(c & 1023 | (long)readS4() << 10), cla);
		case 12:
		case 13:
		case 14:
		case 15: // \u0C00 + 10u
			String s = new String(bs, begin, n(c & 1023));
			return Class2.cast(Clob.class.isAssignableFrom(cla) ? clob(s) : s, cla);
		}
		switch (c)
		{
		case 0: // \u0000
			return null;
		case 1: // \u0001
			return false;
		case 2: // \u0002
			return true;
		case 3: // \u0003 31u
			return new String(bs, begin + 2, n(readS4()));
		case 4: // \u0004 32s
			numl = readS4();
			return Class2.cast(Num(0, Class2.boxTry(cla, false)), cla);
		case 5: // \u0005 64s
			numl = readS8();
			return Class2.cast(Num(1, Class2.boxTry(cla, false)), cla);
		case 6: // \u0006 64d
			numd = Double.longBitsToDouble(readS8());
			return Class2.cast(Num( -1, Class2.boxTry(cla, false)), cla);
		}
		throw new RuntimeException("invalid tag \\u".concat(Integer.toHexString(c)));
	}

	@SuppressWarnings("all")
	private Object listFast(int ref, final int len, Class<?> cla, Class<?> elem)
		throws Exception
	{
		boolean[] lb = null;
		int[] li = null;
		long[] ll = null;
		Object[] lo = null;
		Object l = null;
		Collection<Object> ls = null;

		if (cla == Object.class)
			cla = codec.arrayForList ? Object[].class : Collection.class;
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
				throw new RuntimeException(l.getClass() + " forbidden for " + cla);
		}

		if (ref > 0)
			(refs = Array2.ensureN(refs, ref + 1))[ref] = l;
		char c;
		if (lb != null)
			for (int i = 0; i < len; i++)
				if ((c = readU2()) == '\u0001')
					lb[i] = false;
				else if (c == '\u0002')
					lb[i] = true;
				else
					throw new RuntimeException("bool expected for boolean[] but \\u"
						+ Integer.toHexString(c) + " at " + (begin - 1));
		else if (li != null)
			for (int i = 0; i < len; i++)
				li[i] = numi(numFast());
		else if (ll != null)
			for (int i = 0; i < len; i++)
				ll[i] = numl(numFast());
		else if (lo != null)
			for (int i = 0; i < len; i++)
				lo[i] = valueFast(elem, Object.class);
		else
			for (int i = 0; i < len; i++)
				ls.add(valueFast(elem, Object.class));
		return l;
	}

	@SuppressWarnings("unchecked")
	Object objectFast(int ref, final int len, Class<?> cla0) throws Exception
	{
		String name = new String(bs, begin, n(len));
		Object o = codec.byName(name, ruleKey);
		Class<?> cla = o instanceof Class ? (Class<?>)o : o.getClass();
		Clazz z = codec.clazz(cla);
		o = cla == o ? z.object(codec) : o;
		if ( !cla0.isAssignableFrom(cla))
			throw new RuntimeException(cla.getName() + " forbidden for " + cla0.getName());
		Map<String, Object> m = o instanceof Map ? (Map)o : null;

		if (ref > 0)
			(refs = Array2.ensureN(refs, ref + 1))[ref] = o;
		for (char c; (c = readU2()) != '\uFFFF';)
		{
			String n = new String(bs, begin, n(c & 8191));
			Object v = null;
			Property p = z.decs.get(n);
			if (p != null)
			{
				if ( !p.decodable(o, ruleKey))
					throw new RuntimeException("decoding " + o.getClass().getName() + "." + n
						+ " forbidden for " + ruleKey);
				try
				{
					switch (c >> 13)
					{
					case 0: // \u0000
						z.decode(o, p.index, v = null);
						continue;
					case 1: // \u2000
						z.decode(o, p.index, v = false);
						continue;
					case 2: // \u4000
						z.decode(o, p.index, v = true);
						continue;
					case 3: // \u6000 16s
						numl = readS2();
						if (p.cla == int.class || p.cla == long.class)
							z.decode(o, p.index, numl);
						else if (p.cla == double.class || p.cla == float.class)
							z.decode(o, p.index, (double)numl);
						else
							z.decode(o, p.index, v = Num(0, p.cla));
						continue;
					case 4: // \u8000 32s
						numl = readS4();
						if (p.cla == int.class || p.cla == long.class)
							z.decode(o, p.index, numl);
						else if (p.cla == double.class || p.cla == float.class)
							z.decode(o, p.index, (double)numl);
						else
							z.decode(o, p.index, v = Num(0, p.cla));
						continue;
					case 5: // \uA000 64s
						numl = readS8();
						if (p.cla == int.class)
							numi(1);
						else if (p.cla == long.class)
							z.decode(o, p.index, numl);
						else if (p.cla == double.class || p.cla == float.class)
							z.decode(o, p.index, (double)numl);
						else
							z.decode(o, p.index, v = Num(1, p.cla));
						continue;
					case 6: // \uC000 64s
						numd = Double.longBitsToDouble(readS8());
						if (p.cla == int.class)
							numi( -1);
						else if (p.cla == long.class)
							numl( -1);
						else if (p.cla == double.class || p.cla == float.class)
							z.decode(o, p.index, numd);
						else
							z.decode(o, p.index, v = Num( -1, p.cla));
						continue;
					case 7: // \uE000
						z.decode(o, p.index, v = valueFast(p.cla, p.listElem));
						continue;
					}
				}
				catch (ClassCastException e)
				{
					throw new RuntimeException(o.getClass().getName() + "." + n + " : "
						+ (v != null ? v.getClass().getName() : "null") + " forbidden for "
						+ p.cla, e);
				}
			}
			else
			{
				if (m == null) // not found
					codec.undecodable(o, n, ruleKey);
				switch (c >> 13)
				{
				case 0: // \u0000
					v = null;
					break;
				case 1: // \u2000
					v = false;
					break;
				case 2: // \u4000
					v = true;
					break;
				case 3: // \u6000 16s
					numl = readS2();
					v = Num(0, Object.class);
					break;
				case 4: // \u8000 32s
					numl = readS4();
					v = Num(0, Object.class);
					break;
				case 5: // \uA000 64s
					numl = readS8();
					v = Num(1, Object.class);
					break;
				case 6: // \uC000 64s
					numd = Double.longBitsToDouble(readS8());
					v = Num( -1, Object.class);
					break;
				case 7: // \uE000
					v = valueFast(Object.class, Object.class);
					break;
				}
				if (m == null)
					codec.undecodeValue(o, n, ruleKey, v);
				else
					m.put(n, v);
			}
		}
		return o;
	}
}
