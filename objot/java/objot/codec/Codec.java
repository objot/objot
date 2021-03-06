//
// Copyright 2007-2015 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.codec;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import objot.util.Array2;
import objot.util.Chars;
import objot.util.Class2;
import objot.util.Mod2;


public class Codec
{
	public static final char S = '\20'; // Ctrl-P in vim
	private final ConcurrentHashMap<Class<?>, Clazz> clazzs;

	public Codec()
	{
		clazzs = new ConcurrentHashMap<Class<?>, Clazz>(64, 0.8f, 32);
	}

	/** keep same class analysis with another codec */
	public Codec(Codec sameClazz)
	{
		clazzs = sameClazz.clazzs;
	}

	/**
	 * @param o the whole encoded data graph must keep unchanged since the references
	 *            detection is not thread safe
	 */
	public StringBuilder enc(Object o, Object ruleKey) throws Exception
	{
		return new Encoder(this, ruleKey, false, null, null).go(o);
	}

	/**
	 * @param o the whole encoded data graph must keep unchanged since the references
	 *            detection is not thread safe
	 */
	public StringBuilder enc(Object o, Object ruleKey, StringBuilder s) throws Exception
	{
		return new Encoder(this, ruleKey, false, s, null).go(o);
	}

	/**
	 * @param o the whole encoded data graph must keep unchanged since the references
	 *            detection is not thread safe
	 */
	public Chars encFast(Object o, Object ruleKey) throws Exception
	{
		return new Encoder(this, ruleKey, true, null, null).goFast(o);
	}

	/**
	 * @param o the whole encoded data graph must keep unchanged since the references
	 *            detection is not thread safe
	 */
	public Chars encFast(Object o, Object ruleKey, Chars s) throws Exception
	{
		return new Encoder(this, ruleKey, true, null, s).goFast(o);
	}

	/**
	 * @param cla null is Object.class
	 * @param ruleKey null is Object.class
	 */
	public <T>T dec(char[] s, Class<T> cla, Object ruleKey) throws Exception
	{
		return new Decoder(this, ruleKey, s, 0, s.length).go(cla);
	}

	/**
	 * @param cla null is Object.class
	 * @param ruleKey null is Object.class
	 */
	public <T>T dec(char[] s, int sBegin, int sEnd1, Class<T> cla, Object ruleKey)
		throws Exception
	{
		return new Decoder(this, ruleKey, s, sBegin, sEnd1).go(cla);
	}

	/**
	 * @param cla null is Object.class
	 * @param ruleKey null is Object.class
	 */
	public <T>T decFast(char[] s, Class<T> cla, Object ruleKey) throws Exception
	{
		return new Decoder(this, ruleKey, s, 0, s.length).goFast(cla);
	}

	/**
	 * @param cla null is Object.class
	 * @param ruleKey null is Object.class
	 */
	public <T>T decFast(char[] s, int sBegin, int sEnd1, Class<T> cla, Object ruleKey)
		throws Exception
	{
		return new Decoder(this, ruleKey, s, sBegin, sEnd1).goFast(cla);
	}

	/**
	 * Get object or class by name, must be thread safe, {@link HashMap} for "" by default
	 * 
	 * @param name may be ""
	 * @param ruleKey not null
	 * @return object, or class which nullary constructor will be called
	 */
	protected Object byName(String name, Object ruleKey) throws Exception
	{
		if (name.length() == 0)
			return HashMap.class;
		return Class.forName(name);
	}

	/**
	 * Get object or class name, must be thread safe, "" for {@link HashMap} by default
	 * 
	 * @param c the object class
	 * @param ruleKey not null
	 * @return could be ""
	 */
	protected String name(Object o, Class<?> c, Object ruleKey) throws Exception
	{
		if (o instanceof HashMap)
			return "";
		return c.getName();
	}

	/**
	 * check long value (and datime milliseconds), not too large for Javascript and
	 * Actionscript
	 */
	protected long beLong(long l) throws Exception
	{
		if (l < -9007199254740992L || l > 9007199254740991L) // 53bit, for Javascript
			throw new RuntimeException("getting integer out of range " + l);
		return l;
	}

