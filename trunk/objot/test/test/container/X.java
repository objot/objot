//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Library General Public License version 2
//
package test.container;

import objot.container.Container;
import objot.container.Inject;


public interface X
{
	@Inject.New
	public class ParentNew
		implements X
	{
		@Inject
		public X x;
	}

	@Inject.Single
	public class ParentSingle
		extends ParentNew
	{
	}

	@Inject.Set
	public class ParentSet
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
		public Set t;

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
		@Inject
		public Set t;

		static boolean created;

		{
			created = true;
		}
	}

	/** should be {@link Inject.Single}, {@link #n} should be {@link New2} */
	public class Single2
		extends Single
	{
		@Inject
		public New n0;
	}

	/** should be {@link Inject.Single} */
	public class ChildSingle
	{
		@Inject
		public ParentNew on;
		@Inject
		public ParentSingle os;
	}

	@Inject.Set
	public class Set
	{
		@Inject
		public New noInject;
	}
}
