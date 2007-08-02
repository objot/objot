//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Clob;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


final class Setting
{
	private Objot objot;
	private Class<?> forClass;
	private char[] bs;
	private int bx;
	private int by;
	private Object[] refs;
	private int intOrLongOrNot;

	Setting(Objot o, Class<?> for_, char[] s)
	{
		objot = o;
		forClass = for_;
		bs = s;
	}

	Object go(Class<?> clazz) throws Exception
	{
		bx = 0;
		by = - 1;
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
			if (! clazz.isAssignableFrom(o.getClass()))
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
		while (by < bs.length && bs[by] != Objot.S)
			by++;
		return bx;
	}

	private Object ref() throws Exception
	{
		int i = (int)Int(1);
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

	/** @param L >0 for int only, < 0 for int or long, 0 for int or long or not */
	private long Int(int L) throws Exception
	{
		if (bx >= by)
			throw new NumberFormatException("illegal number");
		long v = 0, vv;
		for (int x = bs[bx] == '-' || bs[bx] == '+' ? bx + 1 : bx; x < by; x++)
			if (bs[x] >= '0' && bs[x] <= '9')
				if ((vv = v * 10 - (bs[x] - '0')) <= v) // negative
					v = vv;
				else if (L == 0)
					return intOrLongOrNot = 0;
				else
					throw new NumberFormatException("long integer out of range " + str());
			else if (L == 0)
				return intOrLongOrNot = 0;
			else
				throw new NumberFormatException("illegal integer ".concat(str()));
		if (bs[bx] != '-')
			if ((v = - v) < 0)
				throw new NumberFormatException("long integer out of range ".concat(str()));
		intOrLongOrNot = (v >> 31) == 0 || (v >> 31) == - 1 ? 1 : - 1;
		if (L > 0 && intOrLongOrNot < 0)
			throw new NumberFormatException("integer out of range ".concat(str()));
		return v;
	}

	private double number() throws Exception
	{
		if (bx >= by)
			throw new NumberFormatException("illegal number");
		return Double.parseDouble(str());
	}

	Object[] lo_ = null;

	private Object list(Class<?> listClass, Class<?> uniqueClass, Class<?> arrayClass)
		throws Exception
	{
		final int len = (int)Int(1);
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

				@SuppressWarnings("unchecked")
				@Override
				public Object[] toArray(Object[] a)
				{
					return lo_ = a;
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
			lo = (Object[])Array.newInstance(uniqueClass, len);
		else if (arrayClass == boolean.class)
			l = lb = new boolean[len];
		else if (arrayClass == int.class)
			l = li = new int[len];
		else if (arrayClass == long.class)
			l = ll = new long[len];
		else
			l = lo = (Object[])Array.newInstance(arrayClass, len);
		int ref = - 1;
		if (chr() == '=')
		{
			bxy();
			ref = (int)Int(1);
			refs = Objot.ensureN(refs, ref + 1);
			refs[ref] = l;
			bxy();
		}
		Class<?> cla;
		int i = 0;
		if (listClass != null)
		{
			cla = listClass;
		}
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
					li[i++] = (int)Int(1);
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
					ll[i++] = Int(- 1);
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
				set(lo, i++, new Date(Int(- 1)), cla);
			else if (c == '.')
				lo[i++] = null;
			else if (c == '<')
				set(lo, i++, false, cla);
			else if (c == '>')
				set(lo, i++, true, cla);
			else if (cla == Long.class)
				lo[i++] = Int(- 1);
			else if (cla == Double.class)
				lo[i++] = number();
			else if (cla == Float.class)
				lo[i++] = (float)number();
			else
			{
				long _ = Int(0);
				if (intOrLongOrNot > 0)
					set(lo, i++, (int)_, cla);
				else if (intOrLongOrNot < 0)
					set(lo, i++, _, cla);
				else
					set(lo, i++, number(), cla);
			}
		}
		if (uniqueClass != null)
		{
			Set<Object> ls = objot.newUnique(len);
			for (Object o: lo)
				ls.add(o);
			return ls;
		}
		return l;
	}

	private void set(Object[] l, int i, Object o, Class<?> cla)
	{
		if (! cla.isAssignableFrom(o.getClass()))
			throw new RuntimeException(o.getClass().getCanonicalName() + " forbidden for "
				+ cla.getCanonicalName());
		l[i] = o;
	}

	@SuppressWarnings("unchecked")
	Object object(Class<?> cla0) throws Exception
	{
		String cName = str();
		Class<?> cla = cName.length() > 0 ? objot.classByName(cName) : HashMap.class;
		bxy();
		if (! cla0.isAssignableFrom(cla))
			throw new RuntimeException(cla.getCanonicalName() + " forbidden for "
				+ cla0.getCanonicalName());
		int ref = - 1;
		if (chr() == '=')
		{
			bxy();
			ref = (int)Int(1);
			refs = Objot.ensureN(refs, ref + 1);
			bxy();
		}
		Object o = cla.newInstance();
		if (ref >= 0)
			refs[ref] = o;
		for (char c; chr() != '}'; bxy())
		{
			String n = str();
			Property p = null;
			Object v;
			if (cla != HashMap.class)
			{
				p = objot.sets(cla).get(n);
				if (p == null)
					throw new RuntimeException(cla.getCanonicalName() + "." + n
						+ " not found or not setable");
				if (! p.allow(forClass))
					throw new RuntimeException("setting " + cla.getCanonicalName() + "." + n
						+ " forbidden for " + forClass.getCanonicalName());
			}
			bxy();
			c = chr();
			if (c == 0 || c == '[' || c == '{' || c == '+' || c == '*')
				bxy();

			if (c == 0)
				v = p != null && Clob.class.isAssignableFrom(p.cla) ? clob() : str();
			else if (c == '[')
				if (p != null && p.cla.isArray())
					v = list(null, null, p.cla.getComponentType());
				else if (p != null && List.class.isAssignableFrom(p.cla)
					&& p.type instanceof ParameterizedType)
				{
					Type t = ((ParameterizedType)p.type).getActualTypeArguments()[0];
					v = list(t instanceof Class ? (Class)t : Object.class, null, null);
				}
				else if (p != null && Set.class.isAssignableFrom(p.cla)
					&& p.type instanceof ParameterizedType)
				{
					Type t = ((ParameterizedType)p.type).getActualTypeArguments()[0];
					v = list(null, t instanceof Class ? (Class)t : Object.class, null);
				}
				else
					v = list(Object.class, null, null);
			else if (c == '{')
				v = object(p == null ? Object.class : p.cla);
			else if (c == '+')
				v = ref();
			else if (c == '*')
				v = new Date(Int(- 1));
			else if (c == '.')
				v = null;
			else if (c == '<')
				v = false;
			else if (c == '>')
				v = true;
			else if (p.cla == int.class)
			{
				p.set(o, (int)Int(1));
				continue;
			}
			else if (p.cla == long.class)
			{
				p.set(o, Int(- 1));
				continue;
			}
			else if (p.cla == double.class)
			{
				p.set(o, number());
				continue;
			}
			else if (p.cla == float.class)
			{
				p.set(o, (float)number());
				continue;
			}
			else if (p.cla == Long.class)
				v = Int(- 1);
			else if (p.cla == Double.class)
				v = number();
			else if (p.cla == Float.class)
				v = (float)number();
			else
			{
				long _ = Int(0);
				if (intOrLongOrNot > 0)
					v = (int)_;
				else if (intOrLongOrNot < 0)
					v = _;
				else
					v = number();
			}

			try
			{
				if (p == null)
					((HashMap)o).put(n, v);
				else
					p.set(o, v);
			}
			catch (InvocationTargetException e)
			{
				throw e;
			}
			catch (Exception e)
			{
				throw new ClassCastException(cla.getCanonicalName() + "." + n + " : " //
					+ (v != null ? v.getClass().getCanonicalName() : "null") //
					+ " forbidden for " + p.cla);
			}
		}
		return o;
	}
}
