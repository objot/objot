//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot;

import java.lang.reflect.Method;


class PropMethod
	extends Property
{
	private Method m;

	/** @throws RuntimeException if not available getter/setter */
	PropMethod(Method m_, Get g, Set s, boolean get)
	{
		super(m_, m_.getDeclaringClass(), name(m_, get), g, s, null, get);
		m = m_;
		cla = get ? m_.getReturnType() : m_.getParameterTypes()[0];
		type = get ? m_.getGenericReturnType() : m_.getGenericParameterTypes()[0];
	}

	static String name(Method _, boolean get)
	{
		String n = _.getName();
		if (get && (! n.startsWith("get") || _.getParameterTypes().length > 0 //
		|| _.getReturnType() == void.class))
			throw new RuntimeException("invalid getter: " + _);
		if (! get && (! n.startsWith("set") || _.getParameterTypes().length != 1 //
		|| _.getReturnType() != void.class))
			throw new RuntimeException("invalid setter: " + _);
		if (n.length() > 4 && Character.isUpperCase(n.charAt(3))
			&& Character.isUpperCase(n.charAt(4)))
			return n.substring(3);
		else
			return Character.toLowerCase(n.charAt(3)) + n.substring(4);
	}

	@Override
	Object get(Object o) throws Exception
	{
		return m.invoke(o, (Object[])null);
	}

	@Override
	boolean getBoolean(Object o) throws Exception
	{
		return (Boolean)m.invoke(o, (Object[])null);
	}

	@Override
	int getInt(Object o) throws Exception
	{
		return (Integer)m.invoke(o, (Object[])null);
	}

	@Override
	long getLong(Object o) throws Exception
	{
		return (Long)m.invoke(o, (Object[])null);
	}

	@Override
	float getFloat(Object o) throws Exception
	{
		return (Float)m.invoke(o, (Object[])null);
	}

	@Override
	double getDouble(Object o) throws Exception
	{
		return (Double)m.invoke(o, (Object[])null);
	}

	@Override
	void set(Object o, Object v) throws Exception
	{
		m.invoke(o, v);
	}

	@Override
	void set(Object o, boolean v) throws Exception
	{
		m.invoke(o, v);
	}

	@Override
	void set(Object o, int v) throws Exception
	{
		m.invoke(o, v);
	}

	@Override
	void set(Object o, long v) throws Exception
	{
		m.invoke(o, v);
	}

	@Override
	void set(Object o, float v) throws Exception
	{
		m.invoke(o, v);
	}

	@Override
	void set(Object o, double v) throws Exception
	{
		m.invoke(o, v);
	}
}
