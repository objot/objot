//
// Objot 1
//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat.service;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpSession;

import objot.Err;
import objot.ErrThrow;
import chat.model.User;


public class Do
	implements Cloneable // @todo Hibernate Session
{
	/** @todo underlying Hibernate Session */
	Object $;
	protected HttpSession http;
	protected User me;

	private static final ArrayList<User> USERS_ID = new ArrayList<User>();
	private static final HashMap<String, User> USERS_NAME = new HashMap<String, User>();

	/** Hibernate Session.load, PO */
	public User load(Integer id)
	{
		if (id <= 0 || id > USERS_ID.size())
			throw new RuntimeException("user id " + id + " not found");
		return USERS_ID.get(id - 1);
	}

	/** like Hibernate Criteria.uniqueResult, PO */
	public User load(String name)
	{
		User u = USERS_NAME.get(name);
		if (u == null)
			throw new RuntimeException("user name " + name + " not found");
		return u;
	}

	/** like Hibernate Session.save, SO as PO */
	public Integer create(User u)
	{
		USERS_ID.add(u);
		USERS_NAME.put(u.name, u);
		u.id = USERS_ID.size() + 1;
		return u.id;
	}

	// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

	public static ErrThrow err(Err e)
	{
		return new ErrThrow(e);
	}

	public static ErrThrow err(String hint)
	{
		return new ErrThrow(null, hint);
	}

	public static ErrThrow err(Throwable e)
	{
		return new ErrThrow(null, e);
	}

	public static ErrThrow err(String hint, Throwable e)
	{
		return new ErrThrow(null, hint, e);
	}

	/**
	 * trimed must be not empty
	 * 
	 * @return trimed
	 */
	public static String noEmpty(String name, String s) throws Exception
	{
		if (s == null || (s = s.trim()).length() <= 0)
			throw new Exception(name + " required");
		return s;
	}

	/**
	 * original/trimed must be not empty
	 * 
	 * @return original
	 */
	public static String noEmpty(String name, String s, boolean trim) throws Exception
	{
		if (s == null || (trim ? s.trim() : s).length() <= 0)
			throw new Exception(name + " required");
		return s;
	}
}
