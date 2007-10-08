//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.container;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import objot.util.Class2;
import objot.util.Mod2;
import objot.util.Parameter;


public class Bind
{
	Clazz b;
	Object o;
	/** null for binding {@link Clazz} to outer container */
	Class<? extends Annotation> mode;

	public final Clazz bind()
	{
		return b;
	}

	public final Object object()
	{
		return o;
	}

	/** null for binding {@link Clazz} to outer container */
	public final Class<? extends Annotation> mode()
	{
		return mode;
	}

	public static final class Clazz
		extends Bind
	{
		Factory fac;
		/** not primitive */
		Class<?> c;

		/** null iif {@link #b} != this */
		Proc t;
		Value[] fs;
		Proc[] ms;

		/** array of {@link #o} */
		Object[] os;
		int maxParamN;

		Clazz(Factory f, Class<?> c_)
		{
			fac = f;
			c = c_;
			if (c.isPrimitive() || Bind.class.isAssignableFrom(c) //
				|| c != Container.class && Container.class.isAssignableFrom(c))
				throw new IllegalArgumentException("binding " + c + " forbidden");
			if ( !Mod2.match(c, Mod2.PUBLIC))
				throw new IllegalArgumentException("binding not-public " + c + " forbidden");
			Annotation a = Class2.annoExclusive(c, MODES);
			mode = a != null ? a.annotationType() : Inject.Single.class;
		}

		public Factory factory()
		{
			return fac;
		}

		/** not primitive */
		public Class<?> clazz()
		{
			return c;
		}

		/** null iif {@link #bind()} is not self */
		public Proc ctor()
		{
			return t;
		}

		public Value[] fields()
		{
			return fs.length == 0 ? fs : fs.clone();
		}

		public Proc[] methods()
		{
			return ms.length == 0 ? ms : ms.clone();
		}

		@Override
		public String toString()
		{
			return "binding of " + c;
		}

		private static final Class<?>[] MODES = Inject.class.getDeclaredClasses();
	}

	public static final class Proc
	{
		AccessibleObject tm;
		Value[] ps;

		public AccessibleObject member()
		{
			return tm;
		}

		public Constructor<?> ctor()
		{
			return (Constructor<?>)tm;
		}

		public Method method()
		{
			return (Method)tm;
		}

		public Value[] params()
		{
			return ps.length == 0 ? ps : ps.clone();
		}
	}

	public static final class Value
		extends Bind
	{
		AccessibleObject fp;
		Class<?> cla;
		Type generic;

		public AccessibleObject member()
		{
			return fp;
		}

		public Field field()
		{
			return (Field)fp;
		}

		public Parameter param()
		{
			return (Parameter)fp;
		}

		public Class<?> type()
		{
			return cla;
		}

		public Type genericType()
		{
			return generic;
		}
	}
}
