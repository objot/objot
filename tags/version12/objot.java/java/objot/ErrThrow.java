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
		super(e.hint);
		err = e;
	}
}
