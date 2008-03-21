//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU General Public License version 2
//
package objot.codec;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.Clob;
import java.util.Collection;
import java.util.HashMap;

import objot.util.Array2;
import objot.util.Class2;


final class Property
{
	Class<?> out;
	Field field;
	Method method;
	static final Field F_name = Class2.declaredField(Property.class, "name");
	String name;
	Class<?> cla;
	boolean clob;
	Class<?> listElem;
	int index;
	private Class<?>[] clas;
	private boolean[] allows;

	Property(Field f, Enc e, Dec d, EncDec ed, boolean enc)
	{
		out = f.getDeclaringClass();
		field = f;
		name = f.getName();
		cla = f.getType();
		init(f, f.getGenericType(), e, d, ed, enc);
	}

	/** @param m be checked against getter/setter rules */
	Property(Method m, Enc e, Dec d, boolean enc)
	{
		out = m.getDeclaringClass();
		method = m;
		name = Class2.propertyOrName(m, enc);
		cla = enc ? m.getReturnType() : m.getParameterTypes()[0];
		init(m, enc ? m.getGenericReturnType() : m.getGenericParameterTypes()[0], e, d, null,
			enc);
	}

	private void init(AccessibleObject p, Type type, Enc e, Dec d, EncDec ed, boolean enc)
	{
		clob = Clob.class.isAssignableFrom(cla);
		if (Collection.class.isAssignableFrom(cla))
			listElem = Class2.typeParamClass(type, 0, Object.class);

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
			ocs = ce != null ? ce.value() : cd != null ? cd.value() : Array2.CLASSES0;
		else if (ce == null && cd == null)
			ocs = ced.value();
		else
			throw new RuntimeException("duplicate "
				+ (enc ? Enc.class.getName() : Dec.class.getName()) + " for " + out);

		NameEnc ne = enc ? p.getAnnotation(NameEnc.class) : null;
		NameDec nd = enc ? null : p.getAnnotation(NameDec.class);
		Name ned = p.getAnnotation(Name.class);
		if (ned == null)
			name = ne != null ? ne.value() : nd != null ? nd.value() : name;
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

	void into(HashMap<String, Property> map)
	{
		Property p = map.get(name);
		if (p != null)
			throw new RuntimeException("duplicate name " + name + ", see " + p);
		map.put(name, this);
	}

	static final Method M_allow = Class2.declaredMethod1(Property.class, "allow");

	boolean allow(Class<?> c)
	{
		for (int x = clas.length - 1; x >= 0; x--)
			if (clas[x].isAssignableFrom(c))
				return allows[x];
		return clas.length == 0 || !allows[0];
	}
}
