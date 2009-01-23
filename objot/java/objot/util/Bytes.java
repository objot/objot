//
// Copyright 2007-2009 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.util;

import java.io.IOException;
import java.io.InputStream;


public class Bytes
{
	public byte[] bytes = Array2.BYTES0;
	public int beginBi;
	public int end1Bi;

	public Bytes()
	{
	}

	public Bytes(byte[] bs)
	{
		this(bs, 0, bs != null ? bs.length : 0);
	}

	public Bytes(byte[] bs, int begin, int end1)
	{
		bytes = bs != null ? bs : Array2.BYTES0;
		Math2.range(begin, end1, bytes.length);
		beginBi = begin;
		end1Bi = end1;
	}

	public Bytes(Bytes bs)
	{
		this(bs, 0, bs.byteN());
	}

	public Bytes(Bytes bs, int begin, int end1)
	{
		bytes = bs.bytes;
		Math2.range(begin, end1, bs.end1Bi - bs.beginBi);
		beginBi = bs.beginBi + begin;
		end1Bi = bs.beginBi + end1;
	}

	public Bytes(InputStream i, boolean close) throws IOException
	{
		inputFull(i, close);
	}

	public int byteN()
	{
		return end1Bi - beginBi;
	}

	public int lastI()
	{
		return end1Bi - beginBi - 1;
	}

	public int copyTo(int bi, byte[] dest, int destBi, int bn)
	{
		Math2.range(bi, bi + bn, end1Bi - beginBi);
		System.arraycopy(bytes, bi + beginBi, dest, destBi, bn);
		return destBi + bn;
	}

	public int copyTo(int bi, Bytes dest, int destBi, int bn)
	{
		Math2.range(bi, bi + bn, end1Bi - beginBi);
		Math2.range(destBi, destBi + bn, dest.end1Bi - dest.beginBi);
		System.arraycopy(bytes, bi + beginBi, dest.bytes, destBi + dest.beginBi, bn);
		return destBi + bn;
	}

	public final boolean equals(byte[] bs)
	{
		return equals(bs, 0, bs.length);
	}

	public boolean equals(byte[] bs, int begin, int end1)
	{
		Math2.range(begin, end1, bs.length);
		if (end1Bi - beginBi != end1 - begin)
			return false;
		if (bytes != bs || beginBi != begin)
			for (int i = beginBi, bsi = begin; bsi < end1; i++, bsi++)
				if (bytes[i] != bs[bsi])
					return false;
		return true;
	}

	public final boolean equals(Bytes bs)
	{
		return equals(bs, 0, bs.end1Bi - bs.beginBi);
	}

	public boolean equals(Bytes bs, int begin, int end1)
	{
		Math2.range(begin, end1, bs.end1Bi - bs.beginBi);
		if (end1Bi - beginBi != end1 - begin)
			return false;
		if (bytes != bs.bytes || beginBi != begin + bs.beginBi)
			for (int i = beginBi, bsi = begin + bs.beginBi; i < end1Bi; i++, bsi++)
				if (bytes[i] != bs.bytes[bsi])
					return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		int h = 1;
		for (int i = beginBi; i < end1Bi; i++)
			h = 31 * h + bytes[i];
		return h;
	}

	@Override
	public boolean equals(Object o)
	{
		return o != null && o.getClass() == getClass() && equals((Bytes)o);
	}

	public byte readS1(int i)
	{
		Math2.index(i, end1Bi - beginBi);
		i += beginBi;
		return bytes[i];
	}

	public int readU1(int i)
	{
		Math2.index(i, end1Bi - beginBi);
		i += beginBi;
		return bytes[i] & 0xFF;
	}

	public short readS2(int i)
	{
		Math2.index(i, end1Bi - beginBi - 1);
		i += beginBi;
		return (short)(bytes[i] << 8 | bytes[i + 1] & 0xFF);
	}

	public int readU2(int i)
	{
		Math2.index(i, end1Bi - beginBi - 1);
		i += beginBi;
		return bytes[i] << 8 & 0xFF00 | bytes[i + 1] & 0xFF;
	}

	public int readS4(int i)
	{
		Math2.index(i, end1Bi - beginBi - 3);
		i += beginBi;
		return bytes[i] << 24 | (bytes[i + 1] & 0xFF) << 16 //
			| (bytes[i + 2] & 0xFF) << 8 | bytes[i + 3] & 0xFF;
	}

	public int readU4(int i)
	{
		Math2.index(i, end1Bi - beginBi - 3);
		i += beginBi;
		i = bytes[i] << 24 | (bytes[i + 1] & 0xFF) << 16 //
			| (bytes[i + 2] & 0xFF) << 8 | bytes[i + 3] & 0xFF;
		if (i < 0)
			throw new ArithmeticException("unsigned quad bytes too large : 0x"
				+ Integer.toHexString(i));
		return i;
	}

