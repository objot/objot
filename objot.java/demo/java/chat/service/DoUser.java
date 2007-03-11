package chat.service;

import java.util.ListIterator;

import javax.servlet.http.HttpSession;

import objot.servlet.Service;
import chat.model.Ok;
import chat.model.User;


public class DoUser
	extends DoService
{
	/** @return self PO */
	@Service
	public static User self(Void Void, HttpSession ses) throws Exception
	{
		return DoSign.self(ses).clone();
	}

	/** @return PO by {@link User#name} */
	@Service
	public static User[] getByName(String[] names, HttpSession ses) throws Exception
	{
		DoSign.self(ses);
		User[] s = new User[names.length];
		for (int i = 0; i < names.length; i++)
			s[i] = User.NAMES.get(names[i]);
		return s;
	}

	/** update {@link User#friends} if SO' is not null */
	@Service
	public static Ok update(User u, HttpSession ses) throws Exception
	{
		User self = DoSign.self(ses);
		if (u.friends != null)
		{
			for (ListIterator<User> i = u.friends.listIterator(); i.hasNext();)
				i.set(User.IDS.get(i.next().id - 1));
			self.friends = u.friends;
		}
		return Ok.OK;
	}
}
