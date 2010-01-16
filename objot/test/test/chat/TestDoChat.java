//
// Copyright 2007-2010 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package test.chat;

import java.util.Date;
import java.util.List;

import objot.util.ErrThrow;

import org.junit.Test;

import chat.model.Chat;
import chat.model.User;
import chat.service.DoChat;


public class TestDoChat
	extends TestDo
{
	static final Date DATE0 = new Date(0);

	DoChat doChat = container.get(DoChat.class);
	User u1;
	User u2;

	void signIn() throws Exception
	{
		User u = new User();
		u.name = u.password = "b";
		u2 = doChat.doSign.inUp(u);
		u = new User();
		u.name = u.password = "a";
		doChat.doSign.inUp(u);
		u1 = doChat.doUser.me();
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
		doChat.post(c, null);
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
		doChat.post(c, null);
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
		doChat.post(c, null);
	}

	@Test
	public void read_meToMe() throws Exception
	{
		signIn();
		Chat c = new Chat();
		c.in = u1;
		c.text = clob("post1");
		c.datime = DATE0;
		doChat.post(c, null);
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
		doChat.post(c, null);
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
		doChat.post(c, null);
		doChat.doSign.inUp(u2);
		c.in = u1;
		c.text = clob("post1");
		doChat.post(c, null);

		Chat cc = new Chat();
		cc.in = u1;
		cc.datime = c.datime;
		assertEquals(0, doChat.read(cc).size());
		cc.datime = new Date(c.datime.getTime() - 1);
		asserts(BAG, new Chat[] { c }, doChat.read(cc));
	}
}
