//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.aspect;

import objot.bytecode.Element;
import objot.util.Bytes;
import objot.util.Class2;


/** All references to subclass will be replaced to the weaved class (target subclass) */
public abstract class Aspect
{
	/** @return an object specified by {@link Weaver#doWeave} */
	protected static Object getData()
	{
		throw new AbstractMethodError();
	}

	/**
	 * keyword "this" is the weaved object (== target object) not this asepct, all fields
	 * and methods in this aspect will be in the weaved object (== target object)
	 */
	protected abstract void aspect() throws Exception;

	/** @return name of target */
	protected static String getName()
	{
		throw new AbstractMethodError();
	}

	/** @return descriptor of target */
	protected static String getDescript()
	{
		throw new AbstractMethodError();
	}

	/** @return name + descriptor of target */
	protected static String getNameDescript()
	{
		throw new AbstractMethodError();
	}

	/** @return target class */
	protected static Class<?> getClazz()
	{
		throw new AbstractMethodError();
	}

	/** Invoke the target method with parameters which may be modified. */
	protected static void invoke()
	{
		throw new AbstractMethodError();
	}

	static enum Proc
	{
		getData, aspect, getName, getDescript, getNameDescript, getClazz, invoke;

		Bytes utf = Element.utf(Class2.declaredMethod1(Aspect.class, name()).getName());
	}
}
