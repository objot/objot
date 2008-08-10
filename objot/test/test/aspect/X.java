//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package test.aspect;

import objot.container.Inject;
import objot.util.Class2;


public class X
{
	int a;
	long b;
	char c;
	double d;
	String e;
	Object result;

	@Inject
	public X(Object x)
	{
		result = x;
	}

	protected X()
	{
	}

	protected void Void()
	{
	}

	protected int Int(int aa)
	{
		result = a = aa;
		return a;
	}

	protected long Long(long bb, int aa)
	{
		a = aa;
		result = b = bb;
		return b;
	}

	protected char Char(char cc, long bb, int aa)
	{
		a = aa;
		b = bb;
		result = c = cc;
		return c;
	}

	protected double Double(double dd, long bb)
	{
		b = bb;
		result = d = dd;
		return d;
	}

	protected String Str(String ee, double dd, long bb)
	{
		b = bb;
		d = dd;
		result = e = ee;
		return e;
	}

	protected void Throw1(String ee)
	{
		e = ee;
		result = new RuntimeException(e);
		throw (RuntimeException)result;
	}

	protected void Throw2(String ee)
	{
		e = ee;
		result = new RuntimeException(e);
		throw (RuntimeException)result;
	}

	protected void Throw3(String ee)
	{
		e = ee;
		result = new RuntimeException(e);
		throw (RuntimeException)result;
	}

	static enum P
	{
		Void, Int, Long, Char, Double, Str, Throw1, Throw2, Throw3;

		final A a = new A(X.class, name(), Class2.descript(Class2.declaredMethod1(X.class,
			name())));
	}
}
