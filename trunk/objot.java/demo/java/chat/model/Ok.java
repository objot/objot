package chat.model;

import objot.Get;


public class Ok
{
	public static final Ok OK = new Ok("");

	@Get
	String hint;

	public Ok(String hint_)
	{
		hint = hint_;
	}
}