	public long readU4ex(int i)
	{
		Math2.index(i, end1Bi - beginBi - 3);
		i += beginBi;
		return (bytes[i] & 0xFFL) << 24 | ((bytes[i + 1] & 0xFF) << 16 //
			| (bytes[i + 2] & 0xFF) << 8 | bytes[i + 3] & 0xFF);
	}

	public long readS8(int i)
	{
		Math2.index(i, end1Bi - beginBi - 7);
		i += beginBi;
		return (long)(bytes[i] << 24 | (bytes[i + 1] & 0xFF) << 16 //
			/**/| (bytes[i + 2] & 0xFF) << 8 | bytes[i + 3] & 0xFF) << 32 //
			| (bytes[i + 4] << 24 | (bytes[i + 5] & 0xFF) << 16 //
				| (bytes[i + 6] & 0xFF) << 8 | bytes[i + 7] & 0xFF) & 0xFFFFFFFFL;
	}

	public byte read0s1(int i)
	{
		return bytes[i];
	}

	public int read0u1(int i)
	{
		return bytes[i] & 0xFF;
	}

	public short read0s2(int i)
	{
		return (short)(bytes[i] << 8 | bytes[i + 1] & 0xFF);
	}

	public int read0u2(int i)
	{
		return bytes[i] << 8 & 0xFF00 | bytes[i + 1] & 0xFF;
	}

	public int read0s4(int i)
	{
		return bytes[i] << 24 | (bytes[i + 1] & 0xFF) << 16 //
			| (bytes[i + 2] & 0xFF) << 8 | bytes[i + 3] & 0xFF;
	}

	public int read0u4(int i)
	{
		i = bytes[i] << 24 | (bytes[i + 1] & 0xFF) << 16 //
			| (bytes[i + 2] & 0xFF) << 8 | bytes[i + 3] & 0xFF;
		if (i < 0)
			throw new ArithmeticException("unsigned quad bytes too large : 0x"
				+ Integer.toHexString(i));
		return i;
	}

	public long read0u4ex(int i)
	{
		return (bytes[i] & 0xFFL) << 24 | ((bytes[i + 1] & 0xFF) << 16 //
			| (bytes[i + 2] & 0xFF) << 8 | bytes[i + 3] & 0xFF);
	}

	public long read0s8(int i)
	{
		return (long)(bytes[i] << 24 | (bytes[i + 1] & 0xFF) << 16 //
			/**/| (bytes[i + 2] & 0xFF) << 8 | bytes[i + 3] & 0xFF) << 32 //
			| (bytes[i + 4] << 24 | (bytes[i + 5] & 0xFF) << 16 //
				| (bytes[i + 6] & 0xFF) << 8 | bytes[i + 7] & 0xFF) & 0xFFFFFFFFL;
	}

	public static byte readS1(byte[] bs, int i)
	{
		return bs[i];
	}

	public static int readU1(byte[] bs, int i)
	{
		return bs[i] & 0xFF;
	}

	public static short readS2(byte[] bs, int i)
	{
		return (short)(bs[i] << 8 | bs[i + 1] & 0xFF);
	}

	public static int readU2(byte[] bs, int i)
	{
		return bs[i] << 8 & 0xFF00 | bs[i + 1] & 0xFF;
	}

	public static int readS4(byte[] bs, int i)
	{
		return bs[i] << 24 | (bs[i + 1] & 0xFF) << 16 //
			| (bs[i + 2] & 0xFF) << 8 | bs[i + 3] & 0xFF;
	}

	/** @throws ArithmeticException if negative. */
	public static int readU4(byte[] bs, int i)
	{
		i = bs[i] << 24 | (bs[i + 1] & 0xFF) << 16 //
			| (bs[i + 2] & 0xFF) << 8 | bs[i + 3] & 0xFF;
		if (i < 0)
			throw new ArithmeticException("unsigned quad bytes too large : 0x"
				+ Integer.toHexString(i));
		return i;
	}

	public static long readU4ex(byte[] bs, int i)
	{
		return (bs[i] & 0xFFL) << 24 | ((bs[i + 1] & 0xFF) << 16 //
			| (bs[i + 2] & 0xFF) << 8 | bs[i + 3] & 0xFF);
	}

	public static long readS8(byte[] bs, int i)
	{
		return (long)(bs[i] << 24 | (bs[i + 1] & 0xFF) << 16 //
			/**/| (bs[i + 2] & 0xFF) << 8 | bs[i + 3] & 0xFF) << 32 //
			| (bs[i + 4] << 24 | (bs[i + 5] & 0xFF) << 16 //
				| (bs[i + 6] & 0xFF) << 8 | bs[i + 7] & 0xFF) & 0xFFFFFFFFL;
	}

