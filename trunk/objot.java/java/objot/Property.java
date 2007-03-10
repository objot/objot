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
	boolean[] ins;

	Property(Field f_, Get g, GetSet gs)
	{
		f = f_;
		if (g != null && gs != null)
			throw new RuntimeException("duplicate " + Get.class.getName() + " for " + f);
		clas = g != null ? g.value() : gs.value();
		g = f.getDeclaringClass().getAnnotation(Get.class);
		gs = f.getDeclaringClass().getAnnotation(GetSet.class);
		if (g != null || gs != null)
		{
			if (g != null && gs != null)
				throw new RuntimeException("duplicate " + Get.class.getName() + " for "
					+ f.getDeclaringClass());
			clas = Objot.concat(g != null ? g.value() : gs.value(), clas);
		}
		init2();
	}

	Property(Field f_, Set s, GetSet gs)
	{
		f = f_;
		if (s != null && gs != null)
			throw new RuntimeException("duplicate " + Set.class.getName() + " for " + f);
		clas = s != null ? s.value() : gs.value();
		s = f.getDeclaringClass().getAnnotation(Set.class);
		gs = f.getDeclaringClass().getAnnotation(GetSet.class);
		if (s != null || gs != null)
		{
			if (s != null && gs != null)
				throw new RuntimeException("duplicate " + Set.class.getName() + " for "
					+ f.getDeclaringClass());
			clas = Objot.concat(s != null ? s.value() : gs.value(), clas);
		}
		init2();
	}

	private void init2()
	{
		Name n = f.getAnnotation(Name.class);
		name = n != null ? n.value() : f.getName();
		if (clas.length == 0)
			return;
		ins = new boolean[clas.length];
		boolean in = true;
		for (int x = 0; x < clas.length; x++)
			ins[x] = clas[x] == In.class ? (in = true) : clas[x] == Out.class ? (in = false)
				: in;
	}

	/** not thread safe */
	void into(HashMap<String, Property> m)
	{
		Property p = m.get(name);
		if (p != null)
			throw new RuntimeException("duplicate name " + name + ", see " + f);
		m.put(name, this);
	}

	boolean in(Class<?> c)
	{
		for (int x = clas.length - 1; x >= 0; x--)
			if (clas[x].isAssignableFrom(c))
				return ins[x];
		return clas.length == 0 || ! ins[0];
	}
}