	/**
	 * use Integer, Long, Double or Float if expected non-primitive class is Number or
	 * Object while decoding a number, null by default which prefer Integer to Long and to
	 * Double
	 */
	protected Class<? extends Number> numCla;

	/** If use array to present list when list type is undetermined, true by default */
	protected boolean arrayForList = true;

	/**
	 * by default, {@link Collection} for list and object
	 * 
	 * @param c collection class, not element class
	 */
	protected Collection<Object> newList(Class<?> c, int len) throws Exception
	{
		return Set.class.isAssignableFrom(c) ? new HashSet<Object>(len)
			: new ArrayList<Object>(len);
	}

	/**
	 * called if the property not found on the object or can't decode it
	 * 
	 * @throws RuntimeException by default
	 */
	protected void undecodable(Object o, String prop, Object ruleKey) throws Exception
	{
		throw new RuntimeException(o.getClass().getName() + "." + prop
			+ " not found or not decodable");
	}

	/** pick up the undecoded property if {@link #undecodable} doesn't throw */
	protected void undecodeValue(Object o, String prop, Object ruleKey, Object value)
		throws Exception
	{
	}

	/**
	 * analyze a class
	 * 
	 * @return the return value of {@link #getClazz} or
	 *         {@link #addClazz(Class, Property[], Property[], Map)} or {
	 *         {@link #addClazz(Class, Map, Map, Map)}
	 */
	public Clazz clazz(Class<?> c) throws Exception
	{
		Clazz z = getClazz(c);
		if (z != null)
			return z;
		HashMap<String, Property> es_ = new HashMap<String, Property>();
		HashMap<String, Property> ds_ = new HashMap<String, Property>();
		for (Field f: Class2.fields(c, 0, 0, 0))
		{
			Enc e = f.getAnnotation(Enc.class);
			Dec d = f.getAnnotation(Dec.class);
			EncDec gs = f.getAnnotation(EncDec.class);
			if ((e != null || d != null || gs != null)
				&& Mod2.match(f, Mod2.STATIC | Mod2.PRIVATE))
				throw new IllegalArgumentException("encoding/decoding " + Mod2.toString(f)
					+ f + " forbidden");
			if (e != null || gs != null)
				new PropertyAnno(f, e, null, gs, true).into(es_);
			if (d != null || gs != null)
				new PropertyAnno(f, null, d, gs, false).into(ds_);
		}
		for (Method m: Class2.methods(c, 0, 0, 0))
		{
			Enc e = m.getAnnotation(Enc.class);
			Dec d = m.getAnnotation(Dec.class);
			if ((e != null || d != null) && Mod2.match(m, Mod2.STATIC | Mod2.PRIVATE))
				throw new IllegalArgumentException("encoding/decoding " + Mod2.toString(m)
					+ m + " forbidden");
			if (e != null)
				new PropertyAnno(m, e, null, null, true).into(es_);
			if (d != null)
				new PropertyAnno(m, null, d, null, false).into(ds_);
		}
		return addClazz(c, es_, ds_, ds_);
	}

	/** @return the info of the analyzed class, or null if not analyzed */
	protected final Clazz getClazz(Class<?> c)
	{
		return clazzs.get(c);
	}

	/** @return the info of the class being analyzed */
	protected final Clazz addClazz(Class<?> c, Property[] encs, Property[] decs,
		Map<String, Property> decNames) throws Exception
	{
		for (int i = 0; i < decs.length; i++)
		{
			decs[i].index = i;
			decs[i].clob = Clob.class.isAssignableFrom(decs[i].cla);
		}
		if (decNames == null)
		{
			decNames = new HashMap<String, Property>(decs.length);
			for (Property d: decs)
				decNames.put(d.name, d);
		}
		Clazz z = Clazz.make(this, c, encs, decs, decNames);
		clazzs.put(c, z);
		return z;
	}

	/** @return the info of the class being analyzed */
	protected final Clazz addClazz(Class<?> c, Map<?, Property> encs, Map<?, Property> decs,
		Map<String, Property> decNames) throws Exception
	{
		return addClazz(c, Array2.from(encs.values(), Property.class), //
			Array2.from(decs.values(), Property.class), decNames);
	}
}
