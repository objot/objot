//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU General Public License version 2
//
package objot.util;

public class ErrThrow
	extends RuntimeException
{
	private static final long serialVersionUID = 1022295548108006861L;

	public final Err err;
	public boolean log;

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

	/** whether to log this exception */
	public ErrThrow log(boolean _)
	{
		log = _;
		return this;
	}

	@Override
	public String getMessage()
	{
		return err.toString();
	}
}
