//
// Copyright 2007 Qianyan Cai
// Under the terms of the GNU General Public License version 2
//
package objot.util;

public class Ints
{
	public int[] ints = Array2.INTS0;
	public int beginI;
	public int end1I;

	public Ints()
	{
	}

	public Ints(int[] s)
	{
		this(s, 0, s != null ? s.length : 0);
	}

	public Ints(int[] s, int begin, int end1)
	{
		ints = s != null ? s : Array2.INTS0;
		Math2.checkRange(begin, end1, ints.length);
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
		Math2.checkRange(begin, end1, s.end1I - s.beginI);
		beginI = s.beginI + begin;
		end1I = s.beginI + end1;
	}

	public int n()
	{
		return end1I - beginI;
	}

	public int lastI()
	{
		return end1I - beginI - 1;
	}

	public int copyTo(int i, int[] dest, int destI, int n)
	{
		Math2.checkRange(i, i + n, end1I - beginI);
		System.arraycopy(ints, i + beginI, dest, destI, n);
		return destI + n;
	}

	public int copyTo(int i, Ints dest, int destI, int n)
	{
		Math2.checkRange(i, i + n, end1I - beginI);
		Math2.checkRange(destI, destI + n, dest.end1I - dest.beginI);
		System.arraycopy(ints, i + beginI, dest.ints, destI + dest.beginI, n);
		return destI + n;
	}

	public int copyFrom(int i, int[] src, int srcI, int n)
	{
		Math2.checkRange(i, i + n, end1I - beginI);
		System.arraycopy(src, srcI, ints, i + beginI, n);
		return i + n;
	}

	public final boolean equals(int[] s)
	{
		return equals(s, 0, s.length);
	}

	public boolean equals(int[] s, int begin, int end1)
	{
		Math2.checkRange(begin, end1, s.length);
		if (end1I - beginI != end1 - begin)
			return false;
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
		Math2.checkRange(begin, end1, s.end1I - s.beginI);
		if (end1I - beginI != end1 - begin)
			return false;
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

	public int readS4(int i)
	{
		Math2.checkIndex(i, end1I - beginI);
		return ints[i + beginI];
	}

	public int readU4(int i)
	{
		Math2.checkIndex(i, end1I - beginI);
		i = ints[i + beginI];
		if (i < 0)
			throw new ArithmeticException("unsigned quad bytes too large : 0x"
				+ Integer.toHexString(i));
		return i;
	}

	public long readU4ex(int i)
	{
		Math2.checkIndex(i, end1I - beginI);
		return ints[i + beginI] & 0xFFFFFFFFL;
	}

	public long readS8(int i)
	{
		Math2.checkIndex(i, end1I - beginI - 1);
		i += beginI;
		return (long)ints[i] << 32 | ints[++i] & 0xFFFFFFFFL;
	}

	public int read0s4(int i)
	{
		return ints[i];
	}

	public int read0u4(int i)
	{
		i = ints[i];
		if (i < 0)
			throw new ArithmeticException("unsigned quad bytes too large : 0x"
				+ Integer.toHexString(i));
		return i;
	}

	public long read0u4ex(int i)
	{
		return ints[i] & 0xFFFFFFFFL;
	}

	public long read0s8(int i)
	{
		return (long)ints[i] << 32 | ints[++i] & 0xFFFFFFFFL;
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

	// ********************************************************************************

	public Ints ensureN(int n)
	{
		ints = Array2.ensureN(ints, beginI + n);
		return this;
	}

	public Ints addN(int n)
	{
		ints = Array2.ensureN(ints, end1I + n);
		end1I += n;
		return this;
	}

	public void writeS4(int i, int v)
	{
		Math2.checkIndex(i, end1I - beginI);
		ints[i + beginI] = v;
	}

	public void writeU4(int i, long v)
	{
		if (v >> 32 != 0)
			throw new ArithmeticException("invalid unsigned quad bytes");
		Math2.checkIndex(i, end1I - beginI);
		ints[i + beginI] = (int)v;
	}

	public void writeS8(int i, long v)
	{
		Math2.checkIndex(i, end1I - beginI - 1);
		i += beginI;
		ints[i] = (int)(v >> 32);
		ints[++i] = (int)v;
	}

	public void write0s4(int i, int v)
	{
		ints[i] = v;
	}

	public void write0u4(int i, long v)
	{
		if (v >> 32 != 0)
			throw new ArithmeticException("invalid unsigned quad bytes");
		ints[i] = (int)v;
	}

	public void write0s8(int i, long v)
	{
		ints[i] = (int)(v >> 32);
		ints[++i] = (int)v;
	}

	public static void writeS4(int[] s, int i, int v)
	{
		s[i] = v;
	}

	public static void writeU4(int[] s, int i, long v)
	{
		if (v >> 32 != 0)
			throw new ArithmeticException("invalid unsigned quad bytes");
		s[i] = (int)v;
	}

	public static void writeS8(int[] s, int i, long v)
	{
		s[i] = (int)(v >> 32);
		s[++i] = (int)v;
	}
}
