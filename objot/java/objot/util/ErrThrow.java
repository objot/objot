//
// Copyright 2007-2015 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.util;

public class ErrThrow
	extends RuntimeException
{
	private static final long serialVersionUID = 2694138948994462789L;

	public final Err err;

	public ErrThrow(Err e)
	{
		err = e != null ? e : new Err();
	}

	public ErrThrow(Err e, String hint)
	{
		err = e != null ? e : new Err(hint);
	}

	public ErrThrow(Err e, Throwable cause)
	{
		super(cause);
		err = e != null ? e : new Err(cause);
	}

	public ErrThrow(Err e, String hint, Throwable cause)
	{
		super(cause);
		err = e != null ? e : new Err(hint, cause);
	}

	@Override
	public String getMessage()
	{
		return err.toString();
	}
}
