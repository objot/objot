//
// Objot 1
//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat.service;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpSession;

import objot.servlet.Service;
import chat.model.Chat;
import chat.model.ErrUnsigned;
import chat.model.Ok;
import chat.model.User;


public class DoSign
	extends DoService
{
	/** {@link User#id} as key, SO as key for sign out, PO in session */
	public static final HashMap<Integer, Object> S = new HashMap<Integer, Object>();

	@Service
	public static User inUp(User u_, HttpSession ses) throws Exception
	{
		u_.name = noEmpty("name", u_.name);
		u_.password = noEmpty("password", u_.password, false);
		User u = User.NAMES.get(u_.name); // PO
		// sign up
		if (u == null)
		{
			u = u_; // SO as PO
			User.IDS.add(u);
			User.NAMES.put(u.name, u);
			u.id = User.IDS.size();
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
		ses.setAttribute("signed", u);
		S.put(u.id, S);
		return u;
	}

	@Service
	public static Ok out(HttpSession ses) throws Exception
	{
		ses.invalidate(); // fire the unbound event immediately
		return Ok.OK;
	}

	@Service
	public static boolean[] signed(int[] ids, HttpSession ses) throws Exception
	{
		me(ses);
		boolean[] s = new boolean[ids.length];
		for (int i = 0; i < s.length; i++)
			s[i] = S.containsKey(ids[i]);
		return s;
	}

	/** @return PO */
	static User me(HttpSession ses) throws Exception
	{
		User u = (User)ses.getAttribute("signed");
		if (u == null)
			throw err(new ErrUnsigned("not signed in"));
		return u;
	}
}
