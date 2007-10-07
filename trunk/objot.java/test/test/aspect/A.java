//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
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
	Throwable except;
	String Finally;

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
	@Override
	protected void aspect()
	{
		A a = (A)getData();
		a.name = getName();
		a.desc = getDescript();
		a.target = getTarget();
		a.thiz = getThis();
		a.clazz = getClazz();
		invoke();
	}
}


class A2
	extends Aspect
{
	static final String FINALLY = "Final".toLowerCase();
	final String Finally = FINALLY.concat("ly");

	@Override
	protected void aspect() throws Throwable
	{
		A a = (A)Target.getData();
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
