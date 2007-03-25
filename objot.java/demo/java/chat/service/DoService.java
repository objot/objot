//
// Objot 1
//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat.service;

import objot.Err;
import objot.ErrThrow;


public abstract class DoService
{
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
