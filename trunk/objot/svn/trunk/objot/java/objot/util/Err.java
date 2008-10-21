//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.util;

import objot.codec.Enc;


public class Err
{
	@Enc
	public final String hint;

	public Err()
	{
		hint = "";
	}

	public Err(String hint_)
	{
		hint = hint_ == null ? "" : hint_;
	}

	public Err(Throwable e)
	{
		hint = e.getMessage() == null ? e.getClass().getName() //
			: e.getMessage() + "  [" + e.getClass().getName() + "]";
	}

	public Err(String hint_, Throwable e)
	{
		if (hint_ == null)
			hint = e.getMessage() == null ? e.getClass().getName() //
				: e.getMessage() + "  [" + e.getClass().getName() + "]";
		else
			hint = e.getMessage() == null ? hint_ + " : " + e.getClass().getName() //
			: hint_ + " : " + e.getMessage() + "  [" + e.getClass().getName() + "]";
	}

	@Override
	public String toString()
	{
		return hint;
	}
}
