//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package test.chat;

import java.util.Date;
import java.util.List;

import objot.codec.ErrThrow;

import org.junit.Test;

import chat.model.Chat;
import chat.model.User;
import chat.service.DoChat;


public class TestDoChat
	extends TestDo
{
	static final Date DATE0 = new Date(0);
	DoChat doChat;
	User u1;
	User u2;

	{
		doChat = container.getInstance(DoChat.class);
	}

	void signIn() throws Exception
	{
		User u = new User();
		u.name = u.password = "b";
		u2 = doChat.doSign.inUp(u);
		// just for sub requests, since PersistentBag/List/Set won't be evicted
		u2.friends = copy(u2.friends);
		u = new User();
		u.name = u.password = "a";
		doChat.doSign.inUp(u);
		u1 = doChat.doUser.me();
		// just for sub requests, since PersistentBag/List/Set won't be evicted
		u1.friends = copy(u1.friends);
		u1.friends_ = null;
	}

	@Test
	public void post_meToMe() throws Exception
	{
		signIn();
		Chat c = new Chat();
		c.in = u1;
		c.text = clob("post1");
		c.datime = DATE0;
		long time = System.currentTimeMillis();
		doChat.post(c);
		asser(c.id > 0);
		asser(c.datime.getTime() >= time);
		asser(c.datime.getTime() < time + 2000);
	}

	@Test(expected = ErrThrow.class)
	public void post_notFriend() throws Exception
	{
		signIn();
		Chat c = new Chat();
		c.in = u2;
		c.text = clob("post1");
		c.datime = DATE0;
		doChat.post(c);
	}

	@Test
	public void post_fromFriend() throws Exception
	{
		signIn();
		(u2.friends_ = u2.friends).add(u1);
		doChat.doSign.inUp(u2);
		doChat.doUser.update(u2);
		(u1.friends_ = u1.friends).add(u2);
		doChat.doSign.inUp(u1);
		doChat.doUser.update(u1);
		Chat c = new Chat();
		c.in = u2;
		c.text = clob("post1");
		c.datime = DATE0;
		doChat.post(c);
	}

	@Test
	public void read_meToMe() throws Exception
	{
		signIn();
		Chat c = new Chat();
		c.in = u1;
		c.text = clob("post1");
		c.datime = DATE0;
		doChat.post(c);
		Chat cc = new Chat();
		cc.in = u1;
		List<Chat> s = doChat.read(cc);
		asserts(BAG, new Chat[] { c }, s);
		cc = s.get(0);
		assertEquals(c.in, cc.in);
		assertEquals(c.out, cc.out);
		assertEquals(c.datime, cc.datime);
		assertEquals(string(c.text), string(cc.text));
	}

	@Test
	public void read_meToMe_datime() throws Exception
	{
		signIn();
		Chat c = new Chat();
		c.in = u1;
		c.text = clob("post1");
		c.datime = DATE0;
		doChat.post(c);
		Chat cc = new Chat();
		cc.in = u1;
		cc.datime = c.datime;
		assertEquals(0, doChat.read(cc).size());
		cc.datime = new Date(c.datime.getTime() - 1);
		asserts(BAG, new Chat[] { c }, doChat.read(cc));
	}

	@Test
	public void read_eachOther() throws Exception
	{
		signIn();
		(u2.friends_ = u2.friends).add(u1);
		doChat.doSign.inUp(u2);
		doChat.doUser.update(u2);
		(u1.friends_ = u1.friends).add(u2);
		doChat.doSign.inUp(u1);
		doChat.doUser.update(u1);

		Chat c = new Chat();
		c.in = u2;
		c.text = clob("post1");
		c.datime = DATE0;
		doChat.post(c);
		doChat.doSign.inUp(u2);
		c.in = u1;
		c.text = clob("post1");
		doChat.post(c);

		Chat cc = new Chat();
		cc.in = u1;
		cc.datime = c.datime;
		assertEquals(0, doChat.read(cc).size());
		cc.datime = new Date(c.datime.getTime() - 1);
		asserts(BAG, new Chat[] { c }, doChat.read(cc));
	}
}
