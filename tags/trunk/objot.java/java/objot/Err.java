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
	public String hint = "";

	public Err hint(String hint_)
	{
		hint = hint_ == null ? "" : hint_;
		return this;
	}

	public Err cause(Throwable e)
	{
		hint = e.getMessage() == null ? e.getClass().getName() //
			: e.getMessage() + "  [" + e.getClass().getName() + "]";
		return this;
	}

	public Err hintCause(String hint_, Throwable e)
	{
		if (hint_ == null)
			cause(e);
		else
			hint = e.getMessage() == null ? hint_ + " : " + e.getClass().getName() //
			: hint_ + " : " + e.getMessage() + "  [" + e.getClass().getName() + "]";
		return this;
	}
}
