package chat.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import objot.Get;
import objot.GetSet;
import objot.Name;
import objot.Set;
import chat.service.DoSign;
import chat.service.DoUser;
import chat.service.Servlet;


@Get(DoUser.class)
public class User
	implements Cloneable // different instances for PO and SO
	, HttpSessionBindingListener // only for servlet session expiration
{
	// @Id, use Integer just for less object creation in Hibernate
	@GetSet(Object.class)
	public Integer id;

	@Get
	@Set(DoSign.class)
	public String name;

	@Set(DoSign.class)
	public String password;

	// @ManyToMany
	@Set(DoUser.class)
	public List<User> friends;

	// @Transient for service
	@Get
	@Name("friends")
	public List<User> friendsSelf;

	/** @return a new SO with {@link #friendsSelf} */
	@Override
	public User clone() throws CloneNotSupportedException
	{
		User u = (User)super.clone();
		u.friendsSelf = u.friends;
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
