//
// Objot 1
//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot;

public class ErrThrow
	extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public final Err err;

	public ErrThrow(Err e)
	{
		super((e = e != null ? e : new Err()).hint);
		err = e;
	}

	public ErrThrow(Err e, String hint)
	{
		super((e = e != null ? e : new Err(hint)).hint);
		err = e;
	}

	public ErrThrow(Err e, Throwable cause)
	{
		super((e = e != null ? e : new Err(cause)).hint);
		err = e;
	}

	public ErrThrow(Err e, String hint, Throwable cause)
	{
		super((e = e != null ? e : new Err(hint, cause)).hint);
		err = e;
	}
}
