//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU General Public License version 2
//
package objot.util;

import java.io.UTFDataFormatException;


public class String2
{
	protected String2()
	{
		throw new AbstractMethodError();
	}

	public static boolean empty(String s)
	{
		return s == null || s.length() == 0;
	}

	/** @return the string, or "" if the string is null */
	public static String maskNull(String x)
	{
		return x == null ? "" : x;
	}

	/** @return the string, or null if the string is "" */
	public static String unmaskNull(String x)
	{
		return x == null || x.length() == 0 ? null : x;
	}

	/** @return the string, or <code>mask</code> if the string is null or empty */
	public static String maskEmpty(String x, String mask)
	{
		return x == null || x.length() == 0 ? maskNull(mask) : x;
	}

	/** @return the string, or "" if the string is null or mask */
	public static String unmaskEmpty(String x, String mask)
	{
		return x == null || maskNull(mask).equals(x) ? "" : x;
	}

	/** @return {@link String#indexOf(int, int)} if found, or length if not found */
	public static int index(String s, char sub, int begin)
	{
		int x = s.indexOf(sub, begin);
		return x >= 0 ? x : s.length();
	}

	/** @return {@link String#indexOf(String, int)} if found, or length if not found */
	public static int index(String s, String sub, int begin)
	{
		int x = s.indexOf(sub, begin);
		return x >= 0 ? x : s.length();
	}

	/** @return {@link String#indexOf(int, int)} + 1 if found, or length if not found */
	public static int indexAfter(String s, char sub, int begin)
	{
		int x = s.indexOf(sub, begin);
		return x >= 0 ? x + 1 : s.length();
	}

	/**
	 * @return {@link String#indexOf(String, int)} + sub length if found, or length if not
	 *         found
	 */
	public static int indexAfter(String s, String sub, int begin)
	{
		int x = s.indexOf(sub, begin);
		return x >= 0 ? x + sub.length() : s.length();
	}

	public static String sub(String s, int begin)
	{
		return s.substring(Math2.bound(begin, 0, s.length()));
	}

	public static String sub(String s, int begin, int end1)
	{
		end1 = Math2.bound(end1, 0, s.length());
		begin = Math2.bound(begin, 0, end1);
		return s.substring(begin, end1);
	}

	/** @return substring ended before the delimiter */
	public static String sub(String s, char deli, int begin)
	{
		return sub(s, begin, index(s, deli, begin));
	}

	/** @return substring ended before the delimiter */
	public static String sub(String s, String deli, int begin)
	{
		return sub(s, begin, index(s, deli, begin));
	}

	/** @return {@link String#indexOf(int, int)} if found, or length if not found */
	public static int lastIndex(String s, char sub)
	{
		int x = s.lastIndexOf(sub);
		return x >= 0 ? x : s.length();
	}

	/** @return {@link String#indexOf(String, int)} if found, or length if not found */
	public static int lastIndex(String s, String sub)
	{
		int x = s.lastIndexOf(sub);
		return x >= 0 ? x : s.length();
	}

	/** @return substring ended before the last delimiter */
	public static String lastSub(String s, char deli, int begin)
	{
		return sub(s, begin, lastIndex(s, deli));
	}

	/** @return substring ended before the last delimiter */
	public static String lastSub(String s, String deli, int begin)
	{
		return sub(s, begin, lastIndex(s, deli));
	}

	public static char[] utf(byte[] s) throws UTFDataFormatException
	{
		return utf(s, 0, s.length);
	}

	public static char[] utf(byte[] s, int begin, int end1) throws UTFDataFormatException
	{
		Math2.range(begin, end1, s.length);
		int len = 0;
		try
		{
			for (int x = begin, u; x < end1; x++)
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
		for (int x = begin, u; x < end1; x++)
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
		return utf(s, 0);
	}

	public static byte[] utf(CharSequence s, int prefixByteN)
	{
		char c;
		int len = s.length();
		int ulen = prefixByteN;
		for (int x = 0; x < len; x++)
			if ((c = s.charAt(x)) < 0x80)
				ulen++;
			else if (c < 0x800)
				ulen += 2;
			else
				ulen += 3;
		byte[] utf = new byte[ulen];
		int y = prefixByteN;
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
