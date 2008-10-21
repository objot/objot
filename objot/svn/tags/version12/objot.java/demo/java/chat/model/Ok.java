//
// Objot 1
//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
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
