//
// Copyright 2007 Qianyan Cai
// Under the terms of the GNU General Public License version 2
//
package objot.util;

import java.nio.ByteBuffer;


public class Math2
{
	protected Math2()
	{
		throw new AbstractMethodError();
	}

	public final static int MaxPosIntDecimalN = 10;
	public final static int MaxIntDecimalN = 11;
	public final static int MaxPosLongDecimalN = 20;
	public final static int MaxLongDecimalN = 21;

	/** @return the maximum number of a, b and c */
	public static int max(int a, int b, int c)
	{
		if (a < b)
			a = b;
		return a < c ? c : a;
	}

	/** @return the maximum number of a, b and c */
	public static long max(long a, long b, long c)
	{
		if (a < b)
			a = b;
		return a < c ? c : a;
	}

	/** @return the minimum number of a, b and c */
	public static int min(int a, int b, int c)
	{
		if (a > b)
			a = b;
		return a > c ? c : a;
	}

	/** @return the minimum number of a, b and c */
	public static long min(long a, long b, long c)
	{
		if (a > b)
			a = b;
		return a > c ? c : a;
	}

	/**
	 * @return <code>x > max ? max : x < min ? min : x</code>
	 * @throws IllegalArgumentException if min > max
	 */
	public static int bound(int x, int min, int max)
	{
		if (min > max)
			throw new IllegalArgumentException();
		return x > max ? max : x < min ? min : x;
	}

	/**
	 * @return <code>x > max ? max : x < min ? min : x</code>
	 * @throws IllegalArgumentException if min > max
	 */
	public static long bound(long x, long min, long max)
	{
		if (min > max)
			throw new IllegalArgumentException();
		return x > max ? max : x < min ? min : x;
	}

	/**
	 * @throws InvalidValueException if <code>index < 0</code> or
	 *             <code>index >= length</code>
	 */
	public static void checkIndex(int index, int length)
	{
		if ((index | length - index - 1) < 0)
			throw new InvalidValueException(index);
	}

	/**
	 * @throws InvalidValueException if <code>index < 0</code> or
	 *             <code>index >= length</code>
	 */
	public static void checkIndex(long index, long length)
	{
		if ((index | length - index - 1) < 0)
			throw new InvalidValueException(index);
	}

	/**
	 * @throws InvalidValueException if <code>index < minIndex</code> or
	 *             <code>index >= length</code>
	 */
	public static void checkIndex(int index, int minIndex, int length)
	{
		if ((index - minIndex | length - index - 1) < 0)
			throw new InvalidValueException(index);
	}

	/**
	 * @throws InvalidValueException if <code>index < minIndex</code> or
	 *             <code>index >= length</code>
	 */
	public static void checkIndex(long index, long minIndex, long length)
	{
		if ((index - minIndex | length - index - 1) < 0)
			throw new InvalidValueException(index);
	}

	/**
	 * @throws InvalidLengthException if <code>length < 0</code> or
	 *             <code>length > maxLen</code>
	 */
	public static void checkLength(int length, int maxLen)
	{
		if ((length | maxLen - length) < 0)
			throw new InvalidLengthException(length, 0, maxLen);
	}

	/**
	 * @throws InvalidLengthException if <code>length < 0</code> or
	 *             <code>length > maxLen</code>
	 */
	public static void checkLength(long length, long maxLen)
	{
		if ((length | maxLen - length) < 0)
			throw new InvalidLengthException(length, 0, maxLen);
	}

	/**
	 * @throws InvalidLengthException if <code>length < minLen</code> or
	 *             <code>length > maxLen</code>
	 */
	public static void checkLength(int length, int minLen, int maxLen)
	{
		if ((length - minLen | maxLen - length) < 0)
			throw new InvalidLengthException(length, minLen, maxLen);
	}

	/**
	 * @throws InvalidLengthException if <code>length < minLen</code> or
	 *             <code>length > maxLen</code>
	 */
	public static void checkLength(long length, long minLen, long maxLen)
	{
		if ((length - minLen | maxLen - length) < 0)
			throw new InvalidLengthException(length, minLen, maxLen);
	}

