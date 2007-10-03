//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package test.container;

import objot.container.Container;
import objot.container.Inject;
import objot.container.Scope;


public interface S
{
	@Scope.None
	public class None
	{
		Container con;
		String name;
		@Inject
		public int none;
		@Inject
		public int[] ints;
		@Inject
		public long[] longs;

		@Inject
		public None(@Deprecated
		String name_)
		{
			name = name_;
		}

		@Inject
		public void con(Container v)
		{
			con = v;
		}
	}

	public class None2
		extends None
	{
		Private p;

		@Inject
		public None2(@Deprecated
		String name_)
		{
			super(name_);
		}

		@Inject
		public void pri(Private v)
		{
			p = v;
		}
	}

	@Scope.Private
	public class Private
	{
		@Inject
		public Private p;
		@Inject
		public None n;
	}

	/** should be {@link Scope.Private}, {@link #n} should be {@link None2} */
	public class Private2
		extends Private
	{
		@Inject
		public None n0;
	}

	@Scope.Spread
	public class Spread
		implements S
	{
		@Inject
		public S s;

		/** should not be {@link Scope.Spread} */
		public static class P
			extends Spread
		{
		}
	}

	@Scope.Spread
	public class Spread2
		extends Spread
	{
	}

	@Scope.SpreadCreate
	public class SpreadC
		implements S
	{
		@Inject
		public S s;
		@Inject
		public SpreadC c;
	}

	@Scope.SpreadCreate
	public class SpreadC2
		extends SpreadC
	{
	}
}
