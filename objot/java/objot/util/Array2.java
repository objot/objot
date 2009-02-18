//
// Copyright 2007-2009 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.ConcurrentMap;


public class Array2
{
	protected Array2()
	{
		throw new AbstractMethodError();
	}

	public static final boolean[] BOOLS0 = {};
	public static final byte[] BYTES0 = {};
	public static final char[] CHARS0 = {};
	public static final short[] SHORTS0 = {};
	public static final int[] INTS0 = {};
	public static final long[] LONGS0 = {};
	public static final float[] FLOATS0 = {};
	public static final double[] DOUBLES0 = {};
	public static final Object[] OBJECTS0 = {};
	public static final String[] STRINGS0 = {};
	public static final Class<?>[] CLASSES0 = {};

	/** @return new allocated array or a reused empty array */
	public static boolean[] newBools(int n)
	{
		return n != 0 ? new boolean[n] : BOOLS0;
	}

	/** @return new allocated array or a reused empty array */
	public static byte[] newBytes(int n)
	{
		return n != 0 ? new byte[n] : BYTES0;
	}

	/** @return new allocated array or a reused empty array */
	public static char[] newChars(int n)
	{
		return n != 0 ? new char[n] : CHARS0;
	}

	/** @return new allocated array or a reused empty array */
	public static short[] newShorts(int n)
	{
		return n != 0 ? new short[n] : SHORTS0;
	}

	/** @return new allocated array or a reused empty array */
	public static int[] newInts(int n)
	{
		return n != 0 ? new int[n] : INTS0;
	}

	/** @return new allocated array or a reused empty array */
	public static long[] newLongs(int n)
	{
		return n != 0 ? new long[n] : LONGS0;
	}

	/** @return new allocated array or a reused empty array */
	public static float[] newFloats(int n)
	{
		return n != 0 ? new float[n] : FLOATS0;
	}

	/** @return new allocated array or a reused empty array */
	public static double[] newDoubles(int n)
	{
		return n != 0 ? new double[n] : DOUBLES0;
	}

	/** @return new allocated array or a reused empty array */
	public static Object[] newObjects(int n)
	{
		return n != 0 ? new Object[n] : OBJECTS0;
	}

	/** @return new allocated 1-dimension array */
	@SuppressWarnings("unchecked")
	public static <T>T[] news(Class<T> component, int length)
	{
		return (T[])Array.newInstance(component, length);
	}

	/** @return new allocated 1-dimension array */
	@SuppressWarnings("unchecked")
	public static <T>T[] news(T[] s, int length)
	{
		return (T[])(s.getClass() == Object[].class ? new Object[length] //
			: Array.newInstance(s.getClass().getComponentType(), length));
	}

	/** @return the array, or a reused empty array if the array is null */
	public static boolean[] maskNull(boolean[] x)
	{
		return x == null ? BOOLS0 : x;
	}

	/** @return the array, or a reused empty array if the array is null */
	public static byte[] maskNull(byte[] x)
	{
		return x == null ? BYTES0 : x;
	}

	/** @return the array, or a reused empty array if the array is null */
	public static short[] maskNull(short[] x)
	{
		return x == null ? SHORTS0 : x;
	}

	/** @return the array, or a reused empty array if the array is null */
	public static char[] maskNull(char[] x)
	{
		return x == null ? CHARS0 : x;
	}

	/** @return the array, or a reused empty array if the array is null */
	public static int[] maskNull(int[] x)
	{
		return x == null ? INTS0 : x;
	}

	/** @return the array, or a reused empty array if the array is null */
	public static long[] maskNull(long[] x)
	{
		return x == null ? LONGS0 : x;
	}

	/** @return the array, or a reused empty array if the array is null */
	public static float[] maskNull(float[] x)
	{
		return x == null ? FLOATS0 : x;
	}

	/** @return the array, or a reused empty array if the array is null */
	public static double[] maskNull(double[] x)
	{
		return x == null ? DOUBLES0 : x;
	}

	/** @return the array, or a reused empty array if the array is null */
	public static Object[] maskNull(Object[] x)
	{
		return x == null ? OBJECTS0 : x;
	}

	/** @return the array, or null if the array is the reused empty array */
	public static boolean[] unmaskNull(boolean[] x)
	{
		return x == BOOLS0 ? null : x;
	}

	/** @return the array, or null if the array is the reused empty array */
	public static byte[] unmaskNull(byte[] x)
	{
		return x == BYTES0 ? null : x;
	}

	/** @return the array, or null if the array is the reused empty array */
	public static short[] unmaskNull(short[] x)
	{
		return x == SHORTS0 ? null : x;
	}