	/**
	 * Similar to {@link #checkLength(int, int)}.
	 * 
	 * @throws InvalidRangeException if <code>begin < 0</code> or
	 *             <code>begin > end1</code>
	 */
	public static void checkRange(int begin, int end1)
	{
		if ((begin | end1 - begin) < 0)
			throw new InvalidRangeException(begin, end1);
	}

	/**
	 * Similar to {@link #checkLength(long, long)}.
	 * 
	 * @throws InvalidRangeException if <code>begin < 0</code> or
	 *             <code>begin > end1</code>
	 */
	public static void checkRange(long begin, long end1)
	{
		if ((begin | end1 - begin) < 0)
			throw new InvalidRangeException(begin, end1);
	}

	/**
	 * @throws InvalidRangeException if <code>begin < 0</code> or
	 *             <code>begin > end1</code> or <code>end1 > maxEnd1</code>
	 */
	public static void checkRange(int begin, int end1, int maxEnd1)
	{
		if ((begin | end1 - begin | maxEnd1 - end1) < 0)
			throw new InvalidRangeException(begin, end1, maxEnd1);
	}

	/**
	 * @throws InvalidRangeException if <code>begin < 0</code> or
	 *             <code>begin > end1</code> or <code>end1 > maxEnd1</code>
	 */
	public static void checkRange(long begin, long end1, long maxEnd1)
	{
		if ((begin | end1 - begin | maxEnd1 - end1) < 0)
			throw new InvalidRangeException(begin, end1, maxEnd1);
	}

	/** @return <code>x >= min && x <= max</code> */
	public static boolean isGELE(int x, int min, int max)
	{
		return x >= min && x <= max;
	}

	/** @return <code>x >= min && x <= max</code> */
	public static boolean isGELE(long x, long min, long max)
	{
		return x >= min && x <= max;
	}

	/** @return <code>x > min1 && x < max1</code> */
	public static boolean isGL(int x, int min1, int max1)
	{
		return x > min1 && x < max1;
	}

	/** @return <code>x > min1 && x < max1</code> */
	public static boolean isGL(long x, long min1, long max1)
	{
		return x > min1 && x < max1;
	}

	/** @return <code>x >= min && x < max1</code> */
	public static boolean isGEL(int x, int min, int max1)
	{
		return x >= min && x < max1;
	}

	/** @return <code>x >= min && x < max1</code> */
	public static boolean isGEL(long x, long min, long max1)
	{
		return x >= min && x < max1;
	}

	/** @return <code>x > min1 && x <= max</code> */
	public static boolean isGLE(int x, int min1, int max)
	{
		return x > min1 && x <= max;
	}

	/** @return <code>x > min1 && x <= max</code> */
	public static boolean isGLE(long x, long min1, long max)
	{
		return x > min1 && x <= max;
	}

	/** @return <code>x <= min || x >= max</code> */
	public static boolean isLEGE(int x, int min, int max)
	{
		return x <= min || x >= max;
	}

	/** @return <code>x <= min || x >= max</code> */
	public static boolean isLEGE(long x, long min, long max)
	{
		return x <= min || x >= max;
	}

	/** @return <code>x < min1 || x > max1</code> */
	public static boolean isLG(int x, int min1, int max1)
	{
		return x < min1 || x > max1;
	}

	/** @return <code>x < min1 || x > max1</code> */
	public static boolean isLG(long x, long min1, long max1)
	{
		return x < min1 || x > max1;
	}

	/** @return <code>x <= min || x > max1</code> */
	public static boolean isLEG(int x, int min, int max1)
	{
		return x <= min || x > max1;
	}

	/** @return <code>x <= min || x > max1</code> */
	public static boolean isLEG(long x, long min, long max1)
	{
		return x <= min || x > max1;
	}

	/** @return <code>x < min1 || x >= max</code> */
	public static boolean isLGE(int x, int min1, int max)
	{
		return x < min1 || x >= max;
	}

	/** @return <code>x < min1 || x >= max</code> */
	public static boolean isLGE(long x, long min1, long max)
	{
		return x < min1 || x >= max;
	}

	/** @return <code>x > 0 ? x : 1</code> */
	public static int pos(int x)
	{
		return x > 0 ? x : 1;
	}

	/** @return <code>x > 0 ? x : 1</code> */
	public static long pos(long x)
	{
		return x > 0 ? x : 1;
	}