	// ********************************************************************************

	public int copyFrom(int bi, byte[] src, int srcBi, int bn)
	{
		Math2.range(bi, bi + bn, end1Bi - beginBi);
		System.arraycopy(src, srcBi, bytes, bi + beginBi, bn);
		return bi + bn;
	}

	/** {@link #beginBi} unchanged, call {@link #ensureByteN} if necessary */
	public int inputFull(InputStream i, boolean close) throws IOException
	{
		try
		{
			end1Bi = beginBi;
			int len = 0;
			do
				ensureByteN((end1Bi += len) + i.available() + 1 - beginBi);
			while ((len = i.read(bytes, end1Bi, bytes.length - end1Bi)) > 0);
			return end1Bi - beginBi;
		}
		finally
		{
			if (close)
				try
				{
					i.close();
				}
				catch (Throwable e)
				{
				}
		}
	}

	public Bytes ensureByteN(int n)
	{
		n += beginBi;
		if (n < 0)
			throw new InvalidLengthException();
		bytes = Array2.ensureN(bytes, n);
		return this;
	}

	public Bytes addByteN(int n)
	{
		n += end1Bi;
		if (n < 0)
			throw new InvalidLengthException();
		bytes = Array2.ensureN(bytes, n);
		end1Bi = n;
		return this;
	}

	public void writeS1(int i, byte v)
	{
		Math2.index(i, end1Bi - beginBi);
		i += beginBi;
		bytes[i] = v;
	}

	public void writeS1(int i, int v)
	{
		if (v << 24 >> 24 != v)
			throw new ArithmeticException("invalid signed byte");
		Math2.index(i, end1Bi - beginBi);
		i += beginBi;
		bytes[i] = (byte)v;
	}

	public void writeU1(int i, int v)
	{
		if (v >> 8 != 0)
			throw new ArithmeticException("invalid unsigned byte");
		Math2.index(i, end1Bi - beginBi);
		i += beginBi;
		bytes[i] = (byte)v;
	}

	public void writeS2(int i, short v)
	{
		Math2.index(i, end1Bi - beginBi - 1);
		i += beginBi;
		bytes[i] = (byte)(v >> 8);
		bytes[i + 1] = (byte)v;
	}

	public void writeS2(int i, int v)
	{
		if (v << 16 >> 16 != v)
			throw new ArithmeticException("invalid signed dual bytes");
		Math2.index(i, end1Bi - beginBi - 1);
		i += beginBi;
		bytes[i] = (byte)(v >> 8);
		bytes[i + 1] = (byte)v;
	}

	public void writeU2(int i, int v)
	{
		if (v >> 16 != 0)
			throw new ArithmeticException("invalid unsigned dual bytes");
		Math2.index(i, end1Bi - beginBi - 1);
		i += beginBi;
		bytes[i] = (byte)(v >> 8);
		bytes[i + 1] = (byte)v;
	}

	public void writeS4(int i, int v)
	{
		Math2.index(i, end1Bi - beginBi - 3);
		i += beginBi;
		bytes[i] = (byte)(v >> 24);
		bytes[i + 1] = (byte)(v >> 16);
		bytes[i + 2] = (byte)(v >> 8);
		bytes[i + 3] = (byte)v;
	}

	public void writeU4(int i, long v_)
	{
		if (v_ >> 32 != 0)
			throw new ArithmeticException("invalid unsigned quad bytes");
		Math2.index(i, end1Bi - beginBi - 3);
		i += beginBi;
		int v = (int)v_;
		bytes[i] = (byte)(v >> 24);
		bytes[i + 1] = (byte)(v >> 16);
		bytes[i + 2] = (byte)(v >> 8);
		bytes[i + 3] = (byte)v;
	}

	public void writeS8(int i, long v)
	{
		Math2.index(i, end1Bi - beginBi - 7);
		i += beginBi;
		int v4 = (int)(v >> 32);
		bytes[i] = (byte)(v4 >> 24);
		bytes[i + 1] = (byte)(v4 >> 16);
		bytes[i + 2] = (byte)(v4 >> 8);
		bytes[i + 3] = (byte)v4;
		v4 = (int)v;
		bytes[i + 4] = (byte)(v4 >> 24);
		bytes[i + 5] = (byte)(v4 >> 16);
		bytes[i + 6] = (byte)(v4 >> 8);
		bytes[i + 7] = (byte)v4;
	}

	public void write0s1(int i, byte v)
	{
		bytes[i] = v;
	}

	public void write0s1(int i, int v)
	{
		if (v << 24 >> 24 != v)
			throw new ArithmeticException("invalid signed byte");
		bytes[i] = (byte)v;
	}

