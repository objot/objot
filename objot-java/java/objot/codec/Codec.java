//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Library General Public License version 2
//
package objot.codec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;


public class Codec
{
	public static final char S = '\20'; // Ctrl-P in vim

	/**
	 * @param o the whole encoded object graph must keep unchanged since the references
	 *            detection is not thread safe
	 * @param for_ null is Object.class
	 */
	public StringBuilder enc(Object o, Class<?> for_) throws Exception
	{
		return new Encoder(this, for_).go(o);
	}

	/**
	 * @param cla null is Object.class
	 * @param for_ null is Object.class
	 */
	public Object dec(char[] s, Class<?> cla, Class<?> for_) throws Exception
	{
		return new Decoder(this, for_, s, 0, s.length).go(cla);
	}

	/**
	 * @param cla null is Object.class
	 * @param for_ null is Object.class
	 */
	public Object dec(char[] s, int sBegin, int sEnd1, Class<?> cla, Class<?> for_)
		throws Exception
	{
		return new Decoder(this, for_, s, sBegin, sEnd1).go(cla);
	}

	/**
	 * Get object or class by name, must be thread safe, {@link HashMap} for "" by default
	 * 
	 * @param name may be ""
	 * @return class for creating, otherwise for reusing
	 */
	protected Object byName(String name) throws Exception
	{
		if (name.length() == 0)
			return HashMap.class;
		return Class.forName(name);
	}

	/**
	 * Get object or class name, must be thread safe, "" for {@link HashMap} by default
	 * 
	 * @param o may be null
	 * @param c the object class
	 * @return could be ""
	 */
	protected String name(Object o, Class<?> c) throws Exception
	{
		if (c == HashMap.class)
			return "";
		return c.getName();
	}

	/** If use array to present list when list type is undetermined, true by default */
	protected boolean arrayForList() throws Exception
	{
		return true;
	}

	/**
	 * by default, {@link ArrayList} for list and object, otherwise {@link HashSet} which
	 * is not recommended for ORM
	 * 
	 * @param c collection class
	 */
	protected Collection<Object> newList(Class<?> c, int len) throws Exception
	{
		return c.isAssignableFrom(ArrayList.class) ? new ArrayList<Object>(len)
			: new HashSet<Object>(len);
	}

	/** check long value, not too large for Javascript */
	protected long getLong(long l) throws Exception
	{
		if (l < -4503599627370496L || l > 4503599627370496L) // 2^52, for Javascript
			throw new RuntimeException("getting integer out of range " + l);
		return l;
	}

	private final ConcurrentHashMap<Class<?>, Clazz> clas //
	= new ConcurrentHashMap<Class<?>, Clazz>(64, 0.8f, 32);

	final Clazz clazz(Class<?> c) throws Exception
	{
		Clazz z = clas.get(c);
		if (z == null)
			clas.put(c, z = Clazz.clazz(c));
		return z;
	}
}
