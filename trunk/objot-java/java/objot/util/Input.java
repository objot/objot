//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Library General Public License version 2
//
package objot.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;


public class Input
{
	protected Input()
	{
		throw new AbstractMethodError();
	}

	public static byte[] readFull(InputStream in, byte[] bs, int begin, int end1)
		throws IOException
	{
		Math2.range(begin, end1, bs.length);
		for (int n; begin < end1; begin += n)
			if ((n = in.read(bs, begin, end1 - begin)) <= 0)
				throw new EOFException();
		return bs;
	}

	public static class Line
		extends InputStream
	{
		public static final Charset UTF = Charset.forName("UTF-8");

		public InputStream in;
		public Charset cs;
		public byte preEol;
		public byte eol;
		public byte[] bs;
		public int begin;
		public int end1;
		/** total number of read bytes */
		public long readBn;

		public Line(InputStream in_)
		{
			in = in_;
			cs = UTF;
			preEol = '\r';
			eol = '\n';
			bs = new byte[8112];
		}

		public Line(InputStream in_, int maxLen)
		{
			in = in_;
			bs = new byte[maxLen + 2];
		}

		/** set charset, default is {@link #UTF} */
		public Line charset(Charset cs_)
		{
			cs = cs_ != null ? cs_ : UTF;
			return this;
		}

		public Line preEol(byte v)
		{
			preEol = v;
			return this;
		}

		public Line eol(byte v)
		{
			eol = v;
			return this;
		}

		@Override
		public void close() throws IOException
		{
			bs = null;
			in.close();
		}

		@Override
		public int available() throws IOException
		{
			return Math2.addOver(end1 - begin, in.available());
		}

		@Override
		public int read() throws IOException
		{
			if (begin < end1)
				return bs[begin++] & 255;
			int n = in.read(bs, 0, bs.length);
			if (n <= 0)
				return n;
			begin = 1;
			end1 = n;
			readBn += n;
			return bs[0] & 255;
		}

		@Override
		public int read(byte[] bs_, int begin_, int len) throws IOException
		{
			Math2.range(begin_, begin_ + len, bs_.length);
			if (begin < end1)
			{
				len = Math.min(end1 - begin, len);
				System.arraycopy(bs, begin, bs_, begin_, len);
				begin += len;
				return len;
			}
			int n = in.read(bs_, begin_, len);
			readBn += n;
			return n;
		}

		protected int lineMore(boolean read, int x) throws IOException
		{
			if (end1 == bs.length)
			{
				if (begin == 0)
					throw new InvalidLengthException("line too long : must <= "
						+ (bs.length - 2));
				if (read)
					System.arraycopy(bs, begin, bs, 0, end1 - begin);
				end1 -= begin;
				x -= begin;
				begin = 0;
			}
			int n = in.read(bs, end1, bs.length - end1);
			if (n <= 0)
				throw new EOFException();
			end1 += n;
			readBn += n;
			return x;
		}

		protected int lineEnd(boolean read, boolean pre) throws IOException
		{
			if (pre)
				for (int x = begin;; x = lineMore(read, x))
					for (; x + 1 < end1; x++)
						if (bs[x] == preEol && bs[x + 1] == eol)
							return x;
			for (int x = begin;; x = lineMore(read, x))
				for (; x < end1; x++)
					if (bs[x] == eol)
						return x;
		}

		/** @param pre if {@link #preEol} is enabled */
		public byte[] readLine(boolean pre) throws Exception
		{
			int x = lineEnd(true, pre);
			byte[] l = Array2.subClone(bs, begin, x);
			begin = pre ? x + 2 : x + 1;
			return l;
		}

		/**
		 * @param cs_ by {@link #charset} if null
		 * @param pre if {@link #preEol} is enabled
		 */
		public String readLine(Charset cs_, boolean pre) throws Exception
		{
			if (cs_ == null)
				cs_ = cs;
			int x = lineEnd(true, pre);
			String l = UTF.equals(cs_) ? new String(String2.utf(bs, begin, x)) : new String(
				bs, begin, x - begin, cs_);
			begin = pre ? x + 2 : x + 1;
			return l;
		}

		@Override
		public long skip(long n) throws IOException
		{
			if (n <= 0)
				return 0;
			long x = Math.min(n, end1 - begin);
			begin += x;
			if (n == x)
				return n;
			n = in.skip(n - x);
			readBn += n;
			return x + n;
		}

		/**
		 * @param pre if {@link #preEol} is enabled
		 * @return the number of bytes of the skipped line
		 */
		public int skipLine(boolean pre) throws Exception
		{
			int x = lineEnd(false, pre);
			int l = x - begin;
			begin = pre ? x + 2 : x + 1;
			return l;
		}
	}

