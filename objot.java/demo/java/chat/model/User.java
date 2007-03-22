//
// Objot 1
//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat.model;

import java.util.ArrayList;
import java.util.HashMap;
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
	implements Cloneable // different instances for PO and SO
	, HttpSessionBindingListener // only for servlet session expiration
{
	// @Id, use Integer just for less object creation in Hibernate
	@GetSet(Object.class)
	public Integer id;

	@Get( { DoUser.class, DoChat.class })
	@Set(DoSign.class)
	public String name;

	@Set(DoSign.class)
	public String password;

	// @ManyToMany
	public List<User> friends;

	// @Transient for service
	@GetSet(DoUser.class)
	@NameGet("friends")
	public List<User> myFriends;

	/** @return a new SO with {@link #myFriends} */
	@Override
	public User clone() throws CloneNotSupportedException
	{
		User u = (User)super.clone();
		u.myFriends = u.friends;
		return u;
	}

	/** index is {@link #id} - 1 */
	public static final ArrayList<User> IDS = new ArrayList<User>();
	public static final HashMap<String, User> NAMES = new HashMap<String, User>();

	public List<Chat> chatOuts;
	public List<Chat> chatIns;

	/** for signed out */
	public void valueUnbound(HttpSessionBindingEvent e)
	{
		synchronized (Servlet.class)
		{
			DoSign.S.remove(id);
		}
	}

	public void valueBound(HttpSessionBindingEvent e)
	{
	}
}
