//
// Objot 1
//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat.service;

import java.util.ListIterator;

import objot.servlet.Service;
import chat.model.Ok;
import chat.model.User;


public class DoUser
	extends Do
{
	/** @return me PO */
	@Service
	public static User me(Do $) throws Exception
	{
		return $.me.clone();
	}

	/** update {@link User#friends} if SO' is not null */
	@Service
	public static Ok update(User u, Do $) throws Exception
	{
		if (u.myFriends != null)
		{
			for (ListIterator<User> i = u.myFriends.listIterator(); i.hasNext();)
				i.set($.load(i.next().id));
			$.me.friends = u.myFriends;
		}
		return Ok.OK;
	}

	/**
	 * Get POs by {@link User#id} (if > 0) or {@link User#name} (if not null).
	 * 
	 * @return POs, or nulls if not found
	 */
	@Service
	public static User[] get(User[] us, Do $) throws Exception
	{
		for (int i = 0; i < us.length; i++)
			us[i] = us[i].id != null && us[i].id > 0 ? $.load(us[i].id) : us[i].name != null
				? $.load(us[i].name) : null;
		return us;
	}
}
