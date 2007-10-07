//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.aspect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;

import objot.bytecode.Bytecode;
import objot.bytecode.Code;
import objot.bytecode.Constants;
import objot.bytecode.Field;
import objot.bytecode.Procedure;
import objot.util.Array2;
import objot.util.Bytes;
import objot.util.Class2;
import objot.util.Mod2;


public abstract class Weaver
{
	static final Bytes ASPECT_NAME = Bytecode.utf(Class2.pathName(Aspect.class));
	static final Bytes TARGET_NAME = Bytecode.utf(Class2.pathName(Aspect.Target.class));
	static final Bytes CTOR_NAME = Bytecode.utf(Procedure.CTOR_NAME);
	private static final String DATAS_NAME = "$$";

	private Class<? extends Aspect>[] acs;
	private Bytes[] abs;

	@SuppressWarnings("unchecked")
	public Weaver(Class... aspectClasses) throws Exception
	{
		acs = aspectClasses;
		if (acs.length == 0)
			throw new IllegalArgumentException("at least one aspect class");
		for (Class<? extends Aspect> ac: acs)
		{
			if (ac.getSuperclass() != Aspect.class)
				throw new IllegalArgumentException(ac + " must be direct subclass of "
					+ Aspect.class);
			if (Mod2.match(ac, Mod2.ABSTRACT))
				throw new IllegalArgumentException("abstract " + ac + " forbidden");
			if (ac.getInterfaces().length != 0)
				throw new IllegalArgumentException("any interface of " + ac + " forbidden");
			int dup = 0;
			for (Class<? extends Aspect> ac0: acs)
				if (ac0 == ac && (dup++) > 0)
					throw new IllegalArgumentException("duplicate aspect " + ac);
			if (ac.getDeclaredConstructors().length != 1
				|| ac.getDeclaredConstructors()[0].getParameterTypes().length != 0)
				throw new IllegalArgumentException(ac
					+ ": exact one constructor expected with no parameter");
		}
		abs = new Bytes[acs.length];
		for (int i = 0; i < abs.length; i++)
			abs[i] = Class2.classFile(acs[i]);
	}

	@SuppressWarnings("unchecked")
	public synchronized <T>Class<T> weave(Class<T> target) throws Exception
	{
		if (Aspect.class.isAssignableFrom(target))
			throw new IllegalArgumentException(Aspect.class + " subclass " + target
				+ " as target forbidden");
		if (target.isPrimitive() || target.isInterface()
			|| Mod2.match(target, Mod2.ABSTRACT | Mod2.FINAL))
			throw new IllegalArgumentException(target + " forbidden");

		ArrayList<Constructor<?>> ats = new ArrayList<Constructor<?>>();
		for (Constructor<?> t: target.getDeclaredConstructors())
			if (Mod2.match(t, Mod2.PUBLIC_PROTECT))
				ats.add(t);
		if (ats.size() == 0)
			throw new IllegalArgumentException(target
				+ " at least one public/protected constructor");
		Method[] ms = Array2.from(Class2.methods(target, 0, 0, 0), Method.class);
		Class<T> sup = target;

		for (int ax = abs.length - 1; ax >= 0; ax--)
		{
			ArrayList<Method> ams = new ArrayList<Method>();
			ArrayList<Object> aos = new ArrayList<Object>();
			for (Method m: ms)
			{
				Object o = doWeave(acs[ax], m);
				if (o != this)
				{
					if (Mod2.match(m, Mod2.FRIEND_PRIVATE | Mod2.FINAL | Mod2.P.NOTOBJECT))
						throw new IllegalArgumentException("weaving " + Mod2.toString(m) + m
							+ " forbidden");
					ams.add(m);
					aos.add(o);
				}
			}
			if (ams.size() == 0)
				continue;

			String name = acs[ax].getName() + "$$" + target.getName().replace('.', '$');
			sup = Class2.<T>load(acs[ax].getClassLoader(), name, //
				make1(target, abs[ax], name, sup, ats, ams));
			Class2.declaredField(sup, DATAS_NAME).set(null, aos.toArray());
		}
		return sup;
	}

	/** @return this {@link Weaver} not to weave the method with the aspect, other to weave */
	protected abstract Object doWeave(Class<? extends Aspect> a, Method m) throws Exception;

	private byte[] make1(Class<?> target, Bytes ab, String name, Class<?> sup,
		ArrayList<Constructor<?>> ts, ArrayList<Method> ms)
	{
		Bytecode y = new Bytecode(ab);
		Constants cs = y.cons;
		y.head.setModifier(Mod2.PUBLIC | Mod2.SYNTHETIC);
		int classCi = cs.addClass(name);
		y.head.setSuperCi(cs.addClass(sup));

		Bytes n = cs.getUtf(cs.getClass(y.head.getClassCi()));
		for (int i = 1; i < cs.getConN(); i++)
			if (cs.getTag(i) == Constants.TAG_CLASS && cs.equalsUtf(cs.getClass(i), n))
				cs.setClass(i, cs.getClass(classCi));

		Field f = new Field(cs);
		f.setModifier(Mod2.PUBLIC | Mod2.STATIC);
		f.setNameCi(cs.addUcs(DATAS_NAME));
		f.setDescCi(cs.addUcs(Class2.descript(Object[].class)));
		y.getFields().addField(f);
		int datasCi = cs.addField(y.head.getClassCi(), cs.addNameDesc(f.getNameCi(), f
			.getDescCi()));

		Code ato = y.getProcs().removeProc(y.getProcs().searchProc(CTOR_NAME, null))
			.getCode();
		for (Constructor<?> t: ts)
			new WeaveProc(target, y, ato).ctor(t);
		Code ao = y.getProcs().removeProc(y.getProcs().searchProc(Aspect.NAME_aspect, null))
			.getCode();
		for (int i = 0; i < ms.size(); i++)
			new WeaveProc(target, y, ao).method(ms.get(i), i, datasCi);
		return y.normalize();
	}
}
