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

	Property(AccessibleObject p, Class<?> out, String name_, Get g, Set s, GetSet gs,
		boolean get)
	{
		Class<?>[] pcs;
		if (gs == null)
			pcs = get ? g.value() : s.value();
		else if (g == null && s == null)
			pcs = gs.value();
		else
			throw new RuntimeException("duplicate "
				+ (get ? Get.class.getName() : Set.class.getName()) + " for " + p);

		Get cg = get ? out.getAnnotation(Get.class) : null;
		Set cs = get ? null : out.getAnnotation(Set.class);
		GetSet cgs = out.getAnnotation(GetSet.class);
		Class<?>[] ocs;
		if (cgs == null)
			ocs = cg != null ? cg.value() : cs != null ? cs.value() : Objot.CS0;
		else if (cg == null && cs == null)
			ocs = cgs.value();
		else
			throw new RuntimeException("duplicate "
				+ (get ? Get.class.getName() : Set.class.getName()) + " for " + out);

		NameGet ng = get ? p.getAnnotation(NameGet.class) : null;
		NameSet ns = get ? null : p.getAnnotation(NameSet.class);
		Name name1 = p.getAnnotation(Name.class);
		if (name1 == null)
			name = ng != null ? ng.value() : ns != null ? ns.value() : name_;
		else if (ng == null && ns == null)
			name = name1.value();
		else
			throw new RuntimeException("duplicate "
				+ (get ? NameGet.class.getName() : NameSet.class.getName()) + " for " + p);

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
		return clas.length == 0 || ! allows[0];
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
