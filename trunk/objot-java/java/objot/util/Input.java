package objot.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;


public class Input
{
	public static byte[] readFull(InputStream in, byte[] bs, int begin, int end1)
		throws IOException
	{
		Math2.checkRange(begin, end1, bs.length);
		for (int done; begin < end1; begin += done)
			if ((done = in.read(bs, begin, end1 - begin)) < 0)
				throw new EOFException();
		return bs;
	}

	public static class Line
		extends InputStream
	{
		public static final Charset UTF = Charset.forName("UTF-8");

		public InputStream in;
		public byte[] bs;
		public int begin;
		public int end1;

		public Line(InputStream in_)
		{
			in = in_;
			bs = new byte[1002];
		}

		public Line(InputStream in_, int maxLen)
		{
			in = in_;
			bs = new byte[maxLen + 2];
		}

		@Override
		public int available() throws IOException
		{
			return Math2.addOver(end1 - begin, in.available());
		}

		@Override
		public void close() throws IOException
		{
			bs = null;
			in.close();
		}

		@Override
		public int read() throws IOException
		{
			return begin < end1 ? bs[begin++] : in.read();
		}

		@Override
		public int read(byte[] bs_, int begin_, int len) throws IOException
		{
			Math2.checkRange(begin_, begin_ + len, bs_.length);
			if (begin < end1)
			{
				len = Math.min(end1 - begin, len);
				System.arraycopy(bs, begin, bs_, begin_, len);
				begin += len;
				return len;
			}
			return in.read(bs_, begin_, len);
		}

		public byte[] readFull(byte[] bs_, int begin_, int end1_) throws IOException
		{
			Math2.checkRange(begin_, end1_, bs_.length);
			if (begin < end1)
			{
				int len = Math.min(end1 - begin, end1_ - begin_);
				System.arraycopy(bs, begin, bs_, begin_, len);
				begin += len;
				begin_ += len;
			}
			for (int done; begin_ < end1_; begin_ += done)
				if ((done = in.read(bs_, begin_, end1_ - begin_)) < 0)
					throw new EOFException();
			return bs_;
		}

		public byte[] readLine(boolean cr) throws Exception
		{
			int x = begin;
			int x1 = cr ? 1 : 0;
			char xc = cr ? '\r' : '\n';
			for (;;)
			{
				for (; x + x1 < end1; x++)
					if (bs[x] == xc && bs[x + x1] == '\n')
					{
						byte[] l = Array2.subClone(bs, begin, x);
						begin = x + 1 + x1;
						return l;
					}
				if (end1 == bs.length)
				{
					if (begin == 0)
						throw new InvalidLengthException("line too long : must <= "
							+ (bs.length - 2));
					System.arraycopy(bs, begin, bs, 0, end1 - begin);
					end1 -= begin;
					x -= begin;
					begin = 0;
				}
				int done = in.read(bs, end1, bs.length - end1);
				if (done < 0)
					throw new EOFException();
				end1 += done;
			}
		}

		/** @param cs null for utf-8 */
		public String readLine(Charset cs, boolean cr) throws Exception
		{
			int x = begin;
			int x1 = cr ? 1 : 0;
			char xc = cr ? '\r' : '\n';
			for (;;)
			{
				for (; x + x1 < end1; x++)
					if (bs[x] == xc && bs[x + x1] == '\n')
					{
						String l = cs == null || UTF.equals(cs) ? //
							new String(String2.utf(bs, begin, x)) : //
							new String(bs, begin, x - begin, cs);
						begin = x + 1 + x1;
						return l;
					}
				if (end1 == bs.length)
				{
					if (begin == 0)
						throw new InvalidLengthException("line too long : must <= "
							+ (bs.length - 2));
					System.arraycopy(bs, begin, bs, 0, end1 - begin);
					end1 -= begin;
					x -= begin;
					begin = 0;
				}
				int done = in.read(bs, end1, bs.length - end1);
				if (done < 0)
					throw new EOFException();
				end1 += done;
			}
		}

		@Override
		public long skip(long x) throws IOException
		{
			long xx = Math.min(x, end1 - begin);
			begin += xx;
			return x == xx ? xx : xx + in.skip(x - xx);
		}
	}
}
