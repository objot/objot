//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot;

public class ErrThrow
	extends RuntimeException
{
	private static final long serialVersionUID = 1022295548108006861L;

	public final Err err;
	public boolean log = false;

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
		super((e = e != null ? e : new Err(cause)).hint, cause);
		err = e;
	}

	public ErrThrow(Err e, String hint, Throwable cause)
	{
		super((e = e != null ? e : new Err(hint, cause)).hint, cause);
		err = e;
	}

	/** whether to log this exception */
	public ErrThrow log(boolean _)
	{
		log = _;
		return this;
	}
}
