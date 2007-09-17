//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.codec;

import java.lang.reflect.Field;


class PropField
	extends Property
{
	private Field f;

	PropField(Field f_, Enc e, Dec d, EncDec ed, boolean enc)
	{
		super(f_, f_.getDeclaringClass(), f_.getName(), e, d, ed, enc);
		f = f_;
		cla = f.getType();
		type = f.getGenericType();
	}

	@Override
	Object get(Object o) throws Exception
	{
		return f.get(o);
	}

	@Override
	boolean getBoolean(Object o) throws Exception
	{
		return f.getBoolean(o);
	}

	@Override
	int getInt(Object o) throws Exception
	{
		return f.getInt(o);
	}

	@Override
	long getLong(Object o) throws Exception
	{
		return f.getLong(o);
	}

	@Override
	float getFloat(Object o) throws Exception
	{
		return f.getFloat(o);
	}

	@Override
	double getDouble(Object o) throws Exception
	{
		return f.getDouble(o);
	}

	@Override
	void set(Object o, Object v) throws Exception
	{
		f.set(o, v);
	}

	@Override
	void set(Object o, boolean v) throws Exception
	{
		f.setBoolean(o, v);
	}

	@Override
	void set(Object o, int v) throws Exception
	{
		f.setInt(o, v);
	}

	@Override
	void set(Object o, long v) throws Exception
	{
		f.setLong(o, v);
	}

	@Override
	void set(Object o, float v) throws Exception
	{
		f.setFloat(o, v);
	}

	@Override
	void set(Object o, double v) throws Exception
	{
		f.setDouble(o, v);
	}
}
