//
// Copyright 2007-2010 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.bytecode;

import java.io.FileInputStream;
import java.io.PrintStream;

import objot.util.Bytes;


public final class Bytecode
	extends Element
{
	static final Bytes SIGNATURE = utf("Signature");
	static final Bytes CONSTANT_VALUE = utf("ConstantValue");
	static final Bytes ANNOS = utf("RuntimeVisibleAnnotations");
	static final Bytes ANNOHIDES = utf("RuntimeInvisibleAnnotations");
	static final Bytes EXCEPTIONS = utf("Exceptions");
	static final Bytes ANNO_PARAMS = utf("RuntimeVisibleParameterAnnotations");
	static final Bytes ANNOHIDE_PARAMS = utf("RuntimeInvisibleParameterAnnotations");
	static final Bytes CODE = utf("Code");
	static final Bytes CODE_LINES = utf("LineNumberTable");
	static final Bytes CODE_VARS = utf("LocalVariableTable");
	static final Bytes CODE_VARSIGNS = utf("LocalVariableTypeTable");
	static final Bytes INNER_CLASS = utf("InnerClasses");

	public final Constants cons;
	public final Head head;
	Fields fields;
	Procedures procs;
	int attrN;
	int attrBi;
	int signatureBi;
	int signatureCi;
	int annosBi;
	Annotations annos;
	int annoHidesBi;
	Annotations annoHides;
	/** negative to remove */
	int innerClassBi;

	/** @param end1Bi_ be lazy checked */
	public Bytecode(byte[] bs, int beginBi_, int end1Bi_)
	{
		super(bs, beginBi_);
		if (readS4(bytes, beginBi) != 0xCAFEBABE)
			throw new ClassFormatError("invalid magic number");
		if ((readU2(bytes, beginBi + 4) | readU2(bytes, beginBi + 6) << 16) > 52 << 16)
			throw new ClassFormatError("unsupported bytecode version");
		end1Bi = end1Bi_;
		if (end1Bi <= beginBi)
			throw new ClassFormatError("invalid bytecode index");
		cons = new Constants(bytes, beginBi + 8);
		head = new Head(cons, bytes, cons.end1Bi);
	}

	public Bytecode(Bytes bs)
	{
		this(bs.bytes, bs.beginBi, bs.end1Bi);
	}

	/** empty bytecode */
	public Bytecode()
	{
		super(null, 0);
		ensureByteN(24);
		writeS4(bytes, 0, 0xCAFEBABE);
		writeU2(bytes, 4, 0); // minor verson
		writeU2(bytes, 6, 49); // major version
		writeU2(bytes, 8, 1); // constantN
		writeU2(bytes, 10, 0); // modifier
		writeU2(bytes, 12, 0); // classCi
		writeU2(bytes, 14, 0); // superCi
		writeU2(bytes, 16, 0); // interfaceN
		writeU2(bytes, 18, 0); // fieldN
		writeU2(bytes, 20, 0); // procN
		writeU2(bytes, 22, 0); // attrN
		end1Bi = 24;
		cons = new Constants(bytes, 8);
		head = new Head(cons, bytes, cons.end1Bi);
	}

	public Fields getFields()
	{
		if (fields == null)
			fields = new Fields(cons, bytes, head.end1Bi);
		return fields;
	}

	public Procedures getProcs()
	{
		if (procs == null)
			procs = new Procedures(cons, bytes, getFields().end1Bi);
		return procs;
	}

	void readAttrs()
	{
		if (attrBi > 0)
			return;
		attrBi = getProcs().end1Bi + 2;
		attrN = readU2(bytes, attrBi - 2);
		int bi = attrBi;
		for (int an = attrN; an > 0; an--)
		{
			int name = readU2(bytes, bi);
			if (signatureBi == 0 && cons.equalsUtf(name, SIGNATURE))
			{
				signatureBi = bi;
				signatureCi = readU2(bytes, bi + 6);
			}
			else if (annosBi == 0 && cons.equalsUtf(name, ANNOS))
				annosBi = bi;
			else if (annoHidesBi == 0 && cons.equalsUtf(name, ANNOHIDES))
				annoHidesBi = bi;
			else if (innerClassBi == 0 && cons.equalsUtf(name, INNER_CLASS))
				innerClassBi = bi;
			bi += 6 + readU4(bytes, bi + 2);
		}
		if (bi != end1Bi)
			throw new ClassFormatError("invalid bytecode length " + bi + " " + end1Bi);
	}

	public int getAttrN()
	{
		readAttrs();
		return attrN;
	}

	public int getAttrBi()
	{
		readAttrs();
		return attrBi;
	}

	public int getSignatureCi()
	{
		readAttrs();
		return signatureCi;
	}

	@Override
	public Annotations getAnnos()
	{
		readAttrs();
		if (annos == null && annosBi > 0)
			annos = new Annotations(cons, bytes, annosBi, false);
		return annos;
	}

	@Override
	public Annotations getAnnoHides()
	{
		readAttrs();
		if (annoHides == null && annoHidesBi > 0)
			annoHides = new Annotations(cons, bytes, annoHidesBi, true);
		return annoHides;
	}

	@Override
	void printContents(PrintStream out, int indent1st, int indent, int verbose)
	{
		out.println();
		cons.printTo(out, indent, indent, verbose);
		head.printTo(out, indent, indent, verbose);
		getFields().printTo(out, indent, indent, verbose);
		getProcs().printTo(out, indent, indent, verbose);
		printIndent(out, indent);
		out.print("attrN ");
		out.println(getAttrN());
		if (getAnnos() != null)
			getAnnos().printTo(out, indent, indent, verbose);
		if (getAnnoHides() != null)
			getAnnoHides().printTo(out, indent, indent, verbose);
		out.print("end of ");
		printIdentity(out, 0).println();
		out.flush();
	}

	public void removeInnerClasses()
	{
		readAttrs();
		innerClassBi = -innerClassBi;
	}

	public byte[] normalize()
	{
		byte[] bs = new byte[normalizeByteN()];
		normalizeTo(bs, 0);
		return bs;
	}

	@Override
	public int normalizeByteN()
	{
		int n = byteN0() + cons.normalizeByteN() - cons.byteN0();
		n += head.normalizeByteN() - head.byteN0();
		if (fields != null)
			n += fields.normalizeByteN() - fields.byteN0();
		if (procs != null)
			n += procs.normalizeByteN() - procs.byteN0();
		if (annos != null)
			n += annos.normalizeByteN() - annos.byteN0();
		if (annoHides != null)
			n += annoHides.normalizeByteN() - annoHides.byteN0();
		if (innerClassBi < 0)
			n -= 6 + readU4(bytes, 2 - innerClassBi);
		return n;
	}

	@Override
	public int normalizeTo(byte[] bs, int begin)
	{
		System.arraycopy(bytes, 0, bs, begin, 8);
		writeU2(bs, begin + 6, 49);
		int bi = cons.end1Bi;
		begin = cons.normalizeTo(bs, begin + 8);

		bi = head.end1Bi;
		begin = head.normalizeTo(bs, begin);

		if (fields == null)
		{
			System.arraycopy(bytes, bi, bs, begin, end1Bi - bi);
			return begin + end1Bi - bi;
		}
		bi = fields.end1Bi;
		begin = fields.normalizeTo(bs, begin);

		if (procs == null)
		{
			System.arraycopy(bytes, bi, bs, begin, end1Bi - bi);
			return begin + end1Bi - bi;
		}
		bi = procs.end1Bi;
		begin = procs.normalizeTo(bs, begin);

		if (attrBi == 0)
		{
			System.arraycopy(bytes, bi, bs, begin, end1Bi - bi);
			return begin + end1Bi - bi;
		}
		writeU2(bs, begin, innerClassBi >= 0 ? attrN : attrN - 1);
		bi = attrBi;
		begin += 2;
		for (int an = attrN; an > 0; an--)
		{
			int bn = 6 + readU4(bytes, bi + 2);
			if (bi == annosBi && annos != null)
				begin = annos.normalizeTo(bs, begin);
			else if (bi == annoHidesBi && annoHides != null)
				begin = annoHides.normalizeTo(bs, begin);
			else if (bi != -innerClassBi)
			{
				System.arraycopy(bytes, bi, bs, begin, bn);
				begin += bn;
			}
			bi += bn;
		}
		return begin;
	}

	public static void main(String[] ps) throws Exception
	{
		int verbose = 1;
		for (String p: ps)
			if (p.startsWith("-v"))
				verbose = Integer.parseInt(p.substring(2));
			else
				new Bytecode(new Bytes(new FileInputStream(p), true)).printTo(System.out, 0,
					0, verbose);
	}
}
