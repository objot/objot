//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.util;

public class Objects<T>
{
	@SuppressWarnings("unchecked")
	public T[] objs = (T[])Array2.OBJECTS0;
	public int beginI;
	public int end1I;

	public Objects()
	{
	}

	public Objects(T[] s)
	{
		this(s, 0, s != null ? s.length : 0);
	}

	@SuppressWarnings("unchecked")
	public Objects(T[] s, int begin, int end1)
	{
		objs = s != null ? s : (T[])Array2.OBJECTS0;
		Math2.range(begin, end1, objs.length);
		beginI = begin;
		end1I = end1;
	}

	public Objects(Objects<? extends T> s)
	{
		this(s, 0, s.n());
	}

	public Objects(Objects<? extends T> s, int begin, int end1)
	{
		objs = s.objs;
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

	public int copyTo(int i, T[] dest, int destI, int n)
	{
		Math2.range(i, i + n, end1I - beginI);
		System.arraycopy(objs, i + beginI, dest, destI, n);
		return destI + n;
	}

	public int copyTo(int i, Objects<? super T> dest, int destI, int n)
	{
		Math2.range(i, i + n, end1I - beginI);
		Math2.range(destI, destI + n, dest.end1I - dest.beginI);
		System.arraycopy(objs, i + beginI, dest.objs, destI + dest.beginI, n);
		return destI + n;
	}

	public final boolean equals(T[] s)
	{
		return equals(s, 0, s.length);
	}

	public boolean equals(T[] s, int begin, int end1)
	{
		Math2.range(begin, end1, s.length);
		if (end1I - beginI != end1 - begin)
			return false;
		if (objs != s || beginI != begin)
			for (int i = beginI, si = begin; si < end1; i++, si++)
				if (objs[i] != s[si])
					return false;
		return true;
	}

	public final boolean equals(Objects<?> s)
	{
		return equals(s, 0, s.end1I - s.beginI);
	}

	public boolean equals(Objects<?> s, int begin, int end1)
	{
		Math2.range(begin, end1, s.end1I - s.beginI);
		if (end1I - beginI != end1 - begin)
			return false;
		if (objs != s.objs || beginI != begin + s.beginI)
			for (int i = beginI, si = begin + s.beginI; i < end1I; i++, si++)
				if (objs[i] != s.objs[si] && (objs[i] == null || !objs[i].equals(s.objs[si])))
					return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		int h = 1;
		for (int i = beginI; i < end1I; i++)
			h = 31 * h + (objs[i] != null ? objs[i].hashCode() : 0);
		return h;
	}

	@Override
	public boolean equals(Object o)
	{
		return o != null && o.getClass() == getClass() && equals((Objects<?>)o);
	}

	public T read(int i)
	{
		Math2.index(i, end1I - beginI);
		return objs[i + beginI];
	}

	public T read0(int i)
	{
		return objs[i];
	}

	public static <T>T readS8(T[] s, int i)
	{
		return s[i];
	}

	// ********************************************************************************

	public int copyFrom(int i, T[] src, int srcI, int n)
	{
		Math2.range(i, i + n, end1I - beginI);
		System.arraycopy(src, srcI, objs, i + beginI, n);
		return i + n;
	}

	public Objects<T> ensureN(int n)
	{
		n += beginI;
		if (n < 0)
			throw new InvalidLengthException();
		objs = Array2.ensureN(objs, n);
		return this;
	}

	public Objects<T> addN(int n)
	{
		n += end1I;
		if (n < 0)
			throw new InvalidLengthException();
		objs = Array2.ensureN(objs, n);
		end1I = n;
		return this;
	}

	public void write(int i, T v)
	{
		Math2.index(i, end1I - beginI);
		objs[i + beginI] = v;
	}

	public void write0(int i, T v)
	{
		objs[i] = v;
	}

	public static <T>void write(T[] s, int i, T v)
	{
		s[i] = v;
	}
}
