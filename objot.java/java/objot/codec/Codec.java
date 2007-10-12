//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.codec;

import java.io.UTFDataFormatException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class Codec
{
	public static final char S = '\20'; // Ctrl-P in vim

	/**
	 * @param o the whole gettable object graph must keep unchanged since the references
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
		return new Decoder(this, for_, s).go(cla);
	}

	/** must be thread safe, will be cached */
	protected Class<?> classByName(String name) throws Exception
	{
		return Class.forName(name);
	}

	/**
	 * must be thread safe, may be cached
	 * 
	 * @param c class of the object
	 */
	protected String className(Object o, Class<?> c) throws Exception
	{
		return c.getName();
	}

	/** check long value, not too large for Javascript */
	protected long getLong(long l) throws Exception
	{
		if (l < -4503599627370496L || l > 4503599627370496L) // 2^52, for Javascript
			throw new RuntimeException("getting integer out of range " + l);
		return l;
	}

	/** {@link HashMap} by default */
	protected Map<String, Object> newMap() throws Exception
	{
		return new HashMap<String, Object>();
	}

	/** {@link HashSet} by default but not recommended for ORM */
	protected Set<Object> newSet(int len) throws Exception
	{
		return new HashSet<Object>(len);
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

	public static char[] utf(byte[] s) throws UTFDataFormatException
	{
		int ulen = s.length;
		int len = 0;
		try
		{
			for (int x = 0, u; x < ulen; x++)
				if ((u = s[x]) >= 0 || //
					(u & 0xE0) == 0xC0 && (s[++x] & 0xC0) == 0x80 || //
					(u & 0xF0) == 0xE0 && (s[++x] & 0xC0) == 0x80 && (s[++x] & 0xC0) == 0x80)
					len++;
				else
					throw new UTFDataFormatException();
		}
		catch (IndexOutOfBoundsException e)
		{
			throw new UTFDataFormatException();
		}
		char[] cs = new char[len];
		int y = 0;
		for (int x = 0, u; x < ulen; x++)
			if ((u = s[x]) >= 0)
				cs[y++] = (char)u;
			else if ((u & 0xE0) == 0xC0)
				cs[y++] = (char)((u & 0x1F) << 6 | s[++x] & 0x3F);
			else
				cs[y++] = (char)((u & 0xF) << 12 | (s[++x] & 0x3F) << 6 | s[++x] & 0x3F);
		return cs;
	}

	public static byte[] utf(CharSequence s)
	{
		char c;
		int len = s.length();
		int ulen = 0;
		for (int x = 0; x < len; x++)
			if ((c = s.charAt(x)) < 0x80)
				ulen++;
			else if (c < 0x800)
				ulen += 2;
			else
				ulen += 3;
		byte[] utf = new byte[ulen];
		int y = 0;
		for (int x = 0; x < len; x++)
			if ((c = s.charAt(x)) < 0x80)
				utf[y++] = (byte)c;
			else if (c < 0x800)
			{
				utf[y++] = (byte)(0xC0 | (c >>> 6) & 0x1F);
				utf[y++] = (byte)(0x80 | c & 0x3F);
			}
			else
			{
				utf[y++] = (byte)(0xE0 | (c >>> 12) & 0x0F);
				utf[y++] = (byte)(0x80 | (c >>> 6) & 0x3F);
				utf[y++] = (byte)(0x80 | c & 0x3F);
			}
		return utf;
	}
}
