package objot.bytecode;

import java.io.PrintStream;

import objot.util.Array2;
import objot.util.Bytes;
import objot.util.InvalidValueException;
import objot.util.Math2;


/** All UTFs in this class are no-null-character UTF. All names of class are internal form. */
public class Constants
	extends Element
{
	public static final byte TAG_UTF = 1;
	public static final byte TAG_INT = 3;
	public static final byte TAG_FLOAT = 4;
	public static final byte TAG_LONG = 5;
	public static final byte TAG_DOUBLE = 6;
	public static final byte TAG_CLASS = 7;
	public static final byte TAG_STRING = 8;
	public static final byte TAG_FIELD = 9;
	public static final byte TAG_CPROC = 10;
	public static final byte TAG_IPROC = 11;
	public static final byte TAG_NAMEDESC = 12;

	/** index [1, N) excluding 0. */
	protected int conN0;
	protected int conN;
	/** [last] is normalized end byte index */
	protected int[] bis;

	public Constants(byte[] bs, int beginBi_)
	{
		super(bs, beginBi_, true);
		conN0 = conN = read0u2(beginBi);
		if (conN <= 0)
			throw new ClassFormatError("invalid constants amount");
		int bi = beginBi + 2;
		for (int ci = 1; ci < conN; ci++)
		{
			byte t = read0s1(bi);
			if (t == TAG_LONG || t == TAG_DOUBLE)
				if (ci < conN)
					ci++; // stupid specification ass
				else
					throw new ClassFormatError("unexpect constant end");
			bi += readConByteN(bi);
		}
		end1Bi = bi;
	}

	public int getConN()
	{
		return conN;
	}

	/** Including the tag byte. */
	protected int readConByteN(int bi)
	{
		switch (read0s1(bi))
		{
		case TAG_UTF:
			int l = read0u2(bi + 1);
			for (int i = bi + 3; i < bi + l; i++)
				if (read0s1(i) == 0)
					throw new ClassFormatError("invalid utf constant");
			return 1 + 2 + l;
		case TAG_INT:
		case TAG_FLOAT:
			return 1 + 4;
		case TAG_LONG:
		case TAG_DOUBLE:
			return 1 + 8;
		case TAG_CLASS:
		case TAG_STRING:
			return 1 + 2;
		case TAG_FIELD:
		case TAG_CPROC:
		case TAG_IPROC:
		case TAG_NAMEDESC:
			return 1 + 2 + 2;
		}
		throw new ClassFormatError("invalid constant info tag");
	}

	protected void readBis()
	{
		if (bis != null)
			return;
		bis = new int[allocN(conN + 1, 150)];
		bis[0] = Integer.MIN_VALUE;
		bis[1] = beginBi + 2;
		for (int ci = 2; ci < conN; ci++)
		{
			bis[ci] = bis[ci - 1] + readConByteN(bis[ci - 1]);
			byte t = read0s1(bis[ci - 1]);
			if (t == TAG_LONG || t == TAG_DOUBLE)
			{
				bis[ci + 1] = bis[ci];
				bis[ci] = bis[ci - 1];
				ci++;
			}
		}
		bis[conN] = end1Bi;
	}

	/** @param ci [1, N) excluding 0. */
	protected void checkIndex(int ci)
	{
		if (ci <= 0 || ci >= conN)
			throw new InvalidValueException(ci);
	}

	protected int readTag(int ci)
	{
		checkIndex(ci);
		readBis();
		return read0s1(bis[ci]);
	}

	public byte[] readUtf(int ci)
	{
		if (readTag(ci) != TAG_UTF)
			throw new ClassCastException();
		byte[] bs = new byte[read0u2(bis[ci] + 1)];
		System.arraycopy(bytes, bis[ci] + 3, bs, 0, bs.length);
		return bs;
	}

	public String readUtfChars(int ci)
	{
		return utf2chars(bytes, readUtfBegin(ci), readUtfEnd1(ci));
	}

	public Bytes readUtfBytes(int ci)
	{
		if (readTag(ci) != TAG_UTF)
			throw new ClassCastException();
		return new Bytes(bytes, readUtfBegin(ci), readUtfEnd1(ci));
	}

	public void readUtfTo(int ci, byte[] bs, int bi)
	{
		System.arraycopy(bytes, readUtfBegin(ci), bs, bi, readUtfByteN(ci));
	}

	public int readUtfByteN(int ci)
	{
		if (readTag(ci) != TAG_UTF)
			throw new ClassCastException();
		return read0u2(bis[ci] + 1);
	}

	public int readUtfBegin(int ci)
	{
		if (readTag(ci) != TAG_UTF)
			throw new ClassCastException();
		return bis[ci] + 3;
	}

	public int readUtfEnd1(int ci)
	{
		if (readTag(ci) != TAG_UTF)
			throw new ClassCastException();
		return bis[ci] + 3 + read0u2(bis[ci] + 1);
	}

	public int readInt(int ci)
	{
		if (readTag(ci) != TAG_INT)
			throw new ClassCastException();
		return read0s4(bis[ci] + 1);
	}

	public long readLong(int ci)
	{
		if (readTag(ci) != TAG_LONG)
			throw new ClassCastException();
		return read0s8(bis[ci] + 1);
	}

	public float readFloat(int ci)
	{
		if (readTag(ci) != TAG_FLOAT)
			throw new ClassCastException();
		return Float.intBitsToFloat(read0s4(bis[ci] + 1));
	}

	public double readDouble(int ci)
	{
		if (readTag(ci) != TAG_DOUBLE)
			throw new ClassCastException();
		return Double.longBitsToDouble(read0s8(bis[ci] + 1));
	}

	protected int readUtfRef(byte tag, int ci)
	{
		if (readTag(ci) != tag)
			throw new ClassCastException();
		return read0u2(bis[ci] + 1);
	}

	public int readClass(int ci)
	{
		return readUtfRef(TAG_CLASS, ci);
	}

	public int readString(int ci)
	{
		return readUtfRef(TAG_STRING, ci);
	}

	protected int readClassNameDescClass(byte tag, int ci)
	{
		if (readTag(ci) != tag)
			throw new ClassCastException();
		return readClass(read0u2(bis[ci] + 1));
	}

	protected int readClassNameDescName(byte tag, int ci)
	{
		if (readTag(ci) != tag)
			throw new ClassCastException();
		return readNameDescName(read0u2(bis[ci] + 3));
	}

	protected int readClassNameDescDesc(byte tag, int ci)
	{
		if (readTag(ci) != tag)
			throw new ClassCastException();
		return readNameDescDesc(read0u2(bis[ci] + 3));
	}

	public int readFieldClass(int ci)
	{
		return readClassNameDescClass(TAG_FIELD, ci);
	}

	public int readFieldName(int ci)
	{
		return readClassNameDescName(TAG_FIELD, ci);
	}

	public int readFieldDesc(int ci)
	{
		return readClassNameDescDesc(TAG_FIELD, ci);
	}

	public int readCprocClass(int ci)
	{
		return readClassNameDescClass(TAG_CPROC, ci);
	}

	public int readCprocName(int ci)
	{
		return readClassNameDescName(TAG_CPROC, ci);
	}

	public int readCprocDesc(int ci)
	{
		return readClassNameDescDesc(TAG_CPROC, ci);
	}

	public int readIprocClass(int ci)
	{
		return readClassNameDescClass(TAG_IPROC, ci);
	}

	public int readIprocName(int ci)
	{
		return readClassNameDescName(TAG_IPROC, ci);
	}

	public int readIprocDesc(int ci)
	{
		return readClassNameDescDesc(TAG_IPROC, ci);
	}

	public int readNameDescName(int ci)
	{
		return readUtfRef(TAG_NAMEDESC, ci);
	}

	public int readNameDescDesc(int ci)
	{
		if (readTag(ci) != TAG_NAMEDESC)
			throw new ClassCastException();
		return read0u2(bis[ci] + 3);
	}

	public boolean equalsUtf(int ci, byte[] utf)
	{
		if (readTag(ci) != TAG_UTF || read0u2(bis[ci] + 1) != utf.length)
			return false;
		int bi = bis[ci] + 3 + utf.length - 1;
		for (int i = utf.length - 1; i >= 0; i--, bi--)
			if (read0s1(bi) != utf[i])
				return false;
		return true;
	}

	public boolean equalsUtf(int ci, byte[] utf, int begin, int end1)
	{
		if (readTag(ci) != TAG_UTF || read0u2(bis[ci] + 1) != end1 - begin)
			return false;
		int bi = bis[ci] + 3 + end1 - begin - 1;
		for (int i = end1 - 1; i >= begin; i--, bi--)
			if (read0s1(bi) != utf[i])
				return false;
		return true;
	}

	public boolean equalsUtf(int ci, Bytes utf)
	{
		if (readTag(ci) != TAG_UTF || read0u2(bis[ci] + 1) != utf.byteN())
			return false;
		int bi = bis[ci] + 3 + utf.byteN() - 1;
		for (int i = utf.byteN() - 1; i >= 0; i--, bi--)
			if (read0s1(bi) != utf.read0s1(i))
				return false;
		return true;
	}

	public boolean startsWithUtf(int ci, byte[] utf)
	{
		if (readTag(ci) != TAG_UTF || read0u2(bis[ci] + 1) < utf.length)
			return false;
		int bi = bis[ci] + 3 + utf.length - 1;
		for (int i = utf.length - 1; i >= 0; i--, bi--)
			if (read0s1(bi) != utf[i])
				return false;
		return true;
	}

	public boolean startsWithUtf(int ci, Bytes utf)
	{
		if (readTag(ci) != TAG_UTF || read0u2(bis[ci] + 1) < utf.byteN())
			return false;
		int bi = bis[ci] + 3 + utf.byteN() - 1;
		for (int i = utf.byteN() - 1; i >= 0; i--, bi--)
			if (read0s1(bi) != utf.read0s1(i))
				return false;
		return true;
	}

	public boolean equalsInt(int ci, int v)
	{
		return readTag(ci) == TAG_INT && read0s4(bis[ci] + 1) == v;
	}

	public boolean equalsLong(int ci, long v)
	{
		return readTag(ci) == TAG_LONG && read0s8(bis[ci] + 1) == v;
	}

	public boolean equalsFloat(int ci, float v)
	{
		return readTag(ci) == TAG_FLOAT && Float.intBitsToFloat(read0s4(bis[ci] + 1)) == v;
	}

	public boolean equalsDouble(int ci, double v)
	{
		return readTag(ci) == TAG_LONG && Double.longBitsToDouble(read0s8(bis[ci] + 1)) == v;
	}

	protected boolean equalsRef(byte tag, int ci, int refCi)
	{
		return readTag(ci) == tag && read0u2(bis[ci] + 1) == refCi;
	}

	protected boolean equalsRefUtf(byte tag, int ci, byte[] utf)
	{
		return readTag(ci) == tag && equalsUtf(read0u2(bis[ci] + 1), utf);
	}

	protected boolean equalsRefUtf(byte tag, int ci, Bytes utf)
	{
		return readTag(ci) == tag && equalsUtf(read0u2(bis[ci] + 1), utf);
	}

	public boolean equalsClass(int ci, int nameCi)
	{
		return equalsRef(TAG_CLASS, ci, nameCi);
	}

	public boolean equalsClass(int ci, byte[] name)
	{
		return equalsRefUtf(TAG_CLASS, ci, name);
	}

	public boolean equalsClass(int ci, Bytes name)
	{
		return equalsRefUtf(TAG_CLASS, ci, name);
	}

	public boolean equalsString(int ci, int strCi)
	{
		return equalsRef(TAG_STRING, ci, strCi);
	}

	public boolean equalsString(int ci, byte[] str)
	{
		return equalsRefUtf(TAG_STRING, ci, str);
	}

	public boolean equalsString(int ci, Bytes str)
	{
		return equalsRefUtf(TAG_STRING, ci, str);
	}

	protected boolean equalsRef2(byte tag, int ci, int ref1Ci, int ref2Ci)
	{
		return readTag(ci) == tag && read0u2(bis[ci] + 1) == ref1Ci
			&& read0u2(bis[ci] + 3) == ref2Ci;
	}

	protected boolean equalsClassNameDesc(byte tag, int ci, int classNameCi, int nameCi,
		int descCi)
	{
		return readTag(ci) == tag && equalsClass(read0u2(bis[ci] + 1), classNameCi)
			&& equalsNameDesc(read0u2(bis[ci] + 3), nameCi, descCi);
	}

	protected boolean equalsClassNameDesc(byte tag, int ci, byte[] className, byte[] name,
		byte[] desc)
	{
		return readTag(ci) == tag && equalsClass(read0u2(bis[ci] + 1), className)
			&& equalsNameDesc(read0u2(bis[ci] + 3), name, desc);
	}

	protected boolean equalsClassNameDesc(byte tag, int ci, Bytes className, Bytes name,
		Bytes desc)
	{
		return readTag(ci) == tag && equalsClass(read0u2(bis[ci] + 1), className)
			&& equalsNameDesc(read0u2(bis[ci] + 3), name, desc);
	}

	public boolean equalsField(int ci, int classNameCi, int nameDescCi)
	{
		return equalsRef2(TAG_FIELD, ci, classNameCi, nameDescCi);
	}

	public boolean equalsField(int ci, int classNameCi, int nameCi, int descCi)
	{
		return equalsClassNameDesc(TAG_FIELD, ci, classNameCi, nameCi, descCi);
	}

	public boolean equalsField(int ci, byte[] className, byte[] name, byte[] desc)
	{
		return equalsClassNameDesc(TAG_FIELD, ci, className, name, desc);
	}

	public boolean equalsField(int ci, Bytes className, Bytes name, Bytes desc)
	{
		return equalsClassNameDesc(TAG_FIELD, ci, className, name, desc);
	}

	public boolean equalsCproc(int ci, int classNameCi, int nameDescCi)
	{
		return equalsRef2(TAG_CPROC, ci, classNameCi, nameDescCi);
	}

	public boolean equalsCproc(int ci, int classNameCi, int nameCi, int descCi)
	{
		return equalsClassNameDesc(TAG_CPROC, ci, classNameCi, nameCi, descCi);
	}

	public boolean equalsCproc(int ci, byte[] className, byte[] name, byte[] desc)
	{
		return equalsClassNameDesc(TAG_CPROC, ci, className, name, desc);
	}

	public boolean equalsCproc(int ci, Bytes className, Bytes name, Bytes desc)
	{
		return equalsClassNameDesc(TAG_CPROC, ci, className, name, desc);
	}

	public boolean equalsIproc(int ci, int classNameCi, int nameDescCi)
	{
		return equalsRef2(TAG_IPROC, ci, classNameCi, nameDescCi);
	}

	public boolean equalsIproc(int ci, int classNameCi, int nameCi, int descCi)
	{
		return equalsClassNameDesc(TAG_IPROC, ci, classNameCi, nameCi, descCi);
	}

	public boolean equalsIproc(int ci, byte[] className, byte[] name, byte[] desc)
	{
		return equalsClassNameDesc(TAG_IPROC, ci, className, name, desc);
	}

	public boolean equalsIproc(int ci, Bytes className, Bytes name, Bytes desc)
	{
		return equalsClassNameDesc(TAG_IPROC, ci, className, name, desc);
	}

	public boolean equalsNameDesc(int ci, int nameCi, int descCi)
	{
		return equalsRef2(TAG_NAMEDESC, ci, nameCi, descCi);
	}

	public boolean equalsNameDesc(int ci, byte[] name, byte[] desc)
	{
		return readTag(ci) == TAG_NAMEDESC && equalsUtf(read0u2(bis[ci] + 1), name)
			&& equalsUtf(read0u2(bis[ci] + 3), desc);
	}

	public boolean equalsNameDesc(int ci, Bytes name, Bytes desc)
	{
		return readTag(ci) == TAG_NAMEDESC && equalsUtf(read0u2(bis[ci] + 1), name)
			&& equalsUtf(read0u2(bis[ci] + 3), desc);
	}

	/** @return the constant index, negative if nothing found. */
	public int searchUtf(byte[] utf)
	{
		for (int ci = 1; ci < conN; ci++)
			if (equalsUtf(ci, utf))
				return ci;
		return -1;
	}

	/** @return the constant index, negative if nothing found. */
	public int searchUtf(byte[] utf, int begin, int end1)
	{
		for (int ci = 1; ci < conN; ci++)
			if (equalsUtf(ci, utf, begin, end1))
				return ci;
		return -1;
	}

	/** @return the constant index, negative if nothing found. */
	public int searchUtf(Bytes utf)
	{
		for (int ci = 1; ci < conN; ci++)
			if (equalsUtf(ci, utf))
				return ci;
		return -1;
	}

	/** @return the constant index, negative if nothing found. */
	public int searchUtfFromLast(byte[] utf)
	{
		for (int ci = conN - 1; ci >= 1; ci--)
			if (equalsUtf(ci, utf))
				return ci;
		return -1;
	}

	/** @return the constant index, negative if nothing found. */
	public int searchUtfFromLast(byte[] utf, int begin, int end1)
	{
		for (int ci = conN - 1; ci >= 1; ci--)
			if (equalsUtf(ci, utf, begin, end1))
				return ci;
		return -1;
	}

	/** @return the constant index, negative if nothing found. */
	public int searchUtfFromLast(Bytes utf)
	{
		for (int ci = conN - 1; ci >= 1; ci--)
			if (equalsUtf(ci, utf))
				return ci;
		return -1;
	}

	/** @return the constant index, negative if nothing found. */
	public int searchInt(int v)
	{
		for (int ci = 1; ci < conN; ci++)
			if (equalsInt(ci, v))
				return ci;
		return -1;
	}

	/** @return the constant index, negative if nothing found. */
	public int searchLong(long v)
	{
		for (int ci = 1; ci < conN; ci++)
			// searching the index next to a long has no effect
			// kick the stupid specification ass!
			if (equalsLong(ci, v))
				return ci;
		return -1;
	}

	/** @return the constant index, negative if nothing found. */
	public int searchFloat(float v)
	{
		for (int ci = 1; ci < conN; ci++)
			if (equalsFloat(ci, v))
				return ci;
		return -1;
	}

	/** @return the constant index, negative if nothing found. */
	public int searchDouble(double v)
	{
		for (int ci = 1; ci < conN; ci++)
			// searching the index next to a double has no effect
			// kick the stupid specification ass!
			if (equalsDouble(ci, v))
				return ci;
		return -1;
	}

	/** @return the constant index, negative if nothing found. */
	protected int searchRef(byte tag, int refCi)
	{
		for (int ci = 1; ci < conN; ci++)
			if (equalsRef(tag, ci, refCi))
				return ci;
		return -1;
	}

	/** @return the constant index, negative if nothing found. */
	protected int searchRefUtf(byte tag, byte[] utf)
	{
		for (int ci = 1; ci < conN; ci++)
			if (equalsRefUtf(tag, ci, utf))
				return ci;
		return -1;
	}

	/** @return the constant index, negative if nothing found. */
	protected int searchRefUtf(byte tag, Bytes utf)
	{
		for (int ci = 1; ci < conN; ci++)
			if (equalsRefUtf(tag, ci, utf))
				return ci;
		return -1;
	}

	/** @return the constant index, negative if nothing found. */
	public int searchClass(int nameCi)
	{
		return searchRef(TAG_CLASS, nameCi);
	}

	/** @return the constant index, negative if nothing found. */
	public int searchClass(byte[] name)
	{
		return searchRefUtf(TAG_CLASS, name);
	}

	/** @return the constant index, negative if nothing found. */
	public int searchClass(Bytes name)
	{
		return searchRefUtf(TAG_CLASS, name);
	}

	/** @return the constant index, negative if nothing found. */
	public int searchString(int strCi)
	{
		return searchRef(TAG_STRING, strCi);
	}

	/** @return the constant index, negative if nothing found. */
	public int searchString(byte[] str)
	{
		return searchRefUtf(TAG_STRING, str);
	}

	/** @return the constant index, negative if nothing found. */
	public int searchString(Bytes str)
	{
		return searchRefUtf(TAG_STRING, str);
	}

	/** @return the constant index, negative if nothing found. */
	protected int searchRef2(byte tag, int ref1Ci, int ref2Ci)
	{
		for (int ci = 1; ci < conN; ci++)
			if (equalsRef2(tag, ci, ref1Ci, ref2Ci))
				return ci;
		return -1;
	}

	/** @return the constant index, negative if nothing found. */
	protected int searchClassNameDesc(byte tag, int classNameCi, int nameCi, int descCi)
	{
		for (int ci = 1; ci < conN; ci++)
			if (equalsClassNameDesc(tag, ci, classNameCi, nameCi, descCi))
				return ci;
		return -1;
	}

	/** @return the constant index, negative if nothing found. */
	protected int searchClassNameDesc(byte tag, byte[] className, byte[] name, byte[] desc)
	{
		for (int ci = 1; ci < conN; ci++)
			if (equalsClassNameDesc(tag, ci, className, name, desc))
				return ci;
		return -1;
	}

	/** @return the constant index, negative if nothing found. */
	protected int searchClassNameDesc(byte tag, Bytes className, Bytes name, Bytes desc)
	{
		for (int ci = 1; ci < conN; ci++)
			if (equalsClassNameDesc(tag, ci, className, name, desc))
				return ci;
		return -1;
	}

	/** @return the constant index, negative if nothing found. */
	public int searchField(int classNameCi, int nameDescCi)
	{
		return searchRef2(TAG_FIELD, classNameCi, nameDescCi);
	}

	/** @return the constant index, negative if nothing found. */
	public int searchField(int classNameCi, int nameCi, int descCi)
	{
		return searchClassNameDesc(TAG_FIELD, classNameCi, nameCi, descCi);
	}

	/** @return the constant index, negative if nothing found. */
	public int searchField(byte[] className, byte[] name, byte[] desc)
	{
		return searchClassNameDesc(TAG_FIELD, className, name, desc);
	}

	/** @return the constant index, negative if nothing found. */
	public int searchField(Bytes className, Bytes name, Bytes desc)
	{
		return searchClassNameDesc(TAG_FIELD, className, name, desc);
	}

	/** @return the constant index, negative if nothing found. */
	public int searchCproc(int classNameCi, int nameDescCi)
	{
		return searchRef2(TAG_CPROC, classNameCi, nameDescCi);
	}

	/** @return the constant index, negative if nothing found. */
	public int searchCproc(int classNameCi, int nameCi, int descCi)
	{
		return searchClassNameDesc(TAG_CPROC, classNameCi, nameCi, descCi);
	}

	/** @return the constant index, negative if nothing found. */
	public int searchCproc(byte[] className, byte[] name, byte[] desc)
	{
		return searchClassNameDesc(TAG_CPROC, className, name, desc);
	}

	/** @return the constant index, negative if nothing found. */
	public int searchCproc(Bytes className, Bytes name, Bytes desc)
	{
		return searchClassNameDesc(TAG_CPROC, className, name, desc);
	}

	/** @return the constant index, negative if nothing found. */
	public int searchIproc(int classNameCi, int nameDescCi)
	{
		return searchRef2(TAG_IPROC, classNameCi, nameDescCi);
	}

	/** @return the constant index, negative if nothing found. */
	public int searchIproc(int classNameCi, int nameCi, int descCi)
	{
		return searchClassNameDesc(TAG_IPROC, classNameCi, nameCi, descCi);
	}

	/** @return the constant index, negative if nothing found. */
	public int searchIproc(byte[] className, byte[] name, byte[] desc)
	{
		return searchClassNameDesc(TAG_IPROC, className, name, desc);
	}

	/** @return the constant index, negative if nothing found. */
	public int searchIproc(Bytes className, Bytes name, Bytes desc)
	{
		return searchClassNameDesc(TAG_IPROC, className, name, desc);
	}

	/** @return the constant index, negative if nothing found. */
	public int searchNameDesc(int nameI, int descI)
	{
		return searchRef2(TAG_NAMEDESC, nameI, descI);
	}

	/** @return the constant index, negative if nothing found. */
	public int searchNameDesc(byte[] name, byte[] desc)
	{
		for (int ci = 1; ci < conN; ci++)
			if (equalsNameDesc(ci, name, desc))
				return ci;
		return -1;
	}

	/** @return the constant index, negative if nothing found. */
	public int searchNameDesc(Bytes name, Bytes desc)
	{
		for (int ci = 1; ci < conN; ci++)
			if (equalsNameDesc(ci, name, desc))
				return ci;
		return -1;
	}

	public byte[] classDesc2Internal(int utfCi)
	{
		return classDesc2Internal(bytes, readUtfBegin(utfCi), readUtfEnd1(utfCi));
	}

	public String classDesc2InternalChars(int utfCi)
	{
		return classDesc2InternalChars(bytes, readUtfBegin(utfCi), readUtfEnd1(utfCi));
	}

	@Override
	protected void printContents(PrintStream out, String indent1st, String indent,
		int verbose, boolean hash)
	{
		out.print(indent1st);
		out.print(" conN ");
		out.println(getConN());
		for (int i = 1; i < getConN(); i++)
		{
			out.print(indent);
			out.print(i);
			out.print(". ");
			printConLn(i, out);
		}
	}

	public void printCon(int ci, PrintStream out)
	{
		switch (readTag(ci))
		{
		case TAG_UTF:
			out.print("utf ");
			out.print(readUtfChars(ci));
			break;
		case TAG_INT:
			out.print("int ");
			out.print(readInt(ci));
			break;
		case TAG_FLOAT:
			out.print("float ");
			out.print(readFloat(ci));
			break;
		case TAG_LONG:
			out.print("long ");
			out.print(readLong(ci));
			break;
		case TAG_DOUBLE:
			out.print("double ");
			out.print(readDouble(ci));
			break;
		case TAG_CLASS:
			out.print("class ");
			out.print(readUtfChars(readClass(ci)));
			break;
		case TAG_STRING:
			out.print("str ");
			out.print(readUtfChars(readString(ci)));
			break;
		case TAG_FIELD:
			out.print("field ");
			out.print(readUtfChars(readFieldClass(ci)));
			out.print('.');
			out.print(readUtfChars(readFieldName(ci)));
			out.print(' ');
			out.print(readUtfChars(readFieldDesc(ci)));
			break;
		case TAG_CPROC:
			out.print("cproc ");
			out.print(readUtfChars(readCprocClass(ci)));
			out.print('.');
			out.print(readUtfChars(readCprocName(ci)));
			out.print(' ');
			out.print(readUtfChars(readCprocDesc(ci)));
			break;
		case TAG_IPROC:
			out.print("iproc ");
			out.print(readUtfChars(readIprocClass(ci)));
			out.print('.');
			out.print(readUtfChars(readIprocName(ci)));
			out.print(readUtfChars(readIprocDesc(ci)));
			break;
		case TAG_NAMEDESC:
			out.print("nameDesc ");
			out.print(readUtfChars(readNameDescName(ci)));
			out.print(' ');
			out.print(readUtfChars(readNameDescDesc(ci)));
			break;
		}
	}

	public void printConLn(int ci, PrintStream out)
	{
		printCon(ci, out);
		out.println();
	}

	public void printConColon(int ci, PrintStream out)
	{
		out.print(':');
		printCon(ci, out);
	}

	public void printConColonLn(int ci, PrintStream out)
	{
		out.print(':');
		printConLn(ci, out);
	}

	public void printUtfChars(PrintStream out, int utfCi, int verbose)
	{
		if (verbose > 0 && utfCi > 0)
		{
			out.print(':');
			out.print(readUtfChars(utfCi));
		}
	}

	public void printClassChars(PrintStream out, int classCi, int verbose)
	{
		if (verbose > 0 && classCi > 0)
		{
			out.print(':');
			out.print(readUtfChars(readClass(classCi)));
		}
	}

	protected void ensureN(int cn, int bn)
	{
		readBis();
		bis = Array2.ensureN(bis, cn);
		if (conN > conN0)
			bytes = Array2.ensureN(bytes, bn);
		else
		{
			byte[] bs = new byte[Math.max((int)(end1Bi * 1.5f), bn)];
			System.arraycopy(bytes, 0, bs, 0, end1Bi);
			bytes = bs;
		}
	}

	/** @return byte index of appended constant */
	protected int append(int bn)
	{
		int n = bis[conN] + bn;
		ensureN(conN + 2, n);
		bis[++conN] = n;
		return n - bn;
	}

	public int appendUtf(byte[] utf)
	{
		int bi = append(3 + utf.length);
		write0s1(bi, TAG_UTF);
		write0u2(bi + 1, utf.length);
		System.arraycopy(utf, 0, bytes, bi + 3, utf.length);
		return conN - 1;
	}

	public int appendUtf(byte[] utf, int begin, int end1)
	{
		Math2.checkRange(begin, end1, utf.length);
		int bi = append(3 + end1 - begin);
		write0s1(bi, TAG_UTF);
		write0u2(bi + 1, end1 - begin);
		System.arraycopy(utf, begin, bytes, bi + 3, end1 - begin);
		return conN - 1;
	}

	public int appendUtf(Bytes utf)
	{
		int bi = append(3 + utf.byteN());
		write0s1(bi, TAG_UTF);
		write0u2(bi + 1, utf.byteN());
		utf.copyTo(0, bytes, bi + 3, utf.byteN());
		return conN - 1;
	}

	public int putUtf(byte[] utf)
	{
		int i = searchUtf(utf);
		if (i < 0)
			i = appendUtf(utf);
		return i;
	}

	public int putUtf(byte[] utf, int begin, int end1)
	{
		int i = searchUtf(utf, begin, end1);
		if (i < 0)
			i = appendUtf(utf, begin, end1);
		return i;
	}

	public int putUtf(Bytes utf)
	{
		int i = searchUtf(utf);
		if (i < 0)
			i = appendUtf(utf);
		return i;
	}

	public int putUtfFromLast(byte[] utf)
	{
		int i = searchUtfFromLast(utf);
		if (i < 0)
			i = appendUtf(utf);
		return i;
	}

	public int putUtfFromLast(byte[] utf, int begin, int end1)
	{
		int i = searchUtfFromLast(utf, begin, end1);
		if (i < 0)
			i = appendUtf(utf, begin, end1);
		return i;
	}

	public int putUtfFromLast(Bytes utf)
	{
		int i = searchUtfFromLast(utf);
		if (i < 0)
			i = appendUtf(utf);
		return i;
	}

	public int appendInt(int v)
	{
		int bi = append(5);
		write0s1(bi, TAG_INT);
		write0s4(bi + 1, v);
		return conN - 1;
	}

	public int putInt(int v)
	{
		int i = searchInt(v);
		if (i < 0)
			i = appendInt(v);
		return i;
	}

	public int appendLong(long v)
	{
		int bi = append(9);
		append(9); // stupid specification
		write0s1(bi, TAG_LONG);
		write0s8(bi + 1, v);
		return conN - 2;
	}

	public int putLong(long v)
	{
		int i = searchLong(v);
		if (i < 0)
			i = appendLong(v);
		return i;
	}

	public int appendFloat(float v)
	{
		int bi = append(5);
		write0s1(bi, TAG_FLOAT);
		write0s4(bi + 1, Float.floatToRawIntBits(v));
		return conN - 1;
	}

	public int putFloat(float v)
	{
		int i = searchFloat(v);
		if (i < 0)
			i = appendFloat(v);
		return i;
	}

	public int appendDouble(double v)
	{
		int bi = append(9);
		append(bi + 9); // stupid specification
		write0s1(bi, TAG_DOUBLE);
		write0s8(bi + 1, Double.doubleToRawLongBits(v));
		return conN - 2;
	}

	public int putDouble(double v)
	{
		int i = searchDouble(v);
		if (i < 0)
			i = appendDouble(v);
		return i;
	}

	protected int appendRef(byte tag, int refCi, byte refTag)
	{
		if (readTag(refCi) != refTag)
			throw new ClassCastException();
		int bi = append(3);
		write0s1(bi, tag);
		write0u2(bi + 1, refCi);
		return conN - 1;
	}

	protected int putRef(byte tag, int refCi, byte refTag)
	{
		if (readTag(refCi) != refTag)
			throw new ClassCastException();
		int i = searchRef(tag, refCi);
		if (i < 0)
			i = appendRef(tag, refCi, refTag);
		return i;
	}

	public int appendClass(int nameCi)
	{
		return appendRef(TAG_CLASS, nameCi, TAG_UTF);
	}

	public int putClass(int nameCi)
	{
		return putRef(TAG_CLASS, nameCi, TAG_UTF);
	}

	public int appendString(int strCi)
	{
		return appendRef(TAG_STRING, strCi, TAG_UTF);
	}

	public int putString(int strCi)
	{
		return putRef(TAG_STRING, strCi, TAG_UTF);
	}

	protected int appendRef2(byte tag, int ref1Ci, byte ref1Tag, int ref2I, byte ref2Tag)
	{
		if (readTag(ref1Ci) != ref1Tag || readTag(ref2I) != ref2Tag)
			throw new ClassCastException();
		int bi = append(5);
		write0s1(bi, tag);
		write0u2(bi + 1, ref1Ci);
		write0u2(bi + 3, ref2I);
		return conN - 1;
	}

	protected int putRef2(byte tag, int ref1Ci, byte ref1Tag, int ref2Ci, byte ref2Tag)
	{
		if (readTag(ref1Ci) != ref1Tag || readTag(ref2Ci) != ref2Tag)
			throw new ClassCastException();
		int i = searchRef2(tag, ref1Ci, ref2Ci);
		if (i < 0)
			i = appendRef2(tag, ref1Ci, ref1Tag, ref2Ci, ref2Tag);
		return i;
	}

	public int appendField(int classCi, int nameDescCi)
	{
		return appendRef2(TAG_FIELD, classCi, TAG_CLASS, nameDescCi, TAG_NAMEDESC);
	}

	public int putField(int classCi, int nameDescCi)
	{
		return putRef2(TAG_FIELD, classCi, TAG_CLASS, nameDescCi, TAG_NAMEDESC);
	}

	public int appendCproc(int classCi, int nameDescCi)
	{
		return appendRef2(TAG_CPROC, classCi, TAG_CLASS, nameDescCi, TAG_NAMEDESC);
	}

	public int putCproc(int classCi, int nameDescCi)
	{
		return putRef2(TAG_CPROC, classCi, TAG_CLASS, nameDescCi, TAG_NAMEDESC);
	}

	public int appendIproc(int classCi, int nameDescCi)
	{
		return appendRef2(TAG_IPROC, classCi, TAG_CLASS, nameDescCi, TAG_NAMEDESC);
	}

	public int putIproc(int classCi, int nameDescCi)
	{
		return putRef2(TAG_IPROC, classCi, TAG_CLASS, nameDescCi, TAG_NAMEDESC);
	}

	public int appendNameDesc(int nameCi, int descI)
	{
		return appendRef2(TAG_NAMEDESC, nameCi, TAG_UTF, descI, TAG_UTF);
	}

	public int putNameDesc(int nameCi, int descI)
	{
		return putRef2(TAG_NAMEDESC, nameCi, TAG_UTF, descI, TAG_UTF);
	}

	public int appendUtf(Constants cons, int ci)
	{
		return appendUtf(cons.bytes, cons.readUtfBegin(ci), cons.readUtfEnd1(ci));
	}

	public int putUtf(Constants cons, int ci)
	{
		return putUtfFromLast(cons.bytes, cons.readUtfBegin(ci), cons.readUtfEnd1(ci));
	}

	public int appendClass(Constants cons, int ci)
	{
		return appendClass(appendUtf(cons, cons.readClass(ci)));
	}

	public int putClass(Constants cons, int ci)
	{
		return putClass(putUtf(cons, cons.readClass(ci)));
	}

	public int appendString(Constants cons, int ci)
	{
		return appendString(appendUtf(cons, cons.readString(ci)));
	}

	public int putString(Constants cons, int ci)
	{
		return putString(putUtf(cons, cons.readString(ci)));
	}

	public int appendField(Constants cons, int ci)
	{
		return appendField(appendClass(appendUtf(cons, cons.readFieldClass(ci))),
			appendNameDesc(appendUtf(cons, cons.readFieldName(ci)), appendUtf(cons, cons
				.readFieldDesc(ci))));
	}

	public int putField(Constants cons, int ci)
	{
		return putField(putClass(putUtf(cons, cons.readFieldClass(ci))), putNameDesc(putUtf(
			cons, cons.readFieldName(ci)), putUtf(cons, cons.readFieldDesc(ci))));
	}

	public int appendCproc(Constants cons, int ci)
	{
		return appendCproc(appendClass(appendUtf(cons, cons.readCprocClass(ci))),
			appendNameDesc(appendUtf(cons, cons.readCprocName(ci)), appendUtf(cons, cons
				.readCprocDesc(ci))));
	}

	public int putCproc(Constants cons, int ci)
	{
		return putCproc(putClass(putUtf(cons, cons.readCprocClass(ci))), putNameDesc(putUtf(
			cons, cons.readCprocName(ci)), putUtf(cons, cons.readCprocDesc(ci))));
	}

	public int appendIproc(Constants cons, int ci)
	{
		return appendIproc(appendClass(appendUtf(cons, cons.readIprocClass(ci))),
			appendNameDesc(appendUtf(cons, cons.readIprocName(ci)), appendUtf(cons, cons
				.readIprocDesc(ci))));
	}

	public int putIproc(Constants cons, int ci)
	{
		return putIproc(putClass(putUtf(cons, cons.readIprocClass(ci))), putNameDesc(putUtf(
			cons, cons.readIprocName(ci)), putUtf(cons, cons.readIprocDesc(ci))));
	}

	public int appendNameDesc(Constants cons, int ci)
	{
		return appendNameDesc(appendUtf(cons, cons.readNameDescName(ci)), appendUtf(cons,
			cons.readNameDescDesc(ci)));
	}

	public int putNameDesc(Constants cons, int ci)
	{
		return putNameDesc(putUtf(cons, cons.readNameDescName(ci)), putUtf(cons, cons
			.readNameDescDesc(ci)));
	}

	public int appendConstant(Constants cons, int ci)
	{
		switch (cons.readTag(ci))
		{
		case TAG_UTF:
			return appendUtf(cons, ci);
		case TAG_INT:
			return appendInt(cons.readInt(ci));
		case TAG_FLOAT:
			return appendFloat(cons.readFloat(ci));
		case TAG_LONG:
			return appendLong(cons.readLong(ci));
		case TAG_DOUBLE:
			return appendDouble(cons.readDouble(ci));
		case TAG_CLASS:
			return appendClass(cons, ci);
		case TAG_STRING:
			return appendString(cons, ci);
		case TAG_FIELD:
			return appendField(cons, ci);
		case TAG_CPROC:
			return appendCproc(cons, ci);
		case TAG_IPROC:
			return appendIproc(cons, ci);
		case TAG_NAMEDESC:
			return appendNameDesc(cons, ci);
		}
		throw new ClassFormatError("invalid constant info tag " + cons.readTag(ci));
	}

	public int putConstant(Constants cons, int ci)
	{
		switch (cons.readTag(ci))
		{
		case TAG_UTF:
			return putUtf(cons, ci);
		case TAG_INT:
			return putInt(cons.readInt(ci));
		case TAG_FLOAT:
			return putFloat(cons.readFloat(ci));
		case TAG_LONG:
			return putLong(cons.readLong(ci));
		case TAG_DOUBLE:
			return putDouble(cons.readDouble(ci));
		case TAG_CLASS:
			return putClass(cons, ci);
		case TAG_STRING:
			return putString(cons, ci);
		case TAG_FIELD:
			return putField(cons, ci);
		case TAG_CPROC:
			return putCproc(cons, ci);
		case TAG_IPROC:
			return putIproc(cons, ci);
		case TAG_NAMEDESC:
			return putNameDesc(cons, ci);
		}
		throw new ClassFormatError("invalid constant info tag");
	}

	@Override
	public int generateByteN()
	{
		return bis == null ? byteN() : bis[conN] - beginBi;
	}

	@Override
	public int generateTo(byte[] bs, int begin)
	{
		int n = generateByteN();
		System.arraycopy(bytes, beginBi, bs, begin, n);
		writeU2(bs, begin, conN);
		return begin + n;
	}
}
