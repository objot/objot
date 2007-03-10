//
// Objot 11a
//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot;

public class Err
	extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public Err()
	{
	}

	public Err(String message)
	{
		super(message);
	}

	public Err(Throwable cause)
	{
		super(cause);
	}

	public Err(String message, Throwable cause)
	{
		super(message, cause);
	}
}
