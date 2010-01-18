//
// Copyright 2007-2010 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.bytecode;

import java.io.PrintStream;

import objot.util.Array2;
import objot.util.InvalidValueException;


public final class Exceptions
	extends Element
{
	public final Constants cons;
	int exceptionN;
	int[] exceptionCis;

	public Exceptions(Constants c, byte[] bs, int beginBi_)
	{
		super(bs, beginBi_);
		cons = c;
		exceptionN = readU2(bytes, beginBi + 6);
		end1Bi = beginBi + 8 + (exceptionN << 1);
		if (end1Bi - beginBi - 6 != readU4(bytes, beginBi + 2))
			throw new ClassFormatError("inconsistent attribute length");
	}

	public int getExceptionN()
	{
		return exceptionN;
	}

	void readExceptionCis()
	{
		if (exceptionCis != null)
			return;
		exceptionCis = new int[allocN(exceptionN)];
		for (int i = 0; i < exceptionN; i++)
			exceptionCis[i] = readU2(bytes, beginBi + 8 + (i << 1));
	}

	void checkIndex(int ei)
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
	void printContents(PrintStream out, int indent1st, int indent, int verbose)
	{
		if (verbose > 0)
		{
			printIndent(out, indent1st);
			out.print(" exceptionN ");
			out.print(exceptionN);
		}
		out.println();
		for (int i = 0; i < exceptionN; i++)
		{
			printIndent(out, indent);
			out.print(i);
			out.print(". exception ");
			cons.print(out, getExceptionCi(i), verbose).println();
		}
	}

	public void ensureExceptionN(int n)
	{
		readExceptionCis();
		exceptionCis = Array2.ensureN(exceptionCis, n);
	}

	/** @return exception index(not Ci) */
	public int addException(int exceptionCi)
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
		return addException(exceptionCi);
	}

	public void setException(int ei, int exceptionCi)
	{
		checkIndex(ei);
		readExceptionCis();
		exceptionCis[ei] = exceptionCi;
	}

	/** @return exception ci */
	public int removeException(int ei)
	{
		checkIndex(ei);
		readExceptionCis();
		int ci = exceptionCis[ei];
		System.arraycopy(exceptionCis, ei + 1, exceptionCis, ei, exceptionN - ei - 1);
		--exceptionN;
		return ci;
	}

	@Override
	public int normalizeByteN()
	{
		return 8 + (exceptionN << 1);
	}

	@Override
	public int normalizeTo(byte[] bs, int begin)
	{
		writeU2(bs, begin, readU2(bytes, beginBi));
		writeU4(bs, begin + 2, normalizeByteN() - 6);
		writeU2(bs, begin + 6, exceptionN);
		if (exceptionCis == null)
		{
			System.arraycopy(bytes, beginBi + 8, bs, begin + 8, exceptionN << 1);
			return begin + normalizeByteN();
		}
		begin += 8;
		for (int i = 0; i < exceptionN; i++, begin += 2)
			writeU2(bs, begin, exceptionCis[i]);
		return begin;
	}
}
