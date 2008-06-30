//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Library General Public License version 2
//
package objot.util;

public class InvalidLengthException
	extends InvalidValueException
{
	private static final long serialVersionUID = 7482724961787288119L;

	public InvalidLengthException()
	{
	}

	public InvalidLengthException(String s)
	{
		super(s);
	}

	public InvalidLengthException(Throwable cause)
	{
		super(cause);
	}

	public InvalidLengthException(String s, Throwable cause)
	{
		super(s, cause);
	}

	public InvalidLengthException(int length, int min, int max)
	{
		super("invalid length " + length + " [" + min + ',' + max + ']');
	}

	public InvalidLengthException(long length, long min, long max)
	{
		super("invalid length " + length + " [" + min + ',' + max + ']');
	}
}
