package objot.bytecode;

import java.io.PrintStream;

import objot.util.Array2;
import objot.util.InvalidValueException;


public class Exceptions
	extends Element
{
	protected int exceptionN;
	protected int[] exceptionCis;

	public Exceptions(byte[] bs, int beginBi_)
	{
		super(bs, beginBi_);
		exceptionN = read0u2(beginBi + 6);
		end1Bi = beginBi + 8 + (exceptionN << 1);
		if (end1Bi - beginBi - 6 != read0u4(beginBi + 2))
			throw new ClassFormatError("inconsistent attribute length");
	}

	public int getExceptionN()
	{
		return exceptionN;
	}

	protected void readExceptionCis()
	{
		if (exceptionCis != null)
			return;
		exceptionCis = new int[allocN(exceptionN)];
		for (int i = 0; i < exceptionN; i++)
			exceptionCis[i] = read0u2(beginBi + 8 + (i << 1));
	}

	protected void checkIndex(int ei)
	{
		if (ei < 0 || ei >= exceptionN)
			throw new InvalidValueException(ei);
	}

	public int getExceptionCi(int ei)
	{
		checkIndex(ei);
		readExceptionCis();
		return exceptionCis[ei];
	}

	@Override
	protected void printContents(PrintStream out, int indent1st, int indent, int verbose,
		boolean hash)
	{
		out.println();
		for (int i = 0; i < getExceptionN(); i++)
		{
			printIndent(out, indent);
			out.print(i);
			out.print(". exceptionCi ");
			out.println(getExceptionCi(i));
		}
	}

	public void ensureExceptionN(int n)
	{
		readExceptionCis();
		exceptionCis = Array2.ensureN(exceptionCis, n);
	}

	/** @return exception index(not Ci) */
	public int appendException(int exceptionCi)
	{
		readExceptionCis();
		ensureExceptionN(exceptionN + 1);
		exceptionCis[exceptionN] = exceptionCi;
		return exceptionN++;
	}

	/** @return exception index(not Ci) */
	public int putException(int exceptionCi)
	{
		readExceptionCis();
		for (int i = 0; i < exceptionN; i++)
			if (exceptionCis[i] == exceptionCi)
				return i;
		return appendException(exceptionCi);
	}

	public void setException(int ei, int exceptionCi)
	{
		checkIndex(ei);
		readExceptionCis();
		exceptionCis[ei] = exceptionCi;
	}

	@Override
	public int generateByteN()
	{
		return 8 + (exceptionN << 1);
	}

	@Override
	public int generateTo(byte[] bs, int begin)
	{
		writeU2(bs, begin, read0u2(beginBi));
		writeU4(bs, begin + 2, generateByteN() - 6);
		writeU2(bs, begin + 6, exceptionN);
		if (exceptionCis == null)
		{
			System.arraycopy(bytes, beginBi + 8, bs, begin + 8, exceptionN << 1);
			return begin + generateByteN();
		}
		begin += 8;
		for (int i = 0; i < exceptionN; i++, begin += 2)
			writeU2(bs, begin, exceptionCis[i]);
		return begin;
	}
}
