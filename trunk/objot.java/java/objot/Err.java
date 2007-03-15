//
// Objot 1
//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot;

public class Err
{
	@Get
	public final String hint;

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
}
