//
// Objot 1
//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat.service;

import java.util.ListIterator;

import javax.servlet.http.HttpSession;

import objot.servlet.Service;
import chat.model.Ok;
import chat.model.User;


public class DoUser
	extends DoService
{
	/** @return me PO */
	@Service
	public static User me(HttpSession ses) throws Exception
	{
		return DoSign.me(ses).clone();
	}

	/**
	 * update {@link User#friends} if SO' is not null.
	 * 
	 * @todo update {@link User#name}
	 */
	@Service
	public static Ok update(User u, HttpSession ses) throws Exception
	{
		User me = DoSign.me(ses);
		if (u.myFriends != null)
		{
			for (ListIterator<User> i = u.myFriends.listIterator(); i.hasNext();)
				i.set(User.IDS.get(i.next().id - 1));
			me.friends = u.myFriends;
		}
		return Ok.OK;
	}

	/**
	 * Get POs by {@link User#id} (if >= 0) or {@link User#name} (if not null).
	 * 
	 * @return POs, or nulls if not found
	 */
	@Service
	public static User[] get(User[] us, HttpSession ses) throws Exception
	{
		DoSign.me(ses);
		for (int i = 0; i < us.length; i++)
			us[i] = us[i].id != null && us[i].id >= 0 ? User.IDS.get(us[i].id)
				: us[i].name != null ? User.NAMES.get(us[i].name) : null;
		return us;
	}
}
