//
// Objot 1
//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public final class Setting
{
	public static Object go(Objot o, Class<?> for_, byte[] s, Class<?> listClass)
		throws Exception
	{
		return new Setting(o, for_, s).go(listClass != null ? listClass : Object[].class);
	}

	private Objot objot;
	private Class<?> forClass;
	private byte[] bs;
	private int bx;
	private int by;
	private Object[] refs;

	private Setting(Objot o, Class<?> for_, byte[] s)
	{
		objot = o;
		forClass = for_;
		bs = s;
	}

	private Object go(Class<?> listClass) throws Exception
	{
		bx = 0;
		by = - 1;
		bxy();
		refs = new Object[28];
		Object o;
		if (bs[0] == '[')
		{
			bxy();
			o = listClass.isArray() ? list(null, listClass.getComponentType()) : list(
				Object.class, null);
			if (! listClass.isAssignableFrom(o.getClass()))
				throw new RuntimeException(o.getClass().getCanonicalName()
					+ " forbidden for " + listClass.getCanonicalName());
		}
		else if (bs[0] == '/')
		{
			bxy();
			o = object(Object.class);
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
		int i = integer();
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
		return new String(bs, bx, by - bx, "UTF-8");
	}

	private boolean isInt()
	{
		if (bx >= by)
			throw new NumberFormatException("illegal number");
		for (int x = bs[bx] == '-' || bs[bx] == '+' ? bx + 1 : bx; x < by; x++)
			if (bs[x] < '0' || bs[x] > '9')
				return false;
		return true;
	}

	private int integer() throws Exception
	{
		int v = 0;
		for (int x = bs[bx] == '-' || bs[bx] == '+' ? bx + 1 : bx; x < by; x++)
			if (bs[x] >= '0' && bs[x] <= '9')
				v = v * 10 + (bs[x] - '0');
			else
				throw new NumberFormatException("illegal integer ".concat(utf()));
		return bs[bx] == '-' ? - v : v;
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
		final int len = integer();
		bxy();
		boolean[] lb = null;
		int[] li = null;
		Object[] lo = null;
		Object l = null;
		if (listClass != null)
		{
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
		else if (arrayClass == boolean.class)
			l = lb = new boolean[len];
		else if (arrayClass == int.class)
			l = li = new int[len];
		else
			l = lo = (Object[])Array.newInstance(arrayClass, len);
		int ref = - 1;
		if (chr() == '=')
		{
			bxy();
			ref = integer();
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
			for (char c; (c = chr()) != ';'; bxy())
				if (c != '<' && c != '>')
					throw new RuntimeException("bool expected for boolean[] but " + c
						+ " at " + bx);
				else
					lb[i++] = c == '>';
			return l;
		}
		else if (arrayClass == int.class)
		{
			for (char c; (c = chr()) != ';'; bxy())
				if (c == 0 || c == '[' || c == '/' || c == '+' || c == '.' || c == '<'
					|| c == '>')
					throw new RuntimeException("integer expected for int[] but " + c + " at "
						+ bx);
				else
					li[i++] = integer();
			return l;
		}
		else
			cla = Object.class;
		for (char c; (c = chr()) != ';'; bxy())
		{
			if (c == 0 || c == '[' || c == '/' || c == '+')
				bxy();
			if (c == 0)
				set(lo, i++, utf(), cla);
			else if (c == '[')
				set(lo, i++, list(Object.class, null), cla);
			else if (c == '/')
				set(lo, i++, object(Object.class), cla);
			else if (c == '+')
				set(lo, i++, ref(), cla);
			else if (c == '.')
				lo[i++] = null;
			else if (c == '<')
				set(lo, i++, false, cla);
			else if (c == '>')
				set(lo, i++, true, cla);
			else if (cla == Double.class)
				lo[i++] = number();
			else if (cla == Float.class)
				lo[i++] = (float)number();
			else if (isInt())
				set(lo, i++, integer(), cla);
			else
				set(lo, i++, number(), cla);
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
			ref = integer();
			refs = Objot.ensureN(refs, ref + 1);
			bxy();
		}
		Object o = cla.newInstance();
		if (ref >= 0)
			refs[ref] = o;
		for (char c; chr() != ';'; bxy())
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
			if (c == 0 || c == '[' || c == '/' || c == '+')
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
			else if (c == '/')
				v = object(f == null ? Object.class : f.getType());
			else if (c == '+')
				v = ref();
			else if (c == '.')
				v = null;
			else if (c == '<')
				v = false;
			else if (c == '>')
				v = true;
			else if (isInt())
				if (t == int.class)
				{
					f.setInt(o, integer());
					continue;
				}
				else
					v = integer();
			else if (t == double.class)
			{
				f.setDouble(o, number());
				continue;
			}
			else if (t == Double.class)
				v = number();
			else if (t == float.class)
			{
				f.setFloat(o, (float)number());
				continue;
			}
			else
				v = Float.valueOf((float)number());

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
