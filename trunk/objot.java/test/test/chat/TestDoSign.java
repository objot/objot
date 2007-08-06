package test.chat;

import objot.codec.ErrThrow;

import org.junit.Test;

import chat.model.Id;
import chat.model.User;
import chat.service.DoSign;


public class TestDoSign
	extends TestDo
{
	DoSign doSign;

	{
		doSign = container.getInstance(DoSign.class);
	}

	@Test
	public void inUp_up() throws Exception
	{
		User a = new User();
		a.name = a.password = "a";
		User aa = doSign.inUp(a);
		assertSame(a, aa);
		asser(a.id > 0);
		asserts(new Id[] { a }, a.friends);
		assertEquals(a.id, sess.me);
	}

	@Test
	public void inUp_in() throws Exception
	{
		User a = new User();
		a.name = a.password = "a";
		doSign.inUp(a);
		User aa = doSign.inUp(a);
		assertNotSame(a, aa);
		assertEquals(a.name, aa.name);
	}

	@Test(expected = ErrThrow.class)
	public void inUp_denyIn() throws Exception
	{
		User a = new User();
		a.name = a.password = "a";
		doSign.inUp(a);
		a = new User();
		a.name = "a";
		a.password = "b";
		doSign.inUp(a);
	}

	@Test
	public void inUp_up2() throws Exception
	{
		User a = new User();
		a.name = a.password = "a";
		a = doSign.inUp(a);
		User b = new User();
		b.name = b.password = "b";
		b = doSign.inUp(b);
		asser(a.id != b.id);
		data.hib.clear();
		assertEquals(3, (int)data.count(data.criteria(User.class))); // + user "admin"
	}

	@Test
	public void out() throws Exception
	{
		assertEquals(0, sess.me);
		doSign.out();
		assertEquals(0, sess.me);
		User a = new User();
		a.name = a.password = "a";
		doSign.inUp(a);
		doSign.out();
		assertEquals(0, sess.me);
	}
}
