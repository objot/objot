package objot.bytecode;

import java.io.PrintStream;

import objot.util.Array2;
import objot.util.InvalidValueException;


public class CodeLines
	extends Element
{
	protected int lineN;
	protected int[] beginAds;
	protected int[] lines;

	public CodeLines(byte[] bs, int beginBi_)
	{
		super(bs, beginBi_);
		lineN = read0u2(beginBi + 6);
		end1Bi = beginBi + 8 + (lineN << 2);
		if (end1Bi - beginBi - 6 != read0u4(beginBi + 2))
			throw new ClassFormatError("inconsistent attribute length");
	}

	public int getLineN()
	{
		return lineN;
	}

	protected void reads()
	{
		if (beginAds != null)
			return;
		beginAds = new int[allocN(lineN)];
		lines = new int[allocN(lineN)];
		int bi = beginBi + 8;
		for (int i = 0; i < lineN; i++)
		{
			beginAds[i] = read0u2(bi);
			lines[i] = read0u2(bi + 2);
			bi += 4;
		}
	}

	protected void checkIndex(int li)
	{
		if (li < 0 || li >= lineN)
			throw new InvalidValueException(li);
	}

	public int getBeginAd(int li)
	{
		checkIndex(li);
		reads();
		return beginAds[li];
	}

	public int getLine(int li)
	{
		checkIndex(li);
		reads();
		return lines[li];
	}

	@Override
	protected void printContents(PrintStream out, int indent1st, int indent, int verbose,
		boolean hash)
	{
		out.println();
		for (int i = 0; i < getLineN(); i++)
		{
			printIndent(out, indent);
			out.print(i);
			out.print(". beginAd ");
			out.print(getBeginAd(i));
			out.print(" line ");
			out.println(getLine(i));
		}
	}

	public void ensureLineN(int n)
	{
		reads();
		beginAds = Array2.ensureN(beginAds, n);
		lines = Array2.ensureN(lines, n);
	}

	public int appendInfo(int beginAd, int line)
	{
		reads();
		ensureLineN(lineN + 1);
		beginAds[lineN] = beginAd;
		lines[lineN] = line;
		return lineN++;
	}

	public int putInfo(int beginAd, int line)
	{
		reads();
		for (int i = 0; i < lineN; i++)
			if (beginAds[i] == beginAd && lines[i] == line)
				return i;
		return appendInfo(beginAd, line);
	}

	public void setInfo(int li, int beginAd, int line)
	{
		checkIndex(li);
		reads();
		beginAds[li] = beginAd;
		lines[li] = line;
	}

	@Override
	public int generateByteN()
	{
		return 8 + (lineN << 2);
	}

	@Override
	public int generateTo(byte[] bs, int begin)
	{
		writeU2(bs, begin, read0u2(beginBi));
		writeS4(bs, begin + 2, generateByteN() - 6);
		writeU2(bs, begin + 6, lineN);
		if (beginAds == null)
		{
			System.arraycopy(bytes, beginBi + 8, bs, begin + 8, lineN << 2);
			return begin + generateByteN();
		}
		begin += 8;
		for (int i = 0; i < lineN; i++, begin += 4)
		{
			writeU2(bs, begin, beginAds[i]);
			writeU2(bs, begin + 2, lines[i]);
		}
		return begin;
	}
}