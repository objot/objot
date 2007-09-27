//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package test.container;

import objot.container.Container;
import objot.container.Inject;
import objot.container.Scope;


@Scope.None
class None
{
	Container con;
	String name;
	@Inject
	int none;

	@Inject
	None(@Deprecated
	String name_)
	{
		name = name_;
	}

	@Inject
	void con(Container v)
	{
		con = v;
	}
}


class None2
	extends None
{
	Private p;

	@Inject
	None2(@Deprecated
	String name_)
	{
		super(name_);
	}

	@Inject
	void pri(Private v)
	{
		p = v;
	}
}


@Scope.Private
class Private
{
	@Inject
	Private p;
	@Inject
	None n;
}


/** should be {@link Scope.Private}, {@link #n} should be {@link None2} */
class Private2
	extends Private
{
	@Inject
	None n0;
}


interface S
{
}


@Scope.Spread
class Spread
	implements S
{
	@Inject
	S s;

	/** should not be {@link Scope.Spread} */
	static class P
		extends Spread
	{
	}
}


@Scope.Spread
class Spread2
	extends Spread
{
}


@Scope.SpreadCreate
class SpreadC
	implements S
{
	@Inject
	S s;
	@Inject
	SpreadC c;
}


@Scope.SpreadCreate
class SpreadC2
	extends SpreadC
{
}
