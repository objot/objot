//
// Objot 1
//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat.service;

import java.util.Arrays;
import java.util.Comparator;

import objot.servlet.Service;
import chat.model.Chat;


public class DoChat
	extends Do
{
	/**
	 * read chats according to SO' {@link Chat#out} and {@link Chat#in} from
	 * {@link Chat#datime}(excluded), order by {@link Chat#datime} asc
	 */
	@Service
	public static Chat[] read(Chat c, Do $) throws Exception
	{
		int n = 0;
		for (Chat o: $.me.chatOuts)
			if (o.datime > c.datime && (c.in == null || o.in.id.equals(c.in.id)))
				n++;
		for (Chat i: $.me.chatIns)
			if (i.datime > c.datime && (c.out == null || i.out.id.equals(c.out.id)))
				n++;
		Chat[] s = new Chat[n];
		n = 0;
		for (Chat o: $.me.chatOuts)
			if (o.datime > c.datime && (c.in == null || o.in.id.equals(c.in.id)))
				s[n++] = o;
		for (Chat i: $.me.chatIns)
			if (i.datime > c.datime && (c.out == null || i.out.id.equals(c.out.id)))
				s[n++] = i;
		Arrays.sort(s, new Comparator<Chat>()
		{
			public int compare(Chat a, Chat b)
			{
				return a.datime < b.datime ? - 1 : a.datime == b.datime ? 0 : 1;
			}
		});
		return s;
	}

	/** @return SO with {@link Chat#datime} */
	@Service
	public static Chat post(Chat c, Do $) throws Exception
	{
		c.text = noEmpty("text", c.text, false);
		c.out = $.me;
		c.in = $.load(c.in.id);
		if (! c.in.friends.contains(c.out))
			throw err("You must be his/her friend");
		c.datime = System.currentTimeMillis();
		// SO as PO
		$.me.chatOuts.add(c);
		c.in.chatIns.add(c);
		Chat _ = new Chat();
		_.datime = c.datime;
		return _;
	}
}
