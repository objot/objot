//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat.service;

import objot.servlet.Service;
import chat.model.Ok;
import chat.model.User;


public class DoUser
	extends Do
{
	/** @return me with {@link User#friends_} */
	@Service
	public static User me(Do $) throws Exception
	{
		User me = $.load(User.class, $.me);
		me.friends_ = me.friends;
		return me;
	}

	/** update {@link User#friends} if SO' is not null */
	@Service
	public static Ok update(User u, Do $) throws Exception
	{
		if (u.friends_ == null) // || name
			return Ok.OK;
		User me = $.load(User.class, $.me);
		if (u.friends_ != null)
			me.friends = u.friends_;
		return Ok.OK;
	}

	/**
	 * by {@link User#id} (if > 0) or {@link User#name} (if not null).
	 * 
	 * @return array of User or null if not found
	 */
	@Service
	public static User[] get(User[] us, Do $) throws Exception
	{
		for (int i = 0; i < us.length; i++)
			us[i] = us[i].id != null && us[i].id > 0 ? $.get(User.class, us[i].id)
				: us[i].name != null ? $.find1(User.class, "name", us[i].name) : null;
		return us;
	}
}
