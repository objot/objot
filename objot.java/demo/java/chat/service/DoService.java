package chat.service;

public abstract class DoService
{
	public static Exception err(String hint)
	{
		return new Exception(hint);
	}

	public static Exception err(Throwable e)
	{
		return new Exception(e);
	}

	public static Exception err(String hint, Throwable e)
	{
		return new Exception(hint, e);
	}

	/**
	 * trimed must be not empty
	 * 
	 * @return trimed
	 */
	public static String noEmpty(String s)
	{
		if (s == null || (s = s.trim()).length() <= 0)
			throw new StringIndexOutOfBoundsException("empty string");
		return s;
	}

	/**
	 * original/trimed must be not empty
	 * 
	 * @return original
	 */
	public static String noEmpty(String s, boolean trim)
	{
		if (s == null || (trim ? s.trim() : s).length() <= 0)
			throw new StringIndexOutOfBoundsException("empty string");
		return s;
	}
}