	public static class Upload
		extends InputStream
	{
		Line in;
		byte[] split;
		String name;
		String fileName;
		/** 0: begin of part, 1: reading part, -1: no more parts */
		int part;
		int avail;

		public Upload(InputStream in_) throws Exception
		{
			this(new Input.Line(in_));
		}

		public Upload(Line in_) throws Exception
		{
			in = in_;
			int x = in.lineEnd(true, true);
			split = new byte[2 + x - in.begin + 2];
			System.arraycopy(in.bs, in.begin, split, 2, x - in.begin + 2);
			in.begin = x + 2;
			Math2.length(split.length - 4, 1, in.bs.length - 6);
			split[0] = '\r';
			split[1] = '\n';
			if (split.length > 5 && split[split.length - 3] == '-')
				part = -1;
		}

		public void closeAll() throws IOException
		{
			in.close();
		}

		/**
		 * @return true: the next part is ready, false: no more parts
		 * @throws IllegalStateException the current part unfinished
		 * @throws Exception
		 */
		public boolean next() throws Exception
		{
			if (part < 0)
				return false;
			if (part > 0)
				throw new IllegalStateException("current part unfinished");
			String l = in.readLine(null, true);
			int x = String2.indexAfter(l, "name=\"", 0);
			name = String2.sub(l, '"', x);
			x = String2.indexAfter(l, "filename=\"", x + name.length());
			fileName = String2.sub(l, '\"', x);
			while (in.skipLine(true) > 0)
				;
			part = 1;
			return true;
		}

		public String name()
		{
			return name;
		}

		public String fileName()
		{
			return fileName;
		}

		@Override
		public int available() throws IOException
		{
			if (avail > 0 || part <= 0)
				return avail;
			if (in.begin >= in.end1)
				in.lineMore(true, 0);
			int x = in.begin;
			if (in.bs[x++] != '\r')
				for (;; x++)
					if (x == in.end1 || in.bs[x] == '\r')
						return avail = x - in.begin;
			int noMore = 0;
			for (int y = 1; y < split.length; x++, y++)
			{
				if (x == in.end1)
					x = in.lineMore(true, x);
				if (in.bs[x] != split[y] || noMore == 1)
					if (in.bs[x] == '-' && y == split.length - 2 && noMore++ < 2)
						y--;
					else
						for (;; x++)
							if (x == in.end1 || in.bs[x] == '\r')
								return avail = x - in.begin;
			}
			in.begin = x;
			part = noMore == 2 ? -1 : 0;
			return avail = 0;
		}

		@Override
		public int read() throws IOException
		{
			if (available() <= 0)
				return -1;
			avail--;
			return in.bs[in.begin++] & 255;
		}

		@Override
		public int read(byte[] bs, int begin, int len) throws IOException
		{
			Math2.range(begin, begin + len, bs.length);
			len = Math.min(len, available());
			if (len <= 0)
				return -1;
			System.arraycopy(in.bs, in.begin, bs, begin, len);
			in.begin += len;
			avail -= len;
			return len;
		}

		@Override
		public long skip(long n) throws IOException
		{
			long m = 0;
			while (m < n && available() > 0)
			{
				long x = Math.min(n - m, avail);
				in.begin += x;
				m += x;
			}
			return m;
		}
	}
}
