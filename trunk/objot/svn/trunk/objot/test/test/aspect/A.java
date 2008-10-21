//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package test.aspect;

import objot.aspect.Aspect;

import static objot.aspect.Aspect.Target.*;


class A
{
	String name0;
	String desc0;
	String target0;
	String name;
	String desc;
	String target;
	X thiz;
	Class<?> clazz;
	Class<?> returnC;
	Object returnV;
	Throwable except;
	String Finally;

	static int ctor;

	A(Class<?> clazz_, String name_, String desc_)
	{
		name0 = name_;
		desc0 = desc_;
		target0 = clazz_.getName() + '.' + name0 + desc0;
	}

	void clear()
	{
		name = desc = target = Finally = null;
		clazz = null;
		except = null;
	}
}


class A1
	extends Aspect
{

	A1()
	{
		A.ctor++;
	}

	@SuppressWarnings("cast")
	@Override
	protected void aspect() throws Throwable
	{
		A a = (A)data();
		a.name = name();
		a.desc = descript();
		a.target = target();
		a.thiz = thiz();
		a.clazz = clazz();
		a.returnC = returnClass();
		invoke();
		a.returnV = getReturn();
		if (a.returnC == void.class)
			setReturn(null);
		else if (a.returnC == int.class)
			setReturn((Integer)a.returnV + 1);
	}
}


class A2
	extends Aspect
{
	static final String FINALLY = "Final".toLowerCase();
	final String Finally = FINALLY.concat("ly");

	A2()
	{
		A.ctor++;
	}

	@Override
	protected void aspect() throws Throwable
	{
		A a = (A)Target.data();
		a.clazz = Target.clazz();
		try
		{
			Target.invoke();
		}
		catch (Throwable e)
		{
			a.except = e;
			throw e;
		}
		finally
		{
			a.Finally = Finally;
		}
	}
}