	/** @return <code>x >= 0 ? x : 0</code> */
	public static int pos0(int x)
	{
		return x >= 0 ? x : 0;
	}

	/** @return <code>x >= 0 ? x : 0</code> */
	public static long pos0(long x)
	{
		return x >= 0 ? x : 0;
	}

	/** @return <code>x < 0 ? x : -1</code> */
	public static int neg(int x)
	{
		return x < 0 ? x : -1;
	}

	/** @return <code>x < 0 ? x : -1</code> */
	public static long neg(long x)
	{
		return x < 0 ? x : -1;
	}

	/** @return <code>x <= 0 ? x : 0</code> */
	public static int neg0(int x)
	{
		return x <= 0 ? x : 0;
	}

	/** @return <code>x <= 0 ? x : 0</code> */
	public static long neg0(long x)
	{
		return x <= 0 ? x : 0;
	}

	/** @return <code>x >= 0 ? x : {@link Integer#MAX_VALUE}</code> */
	public static int posOver(int x)
	{
		return x >= 0 ? x : Integer.MAX_VALUE;
	}

	/** @return <code>x >= 0 ? x : {@link Long#MAX_VALUE}</code> */
	public static long posOver(long x)
	{
		return x >= 0 ? x : Long.MAX_VALUE;
	}

	/** @return <code>x < 0 ? x : {@link Integer#MIN_VALUE}</code> */
	public static int negOver(int x)
	{
		return x < 0 ? x : Integer.MIN_VALUE;
	}

	/** @return <code>x < 0 ? x : {@link Long#MIN_VALUE}</code> */
	public static long negOver(long x)
	{
		return x < 0 ? x : Long.MIN_VALUE;
	}

	/**
	 * @return a + b
	 * @throws ArithmeticException if positive/negative overflow
	 */
	public static int add(int a, int b)
	{
		int s = a + b;
		if (b >= 0 == s < a)
			throw new ArithmeticException("overflow");
		return s;
	}

	/**
	 * @return a + b
	 * @throws ArithmeticException if positive/negative overflow
	 */
	public static long add(long a, long b)
	{
		long s = a + b;
		if (b >= 0 == s < a)
			throw new ArithmeticException("overflow");
		return s;
	}

	/**
	 * @return a + b, or {@link Integer#MAX_VALUE} if positive overflow, or
	 *         {@link Integer#MIN_VALUE} if negative overflow
	 */
	public static int addOver(int a, int b)
	{
		int s = a + b;
		if (b >= 0)
			return s >= a ? s : Integer.MAX_VALUE;
		return s < a ? s : Integer.MIN_VALUE;
	}

	/**
	 * @return a + b, or {@link Integer#MAX_VALUE} if positive overflow, or
	 *         {@link Integer#MIN_VALUE} if negative overflow
	 */
	public static long addOver(long a, long b)
	{
		long s = a + b;
		if (b >= 0)
			return s >= a ? s : Long.MAX_VALUE;
		return s < a ? s : Long.MIN_VALUE;
	}

	/**
	 * @return <code>divisor >= 0 ? x >= 0 ? x / divisor : (x + 1) / divisor - 1
	 *         : x > 0 ? (x - 1) / divisor - 1 : x / divisor</code>
	 */
	public static int divFloor(int x, int divisor)
	{
		return divisor >= 0 ? x >= 0 ? x / divisor : (x + 1) / divisor - 1 : //
			x > 0 ? (x - 1) / divisor - 1 : x / divisor;
	}

	/**
	 * @return <code>divisor >= 0 ? x >= 0 ? x / divisor : (x + 1) / divisor - 1
	 *         : x > 0 ? (x - 1) / divisor - 1 : x / divisor</code>
	 */
	public static long divFloor(long x, long divisor)
	{
		return divisor >= 0 ? x >= 0 ? x / divisor : (x + 1) / divisor - 1 : //
			x > 0 ? (x - 1) / divisor - 1 : x / divisor;
	}

	/**
	 * @return <code>divisor >= 0 ? x >= 0 ? x % divisor : divisor - 1 + (x + 1) % divisor
	 *         : x > 0 ? divisor + 1 + (x - 1) % divisor : x % divisor</code>
	 */
	public static int modFloor(int x, int divisor)
	{
		return divisor >= 0 ? x >= 0 ? x % divisor : divisor - 1 + (x + 1) % divisor : //
			x > 0 ? divisor + 1 + (x - 1) % divisor : x % divisor;
	}

