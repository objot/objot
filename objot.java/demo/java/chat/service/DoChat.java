//
// Objot 1
//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat.service;

import java.util.ArrayList;

import javax.servlet.http.HttpSession;

import objot.servlet.Service;
import chat.model.Chat;
import chat.model.User;


public class DoChat
	extends DoService
{
	/**
	 * read chats according to SO' {@link Chat#out} and {@link Chat#in} from
	 * {@link Chat#datime}, no sort
	 */
	@Service
	public static ArrayList<Chat> read(Chat c, HttpSession ses) throws Exception
	{
		User me = DoSign.me(ses);
		ArrayList<Chat> s = new ArrayList<Chat>();
		for (Chat o: me.chatOuts)
			if (o.datime >= c.datime && (c.in.id != null && (int)o.in.id == (int)c.in.id))
				s.add(o);
		for (Chat i: me.chatIns)
			if (i.datime >= c.datime && (c.out.id != null && (int)i.out.id == (int)c.out.id))
				s.add(i);
		return s;
	}

	/** @return SO with {@link Chat#datime} */
	@Service
	public static Chat post(Chat c, HttpSession ses) throws Exception
	{
		c.text = noEmpty("text", c.text, false);
		User me = DoSign.me(ses);
		c.out = me;
		c.in = User.IDS.get(c.in.id - 1);
		if (! c.in.friends.contains(c.out))
			throw err("You must be his/her friend");
		c.datime = System.currentTimeMillis();
		// SO as PO
		me.chatOuts.add(c);
		c.in.chatIns.add(c);
		Chat _ = new Chat();
		_.datime = c.datime;
		return _;
	}
}
