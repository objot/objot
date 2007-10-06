//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.bytecode;

import java.io.PrintStream;

import objot.util.InvalidValueException;


public final class Annotation
	extends Element
{
	public final Constants cons;
	int descCi;
	int propN;
	int[] propBis;

	static int readByteN(byte[] bs, int bi)
	{
		int nbi = bi + 4;
		for (int n = readU2(bs, nbi - 2); n > 0; n--)
		{
			nbi += 2;
			nbi += readValueByteN(bs, nbi);
		}
		return nbi - bi;
	}

	/** Including the tag byte. */
	static int readValueByteN(byte[] bs, int bi)
	{
		switch (readS1(bs, bi))
		{
		case 'B':
		case 'C':
		case 'D':
		case 'F':
		case 'I':
		case 'J':
		case 'S':
		case 'Z':
		case 's': // string
		case 'c': // class
			return 1 + 2;
		case 'e':
			return 1 + 2 + 2;
		case '@':
			return 1 + readByteN(bs, bi + 1);
		case '[':
		{
			int nbi = bi + 3;
			for (int n = readU2(bs, nbi - 2); n > 0; n--)
				nbi += readValueByteN(bs, nbi);
			return nbi - bi;
		}
		}
		throw new ClassFormatError("invalid annotation value tag " + (char)readU1(bs, bi));
	}

	public Annotation(Constants c, byte[] bs, int beginBi_)
	{
		super(bs, beginBi_);
		cons = c;
		descCi = read0u2(beginBi);
		propN = read0u2(beginBi + 2);
		end1Bi = beginBi + readByteN(bytes, beginBi);
	}

	public int getDescCi()
	{
		return descCi;
	}

	public int getPropN()
	{
		return propN;
	}

	void checkIndex(int pi)
	{
		if (pi < 0 || pi >= propN)
			throw new InvalidValueException(pi);
	}

	void readPropBis()
	{
		if (propBis != null)
			return;
		propBis = new int[propN];
		int bi = beginBi + 4;
		for (int i = 0; i < propN; i++)
		{
			propBis[i] = bi;
			bi += 2;
			bi += readValueByteN(bytes, bi);
		}
	}

	public int getPropNameCi(int pi)
	{
		checkIndex(pi);
		readPropBis();
		return read0u2(propBis[pi]);
	}

	public int getPropValueBi(int pi)
	{
		checkIndex(pi);
		readPropBis();
		return propBis[pi] + 2;
	}

	@Override
	void printContents(PrintStream out, int indent1st, int indent, int verbose)
	{
		printIndent(out, indent1st);
		out.print(" desc ");
		cons.print(out, getDescCi(), verbose);
		out.println();
		for (int i = 0; i < propN; i++)
		{
			printIndent(out, indent);
			out.print(i);
			out.print(". name ");
			cons.print(out, getPropNameCi(i), verbose);
			out.print(" valueBi ");
			out.println(getPropValueBi(i));
		}
	}

	@Override
	public int normalizeByteN()
	{
		return byteN0();
	}

	@Override
	public int normalizeTo(byte[] bs, int begin)
	{
		System.arraycopy(bytes, beginBi, bs, begin, byteN0());
		return begin + byteN0();
	}
}
