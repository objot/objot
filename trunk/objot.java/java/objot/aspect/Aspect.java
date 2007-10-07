//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.aspect;

import objot.bytecode.Bytecode;
import objot.util.Bytes;
import objot.util.Class2;


/** All references to subclass will be replaced to the weaved class (target subclass) */
public abstract class Aspect
{

	/**
	 * keyword "this" is the weaved object (== target object) not this asepct, all fields
	 * and methods in this aspect will be in the weaved object (== target object)
	 */
	protected abstract void aspect() throws Throwable;

	static final Bytes NAME_aspect = Bytecode.utf("aspect");

	/** About the target method */
	public static enum Target
	{
		getData, getName, getDescript, getTarget, getThis, getClazz, invoke;

		/** @return an object per target specified by {@link Weaver#doWeave} */
		public static <T>T getData()
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

		/** @return class name + '.' + name + descriptor of target */
		public static String getTarget()
		{
			throw new AbstractMethodError();
		}

		/** @return same as "this" in {@link Aspect#aspect}, weaved and target object */
		public static <T>T getThis()
		{
			throw new AbstractMethodError();
		}

		/** @return target class */
		public static <T>Class<T> getClazz()
		{
			throw new AbstractMethodError();
		}

		/** Invoke target with parameters which may be modified. */
		public static void invoke()
		{
			throw new AbstractMethodError();
		}

		Bytes utf = Bytecode.utf(Class2.declaredMethod1(Target.class, name()).getName());
	}
}
