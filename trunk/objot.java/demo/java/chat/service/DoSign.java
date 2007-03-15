package chat.service;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpSession;

import objot.servlet.Service;
import chat.model.Ok;
import chat.model.User;


public class DoSign
	extends DoService
{
	/** {@link User#id} as key, SO as key for sign out, PO in session */
	public static final HashMap<Integer, Object> S = new HashMap<Integer, Object>();

	@Service
	public static Ok inUp(User u_, HttpSession ses) throws Exception
	{
		u_.name = noEmpty("name", u_.name);
		u_.password = noEmpty("password", u_.password, false);
		User u = User.NAMES.get(u_.name); // PO
		// sign up
		if (u == null)
		{
			u = u_; // SO as PO
			User.IDS.add(u);
			u.id = User.IDS.size();
			u.friends = new ArrayList<User>();
			User.NAMES.put(u.name, u);
		}
		// sign in
		else if (! u.password.equals(u_.password))
			throw err("user name or password incorrect");
		else if (S.get(u.id) != null)
			throw err("user already signed in");
		ses.setAttribute("signed", u);
		S.put(u.id, null);
		return Ok.OK;
	}

	@Service
	public static Ok out(Void Void, HttpSession ses) throws Exception
	{
		ses.invalidate(); // fire the unbound event immediately
		return Ok.OK;
	}

	@Service
	public static boolean[] signed(int[] ids, HttpSession ses) throws Exception
	{
		self(ses);
		boolean[] s = new boolean[ids.length];
		for (int i = 0; i < s.length; i++)
			s[i] = S.containsKey(ids[i]);
		return s;
	}

	/** @return PO */
	static User self(HttpSession ses) throws Exception
	{
		User u = (User)ses.getAttribute("signed");
		if (u == null)
			throw err("not signed in");
		return u;
	}
}
