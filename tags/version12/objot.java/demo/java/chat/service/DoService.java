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
	public static Exception err(String hint)
	{
		return new Exception(hint);
	}

	public static Exception err(Throwable e)
	{
		return new Exception(e);
	}

	public static Exception err(String hint, Throwable e)
	{
		return new Exception(hint, e);
	}

	public static Exception err(Err e)
	{
		return new ErrThrow(e);
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
