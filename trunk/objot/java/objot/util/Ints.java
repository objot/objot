//
// Copyright 2007-2009 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.util;

public class Ints
{
	public int[] ints;
	public int beginI;
	public int end1I;

	public Ints()
	{
		ints = Array2.INTS0;
	}

	public Ints(int[] s)
	{
		this(s, 0, s != null ? s.length : 0);
	}

	public Ints(int[] s, int begin, int end1)
	{
		ints = s != null ? s : Array2.INTS0;
		Math2.range(begin, end1, ints.length);
		beginI = begin;
		end1I = end1;
	}

	public Ints(Ints s)
	{
		this(s, 0, s.n());
	}

	public Ints(Ints s, int begin, int end1)
	{
		ints = s.ints;
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

	public int copyTo(int i, int[] dest, int destI, int n)
	{
		Math2.range(i, i + n, end1I - beginI);
		System.arraycopy(ints, i + beginI, dest, destI, n);
		return destI + n;
	}

	public int copyTo(int i, Ints dest, int destI, int n)
	{
		Math2.range(i, i + n, end1I - beginI);
		Math2.range(destI, destI + n, dest.end1I - dest.beginI);
		System.arraycopy(ints, i + beginI, dest.ints, destI + dest.beginI, n);
		return destI + n;
	}

	public final boolean equals(int[] s)
	{
		return equals(s, 0, s.length);
	}

	public boolean equals(int[] s, int begin, int end1)
	{
		Math2.range(begin, end1, s.length);
		if (end1I - beginI != end1 - begin)
			return false;
		if (ints != s || beginI != begin)
			for (int i = beginI, si = begin; si < end1; i++, si++)
				if (ints[i] != s[si])
					return false;
		return true;
	}

	public final boolean equals(Ints s)
	{
		return equals(s, 0, s.end1I - s.beginI);
	}

	public boolean equals(Ints s, int begin, int end1)
	{
		Math2.range(begin, end1, s.end1I - s.beginI);
		if (end1I - beginI != end1 - begin)
			return false;
		if (ints != s.ints || beginI != begin + s.beginI)
			for (int i = beginI, si = begin + s.beginI; i < end1I; i++, si++)
				if (ints[i] != s.ints[si])
					return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		int h = 1;
		for (int i = beginI; i < end1I; i++)
			h = 31 * h + ints[i];
		return h;
	}

	@Override
	public boolean equals(Object o)
	{
		return o != null && o.getClass() == getClass() && equals((Ints)o);
	}

	public final int readS4(int i)
	{
		Math2.index(i, end1I - beginI);
		return ints[i + beginI];
	}

	/** @throws ArithmeticException if negative. */
	public final int readU4(int i)
	{
		Math2.index(i, end1I - beginI);
		i = ints[i + beginI];
		if (i < 0)
			throw new ArithmeticException("unsigned quad bytes too large : 0x"
				+ Integer.toHexString(i));
		return i;
	}

	public final long readU4ex(int i)
	{
		Math2.index(i, end1I - beginI);
		return ints[i + beginI] & 0xFFFFFFFFL;
	}

	public final long readS8(int i)
	{
		Math2.index(i, end1I - beginI - 1);
		i += beginI;
		return (long)ints[i] << 32 | ints[++i] & 0xFFFFFFFFL;
	}

	/** @throws ArithmeticException if negative. */
	public final long readU8(int i)
	{
		Math2.index(i, end1I - beginI - 1);
		i += beginI;
		long l = (long)ints[i] << 32 | ints[++i] & 0xFFFFFFFFL;
		if (l < 0)
			throw new ArithmeticException("unsigned octa bytes too large : 0x"
				+ Long.toHexString(l));
		return l;
	}

	public static int readS4(int[] s, int i)
	{
		return s[i];
	}

	/** @throws ArithmeticException if negative. */
	public static int readU4(int[] s, int i)
	{
		i = s[i];
		if (i < 0)
			throw new ArithmeticException("unsigned quad bytes too large : 0x"
				+ Integer.toHexString(i));
		return i;
	}

	public static long readU4ex(int[] s, int i)
	{
		return s[i] & 0xFFFFFFFFL;
	}

	public static long readS8(int[] s, int i)
	{
		return (long)s[i] << 32 | s[++i] & 0xFFFFFFFFL;
	}

	/** @throws ArithmeticException if negative. */
	public static long readU8(int[] s, int i)
	{
		long l = (long)s[i] << 32 | s[++i] & 0xFFFFFFFFL;
		if (l < 0)
			throw new ArithmeticException("unsigned octa bytes too large : 0x"
				+ Long.toHexString(l));
		return l;
	}

	// ********************************************************************************

	public int copyFrom(int i, int[] src, int srcI, int n)
	{
		Math2.range(i, i + n, end1I - beginI);
		System.arraycopy(src, srcI, ints, i + beginI, n);
		return i + n;
	}

	public final Ints ensureN(int n)
	{
		n += beginI;
		if (n < 0)
			throw new InvalidLengthException();
		ints = Array2.ensureN(ints, n);
		return this;
	}

	public final Ints addN(int n)
	{
		n += end1I;
		if (n < 0)
			throw new InvalidLengthException();
		ints = Array2.ensureN(ints, n);
		end1I = n;
		return this;
	}

	public final int writeS4(int i, int v)
	{
		Math2.index(i, end1I - beginI);
		ints[i += beginI] = v;
		return i;
	}

	/** @throws ArithmeticException if not in [0, 4294967296). */
	public final int writeU4(int i, long v)
	{
		if (v >> 32 != 0)
			throw new ArithmeticException("invalid unsigned quad bytes");
		Math2.index(i, end1I - beginI);
		ints[i += beginI] = (int)v;
		return i;
	}

	public final int writeS8(int i, long v)
	{
		Math2.index(i, end1I - beginI - 1);
		i += beginI;
		ints[i++] = (int)(v >> 32);
		ints[i++] = (int)v;
		return i;
	}

	/** @throws ArithmeticException if negative. */
	public final int writeU8(int i, long v)
	{
		if (v < 0)
			throw new ArithmeticException("invalid unsigned octa bytes");
		Math2.index(i, end1I - beginI - 1);
		i += beginI;
		ints[i++] = (int)(v >> 32);
		ints[i++] = (int)v;
		return i;
	}

	public static int writeS4(int[] s, int i, int v)
	{
		s[i++] = v;
		return i;
	}

	/** @throws ArithmeticException if not in [0, 4294967296). */
	public static int writeU4(int[] s, int i, long v)
	{
		if (v >> 32 != 0)
			throw new ArithmeticException("invalid unsigned quad bytes");
		s[i++] = (int)v;
		return i;
	}

	public static int writeS8(int[] s, int i, long v)
	{
		s[i++] = (int)(v >> 32);
		s[i++] = (int)v;
		return i;
	}

	/** @throws ArithmeticException if negative. */
	public static int writeU8(int[] s, int i, long v)
	{
		if (v < 0)
			throw new ArithmeticException("invalid unsigned octa bytes");
		s[i++] = (int)(v >> 32);
		s[i++] = (int)v;
		return i;
	}
}
