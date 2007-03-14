//
// Objot 11a
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

	Property(Field f_, Get g, GetSet gs)
	{
		f = f_;
		if (g != null && gs != null)
			throw new RuntimeException("duplicate " + Get.class.getName() + " for " + f);
		Class<?>[] fcs = g != null ? g.value() : gs.value();
		Class<?>[] ccs = Objot.CS0;
		g = f.getDeclaringClass().getAnnotation(Get.class);
		gs = f.getDeclaringClass().getAnnotation(GetSet.class);
		if (g != null || gs != null)
		{
			if (g != null && gs != null)
				throw new RuntimeException("duplicate " + Get.class.getName() + " for "
					+ f.getDeclaringClass());
			ccs = g != null ? g.value() : gs.value();
		}
		init(ccs, fcs);
	}

	Property(Field f_, Set s, GetSet gs)
	{
		f = f_;
		if (s != null && gs != null)
			throw new RuntimeException("duplicate " + Set.class.getName() + " for " + f);
		Class<?>[] fcs = s != null ? s.value() : gs.value();
		Class<?>[] ccs = Objot.CS0;
		s = f.getDeclaringClass().getAnnotation(Set.class);
		gs = f.getDeclaringClass().getAnnotation(GetSet.class);
		if (s != null || gs != null)
		{
			if (s != null && gs != null)
				throw new RuntimeException("duplicate " + Set.class.getName() + " for "
					+ f.getDeclaringClass());
			ccs = s != null ? s.value() : gs.value();
		}
		init(ccs, fcs);
	}

	private void init(Class<?>[] ccs, Class<?>[] fcs)
	{
		Name name_ = f.getAnnotation(Name.class);
		name = name_ != null ? name_.value() : f.getName();
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
