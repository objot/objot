package test.chat;

import org.hibernate.HibernateException;
import org.junit.Test;

import chat.model.User;
import chat.service.DoUser;


public class TestDoUser
	extends TestDo
{
	DoUser doUser;
	User me;

	{
		doUser = container.getInstance(DoUser.class);
	}

	void signIn() throws Exception
	{
		User a = new User();
		a.name = a.password = "a";
		doUser.doSign.inUp(a);
		me = doUser.me();
		data.evict(me.friends);
		data.fetch(me.friends);
		me.friends_ = null;
	}

	@Test
	public void me() throws Exception
	{
		signIn();
		assertEquals(sess.me, me.id);
		assertEquals("a", me.name);
	}

	@Test
	public void update() throws Exception
	{
		signIn();
		User b = new User();
		b.name = b.password = "b";
		(me.friends_ = copy(me.friends)).add(doUser.doSign.inUp(b));
		doUser.update(me);
		assertEquals(2, doUser.me().friends.size());
		asserts(me.friends_, doUser.me().friends);
	}

	@Test(expected = HibernateException.class)
	public void update_existId() throws Exception
	{
		signIn();
		User b = new User();
		b.name = b.password = "b";
		(me.friends_ = copy(me.friends)).add(doUser.doSign.inUp(b));
		doUser.update(me);
		b = new User();
		b.id = ((User)me.friends.toArray()[me.friends.size() - 1]).id;
		me.friends_.add(b); // aready exist id
		doUser.update(me);
	}

	@Test
	public void get() throws Exception
	{
		signIn();
		User b = new User();
		b.name = b.password = "b";
		doUser.doSign.inUp(b);
		User[] s = new User[] { new User(), new User(), new User() };
		s[1].id = sess.me;
		s[2].name = "b";
		User[] ss = doUser.get(s.clone());
		assertNotSame(s, ss);
		assertNull(ss[0]);
		assertNotSame(s[1], ss[1]);
		assertEquals(s[1].id, ss[1].id);
		assertNotSame(s[2], ss[2]);
		assertEquals(s[2].name, ss[2].name);
	}
}
