package objot.bytecode;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import objot.util.Bytes;
import objot.util.Class2;
import objot.util.Math2;


public abstract class Element
	extends Bytes
{
	public final boolean forExtension;

	protected Element(byte[] bs, int begin, boolean forExtension_)
	{
		super(bs);
		bytes = bs;
		beginBi = begin;
		forExtension = forExtension_;
	}

	public Annotations getAnnos()
	{
		return null;
	}

	public Annotations getAnnoHides()
	{
		return null;
	}

	/** @return {@link #allocN(int, int)} with n and 7 */
	protected int allocN(int n)
	{
		return allocN(n, 7);
	}

	protected int allocN(int n, int extMin)
	{
		return forExtension ? Math.max((int)(n * 1.5f), extMin) : n;
	}

	/**
	 * @param indent1st Indent for 1st line.
	 * @param indent Indent for other lines.
	 */
	public void printTo(PrintStream out, int indent1st, int indent, int verbose, boolean hash)
	{
		printIdentity(out, indent1st, hash);
		printContents(out, 0, indent + 1, verbose, hash);
	}

	public static void printIndent(PrintStream out, int indent)
	{
		while (--indent >= 0)
			out.print('\t');
	}

	protected abstract void printContents(PrintStream out, int indent1st, int indent,
		int verbose, boolean hash);

	public void printIdentity(PrintStream out, int indent, boolean hash)
	{
		printIndent(out, indent);
		if (Element.class.getPackage().equals(getClass().getPackage()))
			out.print(Class2.selfName(getClass()));
		else
			out.print(getClass().getName());
		if (hash)
		{
			out.print('@');
			out.print(Integer.toHexString(System.identityHashCode(this)));
		}
		out.print(" original-bytes[");
		out.print(beginBi);
		out.print(',');
		out.print(end1Bi);
		out.print(")[0x");
		out.print(Integer.toHexString(beginBi));
		out.print(",0x");
		out.print(Integer.toHexString(end1Bi));
		out.print(')');
	}

	public void printIdentityLn(PrintStream out, int indent, boolean hash)
	{
		printIdentity(out, indent, hash);
		out.println();
	}

	/** @return the number of bytes generated. */
	public abstract int generateByteN();

	/**
	 * generate element(s) to the byte array, from the begin index.
	 * 
	 * @return the end1 index of byte array after generating.
	 */
	public abstract int generateTo(byte[] bs, int begin);

	// ********************************************************************************

	public static byte[] chars2Utf(String s)
	{
		return chars2Utf(s, 0, s.length());
	}

	public static byte[] chars2Utf(String s, int begin, int end1)
	{
		Math2.checkRange(begin, end1, s.length());
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
		return utf;
	}

	public static Bytes utf(String s)
	{
		return new Bytes(chars2Utf(s));
	}

	public static String utf2chars(byte[] utf)
	{
		return utf2chars(utf, 0, utf.length);
	}

	public static String utf2chars(byte[] utf, int begin, int end1)
	{
		try
		{
			return new String(utf, begin, end1 - begin, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new Error("UTF-8 expected", e);
		}
	}

	public String utf2chars(int begin, int end1)
	{
		return utf2chars(bytes, begin, end1);
	}

	public static byte[] classDesc2Internal(byte[] descUtf)
	{
		return classDesc2Internal(descUtf, 0, descUtf.length);
	}

	public static byte[] classDesc2Internal(byte[] descUtf, int begin, int end1)
	{
		if (descUtf[begin] != 'L' || descUtf[end1 - 1] != ';')
			throw new IllegalArgumentException("only declaring class supported");
		byte[] name = new byte[end1 - begin - 2];
		System.arraycopy(descUtf, begin + 1, name, 0, name.length);
		return name;
	}

	public static String classDesc2InternalChars(byte[] descUtf)
	{
		return classDesc2InternalChars(descUtf, 0, descUtf.length);
	}

	public static String classDesc2InternalChars(byte[] descUtf, int begin, int end1)
	{
		if (descUtf[begin] != 'L' || descUtf[end1 - 1] != ';')
			throw new IllegalArgumentException("only declaring class supported");
		return utf2chars(descUtf, begin + 1, end1 - 1);
	}

	public static int typeDescByteN(byte[] descUtf, int begin, int end1)
	{
		switch (descUtf[begin])
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
			int bi = begin + 1;
			ObjectDesc:
			{
				for (; bi < end1; bi++)
					if (descUtf[bi] == ';')
						break ObjectDesc;
				throw new ClassFormatError("invalid procedure descriptor "
					+ Element.utf2chars(descUtf, begin, end1));
			}
			return bi - begin + 1;
		}
		case '[':
			return 1 + typeDescByteN(descUtf, begin + 1, end1);
		}
		throw new ClassFormatError("invalid procedure descriptor " + (char)descUtf[begin]);
	}

	public int typeDescByteN(int begin, int end1)
	{
		return typeDescByteN(bytes, begin, end1);
	}
}
