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
	static final Bytes NAME_aspect = Element.utf("aspect");

	/**
	 * keyword "this" is the weaved object (== target object) not this asepct, all fields
	 * and methods in this aspect will be in the weaved object (== target object)
	 */
	protected abstract void aspect() throws Throwable;

	/** About the target method */
	public static enum Target
	{
		getData, getName, getDescript, getNameDescript, getClazz, invoke;

		/** @return an object per target specified by {@link Weaver#doWeave} */
		public static Object getData()
		{
			throw new AbstractMethodError();
		}

		/** @return name of target */
		public static String getName()
		{
			throw new AbstractMethodError();
		}

		/** @return descriptor of target */
		public static String getDescript()
		{
			throw new AbstractMethodError();
		}

		/** @return name + descriptor of target */
		public static String getNameDescript()
		{
			throw new AbstractMethodError();
		}

		/** @return target class */
		public static Class<?> getClazz()
		{
			throw new AbstractMethodError();
		}

		/** Invoke target with parameters which may be modified. */
		public static void invoke()
		{
			throw new AbstractMethodError();
		}

		Bytes utf = Element.utf(Class2.declaredMethod1(Target.class, name()).getName());
	}
}
