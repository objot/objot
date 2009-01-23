//
// Copyright 2007-2009 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.service;

public class RequestException
	extends RuntimeException
{
	private static final long serialVersionUID = 2498823019471105899L;

	public RequestException()
	{
	}

	public RequestException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public RequestException(String message)
	{
		super(message);
	}

	public RequestException(Throwable cause)
	{
		super(cause);
	}
}
