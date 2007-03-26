//
// Objot 1
//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat.model;

import java.util.List;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import objot.Get;
import objot.GetSet;
import objot.NameGet;
import objot.Set;
import chat.service.DoChat;
import chat.service.DoSign;
import chat.service.DoUser;
import chat.service.Servlet;


public class User
	implements Cloneable // different instances for PO and SO, no need for Hibernate
	, HttpSessionBindingListener // only for servlet session expiration
{
	// @Id, use Integer just for less object creation in Hibernate
	@GetSet(Object.class)
	public Integer id;

	@Get( { DoUser.class, DoChat.class })
	@Set( { DoSign.class, DoUser.class })
	public String name;

	@Set(DoSign.class)
	public String password;

	// @ManyToMany
	public List<User> friends;

	// @Transient for service
	@GetSet(DoUser.class)
	@NameGet("friends")
	public List<User> myFriends;

	// @OneToMany
	public List<Chat> chatOuts;
	// @OneToMany
	public List<Chat> chatIns;

	/**
	 * no need for Hibernate
	 * 
	 * @return a new SO with {@link #myFriends}
	 */
	@Override
	public User clone()
	{
		try
		{
			User u = (User)super.clone();
			u.myFriends = u.friends;
			return u;
		}
		catch (CloneNotSupportedException e) // never happen
		{
			throw new InternalError();
		}
	}

	/** for signed out */
	public void valueUnbound(HttpSessionBindingEvent e)
	{
		synchronized (Servlet.class) // for transaction
		{
			DoSign.S.remove(id);
		}
	}

	public void valueBound(HttpSessionBindingEvent e)
	{
	}
}
