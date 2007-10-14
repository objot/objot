//
// Copyright 2007 Qianyan Cai
// Under the terms of the GNU General Public License version 2
//
package chat.model;

import objot.codec.Enc;


public class Ok
{
	public static final Ok OK = new Ok("");

	@Enc
	public final String hint;

	public Ok(String hint_)
	{
		hint = hint_;
	}
}