	/** @return the array, or null if the array is the reused empty array */
	public static char[] unmaskNull(char[] x)
	{
		return x == CHARS0 ? null : x;
	}

	/** @return the array, or null if the array is the reused empty array */
	public static int[] unmaskNull(int[] x)
	{
		return x == INTS0 ? null : x;
	}

	/** @return the array, or null if the array is the reused empty array */
	public static long[] unmaskNull(long[] x)
	{
		return x == LONGS0 ? null : x;
	}

	/** @return the array, or null if the array is the reused empty array */
	public static float[] unmaskNull(float[] x)
	{
		return x == FLOATS0 ? null : x;
	}

	/** @return the array, or null if the array is the reused empty array */
	public static double[] unmaskNull(double[] x)
	{
		return x == DOUBLES0 ? null : x;
	}

	/** @return the array, or null if the array is the reused empty array */
	public static Object[] unmaskNull(Object[] x)
	{
		return x == OBJECTS0 ? null : x;
	}

	/** Search the array. */
	public static int search(int[] s, int key)
	{
		return search(s, 0, s.length, key);
	}

	/** Search the array in [begin, end1). */
	public static int search(int[] s, int begin, int end1, int key)
	{
		Math2.range(begin, end1, s.length);
		for (int i = begin; i < end1; i++)
			if (s[i] == key)
				return i;
		return -1;
	}

	/** Search the array. */
	public static int search(long[] s, long key)
	{
		return search(s, 0, s.length, key);
	}

	/** Search the array in [begin, end1). */
	public static int search(long[] s, int begin, int end1, long key)
	{
		Math2.range(begin, end1, s.length);
		for (int i = begin; i < end1; i++)
			if (s[i] == key)
				return i;
		return -1;
	}

	/** Search the array. */
	public static int search(short[] s, short key)
	{
		return search(s, 0, s.length, key);
	}

	/** Search the array in [begin, end1). */
	public static int search(short[] s, int begin, int end1, short key)
	{
		Math2.range(begin, end1, s.length);
		for (int i = begin; i < end1; i++)
			if (s[i] == key)
				return i;
		return -1;
	}

	/** Search the array. */
	public static int search(char[] s, char key)
	{
		return search(s, 0, s.length, key);
	}

	/** Search the array in [begin, end1). */
	public static int search(char[] s, int begin, int end1, char key)
	{
		Math2.range(begin, end1, s.length);
		for (int i = begin; i < end1; i++)
			if (s[i] == key)
				return i;
		return -1;
	}

	/** Search the array. */
	public static int search(byte[] s, byte key)
	{
		return search(s, 0, s.length, key);
	}

	/** Search the array in [begin, end1). */
	public static int search(byte[] s, int begin, int end1, byte key)
	{
		Math2.range(begin, end1, s.length);
		for (int i = begin; i < end1; i++)
			if (s[i] == key)
				return i;
		return -1;
	}

	/** Search the array. */
	public static int search(double[] s, double key)
	{
		return search(s, 0, s.length, key);
	}

	/** Search the array in [begin, end1). */
	public static int search(double[] s, int begin, int end1, double key)
	{
		Math2.range(begin, end1, s.length);
		if (key == key)
		{
			for (int i = begin; i < end1; i++)
				if (s[i] == key)
					return i;
		}
		else
			for (int i = begin; i < end1; i++)
				if (s[i] != s[i])
					return i;
		return -1;
	}

	/** Search the array. */
	public static int search(float[] s, float key)
	{
		return search(s, 0, s.length, key);
	}

	/** Search the array in [begin, end1). */
	public static int search(float[] s, int begin, int end1, float key)
	{
		Math2.range(begin, end1, s.length);
		if (key == key)
		{
			for (int i = begin; i < end1; i++)
				if (s[i] == key)
					return i;
		}
		else
			for (int i = begin; i < end1; i++)
				if (s[i] != s[i])
					return i;
		return -1;
	}

	/** Search the array. */
	public static <T>int search(Object[] s, Object key)
	{
		return search(s, 0, s.length, key);
	}

	/** Search the array in [begin, end1). */
	public static <T>int search(Object[] s, int begin, int end1, Object key)
	{
		Math2.range(begin, end1, s.length);
		for (int i = begin; i < end1; i++)
			if (s[i].equals(key))
				return i;
		return -1;
	}

	/**
	 * Search the array.
	 * 
	 * @throws NullPointerException if the comparator is null
	 */
	public static <T>int search2(T[] s, T key, Comparator<? super T> c)
	{
		return search2(s, 0, s.length, key, c);
	}

