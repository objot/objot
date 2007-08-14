package test.chat;

import org.hibernate.HibernateException;
import org.junit.Test;

import chat.model.User;
import chat.service.DoUser;


public class TestDoUser
	extends TestDo
{
	DoUser doUser;
	User u1;
	User u2;

	{
		doUser = container.getInstance(DoUser.class);
	}

	void signIn() throws Exception
	{
		User u = new User();
		u.name = u.password = "b";
		u2 = doUser.doSign.inUp(u);
		// just for sub requests, since PersistentBag/List/Set won't be evicted
		u2.friends = copy(u2.friends);
		u = new User();
		u.name = u.password = "a";
		doUser.doSign.inUp(u);
		u1 = doUser.doUser.me();
		// just for sub requests, since PersistentBag/List/Set won't be evicted
		u1.friends = copy(u1.friends);
		u1.friends_ = null;
	}

	@Test
	public void me() throws Exception
	{
		signIn();
		assertEquals(sess.me, u1.id);
		assertEquals("a", u1.name);
	}

	@Test
	public void update() throws Exception
	{
		signIn();
		(u1.friends_ = u1.friends).add(u2);
		doUser.update(u1);
		asserts(new User[] { u1, u2 }, doUser.me().friends);
		(u1.friends_ = u1.friends).remove(u1);
		doUser.update(u1);
		asserts(new User[] { u2 }, doUser.me().friends);
	}

	@Test(expected = HibernateException.class)
	public void update_existId() throws Exception
	{
		signIn();
		(u1.friends_ = u1.friends).add(u2);
		doUser.update(u1);
		User x = new User();
		x.id = ((User)u1.friends.toArray()[u1.friends.size() - 1]).id;
		u1.friends_.add(x); // aready exist id
		doUser.update(u1);
	}

	@Test
	public void get() throws Exception
	{
		signIn();
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
