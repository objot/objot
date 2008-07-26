//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Library General Public License version 2
//
package objot.aspect;

import objot.bytecode.Bytecode;
import objot.util.Bytes;
import objot.util.Class2;


/**
 * All references to subclass will be replaced to the weaved class (target subclass). All
 * fields and methods will be in the weaved class (target subclass).
 */
public abstract class Aspect
{
	/**
	 * keyword "this" is the weaved object (== target object) not this asepct. Return must
	 * be after {@link Target#invoke()}
	 */
	protected abstract void aspect() throws Throwable;

	static final Bytes NAME_aspect = Bytecode.utf("aspect");

	/** About target method */
	public static class Target
	{
		/**
		 * @return a static object specified by {@link Weaver#forWeave} per target method
		 *         per weaved class
		 */
		public static <T>T getData()
		{
			throw new AbstractMethodError();
		}

		/** @return target method name */
		public static String getName()
		{
			throw new AbstractMethodError();
		}

		/** @return target method descriptor */
		public static String getDescript()
		{
			throw new AbstractMethodError();
		}

		/** @return target class name + '.' + target method name + target method descriptor */
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

		/** Invoke target method with the parameters. */
		public static void invoke() throws Throwable
		{
			throw new AbstractMethodError();
		}

		/** @return target method return class */
		public static <T>Class<T> getReturnClass()
		{
			throw new AbstractMethodError();
		}

		/**
		 * get target method return value, primitive boxed, null for void, must after
		 * {@link #invoke()}
		 */
		public static <T>T getReturn()
		{
			throw new AbstractMethodError();
		}

		/**
		 * set return value, boxed primitive, null for void, could before
		 * {@link #invoke()}
		 */
		public static void setReturn(Object o)
		{
			throw new AbstractMethodError();
		}

		private Target()
		{
		}
	}

	static enum Targ
	{
		getData,
		getName,
		getDescript,
		getTarget,
		getThis,
		getClazz,
		invoke,
		getReturnClass,
		getReturn,
		setReturn;

		Bytes utf = Bytecode.utf(Class2.declaredMethod1(Target.class, name()).getName());
	}
}