	/**
	 * Search the array in [begin, end1).
	 * 
	 * @throws NullPointerException if the comparator is null
	 */
	public static <T>int search2(T[] s, int begin, int end1, T key, Comparator<? super T> c)
	{
		if (c == null)
			throw null;
		Math2.range(begin, end1, s.length);
		for (int i = begin; i < end1; i++)
			if (c.compare(s[i], key) == 0)
				return i;
		return -1;
	}

	/**
	 * Search the array in [begin, end1).
	 * 
	 * @see Arrays#binarySearch(long[],long)
	 */
	public static int binarySearch(long[] a, int begin, int end1, long key)
	{
		Math2.range(begin, end1, a.length);
		int low = begin;
		int high = end1 - 1;
		while (low <= high)
		{
			int mid = (low + high) >> 1;
			long midVal = a[mid];
			if (midVal < key)
				low = mid + 1;
			else if (midVal > key)
				high = mid - 1;
			else
				return mid; // key found
		}
		return -(low + 1); // key not found.
	}

	/**
	 * Search the array in [begin, end1).
	 * 
	 * @see Arrays#binarySearch(int[],int)
	 */
	public static int binarySearch(int[] a, int begin, int end1, int key)
	{
		Math2.range(begin, end1, a.length);
		int low = begin;
		int high = end1 - 1;
		while (low <= high)
		{
			int mid = (low + high) >> 1;
			int midVal = a[mid];
			if (midVal < key)
				low = mid + 1;
			else if (midVal > key)
				high = mid - 1;
			else
				return mid; // key found
		}
		return -(low + 1); // key not found.
	}

	/**
	 * Search the array in [begin, end1).
	 * 
	 * @see Arrays#binarySearch(short[],short)
	 */
	public static int binarySearch(short[] a, int begin, int end1, short key)
	{
		Math2.range(begin, end1, a.length);
		int low = begin;
		int high = end1 - 1;
		while (low <= high)
		{
			int mid = (low + high) >> 1;
			short midVal = a[mid];
			if (midVal < key)
				low = mid + 1;
			else if (midVal > key)
				high = mid - 1;
			else
				return mid; // key found
		}
		return -(low + 1); // key not found.
	}

	/**
	 * Search the array in [begin, end1).
	 * 
	 * @see Arrays#binarySearch(char[],char)
	 */
	public static int binarySearch(char[] a, int begin, int end1, char key)
	{
		Math2.range(begin, end1, a.length);
		int low = begin;
		int high = end1 - 1;
		while (low <= high)
		{
			int mid = (low + high) >> 1;
			char midVal = a[mid];
			if (midVal < key)
				low = mid + 1;
			else if (midVal > key)
				high = mid - 1;
			else
				return mid; // key found
		}
		return -(low + 1); // key not found.
	}

	/**
	 * Search the array in [begin, end1).
	 * 
	 * @see Arrays#binarySearch(long[],long)
	 */
	public static int binarySearch(byte[] a, int begin, int end1, byte key)
	{
		Math2.range(begin, end1, a.length);
		int low = begin;
		int high = end1 - 1;
		while (low <= high)
		{
			int mid = (low + high) >> 1;
			byte midVal = a[mid];
			if (midVal < key)
				low = mid + 1;
			else if (midVal > key)
				high = mid - 1;
			else
				return mid; // key found
		}
		return -(low + 1); // key not found.
	}

	/**
	 * Search the array in [begin, end1).
	 * 
	 * @see Arrays#binarySearch(double[],double)
	 */
	public static int binarySearch(double[] a, int begin, int end1, double key)
	{
		Math2.range(begin, end1, a.length);
		int low = begin;
		int high = end1 - 1;
		while (low <= high)
		{
			int mid = (low + high) >> 1;
			double midVal = a[mid];
			int cmp;
			if (midVal < key)
				cmp = -1; // Neither val is NaN, thisVal is smaller
			else if (midVal > key)
				cmp = 1; // Neither val is NaN, thisVal is larger
			else
			{
				long midBits = Double.doubleToLongBits(midVal);
				long keyBits = Double.doubleToLongBits(key);
				cmp = (midBits == keyBits ? 0 : // Values are equal
					(midBits < keyBits ? -1 : // (-0.0, 0.0) or (!NaN, NaN)
						1)); // (0.0, -0.0) or (NaN, !NaN)
			}
			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return mid; // key found
		}
		return -(low + 1); // key not found.
	}

