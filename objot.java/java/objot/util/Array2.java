//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;


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
		return (T[])Array.newInstance(s.getClass().getComponentType(), length);
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

	public static double[] unmaskNull(double[] x)
	{
		return x == DOUBLES0 ? null : x;
	}

	/**
	 * Search the array in [begin, end1).
	 * 
	 * @see Arrays#binarySearch(long[],long)
	 */
	public static int binarySearch(long[] a, int begin, int end1, long key)
	{
		Math2.checkRange(begin, end1, a.length);
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
		Math2.checkRange(begin, end1, a.length);
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
		Math2.checkRange(begin, end1, a.length);
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
		Math2.checkRange(begin, end1, a.length);
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
		Math2.checkRange(begin, end1, a.length);
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
		Math2.checkRange(begin, end1, a.length);
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
		Math2.checkRange(begin, end1, a.length);
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
	 * @see Arrays#binarySearch(Object[],Object)
	 */
	public static <T>int binarySearch(Comparable<? super T>[] a, int begin, int end1, T key)
	{
		Math2.checkRange(begin, end1, a.length);
		int low = begin;
		int high = end1 - 1;
		while (low <= high)
		{
			int mid = (low + high) >> 1;
			int cmp = a[mid].compareTo(key);
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
	 * @throws NullPointerException if the comparator is null, this is a difference from
	 *             {@link Arrays#binarySearch(Object[], Object, Comparator)}</code>.
	 * @see Arrays#binarySearch(Object[],Object,Comparator)
	 */
	public static <T>int binarySearch2(T[] a, int begin, int end1, T key,
		Comparator<? super T> c)
	{
		if (c == null)
			throw null;
		Math2.checkRange(begin, end1, a.length);
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

	public static byte[] ensureN(byte[] array, int n)
	{
		if (n <= array.length)
			return array;
		byte[] a = new byte[Math2.max((int)(array.length * 1.4f + 2.5f), n, 12)];
		System.arraycopy(array, 0, a, 0, array.length);
		return a;
	}

	public static int[] ensureN(int[] array, int n)
	{
		if (n <= array.length)
			return array;
		int[] a = new int[Math2.max((int)(array.length * 1.5f + 2.5f), n, 4)];
		System.arraycopy(array, 0, a, 0, array.length);
		return a;
	}

	public static <T>T[] ensureN(T[] array, int n)
	{
		if (n <= array.length)
			return array;
		T[] a = news(array, Math2.max((int)(array.length * 1.5f + 2.5f), n, 4));
		System.arraycopy(array, 0, a, 0, array.length);
		return a;
	}

	public static byte[] shrink(byte[] array, int begin, int end1)
	{
		Math2.checkRange(begin, end1, array.length);
		if (end1 - begin == array.length)
			return array;
		byte[] a = new byte[end1 - begin];
		System.arraycopy(array, begin, a, 0, end1 - begin);
		return a;
	}

	public static int[] shrink(int[] array, int begin, int end1)
	{
		Math2.checkRange(begin, end1, array.length);
		if (end1 - begin == array.length)
			return array;
		int[] a = new int[end1 - begin];
		System.arraycopy(array, begin, a, 0, end1 - begin);
		return a;
	}

	public static <T>T[] shrink(T[] array, int begin, int end1)
	{
		Math2.checkRange(begin, end1, array.length);
		if (end1 - begin == array.length)
			return array;
		T[] a = news(array, end1 - begin);
		System.arraycopy(array, begin, a, 0, end1 - begin);
		return a;
	}

	public static byte[] subClone(byte[] array, int begin, int end1)
	{
		Math2.checkRange(begin, end1, array.length);
		byte[] a = new byte[end1 - begin];
		System.arraycopy(array, begin, a, 0, end1 - begin);
		return a;
	}

	public static int[] subClone(int[] array, int begin, int end1)
	{
		Math2.checkRange(begin, end1, array.length);
		int[] a = new int[end1 - begin];
		System.arraycopy(array, begin, a, 0, end1 - begin);
		return a;
	}

	public static <T>T[] subClone(T[] array, int begin, int end1)
	{
		Math2.checkRange(begin, end1, array.length);
		T[] a = news(array, end1 - begin);
		System.arraycopy(array, begin, a, 0, end1 - begin);
		return a;
	}

	public static <T>T[] from(Collection<? extends T> s, Class<T> c)
	{
		return s.toArray(news(c, s.size()));
	}

	public static <T, S extends Collection<? super T>>S addTo(T[] s, S l)
	{
		for (T x: s)
			l.add(x);
		return l;
	}
}
