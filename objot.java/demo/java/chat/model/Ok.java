package chat.model;

import objot.Get;


public class Ok
{
	public static final Ok OK = new Ok("");

	@Get
	String message;

	public Ok(String message_)
	{
		message = message_;
	}
}