	/**
	 * Search the array in [begin, end1).
	 * 
	 * @see Arrays#binarySearch(float[],float)
	 */
	public static int binarySearch(float[] a, int begin, int end1, float key)
	{
		Math2.range(begin, end1, a.length);
		int low = begin;
		int high = end1 - 1;
		while (low <= high)
		{
			int mid = (low + high) >> 1;
			float midVal = a[mid];
			int cmp;
			if (midVal < key)
				cmp = -1; // Neither val is NaN, thisVal is smaller
			else if (midVal > key)
				cmp = 1; // Neither val is NaN, thisVal is larger
			else
			{
				int midBits = Float.floatToIntBits(midVal);
				int keyBits = Float.floatToIntBits(key);
				cmp = (midBits == keyBits ? 0 : // Values are equal
					(midBits < keyBits ? -1 : // (-0.0, 0.0) or (!NaN, NaN)
						1)); // (0.0, -0.0) or (NaN, !NaN)
			}
			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return mid; // key found
		}
		return -(low + 1); // key not found.
	}

	/**
	 * Search the array in [begin, end1).
	 * 
	 * @param a {@link Comparable}s
	 * @see Arrays#binarySearch(Object[],Object)
	 */
	@SuppressWarnings("unchecked")
	public static int binarySearch(Object[] a, int begin, int end1, Object key)
	{
		Math2.range(begin, end1, a.length);
		int low = begin;
		int high = end1 - 1;
		while (low <= high)
		{
			int mid = (low + high) >> 1;
			int cmp = ((Comparable)a[mid]).compareTo(key);
			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return mid; // key found
		}
		return -(low + 1); // key not found.
	}

	/**
	 * Search the array in [begin, end1).
	 * 
	 * @throws NullPointerException if the comparator is null, this is different from
	 *             {@link Arrays#binarySearch(Object[], Object, Comparator)}
	 * @see Arrays#binarySearch(Object[],Object,Comparator)
	 */
	public static <T>int binarySearch2(T[] a, int begin, int end1, T key,
		Comparator<? super T> c)
	{
		if (c == null)
			throw null;
		Math2.range(begin, end1, a.length);
		int low = begin;
		int high = end1 - 1;
		while (low <= high)
		{
			int mid = (low + high) >> 1;
			T midVal = a[mid];
			int cmp = c.compare(midVal, key);
			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return mid; // key found
		}
		return -(low + 1); // key not found.
	}

	public static byte[] ensureN(byte[] s, int n)
	{
		if (n <= s.length)
			return s;
		byte[] a = new byte[Math2.max(s.length + (s.length >> 1) + 4, n, 12)];
		System.arraycopy(s, 0, a, 0, s.length);
		return a;
	}

	public static int[] ensureN(int[] s, int n)
	{
		if (n <= s.length)
			return s;
		int[] a = new int[Math2.max(s.length + (s.length >> 1) + 4, n, 4)];
		System.arraycopy(s, 0, a, 0, s.length);
		return a;
	}

	public static long[] ensureN(long[] s, int n)
	{
		if (n <= s.length)
			return s;
		long[] a = new long[Math2.max(s.length + (s.length >> 1) + 4, n, 4)];
		System.arraycopy(s, 0, a, 0, s.length);
		return a;
	}

	public static <T>T[] ensureN(T[] s, int n)
	{
		if (n <= s.length)
			return s;
		T[] a = news(s, Math2.max(s.length + (s.length >> 1) + 4, n, 4));
		System.arraycopy(s, 0, a, 0, s.length);
		return a;
	}

	public static byte[] shrink(byte[] s, int begin, int end1)
	{
		Math2.range(begin, end1, s.length);
		if (end1 - begin == s.length)
			return s;
		byte[] a = new byte[end1 - begin];
		System.arraycopy(s, begin, a, 0, end1 - begin);
		return a;
	}

	public static int[] shrink(int[] s, int begin, int end1)
	{
		Math2.range(begin, end1, s.length);
		if (end1 - begin == s.length)
			return s;
		int[] a = new int[end1 - begin];
		System.arraycopy(s, begin, a, 0, end1 - begin);
		return a;
	}

	public static long[] shrink(long[] s, int begin, int end1)
	{
		Math2.range(begin, end1, s.length);
		if (end1 - begin == s.length)
			return s;
		long[] a = new long[end1 - begin];
		System.arraycopy(s, begin, a, 0, end1 - begin);
		return a;
	}

	public static <T>T[] shrink(T[] s, int begin, int end1)
	{
		Math2.range(begin, end1, s.length);
		if (end1 - begin == s.length)
			return s;
		T[] a = news(s, end1 - begin);
		System.arraycopy(s, begin, a, 0, end1 - begin);
		return a;
	}

