//
// Copyright 2007-2010 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.util;

public class Chars
	implements CharSequence
{
	public char[] chars;
	public int beginI;
	public int end1I;

	public Chars()
	{
		chars = Array2.CHARS0;
	}

	public Chars(char[] s)
	{
		this(s, 0, s != null ? s.length : 0);
	}

	public Chars(char[] s, int begin, int end1)
	{
		chars = s != null ? s : Array2.CHARS0;
		Math2.range(begin, end1, chars.length);
		beginI = begin;
		end1I = end1;
	}

	public Chars(Chars s)
	{
		this(s, 0, s.n());
	}

	public Chars(Chars s, int begin, int end1)
	{
		chars = s.chars;
		Math2.range(begin, end1, s.end1I - s.beginI);
		beginI = s.beginI + begin;
		end1I = s.beginI + end1;
	}

	public final int n()
	{
		return end1I - beginI;
	}

	public final int lastI()
	{
		return end1I - beginI - 1;
	}

	public int copyTo(int i, char[] dest, int destI, int n)
	{
		Math2.range(i, i + n, end1I - beginI);
		System.arraycopy(chars, i + beginI, dest, destI, n);
		return destI + n;
	}

	public int copyTo(int i, Chars dest, int destI, int n)
	{
		Math2.range(i, i + n, end1I - beginI);
		Math2.range(destI, destI + n, dest.end1I - dest.beginI);
		System.arraycopy(chars, i + beginI, dest.chars, destI + dest.beginI, n);
		return destI + n;
	}

	public final boolean equals(char[] s)
	{
		return equals(s, 0, s.length);
	}

	public boolean equals(char[] s, int begin, int end1)
	{
		Math2.range(begin, end1, s.length);
		if (end1I - beginI != end1 - begin)
			return false;
		if (chars != s || beginI != begin)
			for (int i = beginI, si = begin; si < end1; i++, si++)
				if (chars[i] != s[si])
					return false;
		return true;
	}

	public final boolean equals(Chars s)
	{
		return equals(s, 0, s.end1I - s.beginI);
	}

	public boolean equals(Chars s, int begin, int end1)
	{
		Math2.range(begin, end1, s.end1I - s.beginI);
		if (end1I - beginI != end1 - begin)
			return false;
		if (chars != s.chars || beginI != begin + s.beginI)
			for (int i = beginI, si = begin + s.beginI; i < end1I; i++, si++)
				if (chars[i] != s.chars[si])
					return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		int h = 1;
		for (int i = beginI; i < end1I; i++)
			h = 31 * h + chars[i];
		return h;
	}

	@Override
	public boolean equals(Object o)
	{
		return o != null && o.getClass() == getClass() && equals((Chars)o);
	}

	public final short readS2(int i)
	{
		Math2.index(i, end1I - beginI);
		return (short)chars[i + beginI];
	}

	public final char readU2(int i)
	{
		Math2.index(i, end1I - beginI);
		return chars[i];
	}

	public final int readS4(int i)
	{
		Math2.index(i, end1I - beginI - 1);
		i += beginI;
		return chars[i] << 16 | chars[i + 1];
	}

	/** @throws ArithmeticException if negative. */
	public final int readU4(int i)
	{
		Math2.index(i, end1I - beginI - 1);
		i += beginI;
		i = chars[i] << 16 | chars[i + 1];
		if (i < 0)
			throw new ArithmeticException("unsigned quad bytes too large : 0x"
				+ Integer.toHexString(i));
		return i;
	}

	public final long readU4ex(int i)
	{
		Math2.index(i, end1I - beginI);
		i += beginI;
		return (long)chars[i] << 16 | chars[i + 1];
	}

	public final long readS8(int i)
	{
		Math2.index(i, end1I - beginI - 3);
		i += beginI;
		return (long)chars[i] << 16 | (long)chars[i + 1] << 32 //
			| (long)chars[i + 2] << 16 | chars[i + 3];
	}

	/** @throws ArithmeticException if negative. */
	public final long readU8(int i)
	{
		Math2.index(i, end1I - beginI - 3);
		i += beginI;
		long l = (long)chars[i] << 48 | (long)chars[i + 1] << 32 //
			| (long)chars[i + 2] << 16 | chars[i + 3];
		if (l < 0)
			throw new ArithmeticException("unsigned octa bytes too large : 0x"
				+ Long.toHexString(l));
		return l;
	}

	public static int readS4(char[] s, int i)
	{
		return s[i] << 16 | s[i + 1];
	}

	/** @throws ArithmeticException if negative. */
	public static int readU4(char[] s, int i)
	{
		i = s[i] << 16 | s[i + 1];
		if (i < 0)
			throw new ArithmeticException("unsigned quad bytes too large : 0x"
				+ Integer.toHexString(i));
		return i;
	}

	public static long readU4ex(char[] s, int i)
	{
		return (long)s[i] << 16 | s[i + 1];
	}

	public static long readS8(char[] s, int i)
	{
		return (long)s[i] << 48 | (long)s[i + 1] << 32 | (long)s[i + 2] << 16 | s[i + 3];
	}

	/** @throws ArithmeticException if negative. */
	public static long readU8(char[] s, int i)
	{
		long l = (long)s[i] << 48 | (long)s[i + 1] << 32 | (long)s[i + 2] << 16 | s[i + 3];
		if (l < 0)
			throw new ArithmeticException("unsigned octa bytes too large : 0x"
				+ Long.toHexString(l));
		return l;
	}

	@Override
	public final char charAt(int i)
	{
		Math2.index(i, end1I - beginI);
		return chars[i];
	}

	@Override
	public final int length()
	{
		return end1I - beginI;
	}

	@Override
	public final CharSequence subSequence(int start, int end)
	{
		return new Chars(this, start, end);
	}

	@Override
	public String toString()
	{
		return new String(chars, beginI, end1I - beginI);
	}

	// ********************************************************************************

	public int copyFrom(int i, char[] src, int srcI, int n)
	{
		Math2.range(i, i + n, end1I - beginI);
		System.arraycopy(src, srcI, chars, i + beginI, n);
		return i + n;
	}

	public final Chars ensureN(int n)
	{
		n += beginI;
		if (n < 0)
			throw new InvalidLengthException();
		chars = Array2.ensureN(chars, n);
		return this;
	}

	public final Chars addN(int n)
	{
		n += end1I;
		if (n < 0)
			throw new InvalidLengthException();
		chars = Array2.ensureN(chars, n);
		end1I = n;
		return this;
	}

	public final int writeS2(int i, short v)
	{
		Math2.index(i, end1I - beginI);
		i += beginI;
		chars[i++] = (char)v;
		return i;
	}

	/** @throws ArithmeticException if not in [-32768, 32768). */
	public final int writeS2(int i, int v)
	{
		if ((short)v != v)
			throw new ArithmeticException("invalid signed dual bytes");
		Math2.index(i, end1I - beginI);
		i += beginI;
		chars[i++] = (char)v;
		return i;
	}

	public final int writeU2(int i, char v)
	{
		Math2.index(i, end1I - beginI);
		i += beginI;
		chars[i++] = v;
		return i;
	}

	/** @throws ArithmeticException if not in [0, 65536). */
	public final int writeU2(int i, int v)
	{
		if (v >> 16 != 0)
			throw new ArithmeticException("invalid unsigned dual bytes");
		Math2.index(i, end1I - beginI);
		i += beginI;
		chars[i++] = (char)v;
		return i;
	}

	public final int writeS4(int i, int v)
	{
		Math2.index(i, end1I - beginI - 1);
		i += beginI;
		chars[i++] = (char)(v >> 16);
		chars[i++] = (char)v;
		return i;
	}

	/** @throws ArithmeticException if not in [0, 4294967296). */
	public final int writeU4(int i, long v)
	{
		if (v >> 32 != 0)
			throw new ArithmeticException("invalid unsigned quad bytes");
		Math2.index(i, end1I - beginI - 1);
		i += beginI;
		chars[i++] = (char)(v >> 16);
		chars[i++] = (char)v;
		return i;
	}

	public final int writeS8(int i, long v)
	{
		Math2.index(i, end1I - beginI - 3);
		i += beginI;
		chars[i++] = (char)(v >> 48);
		chars[i++] = (char)(v >> 32);
		chars[i++] = (char)(v >> 16);
		chars[i++] = (char)v;
		return i;
	}

	/** @throws ArithmeticException if negative. */
	public final int writeU8(int i, long v)
	{
		if (v < 0)
			throw new ArithmeticException("invalid unsigned octa bytes");
		Math2.index(i, end1I - beginI - 3);
		i += beginI;
		chars[i++] = (char)(v >> 48);
		chars[i++] = (char)(v >> 32);
		chars[i++] = (char)(v >> 16);
		chars[i++] = (char)v;
		return i;
	}

	public static int writeS2(char[] s, int i, short v)
	{
		s[i++] = (char)v;
		return i;
	}

	/** @throws ArithmeticException if not in [-32768, 32768). */
	public static int writeS2(char[] s, int i, int v)
	{
		if ((short)v != v)
			throw new ArithmeticException("invalid signed dual bytes");
		s[i++] = (char)v;
		return i;
	}

	public static int writeU2(char[] s, int i, char v)
	{
		s[i++] = v;
		return i;
	}

	/** @throws ArithmeticException if not in [0, 65536). */
	public static int writeU2(char[] s, int i, int v)
	{
		if (v >> 16 != 0)
			throw new ArithmeticException("invalid unsigned dual bytes");
		s[i++] = (char)v;
		return i;
	}

	public static int writeS4(char[] s, int i, int v)
	{
		s[i++] = (char)(v >> 16);
		s[i++] = (char)v;
		return i;
	}

	/** @throws ArithmeticException if not in [0, 4294967296). */
	public static int writeU4(char[] s, int i, long v)
	{
		if (v >> 32 != 0)
			throw new ArithmeticException("invalid unsigned quad bytes");
		s[i++] = (char)(v >> 16);
		s[i++] = (char)v;
		return i;
	}

	public static int writeS8(char[] s, int i, long v)
	{
		s[i++] = (char)(v >> 48);
		s[i++] = (char)(v >> 32);
		s[i++] = (char)(v >> 16);
		s[i++] = (char)v;
		return i;
	}

	/** @throws ArithmeticException if negative. */
	public static int writeU8(char[] s, int i, long v)
	{
		if (v < 0)
			throw new ArithmeticException("invalid unsigned octa bytes");
		s[i++] = (char)(v >> 48);
		s[i++] = (char)(v >> 32);
		s[i++] = (char)(v >> 16);
		s[i++] = (char)v;
		return i;
	}
}