	/**
	 * @return <code>divisor >= 0 ? x >= 0 ? x % divisor : divisor - 1 + (x + 1) % divisor
	 *         : x > 0 ? divisor + 1 + (x - 1) % divisor : x % divisor</code>
	 */
	public static long modFloor(long x, long divisor)
	{
		return divisor >= 0 ? x >= 0 ? x % divisor : divisor - 1 + (x + 1) % divisor : //
			x > 0 ? divisor + 1 + (x - 1) % divisor : x % divisor;
	}

	/**
	 * @return <code>divisor != {@link Integer#MIN_VALUE} ?
	 *          -divFloor(x, -divisor) : x >= 0 ? 0 : 1</code>
	 */
	public static int divCeiling(int x, int divisor)
	{
		return divisor != Integer.MIN_VALUE ? -divFloor(x, -divisor) : x >= 0 ? 0 : 1;
	}

	/**
	 * @return <code>divisor != {@link Long#MIN_VALUE} ?
	 *          -divFloor(x, -divisor) : x >= 0 ? 0 : 1</code>
	 */
	public static long divCeiling(long x, long divisor)
	{
		return divisor != Long.MIN_VALUE ? -divFloor(x, -divisor) : x >= 0 ? 0 : 1;
	}

	/**
	 * @return <code>divisor != {@link Integer#MIN_VALUE} ?
	 *         modFloor(x, -divisor) : x >= 0 ? x : x - divisor</code>
	 */
	public static int modCeiling(int x, int divisor)
	{
		return divisor != Integer.MIN_VALUE ? modFloor(x, -divisor) : //
			x >= 0 ? x : x - divisor;
	}

	/**
	 * @return <code>divisor != {@link Long#MIN_VALUE} ?
	 *         modFloor(x, -divisor) : x >= 0 ? x : x - divisor</code>
	 */
	public static long modCeiling(long x, long divisor)
	{
		return divisor != Long.MIN_VALUE ? modFloor(x, -divisor) : //
			x >= 0 ? x : x - divisor;
	}

	/**
	 * @return <code>x - modFloor(x, block)</code>
	 * @throws IllegalArgumentException if block < 0
	 * @throws ArithmeticException if negative overflow
	 */
	public static int blockFloor(int x, int block)
	{
		if (block < 0)
			throw new IllegalArgumentException();
		int f = x - modFloor(x, block);
		if (f > x)
			throw new ArithmeticException("negative overflow");
		return f;
	}

	/**
	 * @return <code>x - modFloor(x, block)</code>
	 * @throws IllegalArgumentException if block < 0
	 * @throws ArithmeticException if negative overflow
	 */
	public static long blockFloor(long x, long block)
	{
		if (block < 0)
			throw new IllegalArgumentException();
		long f = x - modFloor(x, block);
		if (f > x)
			throw new ArithmeticException("negative overflow");
		return f;
	}

	/**
	 * @return <code>x - modFloor(x, block)</code>, or {@link Integer#MIN_VALUE} if
	 *         overflow
	 * @throws IllegalArgumentException if block < 0
	 */
	public static int blockFloorOver(int x, int block)
	{
		if (block < 0)
			throw new IllegalArgumentException();
		int f = x - modFloor(x, block);
		return f <= x ? f : Integer.MIN_VALUE;
	}

	/**
	 * @return <code>x - modFloor(x, block)</code>, or {@link Long#MIN_VALUE} if
	 *         overflow
	 * @throws IllegalArgumentException if block < 0
	 */
	public static long blockFloorOver(long x, long block)
	{
		if (block < 0)
			throw new IllegalArgumentException();
		long f = x - modFloor(x, block);
		return f <= x ? f : Long.MIN_VALUE;
	}

	/**
	 * @return <code>x - modCeiling(x, block)</code>
	 * @throws IllegalArgumentException if block < 0
	 * @throws ArithmeticException if positive overflow
	 */
	public static int blockCeiling(int x, int block)
	{
		if (block < 0)
			throw new IllegalArgumentException();
		int f = x - modCeiling(x, block); // x - (-v)
		if (f < x)
			throw new ArithmeticException("positive overflow");
		return f;
	}