	public static byte[] subClone(byte[] s, int begin, int end1)
	{
		Math2.range(begin, end1, s.length);
		byte[] a = new byte[end1 - begin];
		System.arraycopy(s, begin, a, 0, end1 - begin);
		return a;
	}

	public static int[] subClone(int[] s, int begin, int end1)
	{
		Math2.range(begin, end1, s.length);
		int[] a = new int[end1 - begin];
		System.arraycopy(s, begin, a, 0, end1 - begin);
		return a;
	}

	public static long[] subClone(long[] s, int begin, int end1)
	{
		Math2.range(begin, end1, s.length);
		long[] a = new long[end1 - begin];
		System.arraycopy(s, begin, a, 0, end1 - begin);
		return a;
	}

	public static <T>T[] subClone(T[] s, int begin, int end1)
	{
		Math2.range(begin, end1, s.length);
		T[] a = news(s, end1 - begin);
		System.arraycopy(s, begin, a, 0, end1 - begin);
		return a;
	}

	public static byte[] concat(byte[] a, byte[] b)
	{
		if (a == null || a.length == 0)
			return maskNull(b);
		if (b == null || b.length == 0)
			return a;
		byte[] s = new byte[a.length + b.length];
		System.arraycopy(a, 0, s, 0, a.length);
		System.arraycopy(b, 0, s, a.length, b.length);
		return s;
	}

	public static int[] concat(int[] a, int[] b)
	{
		if (a == null || a.length == 0)
			return maskNull(b);
		if (b == null || b.length == 0)
			return a;
		int[] s = new int[a.length + b.length];
		System.arraycopy(a, 0, s, 0, a.length);
		System.arraycopy(b, 0, s, a.length, b.length);
		return s;
	}

	public static long[] concat(long[] a, long[] b)
	{
		if (a == null || a.length == 0)
			return maskNull(b);
		if (b == null || b.length == 0)
			return a;
		long[] s = new long[a.length + b.length];
		System.arraycopy(a, 0, s, 0, a.length);
		System.arraycopy(b, 0, s, a.length, b.length);
		return s;
	}

	/** @return or {@link #OBJECTS0} if both null */
	@SuppressWarnings("unchecked")
	public static <T>T[] concat(T[] a, T[] b)
	{
		if (a == null)
			return (T[])maskNull(b);
		if (b == null || b.length == 0)
			return a;
		if (a.length == 0)
			return b;
		T[] s = news(a, a.length + b.length);
		System.arraycopy(a, 0, s, 0, a.length);
		System.arraycopy(b, 0, s, a.length, b.length);
		return s;
	}

	public static <T>T[] from(Collection<?> s, Class<T> c)
	{
		return s.toArray(news(c, s.size()));
	}

	public static <T, C extends Collection<? super T>>C addTo(T[] s, C c)
	{
		for (T o: s)
			c.add(o);
		return c;
	}

	public static <T, C extends Collection<? super T>>C addTo(T[] s, int begin, int end1, C c)
	{
		Math2.range(begin, end1, s.length);
		for (; begin < end1; begin++)
			c.add(s[begin]);
		return c;
	}

	/** @return current value or new value */
	public static <K, V>V putAbsent(ConcurrentMap<K, V> m, K k, V v)
	{
		V v0 = m.putIfAbsent(k, v);
		return v0 != null ? v0 : v;
	}

	public static CharSequence join(Object[] s, String deli)
	{
		if (s == null || s.length == 0)
			return "";
		if (s.length == 1)
			return s[0].toString();
		StringBuilder j = new StringBuilder(100);
		j.append(s[0]);
		for (int i = 1; i < s.length; i++)
			j.append(deli).append(s[i].toString());
		return j;
	}

	public static CharSequence join(Object[] s, int begin, int end1, String deli)
	{
		if (s == null)
			return "";
		Math2.range(begin, end1, s.length);
		if (begin == end1)
			return "";
		if (begin == end1 - 1)
			return s[begin].toString();
		StringBuilder j = new StringBuilder(100);
		j.append(s[begin++].toString());
		for (; begin < end1; begin++)
			j.append(deli).append(s[begin].toString());
		return j;
	}

	public static CharSequence join(Collection<?> s, String deli)
	{
		if (s == null || s.isEmpty())
			return "";
		Iterator<?> i = s.iterator();
		Object x = i.next();
		if ( !i.hasNext())
			return x.toString();
		StringBuilder j = new StringBuilder(100);
		j.append(x.toString());
		while (i.hasNext())
			j.append(deli).append(i.next().toString());
		return j;
	}
}