	public void write0u1(int i, int v)
	{
		if (v >> 8 != 0)
			throw new ArithmeticException("invalid unsigned byte");
		bytes[i] = (byte)v;
	}

	public void write0s2(int i, short v)
	{
		bytes[i] = (byte)(v >> 8);
		bytes[i + 1] = (byte)v;
	}

	public void write0s2(int i, int v)
	{
		if (v << 16 >> 16 != v)
			throw new ArithmeticException("invalid signed dual bytes");
		bytes[i] = (byte)(v >> 8);
		bytes[i + 1] = (byte)v;
	}

	public void write0u2(int i, int v)
	{
		if (v >> 16 != 0)
			throw new ArithmeticException("invalid unsigned dual bytes");
		bytes[i] = (byte)(v >> 8);
		bytes[i + 1] = (byte)v;
	}

	public void write0s4(int i, int v)
	{
		bytes[i] = (byte)(v >> 24);
		bytes[i + 1] = (byte)(v >> 16);
		bytes[i + 2] = (byte)(v >> 8);
		bytes[i + 3] = (byte)v;
	}

	public void write0u4(int i, long v_)
	{
		if (v_ >> 32 != 0)
			throw new ArithmeticException("invalid unsigned quad bytes");
		int v = (int)v_;
		bytes[i] = (byte)(v >> 24);
		bytes[i + 1] = (byte)(v >> 16);
		bytes[i + 2] = (byte)(v >> 8);
		bytes[i + 3] = (byte)v;
	}

	public void write0s8(int i, long v)
	{
		int v4 = (int)(v >> 32);
		bytes[i] = (byte)(v4 >> 24);
		bytes[i + 1] = (byte)(v4 >> 16);
		bytes[i + 2] = (byte)(v4 >> 8);
		bytes[i + 3] = (byte)v4;
		v4 = (int)v;
		bytes[i + 4] = (byte)(v4 >> 24);
		bytes[i + 5] = (byte)(v4 >> 16);
		bytes[i + 6] = (byte)(v4 >> 8);
		bytes[i + 7] = (byte)v4;
	}

	public static void writeS1(byte[] bs, int i, byte v)
	{
		bs[i] = v;
	}

	/** @throws ArithmeticException if not in [-128, 128). */
	public static void writeS1(byte[] bs, int i, int v)
	{
		if (v << 24 >> 24 != v)
			throw new ArithmeticException("invalid signed byte");
		bs[i] = (byte)v;
	}

	/** @throws ArithmeticException if not in [0, 256). */
	public static void writeU1(byte[] bs, int i, int v)
	{
		if (v >> 8 != 0)
			throw new ArithmeticException("invalid unsigned byte");
		bs[i] = (byte)v;
	}

	public static void writeS2(byte[] bs, int i, short v)
	{
		bs[i] = (byte)(v >> 8);
		bs[i + 1] = (byte)v;
	}

	/** @throws ArithmeticException if not in [-32768, 32768). */
	public static void writeS2(byte[] bs, int i, int v)
	{
		if (v << 16 >> 16 != v)
			throw new ArithmeticException("invalid signed dual bytes");
		bs[i] = (byte)(v >> 8);
		bs[i + 1] = (byte)v;
	}

	/** @throws ArithmeticException if not in [0, 65536). */
	public static void writeU2(byte[] bs, int i, int v)
	{
		if (v >> 16 != 0)
			throw new ArithmeticException("invalid unsigned dual bytes");
		bs[i] = (byte)(v >> 8);
		bs[i + 1] = (byte)v;
	}

	public static void writeS4(byte[] bs, int i, int v)
	{
		bs[i] = (byte)(v >> 24);
		bs[i + 1] = (byte)(v >> 16);
		bs[i + 2] = (byte)(v >> 8);
		bs[i + 3] = (byte)v;
	}

	public static void writeU4(byte[] bs, int i, long v_)
	{
		if (v_ >> 32 != 0)
			throw new ArithmeticException("invalid unsigned quad bytes");
		int v = (int)v_;
		bs[i] = (byte)(v >> 24);
		bs[i + 1] = (byte)(v >> 16);
		bs[i + 2] = (byte)(v >> 8);
		bs[i + 3] = (byte)v;
	}

	public static void writeS8(byte[] bs, int i, long v)
	{
		int v4 = (int)(v >> 32);
		bs[i] = (byte)(v4 >> 24);
		bs[i + 1] = (byte)(v4 >> 16);
		bs[i + 2] = (byte)(v4 >> 8);
		bs[i + 3] = (byte)v4;
		v4 = (int)v;
		bs[i + 4] = (byte)(v4 >> 24);
		bs[i + 5] = (byte)(v4 >> 16);
		bs[i + 6] = (byte)(v4 >> 8);
		bs[i + 7] = (byte)v4;
	}
}