	/**
	 * @return <code>x - modCeiling(x, block)</code>
	 * @throws IllegalArgumentException if block < 0
	 * @throws ArithmeticException if positive overflow
	 */
	public static long blockCeiling(long x, long block)
	{
		if (block < 0)
			throw new IllegalArgumentException();
		long f = x - modCeiling(x, block);
		if (f < x)
			throw new ArithmeticException("positive overflow");
		return f;
	}

	/**
	 * @return <code>x - modCeiling(x, block)</code>, or {@link Integer#MAX_VALUE} if
	 *         overflow
	 * @throws IllegalArgumentException if block < 0
	 */
	public static int blockCeilingOver(int x, int block)
	{
		if (block < 0)
			throw new IllegalArgumentException();
		int f = x - modCeiling(x, block);
		return f > x ? f : Integer.MAX_VALUE;
	}

	/**
	 * @return <code>x - modCeiling(x, block)</code>, or {@link Long#MAX_VALUE} if
	 *         overflow
	 * @throws IllegalArgumentException if block < 0
	 */
	public static long blockCeilingOver(long x, long block)
	{
		if (block < 0)
			throw new IllegalArgumentException();
		long f = x - modCeiling(x, block);
		return f > x ? f : Long.MAX_VALUE;
	}

	/** @return <code>x > 0 && (x & x - 1) == 0</code>, wether x is a power of 2 */
	public static boolean powerOf2(int x)
	{
		return x > 0 && (x & x - 1) == 0;
	}

	/** @return decimal value for hex char, negative for invalid hex char */
	public static int hexToDec(char hex)
	{
		switch (hex)
		{
		case '0':
			return 0;
		case '1':
			return 1;
		case '2':
			return 2;
		case '3':
			return 3;
		case '4':
			return 4;
		case '5':
			return 5;
		case '6':
			return 6;
		case '7':
			return 7;
		case '8':
			return 8;
		case '9':
			return 9;
		}
		switch (hex)
		{
		case 'a':
			return 10;
		case 'b':
			return 11;
		case 'c':
			return 12;
		case 'd':
			return 13;
		case 'e':
			return 14;
		case 'f':
			return 15;
		}
		switch (hex)
		{
		case 'A':
			return 10;
		case 'B':
			return 11;
		case 'C':
			return 12;
		case 'D':
			return 13;
		case 'E':
			return 14;
		case 'F':
			return 15;
		}
		return -1;
	}

	/**
	 * @return decimal value for hex char
	 * @throws IllegalArgumentException if the char is invalid
	 */
	public static int hexToDecThrows(char hex)
	{
		int v = hexToDec(hex);
		if (v < 0)
			throw new IllegalArgumentException(String.valueOf(hex));
		return v;
	}

