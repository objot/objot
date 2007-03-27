//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public final class Setting
{
	public static Object go(Objot o, Class<?> clazz, Class<?> for_, byte[] s)
		throws Exception
	{
		return new Setting(o, for_, s).go(clazz);
	}

	private Objot objot;
	private Class<?> forClass;
	private byte[] bs;
	private int bx;
	private int by;
	private Object[] refs;
	private int intOrLongOrNot;

	private Setting(Objot o, Class<?> for_, byte[] s)
	{
		objot = o;
		forClass = for_;
		bs = s;
	}

	private Object go(Class<?> clazz) throws Exception
	{
		bx = 0;
		by = - 1;
		bxy();
		refs = new Object[28];
		Object o;
		if (bs[0] == '[')
		{
			bxy();
			o = clazz.isArray() ? list(null, clazz.getComponentType()) //
				: list(Object.class, null);
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

	private String utf() throws Exception
	{
		return bx == by ? "" : new String(bs, bx, by - bx, "UTF-8");
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
					throw new NumberFormatException("long integer out of range " + utf());
			else if (L == 0)
				return intOrLongOrNot = 0;
			else
				throw new NumberFormatException("illegal integer ".concat(utf()));
		if (bs[bx] != '-')
			if ((v = - v) < 0)
				throw new NumberFormatException("long integer out of range ".concat(utf()));
		intOrLongOrNot = (v >> 31) == 0 || (v >> 31) == - 1 ? 1 : - 1;
		if (L > 0 && intOrLongOrNot < 0)
			throw new NumberFormatException("integer out of range ".concat(utf()));
		return v;
	}

	private double number() throws Exception
	{
		if (bx >= by)
			throw new NumberFormatException("illegal number");
		return Double.parseDouble(utf());
	}

	Object[] lo_ = null;

	private Object list(Class<?> listClass, Class<?> arrayClass) throws Exception
	{
		final int len = (int)Int(1);
		bxy();
		boolean[] lb = null;
		int[] li = null;
		long[] ll = null;
		Object[] lo = null;
		Object l = null;
		if (listClass != null)
			l = Arrays.asList(lo = new Object[len]);
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
				if (c == 0 || c == '[' || c == '{' || c == '+' || c == '.' || c == '<'
					|| c == '>')
					throw new RuntimeException("integer expected for int[] but " + c + " at "
						+ bx);
				else
					li[i++] = (int)Int(1);
			return l;
		}
		else if (arrayClass == long.class)
		{
			for (char c; (c = chr()) != ']'; bxy())
				if (c == 0 || c == '[' || c == '{' || c == '+' || c == '.' || c == '<'
					|| c == '>')
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
			if (c == 0 || c == '[' || c == '{' || c == '+')
				bxy();
			if (c == 0)
				set(lo, i++, utf(), cla);
			else if (c == '[')
				set(lo, i++, list(Object.class, null), cla);
			else if (c == '{')
				set(lo, i++, object(Object.class), cla);
			else if (c == '+')
				set(lo, i++, ref(), cla);
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
		String cName = utf();
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
			String n = utf();
			Field f = null;
			Type t = null;
			Object v;
			if (cla != HashMap.class)
			{
				Property g = objot.sets(cla).get(n);
				if (g == null)
					throw new RuntimeException(cla.getCanonicalName() + "." + n
						+ " not found or not setable");
				if (! g.allow(forClass))
					throw new RuntimeException("setting " + cla.getCanonicalName() + "." + n
						+ " forbidden for " + forClass.getCanonicalName());
				f = g.f;
				t = f.getGenericType();
			}
			bxy();
			c = chr();
			if (c == 0 || c == '[' || c == '{' || c == '+')
				bxy();

			if (c == 0)
				v = utf();
			else if (c == '[')
				if (f != null && f.getType().isArray())
					v = list(null, f.getType().getComponentType());
				else if (f != null && List.class.isAssignableFrom(f.getType())
					&& t instanceof ParameterizedType)
				{
					t = ((ParameterizedType)t).getActualTypeArguments()[0];
					v = list(t instanceof Class ? (Class)t : Object.class, null);
				}
				else
					v = list(Object.class, null);
			else if (c == '{')
				v = object(f == null ? Object.class : f.getType());
			else if (c == '+')
				v = ref();
			else if (c == '.')
				v = null;
			else if (c == '<')
				v = false;
			else if (c == '>')
				v = true;
			else if (t == int.class)
			{
				f.setInt(o, (int)Int(1));
				continue;
			}
			else if (t == long.class)
			{
				f.setLong(o, Int(- 1));
				continue;
			}
			else if (t == double.class)
			{
				f.setDouble(o, number());
				continue;
			}
			else if (t == float.class)
			{
				f.setFloat(o, (float)number());
				continue;
			}
			else if (t == Long.class)
				v = Int(- 1);
			else if (t == Double.class)
				v = number();
			else if (t == Float.class)
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
				if (f == null)
					((HashMap)o).put(n, v);
				else
					f.set(o, v);
			}
			catch (IllegalArgumentException e)
			{
				throw new RuntimeException(cla.getCanonicalName() + "." + n + " : " //
					+ (v != null ? v.getClass().getCanonicalName() : "null") //
					+ " forbidden for " + t);
			}
		}
		return o;

	}
}
