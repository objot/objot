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
	public final String message;

	public Err(String message_)
	{
		message = message_;
	}

	public Err(Throwable e)
	{
		message = e.toString();
	}

	public Err(String message_, Throwable e)
	{
		message = message_ + " : " + e.toString();
	}
}
