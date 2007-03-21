//
// Objot 1
//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot;

import java.lang.reflect.Field;
import java.util.HashMap;


final class Property
{
	Field f;
	String name;
	Class<?>[] clas;
	boolean[] allows;

	Property(Field f_, Get g, Set s, GetSet gs, boolean get)
	{
		f = f_;
		Class<?>[] fcs;
		if (gs == null)
			fcs = get ? g.value() : s.value();
		else if (g == null && s == null)
			fcs = gs.value();
		else
			throw new RuntimeException("duplicate "
				+ (get ? Get.class.getName() : Set.class.getName()) + " for " + f);

		Get cg = get ? f.getDeclaringClass().getAnnotation(Get.class) : null;
		Set cs = get ? null : f.getDeclaringClass().getAnnotation(Set.class);
		GetSet cgs = f.getDeclaringClass().getAnnotation(GetSet.class);
		Class<?>[] ccs;
		if (cgs == null)
			ccs = cg != null ? cg.value() : cs != null ? cs.value() : Objot.CS0;
		else if (cg == null && cs == null)
			ccs = cgs.value();
		else
			throw new RuntimeException("duplicate "
				+ (get ? Get.class.getName() : Set.class.getName()) + " for "
				+ f.getDeclaringClass());

		NameGet ng = get ? f.getAnnotation(NameGet.class) : null;
		NameSet ns = get ? null : f.getAnnotation(NameSet.class);
		Name name_ = f.getAnnotation(Name.class);
		if (name_ == null)
			name = ng != null ? ng.value() : ns != null ? ns.value() : f.getName();
		else if (ng == null && ns == null)
			name = name_.value();
		else
			throw new RuntimeException("duplicate "
				+ (get ? NameGet.class.getName() : NameSet.class.getName()) + " for " + f);

		int n = 0;
		for (Class<?> c: ccs)
			if (c != Yes.class && c != No.class)
				n++;
		for (Class<?> c: fcs)
			if (c != Yes.class && c != No.class)
				n++;
		clas = n == ccs.length ? ccs : n == fcs.length ? fcs : new Class<?>[n];
		allows = new boolean[n];
		boolean allow = true;
		n = 0;
		for (Class<?> c: ccs)
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
		for (Class<?> c: fcs)
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
	void into(HashMap<String, Property> m)
	{
		Property p = m.get(name);
		if (p != null)
			throw new RuntimeException("duplicate name " + name + ", see " + f);
		m.put(name, this);
	}

	boolean allow(Class<?> c)
	{
		for (int x = clas.length - 1; x >= 0; x--)
			if (clas[x].isAssignableFrom(c))
				return allows[x];
		return clas.length == 0 || ! allows[0];
	}
}
