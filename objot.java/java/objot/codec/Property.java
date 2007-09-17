//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.codec;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.HashMap;


abstract class Property
{
	String name;
	Class<?> cla;
	Type type;
	Class<?>[] clas;
	boolean[] allows;

	Property(AccessibleObject p, Class<?> out, String name_, Enc e, Dec d, EncDec ed,
		boolean enc)
	{
		Class<?>[] pcs;
		if (ed == null)
			pcs = enc ? e.value() : d.value();
		else if (e == null && d == null)
			pcs = ed.value();
		else
			throw new RuntimeException("duplicate "
				+ (enc ? Enc.class.getName() : Dec.class.getName()) + " for " + p);

		Enc ce = enc ? out.getAnnotation(Enc.class) : null;
		Dec cd = enc ? null : out.getAnnotation(Dec.class);
		EncDec ced = out.getAnnotation(EncDec.class);
		Class<?>[] ocs;
		if (ced == null)
			ocs = ce != null ? ce.value() : cd != null ? cd.value() : Codec.CS0;
		else if (ce == null && cd == null)
			ocs = ced.value();
		else
			throw new RuntimeException("duplicate "
				+ (enc ? Enc.class.getName() : Dec.class.getName()) + " for " + out);

		NameEnc ne = enc ? p.getAnnotation(NameEnc.class) : null;
		NameDec nd = enc ? null : p.getAnnotation(NameDec.class);
		Name ned = p.getAnnotation(Name.class);
		if (ned == null)
			name = ne != null ? ne.value() : nd != null ? nd.value() : name_;
		else if (ne == null && nd == null)
			name = ned.value();
		else
			throw new RuntimeException("duplicate "
				+ (enc ? NameEnc.class.getName() : NameDec.class.getName()) + " for " + p);

		int n = 0;
		for (Class<?> c: ocs)
			if (c != Yes.class && c != No.class)
				n++;
		for (Class<?> c: pcs)
			if (c != Yes.class && c != No.class)
				n++;
		clas = n == ocs.length ? ocs : n == pcs.length ? pcs : new Class<?>[n];
		allows = new boolean[n];
		boolean allow = true;
		n = 0;
		for (Class<?> c: ocs)
			if (c == Yes.class)
				allow = true;
			else if (c == No.class)
				allow = false;
			else
			{
				clas[n] = c;
				allows[n] = allow;
				n++;
			}
		for (Class<?> c: pcs)
			if (c == Yes.class)
				allow = true;
			else if (c == No.class)
				allow = false;
			else
			{
				clas[n] = c;
				allows[n] = allow;
				n++;
			}
	}

	/** not thread safe */
	void into(HashMap<String, Property> map)
	{
		Property p = map.get(name);
		if (p != null)
			throw new RuntimeException("duplicate name " + name + ", see " + p);
		map.put(name, this);
	}

	boolean allow(Class<?> c)
	{
		for (int x = clas.length - 1; x >= 0; x--)
			if (clas[x].isAssignableFrom(c))
				return allows[x];
		return clas.length == 0 || !allows[0];
	}

	/** @throws InvocationTargetException or Exception */
	abstract Object get(Object o) throws Exception;

	/** @throws InvocationTargetException or Exception */
	abstract boolean getBoolean(Object o) throws Exception;

	/** @throws InvocationTargetException or Exception */
	abstract int getInt(Object o) throws Exception;

	/** @throws InvocationTargetException or Exception */
	abstract long getLong(Object o) throws Exception;

	/** @throws InvocationTargetException or Exception */
	abstract float getFloat(Object o) throws Exception;

	/** @throws InvocationTargetException or Exception */
	abstract double getDouble(Object o) throws Exception;

	/** @throws InvocationTargetException or Exception */
	abstract void set(Object o, Object v) throws Exception;

	/** @throws InvocationTargetException or Exception */
	abstract void set(Object o, boolean v) throws Exception;

	/** @throws InvocationTargetException or Exception */
	abstract void set(Object o, int v) throws Exception;

	/** @throws InvocationTargetException or Exception */
	abstract void set(Object o, long v) throws Exception;

	/** @throws InvocationTargetException or Exception */
	abstract void set(Object o, float v) throws Exception;

	/** @throws InvocationTargetException or Exception */
	abstract void set(Object o, double v) throws Exception;
}
