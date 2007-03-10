//
// Objot 11a
//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot;

public class Err
{
	@Get
	String hint;

	public Err(String hint_)
	{
		hint = hint_;
	}

	public Err(Throwable e)
	{
		hint = e.getMessage();
	}
}
