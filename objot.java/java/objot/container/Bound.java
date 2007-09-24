//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.container;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class Bound<T>
{
	public Class<T> cla;
	public Field field;
	public Constructor<T> ctor;
	public Method method;
	/** null or {@link #field} or {@link #ctor} or {@link #method} */
	public AccessibleObject access;

	public Bound<? extends T> bound;
}
