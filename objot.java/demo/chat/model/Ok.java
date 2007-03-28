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
	public final String hint;

	public Ok(String hint_)
	{
		hint = hint_;
	}
}
