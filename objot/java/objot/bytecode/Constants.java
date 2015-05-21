//
// Copyright 2007-2010 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.bytecode;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import objot.util.Array2;
import objot.util.Bytes;
import objot.util.Class2;
import objot.util.InvalidValueException;


/**
 * All UTFs in this class are no-null-character UTF-8. All names of class are internal
 * form.
 */
public final class Constants
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
	/** class procedure */
	public static final byte TAG_CPROC = 10;
	/** interface procedure */
	public static final byte TAG_IPROC = 11;
	public static final byte TAG_NAMEDESC = 12;
	public static final byte TAG_MHANDLE = 15;
	public static final byte TAG_MTYPE = 16;
	public static final byte TAG_INVOKED = 18;

	int byteN0;
	/** index [1, N) excluding 0. */
	int conN0;
	int conN;
	int[] bis;

	public Constants(byte[] bs, int beginBi_)
	{
		super(bs, beginBi_);
		conN0 = conN = readU2(bytes, beginBi);
		if (conN <= 0)
			throw new ClassFormatError("invalid constants amount");
		int bi = beginBi + 2;
		for (int ci = 1; ci < conN; ci++)
		{
			byte t = bytes[bi];
			if (t == TAG_LONG || t == TAG_DOUBLE)
				if (ci < conN)
					ci++; // stupid specification ass
				else
					throw new ClassFormatError("unexpect constant end");
			bi += readConByteN(bi);
		}
		end1Bi = bi;
		byteN0 = end1Bi - beginBi;
	}

	@Override
	public int byteN0()
	{
		return byteN0;
	}

	public int getConN()
	{
		return conN;
	}

	/** Including the tag byte. */
	int readConByteN(int bi)
	{
		switch (bytes[bi])
		{
		case TAG_UTF:
			int l = readU2(bytes, bi + 1);
			for (int i = bi + 3; i < bi + l; i++)
				if (bytes[i] == 0)
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
		case TAG_MHANDLE:
			return 1 + 1 + 2;
		case TAG_MTYPE:
			return 1 + 2;
		case TAG_INVOKED:
			return 1 + 2 + 2;
		}
		throw new ClassFormatError("invalid constant info tag");
	}

	void readBis()
	{
		if (bis != null)
			return;
		bis = new int[allocN(Math.max(conN, 100))];
		bis[0] = Integer.MIN_VALUE;
		bis[1] = beginBi + 2;
		for (int ci = 2; ci < conN; ci++)
		{
			bis[ci] = bis[ci - 1] + readConByteN(bis[ci - 1]);
			byte t = bytes[bis[ci - 1]];
			if (t == TAG_LONG || t == TAG_DOUBLE)
			{
				bis[ci + 1] = bis[ci];
				bis[ci] = bis[ci - 1];
				ci++;
			}
		}
	}

	/** @param ci [1, N) excluding 0. */
	void checkIndex(int ci)
	{
		if (ci <= 0 || ci >= conN)
			throw new InvalidValueException(ci);
	}

	public byte getTag(int ci)
	{
		checkIndex(ci);
		readBis();
		return bytes[bis[ci]];
	}

	public byte getTag(int ci, byte tag)
	{
		checkIndex(ci);
		readBis();
		if (tag != bytes[bis[ci]])
			throw new ClassCastException();
		return tag;
	}

	public Bytes getUtf(int ci)
	{
		getTag(ci, TAG_UTF);
		return new Bytes(bytes, getUtfBegin(ci), getUtfEnd1(ci));
	}

	public void getUtfTo(int ci, byte[] bs, int bi)
	{
		System.arraycopy(bytes, getUtfBegin(ci), bs, bi, getUtfByteN(ci));
	}

	public void getUtfTo(int ci, Bytes bs, int bi)
	{
		bs.copyFrom(bi, bytes, getUtfBegin(ci), getUtfByteN(ci));
	}

	public String getUcs(int ci)
	{
		return ucs(this, getUtfBegin(ci) - beginBi, getUtfEnd1(ci) - beginBi);
	}

	public int getUtfByteN(int ci)
	{
		getTag(ci, TAG_UTF);
		return readU2(bytes, bis[ci] + 1);
	}

	public int getUtfBegin(int ci)
	{
		getTag(ci, TAG_UTF);
		return bis[ci] + 3;
	}

	public int getUtfEnd1(int ci)
	{
		getTag(ci, TAG_UTF);
		return bis[ci] + 3 + readU2(bytes, bis[ci] + 1);
	}

	public int getInt(int ci)
	{
		getTag(ci, TAG_INT);
		return readS4(bytes, bis[ci] + 1);
	}

	public long getLong(int ci)
	{
		getTag(ci, TAG_LONG);
		return readS8(bytes, bis[ci] + 1);
	}

	public float getFloat(int ci)
	{
		getTag(ci, TAG_FLOAT);
		return Float.intBitsToFloat(readS4(bytes, bis[ci] + 1));
	}

	public double getDouble(int ci)
	{
		getTag(ci, TAG_DOUBLE);
		return Double.longBitsToDouble(readS8(bytes, bis[ci] + 1));
	}

	int getUtfRef(byte tag, int ci)
	{
		getTag(ci, tag);
		return readU2(bytes, bis[ci] + 1);
	}

	public int getClass(int ci)
	{
		return getUtfRef(TAG_CLASS, ci);
	}

	public int getString(int ci)
	{
		return getUtfRef(TAG_STRING, ci);
	}

	int getClassNameDescClass(byte tag, int ci)
	{
		getTag(ci, tag);
		return getClass(readU2(bytes, bis[ci] + 1));
	}

	int getClassNameDescName(byte tag, int ci)
	{
		getTag(ci, tag);
		return getNameDescName(readU2(bytes, bis[ci] + 3));
	}

	int getClassNameDescDesc(byte tag, int ci)
	{
		getTag(ci, tag);
		return getNameDescDesc(readU2(bytes, bis[ci] + 3));
	}

	public int getFieldClass(int ci)
	{
		return getClassNameDescClass(TAG_FIELD, ci);
	}

	public int getFieldName(int ci)
	{
		return getClassNameDescName(TAG_FIELD, ci);
	}

	public int getFieldDesc(int ci)
	{
		return getClassNameDescDesc(TAG_FIELD, ci);
	}

	public int getCprocClass(int ci)
	{
		return getClassNameDescClass(TAG_CPROC, ci);
	}

	public int getCprocName(int ci)
	{
		return getClassNameDescName(TAG_CPROC, ci);
	}

	public int getCprocDesc(int ci)
	{
		return getClassNameDescDesc(TAG_CPROC, ci);
	}

	public int getIprocClass(int ci)
	{
		return getClassNameDescClass(TAG_IPROC, ci);
	}

	public int getIprocName(int ci)
	{
		return getClassNameDescName(TAG_IPROC, ci);
	}

	public int getIprocDesc(int ci)
	{
		return getClassNameDescDesc(TAG_IPROC, ci);
	}

	public int getNameDescName(int ci)
	{
		return getUtfRef(TAG_NAMEDESC, ci);
	}

	public int getNameDescDesc(int ci)
	{
		getTag(ci, TAG_NAMEDESC);
		return readU2(bytes, bis[ci] + 3);
	}

	// ********************************************************************************

	public boolean equalsUtf(int ci, Bytes utf)
	{
		return getTag(ci) == TAG_UTF
			&& utf.equals(bytes, bis[ci] + 3, bis[ci] + 3 + readU2(bytes, bis[ci] + 1));
	}

	public boolean startsWithUtf(int ci, Bytes utf)
	{
		return getTag(ci) == TAG_UTF && readU2(bytes, bis[ci] + 1) >= utf.byteN()
			&& utf.equals(bytes, bis[ci] + 3, bis[ci] + 3 + utf.byteN());
	}

	public boolean equalsInt(int ci, int v)
	{
		return getTag(ci) == TAG_INT && readS4(bytes, bis[ci] + 1) == v;
	}

	public boolean equalsLong(int ci, long v)
	{
		return getTag(ci) == TAG_LONG && readS8(bytes, bis[ci] + 1) == v;
	}

	public boolean equalsFloat(int ci, float v)
	{
		return getTag(ci) == TAG_FLOAT
			&& Float.intBitsToFloat(readS4(bytes, bis[ci] + 1)) == v;
	}

	public boolean equalsDouble(int ci, double v)
	{
		return getTag(ci) == TAG_LONG
			&& Double.longBitsToDouble(readS8(bytes, bis[ci] + 1)) == v;
	}

	boolean equalsRef(byte tag, int ci, int refCi)
	{
		return getTag(ci) == tag && readU2(bytes, bis[ci] + 1) == refCi;
	}

	boolean equalsRefUtf(byte tag, int ci, Bytes utf)
	{
		return getTag(ci) == tag && equalsUtf(readU2(bytes, bis[ci] + 1), utf);
	}

	public boolean equalsClass(int ci, int nameCi)
	{
		return equalsRef(TAG_CLASS, ci, nameCi);
	}

	public boolean equalsClass(int ci, Bytes name)
	{
		return equalsRefUtf(TAG_CLASS, ci, name);
	}

	public boolean equalsString(int ci, int strCi)
	{
		return equalsRef(TAG_STRING, ci, strCi);
	}

	public boolean equalsString(int ci, Bytes str)
	{
		return equalsRefUtf(TAG_STRING, ci, str);
	}

	boolean equalsRef2(byte tag, int ci, int ref1Ci, int ref2Ci)
	{
		return getTag(ci) == tag && readU2(bytes, bis[ci] + 1) == ref1Ci
			&& readU2(bytes, bis[ci] + 3) == ref2Ci;
	}

	boolean equalsClassNameDesc(byte tag, int ci, int classNameCi, int nameCi, int descCi)
	{
		return getTag(ci) == tag && equalsClass(readU2(bytes, bis[ci] + 1), classNameCi)
			&& equalsNameDesc(readU2(bytes, bis[ci] + 3), nameCi, descCi);
	}

	boolean equalsClassNameDesc(byte tag, int ci, Bytes className, Bytes name, Bytes desc)
	{
		return getTag(ci) == tag && equalsClass(readU2(bytes, bis[ci] + 1), className)
			&& equalsNameDesc(readU2(bytes, bis[ci] + 3), name, desc);
	}

	public boolean equalsField(int ci, int classNameCi, int nameDescCi)
	{
		return equalsRef2(TAG_FIELD, ci, classNameCi, nameDescCi);
	}

	public boolean equalsField(int ci, int classNameCi, int nameCi, int descCi)
	{
		return equalsClassNameDesc(TAG_FIELD, ci, classNameCi, nameCi, descCi);
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

	public boolean equalsIproc(int ci, Bytes className, Bytes name, Bytes desc)
	{
		return equalsClassNameDesc(TAG_IPROC, ci, className, name, desc);
	}

	public boolean equalsNameDesc(int ci, int nameCi, int descCi)
	{
		return equalsRef2(TAG_NAMEDESC, ci, nameCi, descCi);
	}

	public boolean equalsNameDesc(int ci, Bytes name, Bytes desc)
	{
		return getTag(ci) == TAG_NAMEDESC && equalsUtf(readU2(bytes, bis[ci] + 1), name)
			&& equalsUtf(readU2(bytes, bis[ci] + 3), desc);
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
	public int searchUtfLast(Bytes utf)
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
			// stupid specification
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
			// stupid specification
			if (equalsDouble(ci, v))
				return ci;
		return -1;
	}

	/** @return the constant index, negative if nothing found. */
	int searchRef(byte tag, int refCi)
	{
		for (int ci = 1; ci < conN; ci++)
			if (equalsRef(tag, ci, refCi))
				return ci;
		return -1;
	}

	/** @return the constant index, negative if nothing found. */
	int searchRefUtf(byte tag, Bytes utf)
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
	public int searchString(Bytes str)
	{
		return searchRefUtf(TAG_STRING, str);
	}

	/** @return the constant index, negative if nothing found. */
	int searchRef2(byte tag, int ref1Ci, int ref2Ci)
	{
		for (int ci = 1; ci < conN; ci++)
			if (equalsRef2(tag, ci, ref1Ci, ref2Ci))
				return ci;
		return -1;
	}

	/** @return the constant index, negative if nothing found. */
	int searchClassNameDesc(byte tag, int classNameCi, int nameCi, int descCi)
	{
		for (int ci = 1; ci < conN; ci++)
			if (equalsClassNameDesc(tag, ci, classNameCi, nameCi, descCi))
				return ci;
		return -1;
	}

	/** @return the constant index, negative if nothing found. */
	int searchClassNameDesc(byte tag, Bytes className, Bytes name, Bytes desc)
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
	public int searchNameDesc(Bytes name, Bytes desc)
	{
		for (int ci = 1; ci < conN; ci++)
			if (equalsNameDesc(ci, name, desc))
				return ci;
		return -1;
	}

	/** @see Class2#pathName(Class) */
	public Bytes classDesc2Name(int utfCi)
	{
		return classDesc2Name(this, getUtfBegin(utfCi) - beginBi, getUtfEnd1(utfCi) - beginBi);
	}

	/** @see Class2#pathName(Class) */
	public String classDesc2NameUcs(int utfCi)
	{
		return classDesc2NameUcs(this, getUtfBegin(utfCi) - beginBi, getUtfEnd1(utfCi)
			- beginBi);
	}

	@Override
	void printContents(PrintStream out, int indent1st, int indent, int verbose)
	{
		if (verbose > 0)
		{
			printIndent(out, indent1st);
			out.print(" conN ");
			out.print(conN);
			out.print("-1");
		}
		out.println();
		if (verbose > 1)
			for (int i = 1; i < conN; i++)
			{
				printIndent(out, indent);
				print(out, i, verbose).println();
			}
	}

	public PrintStream print(PrintStream out, int ci, int verbose)
	{
		out.print(ci);
		if (verbose < 0 || ci <= 0)
			out.print('~');
		else
			switch (getTag(ci))
			{
			case TAG_UTF:
				out.print("'");
				out.print(getUcs(ci));
				break;
			case TAG_INT:
				out.print("-int ");
				out.print(getInt(ci));
				break;
			case TAG_FLOAT:
				out.print("-float ");
				out.print(getFloat(ci));
				break;
			case TAG_LONG:
				out.print("-long ");
				out.print(getLong(ci));
				break;
			case TAG_DOUBLE:
				out.print("-double ");
				out.print(getDouble(ci));
				break;
			case TAG_CLASS:
				out.print("{");
				out.print(getUcs(getClass(ci)));
				break;
			case TAG_STRING:
				out.print("-str ");
				out.print(getUcs(getString(ci)));
				break;
			case TAG_FIELD:
				out.print("-field ");
				out.print(getUcs(getFieldClass(ci)));
				out.print('.');
				out.print(getUcs(getFieldName(ci)));
				out.print(' ');
				out.print(getUcs(getFieldDesc(ci)));
				break;
			case TAG_CPROC:
				out.print("-cproc ");
				out.print(getUcs(getCprocClass(ci)));
				out.print('.');
				out.print(getUcs(getCprocName(ci)));
				out.print(' ');
				out.print(getUcs(getCprocDesc(ci)));
				break;
			case TAG_IPROC:
				out.print("-iproc ");
				out.print(getUcs(getIprocClass(ci)));
				out.print('.');
				out.print(getUcs(getIprocName(ci)));
				out.print(getUcs(getIprocDesc(ci)));
				break;
			case TAG_NAMEDESC:
				out.print("-nameDesc ");
				out.print(getUcs(getNameDescName(ci)));
				out.print(' ');
				out.print(getUcs(getNameDescDesc(ci)));
				break;
			}
		return out;
	}

	public static PrintStream print(Constants cons, PrintStream out, int ci, int verbose)
	{
		if (cons != null)
			return cons.print(out, ci, verbose);
		out.print(ci);
		out.print('~');
		return out;
	}

	// ********************************************************************************

	/** bytes will be different with the original bytes */
	void ensureN(int cn, int bn)
	{
		readBis();
		bis = Array2.ensureN(bis, cn);
		if (conN > conN0)
			bytes = Array2.ensureN(bytes, bn);
		else
		{ // new bytes always
			byte[] bs = new byte[Math.max((int)(end1Bi * 1.5f), bn)];
			System.arraycopy(bytes, 0, bs, 0, end1Bi);
			bytes = bs;
		}
	}

	/** @return second byte index of added constant */
	int add(byte tag, int bn)
	{
		readBis();
		ensureN(conN + 2, end1Bi + bn);
		if (tag == TAG_LONG || tag == TAG_DOUBLE)
			bis[conN++] = end1Bi; // stupid specification
		bis[conN++] = end1Bi;
		bytes[end1Bi] = tag;
		end1Bi += bn;
		return end1Bi - bn;
	}

	public void setUtf(int ci, Bytes utf)
	{
		getTag(ci, TAG_UTF);
		int d = utf.byteN() - getUtfByteN(ci);
		ensureN(conN, end1Bi + d); // new bytes
		if (d != 0 && ci < conN - 1)
		{
			int i = ci + 1;
			System.arraycopy(bytes, bis[i], bytes, bis[i] + d, end1Bi - bis[i]);
			while (i < conN)
				bis[i++] += d;
		}
		end1Bi += d;
		writeU2(bytes, bis[ci] + 1, utf.byteN());
		utf.copyTo(0, bytes, bis[ci] + 3, utf.byteN());
	}

	public int addUtf(Bytes utf)
	{
		int bi = add(TAG_UTF, 3 + utf.byteN());
		writeU2(bytes, bi + 1, utf.byteN());
		utf.copyTo(0, bytes, bi + 3, utf.byteN());
		return conN - 1;
	}

	public int putUtf(Bytes utf)
	{
		int i = searchUtf(utf);
		if (i < 0)
			i = addUtf(utf);
		return i;
	}

	public int putUtfLast(Bytes utf)
	{
		int i = searchUtfLast(utf);
		if (i < 0)
			i = addUtf(utf);
		return i;
	}

	public void setUcs(int ci, String s)
	{
		setUtf(ci, utf(s));
	}

	public int addUcs(String s)
	{
		return addUtf(utf(s));
	}

	public int putUcs(String s)
	{
		return putUtf(utf(s));
	}

	public int putUcsLast(String s)
	{
		return putUtfLast(utf(s));
	}

	public void setInt(int ci, int v)
	{
		getTag(ci, TAG_INT);
		ensureN(conN, end1Bi);
		writeS4(bytes, bis[ci] + 1, v);
	}

	public int addInt(int v)
	{
		int bi = add(TAG_INT, 5);
		writeS4(bytes, bi + 1, v);
		return conN - 1;
	}

	public int putInt(int v)
	{
		int i = searchInt(v);
		if (i < 0)
			i = addInt(v);
		return i;
	}

	public void setLong(int ci, long v)
	{
		getTag(ci, TAG_LONG);
		ensureN(conN, end1Bi);
		writeS8(bytes, bis[ci] + 1, v);
	}

	public int addLong(long v)
	{
		int bi = add(TAG_LONG, 9);
		writeS8(bytes, bi + 1, v);
		return conN - 2; // stupid specification
	}

	public int putLong(long v)
	{
		int i = searchLong(v);
		if (i < 0)
			i = addLong(v);
		return i;
	}

	public void setFloat(int ci, float v)
	{
		getTag(ci, TAG_FLOAT);
		ensureN(conN, end1Bi);
		writeS4(bytes, bis[ci] + 1, Float.floatToRawIntBits(v));
	}

	public int addFloat(float v)
	{
		int bi = add(TAG_FLOAT, 5);
		writeS4(bytes, bi + 1, Float.floatToRawIntBits(v));
		return conN - 1;
	}

	public int putFloat(float v)
	{
		int i = searchFloat(v);
		if (i < 0)
			i = addFloat(v);
		return i;
	}

	public void setDouble(int ci, double v)
	{
		getTag(ci, TAG_DOUBLE);
		ensureN(conN, end1Bi);
		writeS8(bytes, bis[ci] + 1, Double.doubleToRawLongBits(v));
	}

	public int addDouble(double v)
	{
		int bi = add(TAG_DOUBLE, 9);
		writeS8(bytes, bi + 1, Double.doubleToRawLongBits(v));
		return conN - 2; // stupid specification
	}

	public int putDouble(double v)
	{
		int i = searchDouble(v);
		if (i < 0)
			i = addDouble(v);
		return i;
	}

	public void setRef(int ci, byte tag, int refCi, byte refTag)
	{
		getTag(ci, tag);
		getTag(refCi, refTag);
		ensureN(conN, end1Bi);
		writeU2(bytes, bis[ci] + 1, refCi);
	}

	int addRef(byte tag, int refCi, byte refTag)
	{
		getTag(refCi, refTag);
		int bi = add(tag, 3);
		writeU2(bytes, bi + 1, refCi);
		return conN - 1;
	}

	int putRef(byte tag, int refCi, byte refTag)
	{
		getTag(refCi, refTag);
		int i = searchRef(tag, refCi);
		if (i < 0)
			i = addRef(tag, refCi, refTag);
		return i;
	}

	public void setClass(int ci, int nameCi)
	{
		setRef(ci, TAG_CLASS, nameCi, TAG_UTF);
	}

	public int addClass(int nameCi)
	{
		return addRef(TAG_CLASS, nameCi, TAG_UTF);
	}

	public int putClass(int nameCi)
	{
		return putRef(TAG_CLASS, nameCi, TAG_UTF);
	}

	public int addClass(String name)
	{
		return addRef(TAG_CLASS, addUcs(Class2.pathName(name)), TAG_UTF);
	}

	public int putClass(String name)
	{
		return putRef(TAG_CLASS, putUcs(Class2.pathName(name)), TAG_UTF);
	}

	public int addClass(Class<?> cla)
	{
		return addRef(TAG_CLASS, addUtf(utf(Class2.pathName(cla))), TAG_UTF);
	}

	public int putClass(Class<?> cla)
	{
		return putRef(TAG_CLASS, putUtf(utf(Class2.pathName(cla))), TAG_UTF);
	}

	public void setString(int ci, int strCi)
	{
		setRef(ci, TAG_STRING, strCi, TAG_UTF);
	}

	public int addString(int strCi)
	{
		return addRef(TAG_STRING, strCi, TAG_UTF);
	}

	public int putString(int strCi)
	{
		return putRef(TAG_STRING, strCi, TAG_UTF);
	}

	public int addString(String str)
	{
		return addRef(TAG_STRING, addUcs(str), TAG_UTF);
	}

	public int putString(String str)
	{
		return putRef(TAG_STRING, putUcs(str), TAG_UTF);
	}

	public void setRef2(int ci, byte tag, int ref1Ci, byte ref1Tag, int ref2Ci, byte ref2Tag)
	{
		getTag(ci, tag);
		getTag(ref1Ci, ref1Tag);
		getTag(ref2Ci, ref2Tag);
		ensureN(conN, end1Bi);
		writeU2(bytes, bis[ci] + 1, ref1Ci);
		writeU2(bytes, bis[ci] + 3, ref2Ci);
	}

	int addRef2(byte tag, int ref1Ci, byte ref1Tag, int ref2Ci, byte ref2Tag)
	{
		getTag(ref1Ci, ref1Tag);
		getTag(ref2Ci, ref2Tag);
		int bi = add(tag, 5);
		writeU2(bytes, bi + 1, ref1Ci);
		writeU2(bytes, bi + 3, ref2Ci);
		return conN - 1;
	}

	int putRef2(byte tag, int ref1Ci, byte ref1Tag, int ref2Ci, byte ref2Tag)
	{
		getTag(ref1Ci, ref1Tag);
		getTag(ref2Ci, ref2Tag);
		int i = searchRef2(tag, ref1Ci, ref2Ci);
		if (i < 0)
			i = addRef2(tag, ref1Ci, ref1Tag, ref2Ci, ref2Tag);
		return i;
	}

	public void setField(int ci, int classCi, int nameDescCi)
	{
		setRef2(ci, TAG_FIELD, classCi, TAG_CLASS, nameDescCi, TAG_NAMEDESC);
	}

	public int addField(int classCi, int nameDescCi)
	{
		return addRef2(TAG_FIELD, classCi, TAG_CLASS, nameDescCi, TAG_NAMEDESC);
	}

	public int putField(int classCi, int nameDescCi)
	{
		return putRef2(TAG_FIELD, classCi, TAG_CLASS, nameDescCi, TAG_NAMEDESC);
	}

	public int addField(java.lang.reflect.Field f)
	{
		return addField(addClass(f.getDeclaringClass()), addNameDesc(f));
	}

	public int putField(java.lang.reflect.Field f)
	{
		return putField(putClass(f.getDeclaringClass()), putNameDesc(f));
	}

	public void setCproc(int ci, int classCi, int nameDescCi)
	{
		setRef2(ci, TAG_CPROC, classCi, TAG_CLASS, nameDescCi, TAG_NAMEDESC);
	}

	public int addCproc(int classCi, int nameDescCi)
	{
		return addRef2(TAG_CPROC, classCi, TAG_CLASS, nameDescCi, TAG_NAMEDESC);
	}

	public int putCproc(int classCi, int nameDescCi)
	{
		return putRef2(TAG_CPROC, classCi, TAG_CLASS, nameDescCi, TAG_NAMEDESC);
	}

	public void setIproc(int ci, int classCi, int nameDescCi)
	{
		setRef2(ci, TAG_IPROC, classCi, TAG_CLASS, nameDescCi, TAG_NAMEDESC);
	}

	public int addIproc(int classCi, int nameDescCi)
	{
		return addRef2(TAG_IPROC, classCi, TAG_CLASS, nameDescCi, TAG_NAMEDESC);
	}

	public int putIproc(int classCi, int nameDescCi)
	{
		return putRef2(TAG_IPROC, classCi, TAG_CLASS, nameDescCi, TAG_NAMEDESC);
	}

	public int addProc(Constructor<?> c)
	{
		return addRef2(TAG_CPROC, addClass(c.getDeclaringClass()), TAG_CLASS, addNameDesc(c),
			TAG_NAMEDESC);
	}

	public int putProc(Constructor<?> c)
	{
		return putRef2(TAG_CPROC, putClass(c.getDeclaringClass()), TAG_CLASS, putNameDesc(c),
			TAG_NAMEDESC);
	}

	public int addProc(Method m)
	{
		return addRef2(m.getDeclaringClass().isInterface() ? TAG_IPROC : TAG_CPROC,
			addClass(m.getDeclaringClass()), TAG_CLASS, addNameDesc(m), TAG_NAMEDESC);
	}

	public int putProc(Method m)
	{
		return putRef2(m.getDeclaringClass().isInterface() ? TAG_IPROC : TAG_CPROC,
			putClass(m.getDeclaringClass()), TAG_CLASS, putNameDesc(m), TAG_NAMEDESC);
	}

	public int addCtor0(int classCi)
	{
		return addRef2(TAG_CPROC, classCi, TAG_CLASS, //
			addNameDesc(Procedure.CTOR_NAME_, Procedure.VOID_DESC_), TAG_NAMEDESC);
	}

	public int putCtor0(int classCi)
	{
		return putRef2(TAG_CPROC, classCi, TAG_CLASS, //
			putNameDesc(Procedure.CTOR_NAME_, Procedure.VOID_DESC_), TAG_NAMEDESC);
	}

	public void setNameDesc(int ci, int nameCi, int descCi)
	{
		setRef2(ci, TAG_NAMEDESC, nameCi, TAG_UTF, descCi, TAG_UTF);
	}

	public int addNameDesc(int nameCi, int descCi)
	{
		return addRef2(TAG_NAMEDESC, nameCi, TAG_UTF, descCi, TAG_UTF);
	}

	public int putNameDesc(int nameCi, int descCi)
	{
		return putRef2(TAG_NAMEDESC, nameCi, TAG_UTF, descCi, TAG_UTF);
	}

	public int addNameDesc(Bytes name, Bytes desc)
	{
		return addRef2(TAG_NAMEDESC, addUtf(name), TAG_UTF, addUtf(desc), TAG_UTF);
	}

	public int putNameDesc(Bytes name, Bytes desc)
	{
		return putRef2(TAG_NAMEDESC, putUtf(name), TAG_UTF, putUtf(desc), TAG_UTF);
	}

	public int addNameDesc(java.lang.reflect.Field f)
	{
		return addNameDesc(utf(f.getName()), utf(Class2.descript(f)));
	}

	public int putNameDesc(java.lang.reflect.Field f)
	{
		return putNameDesc(utf(f.getName()), utf(Class2.descript(f)));
	}

	public int addNameDesc(Constructor<?> c)
	{
		return addNameDesc(Procedure.CTOR_NAME_, utf(Class2.descript(c)));
	}

	public int putNameDesc(Constructor<?> c)
	{
		return putNameDesc(Procedure.CTOR_NAME_, utf(Class2.descript(c)));
	}

	public int addNameDesc(Method m)
	{
		return addNameDesc(utf(m.getName()), utf(Class2.descript(m)));
	}

	public int putNameDesc(Method m)
	{
		return putNameDesc(utf(m.getName()), utf(Class2.descript(m)));
	}

	// ********************************************************************************

	public int addUtf(Constants cons, int ci)
	{
		return addUtf(new Bytes(cons.bytes, cons.getUtfBegin(ci), cons.getUtfEnd1(ci)));
	}

	public int putUtf(Constants cons, int ci)
	{
		return putUtf(new Bytes(cons.bytes, cons.getUtfBegin(ci), cons.getUtfEnd1(ci)));
	}

	public int addClass(Constants cons, int ci)
	{
		return addClass(addUtf(cons, cons.getClass(ci)));
	}

	public int putClass(Constants cons, int ci)
	{
		return putClass(putUtf(cons, cons.getClass(ci)));
	}

	public int addString(Constants cons, int ci)
	{
		return addString(addUtf(cons, cons.getString(ci)));
	}

	public int putString(Constants cons, int ci)
	{
		return putString(putUtf(cons, cons.getString(ci)));
	}

	public int addField(Constants cons, int ci)
	{
		return addField(addClass(addUtf(cons, cons.getFieldClass(ci))),
			addNameDesc(addUtf(cons, cons.getFieldName(ci)), //
				addUtf(cons, cons.getFieldDesc(ci))));
	}

	public int putField(Constants cons, int ci)
	{
		return putField(putClass(putUtf(cons, cons.getFieldClass(ci))), //
			putNameDesc(putUtf(cons, cons.getFieldName(ci)), //
				putUtf(cons, cons.getFieldDesc(ci))));
	}

	public int addCproc(Constants cons, int ci)
	{
		return addCproc(addClass(addUtf(cons, cons.getCprocClass(ci))),
			addNameDesc(addUtf(cons, cons.getCprocName(ci)), //
				addUtf(cons, cons.getCprocDesc(ci))));
	}

	public int putCproc(Constants cons, int ci)
	{
		return putCproc(putClass(putUtf(cons, cons.getCprocClass(ci))), //
			putNameDesc(putUtf(cons, cons.getCprocName(ci)), //
				putUtf(cons, cons.getCprocDesc(ci))));
	}

	public int addIproc(Constants cons, int ci)
	{
		return addIproc(addClass(addUtf(cons, cons.getIprocClass(ci))),
			addNameDesc(addUtf(cons, cons.getIprocName(ci)), //
				addUtf(cons, cons.getIprocDesc(ci))));
	}

	public int putIproc(Constants cons, int ci)
	{
		return putIproc(putClass(putUtf(cons, cons.getIprocClass(ci))), //
			putNameDesc(putUtf(cons, cons.getIprocName(ci)), //
				putUtf(cons, cons.getIprocDesc(ci))));
	}

	public int addNameDesc(Constants cons, int ci)
	{
		return addNameDesc(addUtf(cons, cons.getNameDescName(ci)), //
			addUtf(cons, cons.getNameDescDesc(ci)));
	}

	public int putNameDesc(Constants cons, int ci)
	{
		return putNameDesc(putUtf(cons, cons.getNameDescName(ci)), //
			putUtf(cons, cons.getNameDescDesc(ci)));
	}

	public int addConstant(Constants cons, int ci)
	{
		switch (cons.getTag(ci))
		{
		case TAG_UTF:
			return addUtf(cons, ci);
		case TAG_INT:
			return addInt(cons.getInt(ci));
		case TAG_FLOAT:
			return addFloat(cons.getFloat(ci));
		case TAG_LONG:
			return addLong(cons.getLong(ci));
		case TAG_DOUBLE:
			return addDouble(cons.getDouble(ci));
		case TAG_CLASS:
			return addClass(cons, ci);
		case TAG_STRING:
			return addString(cons, ci);
		case TAG_FIELD:
			return addField(cons, ci);
		case TAG_CPROC:
			return addCproc(cons, ci);
		case TAG_IPROC:
			return addIproc(cons, ci);
		case TAG_NAMEDESC:
			return addNameDesc(cons, ci);
		}
		throw new ClassFormatError("invalid constant info tag " + cons.getTag(ci));
	}

	public int putConstant(Constants cons, int ci)
	{
		switch (cons.getTag(ci))
		{
		case TAG_UTF:
			return putUtf(cons, ci);
		case TAG_INT:
			return putInt(cons.getInt(ci));
		case TAG_FLOAT:
			return putFloat(cons.getFloat(ci));
		case TAG_LONG:
			return putLong(cons.getLong(ci));
		case TAG_DOUBLE:
			return putDouble(cons.getDouble(ci));
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
		throw new ClassFormatError("invalid constant info tag " + cons.getTag(ci));
	}

	@Override
	public int normalizeByteN()
	{
		return byteN();
	}

	@Override
	public int normalizeTo(byte[] bs, int begin)
	{
		System.arraycopy(bytes, beginBi, bs, begin, byteN());
		writeU2(bs, begin, conN);
		return begin + byteN();
	}
}
