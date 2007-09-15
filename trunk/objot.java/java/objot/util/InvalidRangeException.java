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

	public InvalidRangeException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public InvalidRangeException(int begin, int end1)
	{
		super("invalid range " + begin + '-' + end1);
	}

	public InvalidRangeException(long begin, long end1)
	{
		super("invalid range " + begin + '-' + end1);
	}

	public InvalidRangeException(int begin, int end1, int maxEnd1)
	{
		super("invalid range " + begin + '-' + end1 + '-' + maxEnd1);
	}

	public InvalidRangeException(long begin, long end1, long maxEnd1)
	{
		super("invalid range " + begin + '-' + end1 + '-' + maxEnd1);
	}
}
