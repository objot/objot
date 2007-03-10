//
// Objot 11a
//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot;

public class Fail
	extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public Fail()
	{
	}

	public Fail(String message)
	{
		super(message);
	}

	public Fail(Throwable cause)
	{
		super(cause);
	}

	public Fail(String message, Throwable cause)
	{
		super(message, cause);
	}
}
