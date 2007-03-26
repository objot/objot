//
// Objot 1
//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat.service;

import java.util.ArrayList;
import java.util.HashMap;

import objot.servlet.Service;
import chat.model.Chat;
import chat.model.ErrUnsigned;
import chat.model.Ok;
import chat.model.User;


public class DoSign
	extends Do
{
	/** {@link User#id} as key, SO as key for sign out, PO in session */
	public static final HashMap<Integer, Object> S = new HashMap<Integer, Object>();

	@Service
	@Signed(need = false)
	public static User inUp(User u_, Do $) throws Exception
	{
		u_.name = noEmpty("name", u_.name);
		u_.password = noEmpty("password", u_.password, false);
		User u = $.find(u_.name); // PO
		// sign up
		if (u == null)
		{
			u = u_; // SO as PO
			$.create(u);
			u.friends = new ArrayList<User>();
			u.friends.add(u); // i'm my friend by default
			u.chatOuts = new ArrayList<Chat>();
			u.chatIns = new ArrayList<Chat>();
		}
		// sign in
		else if (! u.password.equals(u_.password))
			throw err("user name or password incorrect");
		else if (S.get(u.id) != null)
			throw err("user already signed in");
		$.http.setAttribute("signed", u);
		S.put(u.id, S);
		return u;
	}

	@Service
	@Signed(need = false)
	public static Ok out(Do $) throws Exception
	{
		$.http.invalidate(); // fire the unbound event immediately
		return Ok.OK;
	}

	@Service
	public static boolean[] signed(int[] ids, Do $) throws Exception
	{
		boolean[] s = new boolean[ids.length];
		for (int i = 0; i < s.length; i++)
			s[i] = S.containsKey(ids[i]);
		return s;
	}

	/** @return PO, transaction needless */
	static User me(Do $) throws Exception
	{
		User u = (User)$.http.getAttribute("signed");
		if (u == null)
			throw err(new ErrUnsigned("not signed in"));
		return u;
	}
}
