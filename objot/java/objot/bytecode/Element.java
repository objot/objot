//
// Copyright 2007-2009 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.bytecode;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import objot.util.Bytes;
import objot.util.Class2;
import objot.util.Math2;


/**
 * {@link #end1Bi} and {@link #byteN()} except {@link #beginBi} may reflect some of this
 * element's changes
 */
abstract class Element
	extends Bytes
{
	Element(byte[] bs, int begin)
	{
		super(bs);
		beginBi = begin;
	}

	/** original {@link #byteN()}, not reflect changes */
	public int byteN0()
	{
		return byteN();
	}

	int allocN(int n)
	{
		return Math.max((int)(n * 1.3f), 11);
	}

	public Annotations getAnnos()
	{
		return null;
	}

	public Annotations getAnnoHides()
	{
		return null;
	}

	/**
	 * @param indent1st Indent for 1st line.
	 * @param indent Indent for other lines.
	 */
	public PrintStream printTo(PrintStream out, int indent1st, int indent, int verbose)
	{
		printIdentity(out, indent1st);
		printContents(out, 0, indent + 1, verbose);
		return out;
	}

	public static void printIndent(PrintStream out, int indent)
	{
		while (--indent >= 0)
			out.print('\t');
	}

	abstract void printContents(PrintStream out, int indent1st, int indent, int verbose);

	public PrintStream printIdentity(PrintStream out, int indent)
	{
		printIndent(out, indent);
		if (Element.class.getPackage() == getClass().getPackage())
			out.print(Class2.selfName(getClass()));
		else
			out.print(getClass().getName());
		out.print(" [");
		out.print(beginBi);
		if (byteN0() != byteN())
		{
			out.print(',');
			out.print(beginBi + byteN0());
		}
		out.print(',');
		out.print(end1Bi);
		out.print(')');
		return out;
	}

	/** @return the number of bytes generated. */
	public abstract int normalizeByteN();

	/**
	 * generate element(s) to the byte array, from the begin index.
	 * 
	 * @return the end1 index of byte array after generating.
	 */
	public abstract int normalizeTo(byte[] bs, int begin);

	// ********************************************************************************

	/** @return modified utf (no \0) */
	public static Bytes utf(String s)
	{
		return utf(s, 0, s.length());
	}

	public static Bytes utf(String s, int begin, int end1)
	{
		Math2.range(begin, end1, s.length());
		int ulen = 0;
		for (int i = begin; i < end1; i++)
		{
			char c = s.charAt(i);
			if (c > 0 && c < 0x80)
				ulen++;
			else if (c < 0x800)
				ulen += 2;
			else
				ulen += 3;
			if (ulen > 65535)
				throw new IndexOutOfBoundsException("modified utf8 too long");
		}
		byte[] utf = new byte[ulen];
		int ui = 0;
		for (int i = begin; i < end1; i++)
		{
			char c = s.charAt(i);
			if (c > 0 && c < 0x80)
				utf[ui++] = (byte)c;
			else if (c < 0x800)
			{
				utf[ui++] = (byte)(0xC0 | (c >>> 6) & 0x1F);
				utf[ui++] = (byte)(0x80 | c & 0x3F);
			}
			else
			{
				utf[ui++] = (byte)(0xE0 | (c >>> 12) & 0x0F);
				utf[ui++] = (byte)(0x80 | (c >>> 6) & 0x3F);
				utf[ui++] = (byte)(0x80 | c & 0x3F);
			}
		}
		return new Bytes(utf);
	}

	public static String ucs(Bytes utf)
	{
		return ucs(utf, 0, utf.byteN());
	}

	public static String ucs(Bytes utf, int begin, int end1)
	{
		Math2.range(begin, end1, utf.end1Bi - utf.beginBi);
		try
		{
			return new String(utf.bytes, utf.beginBi + begin, end1 - begin, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new Error("UTF-8 expected", e);
		}
	}

	/** @see Class2#pathName(Class) */
	public static Bytes classDesc2Name(Bytes descUtf)
	{
		return classDesc2Name(descUtf, 0, descUtf.byteN());
	}

	/** @see Class2#pathName(Class) */
	public static Bytes classDesc2Name(Bytes descUtf, int begin, int end1)
	{
		if (descUtf.readS1(begin) != 'L' || descUtf.readS1(end1 - 1) != ';')
			throw new IllegalArgumentException("only declaring class supported");
		return new Bytes(descUtf, begin + 1, end1 - begin - 2);
	}

	/** @see Class2#pathName(Class) */
	public static String classDesc2NameUcs(Bytes descUtf)
	{
		return classDesc2NameUcs(descUtf, 0, descUtf.byteN());
	}

	/** @see Class2#pathName(Class) */
	public static String classDesc2NameUcs(Bytes descUtf, int begin, int end1)
	{
		if (descUtf.readS1(begin) != 'L' || descUtf.readS1(end1 - 1) != ';')
			throw new IllegalArgumentException("only declaring class supported");
		return ucs(descUtf, begin + 1, end1 - 1);
	}

	public static int typeDescByteN(Bytes descUtf, int begin)
	{
		switch (descUtf.readS1(begin))
		{
		case 'Z':
		case 'B':
		case 'C':
		case 'S':
		case 'I':
		case 'J':
		case 'F':
		case 'D':
			return 1;
		case 'L':
		{
			int bi = descUtf.beginBi + begin + 1;
			ObjectDesc:
			{
				for (; bi < descUtf.end1Bi; bi++)
					if (descUtf.bytes[bi] == ';')
						break ObjectDesc;
				throw new ClassFormatError("invalid procedure descriptor "
					+ Element.ucs(descUtf, begin, descUtf.byteN()));
			}
			return bi - descUtf.beginBi - begin + 1;
		}
		case '[':
			return 1 + typeDescByteN(descUtf, begin + 1);
		}
		throw new ClassFormatError("invalid procedure descriptor "
			+ (char)descUtf.readS1(begin));
	}
}
