//
// Copyright 2007 Qianyan Cai
// Under the terms of the GNU General Public License version 2
//
package test.container;

import objot.container.Container;
import objot.container.Inject;


public interface X
{
	@Inject.New
	public class OuterNew
		implements X
	{
		@Inject
		public X x;
	}

	@Inject.Single
	public class OuterSingle
		extends OuterNew
	{
	}

	@Inject.New
	public class New
	{
		Container con;
		String name;
		@Inject
		public int new_;
		@Inject
		public long obj;
		@Inject
		public int[] ints;
		@Inject
		public long[] longs;

		@Inject
		public New(@Deprecated
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

	/** should be {@link Inject.New} */
	public class New2
		extends New
	{
		Single p;

		@Inject
		public New2(@Deprecated
		String name_)
		{
			super(name_);
		}

		@Inject
		public void pri(Single v)
		{
			p = v;
		}
	}

	/** should be {@link Inject.Single} */
	public class Single
	{
		@Inject
		public Single s;
		@Inject
		public New n;
	}

	/** should be {@link Inject.Single}, {@link #n} should be {@link New2} */
	public class Single2
		extends Single
	{
		@Inject
		public New n0;
	}

	public class Inner
	{
		@Inject
		public OuterNew on;
		@Inject
		public OuterSingle os;
	}
}
