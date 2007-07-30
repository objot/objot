//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat.service;

import java.util.HashSet;

import objot.servlet.Service;

import org.hibernate.Hibernate;

import chat.model.Ok;
import chat.model.User;


public class DoSign
	extends Do
{
	@Service
	@Signed(need = false)
	public static User inUp(User u_, Do $) throws Exception
	{
		validator(u_);
		User u = $.find1(User.class, "name", u_.name);
		// sign up
		if (u == null)
		{
			u = u_;
			u.friends = new HashSet<User>();
			u.friends.add(u); // i'm my friend by default
			$.save(u);
		}
		// sign in
		else if (! u.password.equals(u_.password))
			throw err("user name or password incorrect");
		else
			Hibernate.initialize(u.friends); // for out of session use
		$.me = u.id;
		return u;
	}

	@Service
	@Signed(need = false)
	public static Ok out(Do $) throws Exception
	{
		$.me = null;
		return Ok.OK;
	}
}
