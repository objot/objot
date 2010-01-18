//
// Copyright 2007-2010 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.util;

public class InvalidRangeException
	extends InvalidValueException
{
	private static final long serialVersionUID = 6161628417816691368L;

	public InvalidRangeException()
	{
	}

	public InvalidRangeException(String s)
	{
		super(s);
	}

	public InvalidRangeException(Throwable cause)
	{
		super(cause);
	}

	public InvalidRangeException(String s, Throwable cause)
	{
		super(s, cause);
	}

	public InvalidRangeException(int begin, int end1)
	{
		super("invalid range [" + begin + ',' + end1 + ')');
	}

	public InvalidRangeException(long begin, long end1)
	{
		super("invalid range [" + begin + ',' + end1 + ')');
	}

	public InvalidRangeException(int begin, int end1, int maxEnd1)
	{
		super("invalid range [" + begin + ',' + end1 + ',' + maxEnd1 + ')');
	}

	public InvalidRangeException(long begin, long end1, long maxEnd1)
	{
		super("invalid range [" + begin + ',' + end1 + ',' + maxEnd1 + ')');
	}
}
