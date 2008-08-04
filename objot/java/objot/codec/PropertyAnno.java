//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Library General Public License version 2
//
package objot.codec;

import java.lang.reflect.AccessibleObject;

import objot.util.Array2;


final class PropertyAnno
	extends Property
{
	private Class<?>[] clas;
	private boolean[] allows;

	PropertyAnno(AccessibleObject p, Enc e, Dec d, EncDec ed, boolean enc)
	{
		super(p, enc);
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

	@Override
	public boolean allowEnc(Object ruleKey)
	{
		return ruleKey instanceof Class ? allow((Class<?>)ruleKey) : allow(ruleKey);
	}

	@Override
	public boolean allowDec(Object ruleKey)
	{
		return ruleKey instanceof Class ? allow((Class<?>)ruleKey) : allow(ruleKey);
	}

	boolean allow(Class<?> ruleKey)
	{
		for (int x = clas.length - 1; x >= 0; x--)
			if (clas[x].isAssignableFrom(ruleKey))
				return allows[x];
		return clas.length == 0 || !allows[0];
	}

	boolean allow(Object ruleKey)
	{
		for (int x = clas.length - 1; x >= 0; x--)
			if (clas[x].isInstance(ruleKey))
				return allows[x];
		return clas.length == 0 || !allows[0];
	}
}
