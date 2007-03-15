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
	public static ArrayList<Chat> read(Chat c_, HttpSession ses) throws Exception
	{
		User self = DoSign.self(ses);
		ArrayList<Chat> s = new ArrayList<Chat>();
		if (c_.out.id == self.id)
		{
			for (Chat c: self.chatOuts)
				if (c.datime >= c_.datime && c.in.id == c_.in.id)
					s.add(c);
		}
		else if (c_.in.id == self.id)
		{
			for (Chat c: self.chatIns)
				if (c.datime >= c_.datime && c.out.id == c_.out.id)
					s.add(c);
		}
		else
			throw err("read others chats forbidden");
		return s;
	}

	@Service
	public static Chat post(Chat c, HttpSession ses) throws Exception
	{
		c.text = noEmpty("text", c.text, false);
		User self = DoSign.self(ses);
		c.out = self;
		c.in = User.IDS.get(c.in.id);
		c.datime = System.currentTimeMillis();
		// SO as PO
		self.chatOuts.add(c);
		c.in.chatIns.add(c);
		return c;
	}
}
