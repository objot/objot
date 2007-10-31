//
// Copyright 2007 Qianyan Cai
// Under the terms of the GNU General Public License version 2
//
package chat.service;

import java.util.HashSet;

import chat.Sign;
import chat.Transac;
import chat.model.Ok;
import chat.model.User;


public class DoSign
	extends Do
{
	/** when sign up, also persist SO. */
	@Service
	@Sign.Any
	public User inUp(User u_) throws Exception
	{
		validate(u_);
		User u = data.find1(User.class, "name", u_.name);
		// sign up
		if (u == null)
		{
			u = u_;
			u.friends = new HashSet<User>();
			u.friends.add(u); // i'm my friend by default
			data.save(u);
		}
		// sign in
		else if ( !u.password.equals(u_.password))
			throw err("user name or password incorrect");
		sess.me = u.id;
		return u;
	}

	@Service
	@Sign.Any
	@Transac.Any
	public Ok out() throws Exception
	{
		sess.me = 0;
		sess.close = true;
		return Ok.OK;
	}
}