	private static final char[] HEXS_LOW = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
		'9', 'a', 'b', 'c', 'd', 'e', 'f' };

	private static final char[] HEXS_UP = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'A', 'B', 'C', 'D', 'E', 'F' };

	/**
	 * @return the lower-case hex char for one decimal value
	 * @throws InvalidValueException
	 */
	public static char decToHexLow(int dec)
	{
		return HEXS_LOW[dec];
	}

	/**
	 * @return the upper-case hex char for one decimal value
	 * @throws InvalidValueException
	 */
	public static char decToHexUp(int dec)
	{
		return HEXS_UP[dec];
	}

	/**
	 * Appends 8 lower-case hex chars to the builder.
	 * 
	 * @return the builder
	 * @throws InvalidValueException
	 */
	public static StringBuilder intTo8HexsLow(int v, StringBuilder b)
	{
		b.append(HEXS_LOW[v >>> 28 & 0xF]);
		b.append(HEXS_LOW[v >>> 24 & 0xF]);
		b.append(HEXS_LOW[v >>> 20 & 0xF]);
		b.append(HEXS_LOW[v >>> 16 & 0xF]);
		b.append(HEXS_LOW[v >>> 12 & 0xF]);
		b.append(HEXS_LOW[v >>> 8 & 0xF]);
		b.append(HEXS_LOW[v >>> 4 & 0xF]);
		b.append(HEXS_LOW[v & 0xF]);
		return b;
	}

	/**
	 * Appends 8 upper-case hex chars to the builder.
	 * 
	 * @return the builder
	 * @throws InvalidValueException
	 */
	public static StringBuilder intTo8HexsUp(int v, StringBuilder b)
	{
		b.append(HEXS_UP[v >>> 28 & 0xF]);
		b.append(HEXS_UP[v >>> 24 & 0xF]);
		b.append(HEXS_UP[v >>> 20 & 0xF]);
		b.append(HEXS_UP[v >>> 16 & 0xF]);
		b.append(HEXS_UP[v >>> 12 & 0xF]);
		b.append(HEXS_UP[v >>> 8 & 0xF]);
		b.append(HEXS_UP[v >>> 4 & 0xF]);
		b.append(HEXS_UP[v & 0xF]);
		return b;
	}

	/**
	 * Appends 16 lower-case hex chars to the builder.
	 * 
	 * @return the builder
	 * @throws InvalidValueException
	 */
	public static StringBuilder longTo16HexsLow(long v, StringBuilder b)
	{
		intTo8HexsLow((int)(v >> 32), b);
		intTo8HexsLow((int)v, b);
		return b;
	}

	/**
	 * Appends 16 upper-case hex chars to the builder.
	 * 
	 * @return the builder
	 * @throws InvalidValueException
	 */
	public static StringBuilder longTo16HexsUp(long v, StringBuilder b)
	{
		intTo8HexsUp((int)(v >> 32), b);
		intTo8HexsUp((int)v, b);
		return b;
	}

	/**
	 * Puts 8 lower-case hex char-bytes to the buffer.
	 * 
	 * @return the buffer
	 * @throws InvalidValueException
	 * @throws RuntimeException from {@link ByteBuffer#put(byte)}
	 */
	public static ByteBuffer intTo8HexsLow(int v, ByteBuffer b)
	{
		b.put((byte)HEXS_LOW[v >>> 28 & 0xF]);
		b.put((byte)HEXS_LOW[v >>> 24 & 0xF]);
		b.put((byte)HEXS_LOW[v >>> 20 & 0xF]);
		b.put((byte)HEXS_LOW[v >>> 16 & 0xF]);
		b.put((byte)HEXS_LOW[v >>> 12 & 0xF]);
		b.put((byte)HEXS_LOW[v >>> 8 & 0xF]);
		b.put((byte)HEXS_LOW[v >>> 4 & 0xF]);
		b.put((byte)HEXS_LOW[v & 0xF]);
		return b;
	}

	/**
	 * Puts 8 upper-case hex char-bytes to the buffer.
	 * 
	 * @return the buffer
	 * @throws InvalidValueException
	 * @throws RuntimeException from {@link ByteBuffer#put(byte)}
	 */
	public static ByteBuffer intTo8HexsUp(int v, ByteBuffer b)
	{
		b.put((byte)HEXS_UP[v >>> 28 & 0xF]);
		b.put((byte)HEXS_UP[v >>> 24 & 0xF]);
		b.put((byte)HEXS_UP[v >>> 20 & 0xF]);
		b.put((byte)HEXS_UP[v >>> 16 & 0xF]);
		b.put((byte)HEXS_UP[v >>> 12 & 0xF]);
		b.put((byte)HEXS_UP[v >>> 8 & 0xF]);
		b.put((byte)HEXS_UP[v >>> 4 & 0xF]);
		b.put((byte)HEXS_UP[v & 0xF]);
		return b;
	}

	/**
	 * Puts 16 lower-case hex char-bytes to the buffer.
	 * 
	 * @return the buffer
	 * @throws InvalidValueException
	 * @throws RuntimeException from {@link ByteBuffer#put(byte)}
	 */
	public static ByteBuffer longTo16HexsLow(long v, ByteBuffer b)
	{
		intTo8HexsLow((int)(v >> 32), b);
		intTo8HexsLow((int)v, b);
		return b;
	}

	/**
	 * Puts 16 upper-case hex char-bytes to the buffer.
	 * 
	 * @return the buffer
	 * @throws InvalidValueException
	 * @throws RuntimeException from {@link ByteBuffer#put(byte)}
	 */
	public static ByteBuffer longTo16HexsUp(long v, ByteBuffer b)
	{
		intTo8HexsUp((int)(v >> 32), b);
		intTo8HexsUp((int)v, b);
		return b;
	}
}
