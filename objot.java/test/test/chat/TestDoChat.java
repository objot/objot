package test.chat;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import objot.codec.ErrThrow;

import org.junit.Test;

import chat.model.Chat;
import chat.model.User;
import chat.service.DoChat;


public class TestDoChat
	extends TestDo
{
	DoChat doChat;
	User me;
	User it;
	static final Date DATE0 = new Date(0);

	{
		doChat = container.getInstance(DoChat.class);
	}

	void signIn() throws Exception
	{
		User u = new User();
		u.name = u.password = "b";
		it = doChat.doSign.inUp(u);
		// just for sub requests, since PersistentBag/List/Set won't be evicted
		it.friends = new HashSet<User>(it.friends);
		u = new User();
		u.name = u.password = "a";
		doChat.doSign.inUp(u);
		me = doChat.doUser.me();
		// just for sub requests, since PersistentBag/List/Set won't be evicted
		me.friends = new HashSet<User>(me.friends);
		me.friends_ = null;
	}

	@Test
	public void post_meToMe() throws Exception
	{
		signIn();
		Chat c = new Chat();
		c.in = me;
		c.text = "post1";
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
		c.in = it;
		c.text = "post1";
		c.datime = DATE0;
		doChat.post(c);
	}

	@Test
	public void post_fromFriend() throws Exception
	{
		signIn();
		(it.friends_ = copy(it.friends)).add(me);
		doChat.doSign.inUp(it);
		doChat.doUser.update(it);
		(me.friends_ = copy(me.friends)).add(it);
		doChat.doSign.inUp(me);
		doChat.doUser.update(me);
		Chat c = new Chat();
		c.in = it;
		c.text = "post1";
		c.datime = DATE0;
		doChat.post(c);
	}

	@Test
	public void read_meToMe() throws Exception
	{
		signIn();
		Chat c = new Chat();
		c.in = me;
		c.text = "post1";
		c.datime = DATE0;
		doChat.post(c);
		Chat cc = new Chat();
		cc.in = me;
		List<Chat> s = doChat.read(cc);
		asserts(BAG, new Chat[] { c }, s);
		cc = s.get(0);
		assertEquals(c.in, cc.in);
		assertEquals(c.out, cc.out);
		assertEquals(c.datime, cc.datime);
		assertEquals(c.text, cc.text);
	}

	@Test
	public void read_meToMe_datime() throws Exception
	{
		signIn();
		Chat c = new Chat();
		c.in = me;
		c.text = "post1";
		c.datime = DATE0;
		doChat.post(c);
		Chat cc = new Chat();
		cc.in = me;
		cc.datime = c.datime;
		assertEquals(0, doChat.read(cc).size());
		cc.datime = new Date(c.datime.getTime() - 1);
		asserts(BAG, new Chat[] { c }, doChat.read(cc));
	}

	@Test
	public void read_eachOther() throws Exception
	{
		signIn();
		(it.friends_ = copy(it.friends)).add(me);
		doChat.doSign.inUp(it);
		doChat.doUser.update(it);
		(me.friends_ = copy(me.friends)).add(it);
		doChat.doSign.inUp(me);
		doChat.doUser.update(me);

		Chat c = new Chat();
		c.in = it;
		c.text = "post1";
		c.datime = DATE0;
		doChat.post(c);
		doChat.doSign.inUp(it);
		c.in = me;
		c.text = "post2";
		doChat.post(c);

		Chat cc = new Chat();
		cc.in = me;
		cc.datime = c.datime;
		assertEquals(0, doChat.read(cc).size());
		cc.datime = new Date(c.datime.getTime() - 1);
		asserts(BAG, new Chat[] { c }, doChat.read(cc));
	}
}
