//
// Copyright 2007-2009 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.util;

public class Longs
{
	public long[] longs = Array2.LONGS0;
	public int beginI;
	public int end1I;

	public Longs()
	{
	}

	public Longs(long[] s)
	{
		this(s, 0, s != null ? s.length : 0);
	}

	public Longs(long[] s, int begin, int end1)
	{
		longs = s != null ? s : Array2.LONGS0;
		Math2.range(begin, end1, longs.length);
		beginI = begin;
		end1I = end1;
	}

	public Longs(Longs s)
	{
		this(s, 0, s.n());
	}

	public Longs(Longs s, int begin, int end1)
	{
		longs = s.longs;
		Math2.range(begin, end1, s.end1I - s.beginI);
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

	public int copyTo(int i, long[] dest, int destI, int n)
	{
		Math2.range(i, i + n, end1I - beginI);
		System.arraycopy(longs, i + beginI, dest, destI, n);
		return destI + n;
	}

	public int copyTo(int i, Longs dest, int destI, int n)
	{
		Math2.range(i, i + n, end1I - beginI);
		Math2.range(destI, destI + n, dest.end1I - dest.beginI);
		System.arraycopy(longs, i + beginI, dest.longs, destI + dest.beginI, n);
		return destI + n;
	}

	public final boolean equals(long[] s)
	{
		return equals(s, 0, s.length);
	}

	public boolean equals(long[] s, int begin, int end1)
	{
		Math2.range(begin, end1, s.length);
		if (end1I - beginI != end1 - begin)
			return false;
		for (int i = beginI, si = begin; si < end1; i++, si++)
			if (longs[i] != s[si])
				return false;
		return true;
	}

	public final boolean equals(Longs s)
	{
		return equals(s, 0, s.end1I - s.beginI);
	}

	public boolean equals(Longs s, int begin, int end1)
	{
		Math2.range(begin, end1, s.end1I - s.beginI);
		if (end1I - beginI != end1 - begin)
			return false;
		for (int i = beginI, si = begin + s.beginI; i < end1I; i++, si++)
			if (longs[i] != s.longs[si])
				return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		int h = 1;
		for (int i = beginI; i < end1I; i++)
		{
			h = 31 * h + (int)(longs[i] >> 32);
			h = 31 * h + (int)longs[i];
		}
		return h;
	}

	@Override
	public boolean equals(Object o)
	{
		return o != null && o.getClass() == getClass() && equals((Longs)o);
	}

	public long readS8(int i)
	{
		Math2.index(i, end1I - beginI);
		return longs[i + beginI];
	}

	public long read0s8(int i)
	{
		return longs[i];
	}

	public static long readS8(long[] s, int i)
	{
		return s[i];
	}

	// ********************************************************************************

	public int copyFrom(int i, long[] src, int srcI, int n)
	{
		Math2.range(i, i + n, end1I - beginI);
		System.arraycopy(src, srcI, longs, i + beginI, n);
		return i + n;
	}

	public Longs ensureN(int n)
	{
		n += beginI;
		if (n < 0)
			throw new InvalidLengthException();
		longs = Array2.ensureN(longs, n);
		return this;
	}

	public Longs addN(int n)
	{
		n += end1I;
		if (n < 0)
			throw new InvalidLengthException();
		longs = Array2.ensureN(longs, n);
		end1I = n;
		return this;
	}

	public void writeS8(int i, long v)
	{
		Math2.index(i, end1I - beginI);
		longs[i + beginI] = v;
	}

	public void write0s8(int i, long v)
	{
		longs[i] = v;
	}

	public static void writeS8(long[] s, int i, long v)
	{
		s[i] = v;
	}
}
