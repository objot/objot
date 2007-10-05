//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.aspect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;

import objot.aspect.Aspect.Proc;
import objot.bytecode.Annotation;
import objot.bytecode.Bytecode;
import objot.bytecode.Code;
import objot.bytecode.Constants;
import objot.bytecode.Element;
import objot.bytecode.Field;
import objot.bytecode.Procedure;
import objot.container.Inject;
import objot.util.Array2;
import objot.util.Bytes;
import objot.util.Class2;
import objot.util.Mod2;


public abstract class Weaver
{
	static final Bytes ASPECT_NAME = Element.utf(Class2.pathName(Aspect.class));
	private static final Bytes CTOR_NAME = Element.utf(Procedure.CTOR_NAME);
	private static final String OSS_NAME = "$$";

	private Class<? extends Aspect>[] acs;
	private Bytes[] abs;

	public Weaver(Class<? extends Aspect>... aspectClasses) throws Exception
	{
		acs = aspectClasses;
		for (Class<? extends Aspect> ac: acs)
		{
			if (ac.getSuperclass() != Aspect.class)
				throw new IllegalArgumentException(ac + " must be direct subclass of "
					+ Aspect.class);
			if ((ac.getModifiers() & (Mod2.ABSTRACT | Mod2.FINAL)) != 0)
				throw new IllegalArgumentException("abstract or final " + ac + " forbidden");
			int dup = 0;
			for (Class<? extends Aspect> ac0: acs)
				if (ac0 == ac && (dup++) > 0)
					throw new IllegalArgumentException("duplicate aspect " + ac);
			if (ac.getDeclaredConstructors().length != 0
				|| ac.getDeclaredConstructors()[0].getParameterTypes().length != 0)
				throw new IllegalArgumentException(ac
					+ ": exact one constructor expected with no parameter");
		}
		abs = new Bytes[acs.length];
		for (int i = 0; i < abs.length; i++)
			abs[i] = Class2.classFile(acs[i]);
	}

	@SuppressWarnings("unchecked")
	public synchronized <T>Class<? extends T> weave(Class<T> target) throws Exception
	{
		if (Aspect.class.isAssignableFrom(target))
			throw new IllegalArgumentException(Aspect.class + " subclass " + target
				+ " as target forbidden");
		if (target.isPrimitive() || target.isInterface())
			throw new IllegalArgumentException(target + " forbidden");

		Method[] ms = Array2.from(Class2.methods(target, 0, 0), Method.class);
		Constructor<?>[] ctors = target.getDeclaredConstructors();
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
					int d = Mod2.get(m.getModifiers(), m.getName().charAt(0));
					if ((d & (Mod2.FRIEND_PRIVATE | Mod2.FINAL | Mod2.P.NOTOBJECT)) != 0)
						throw new IllegalArgumentException("weaving " + Mod2.toString(d)
							+ " " + m + " forbidden");
					ams.add(m);
					aos.add(o);
				}
			}
			if (ams.size() == 0)
				continue;

			String name = acs[ax].getName() + "$$" + target.getName().replace('.', '$');
			Bytecode y = new Bytecode(abs[ax]);
			int classCi = y.cons.addClass(name);
			int superCi = y.cons.addClass(sup);
			makeCons(y.cons, classCi, y.head.getClassCi());

			y.head.setModifier(Mod2.PUBLIC | Mod2.SYNTHETIC);
			y.head.setClassCi(classCi);
			y.head.setSuperCi(superCi);
			y.getProcs().removeProc(y.getProcs().searchProc(CTOR_NAME, null));
			if (makeCtors(y, superCi, ctors) == 0)
				throw new IllegalArgumentException(target
					+ " at least one public/protected constructor");

			int ossCi = makeOss(y, classCi);
			Code ca = y.getProcs().removeProc(y.getProcs().searchProc(Proc.aspect.utf, null))
				.getCode();
			for (int i = 0; i < ams.size(); i++)
				new WeaveProc(y, ca).method(ams.get(i), i, ossCi);

			sup = Class2.<T>load(acs[ax].getClassLoader(), name, y.normalize());
			Class2.declaredField(sup, "$$").set(null, aos.toArray());
		}
		return sup;
	}

	/** @return this {@link Weaver} not to weave the method with the aspect, other to weave */
	protected abstract Object doWeave(Class<? extends Aspect> a, Method m) throws Exception;

	private void makeCons(Constants cs, int oldClassCi, int classCi)
	{
		Bytes old = cs.getUtf(cs.getClass(oldClassCi));
		for (int i = 1; i < cs.getConN(); i++)
			if (cs.getTag(i) == Constants.TAG_CLASS && cs.equalsUtf(cs.getClass(i), old))
				cs.setClass(i, cs.getClass(classCi));
	}

	/** @todo copy default aspect ctor code to these ctors to reserve initialization */
	private int makeCtors(Bytecode y, int superCi, Constructor<?>[] ctors)
	{
		byte[] inject = new byte[4];
		Bytes.writeU2(inject, 0, y.cons.addClass(Inject.class));
		int n = 0;
		for (Constructor<?> c: ctors)
			if ((c.getModifiers() & Mod2.PUBLIC_PROTECT) != 0)
			{
				n++;
				Procedure p = Procedure.addCtor(y.cons, superCi, Mod2.PUBLIC, (Object[])c
					.getParameterTypes());
				if (c.isAnnotationPresent(Inject.class))
					p.getAnnos().addAnno(new Annotation(y.cons, inject, 0));
				y.getProcs().addProc(p);
			}
		return n;
	}

	private int makeOss(Bytecode y, int classCi)
	{
		Field f = new Field(y.cons);
		f.setModifier(Mod2.PUBLIC);
		f.setNameCi(y.cons.addUcs(OSS_NAME));
		f.setDescCi(y.cons.addUcs(Class2.descript(Object[].class)));
		y.getFields().addField(f);
		return y.cons.addField(classCi, y.cons.addNameDesc(f.getNameCi(), f.getDescCi()));
	}
}
