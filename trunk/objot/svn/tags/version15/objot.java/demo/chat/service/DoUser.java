//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat.service;

import chat.Transac;
import chat.model.Ok;
import chat.model.User;


public class DoUser
	extends Do
{
	/** @return me with {@link User#friends_} */
	@Service
	@Transac.Readonly
	public User me() throws Exception
	{
		User me = data.load(User.class, sess.me);
		me.friends_ = me.friends;
		return me;
	}

	/**
	 * update {@link User#friends} if SO' {@link User#friends_} is not null. also persist
	 * SO' {@link User#friends_}
	 */
	@Service
	public Ok update(User u) throws Exception
	{
		if (u.friends_ == null) // || name
			return Ok.OK;
		User me = data.load(User.class, sess.me);
		if (u.friends_ != null)
			me.friends = u.friends_;
		return Ok.OK;
	}

	/**
	 * by {@link User#id} if > 0, or {@link User#name} if not null.
	 * 
	 * @return array of Users or some nulls if not found
	 */
	@Service
	@Transac.Readonly
	public User[] get(User[] us) throws Exception
	{
		for (int i = 0; i < us.length; i++)
			us[i] = us[i].id > 0 ? data.get(User.class, us[i].id) : //
				us[i].name != null ? data.find1(User.class, "name", us[i].name) : null;
		return us;
	}
}
