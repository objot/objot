//
// Copyright 2007 The Objot Team
// Under the terms of The GNU General Public License version 2
//
package test.aspect;

import objot.util.Class2;


public class X
{
	int a;
	long b;
	char c;
	double d;
	String e;
	Object result;

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

		final A a = new A(name(), Class2.descript(Class2.declaredMethod1(X.class, name())));
	}
}
