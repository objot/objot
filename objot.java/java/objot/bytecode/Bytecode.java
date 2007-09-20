package objot.bytecode;

import java.io.PrintStream;

import objot.util.Bytes;


public class Bytecode
	extends Element
{
	protected static final Bytes SIGNATURE = utf("Signature");
	protected static final Bytes CONSTANT_VALUE = utf("ConstantValue");
	protected static final Bytes ANNOS = utf("RuntimeVisibleAnnotations");
	protected static final Bytes ANNOHIDES = utf("RuntimeInvisibleAnnotations");
	protected static final Bytes EXCEPTIONS = utf("Exceptions");
	protected static final Bytes ANNO_PARAMS = utf("RuntimeVisibleParameterAnnotations");
	protected static final Bytes ANNOHIDE_PARAMS = utf("RuntimeInvisibleParameterAnnotations");
	protected static final Bytes CODE = utf("Code");
	protected static final Bytes CODE_LINES = utf("LineNumberTable");
	protected static final Bytes CODE_VARS = utf("LocalVariableTable");
	protected static final Bytes CODE_VARSIGNS = utf("LocalVariableTypeTable");

	public final Constants cons;
	public final Head head;
	protected Fields fields;
	protected Procedures procs;
	protected int attrN;
	protected int attrBi;
	protected int signatureBi;
	protected int signatureCi;
	protected int annosBi;
	protected Annotations annos;
	protected int annoHidesBi;
	protected Annotations annoHides;

	/** @param end1Bi_ be lazy checked */
	public Bytecode(byte[] bs, int beginBi_, int end1Bi_)
	{
		super(bs, beginBi_);
		if (read0s4(beginBi) != 0xCAFEBABE)
			throw new ClassFormatError("invalid magic number");
		if ((read0u2(beginBi + 4) | read0u2(beginBi + 6) << 16) > 50 << 16)
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
		write0s4(0, 0xCAFEBABE);
		write0u2(4, 0); // minor verson
		write0u2(6, 49); // major version
		write0u2(8, 1); // constantN
		write0u2(10, 0); // modifier
		write0u2(12, 0); // classCi
		write0u2(14, 0); // superCi
		write0u2(16, 0); // interfaceN
		write0u2(18, 0); // fieldN
		write0u2(20, 0); // procN
		write0u2(22, 0); // attrN
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

	protected void readAttrs()
	{
		if (attrBi > 0)
			return;
		attrBi = getProcs().end1Bi + 2;
		attrN = read0u2(attrBi - 2);
		int bi = attrBi;
		for (int an = attrN; an > 0; an--)
		{
			int name = read0u2(bi);
			if (signatureBi <= 0 && cons.equalsUtf(name, Bytecode.SIGNATURE))
			{
				signatureBi = bi;
				signatureCi = read0u2(bi + 6);
			}
			else if (annosBi <= 0 && cons.equalsUtf(name, Bytecode.ANNOS))
				annosBi = bi;
			else if (annoHidesBi <= 0 && cons.equalsUtf(name, Bytecode.ANNOHIDES))
				annoHidesBi = bi;
			bi += 6 + read0u4(bi + 2);
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
	protected void printContents(PrintStream out, int indent1st, int indent, int verbose,
		boolean hash)
	{
		out.println();
		cons.printTo(out, indent, indent, verbose, hash);
		head.printTo(out, indent, indent, verbose, hash);
		getFields().printTo(out, indent, indent, verbose, hash);
		getProcs().printTo(out, indent, indent, verbose, hash);
		printIndent(out, indent);
		out.print("attrN ");
		out.println(getAttrN());
		if (getAnnos() != null)
			getAnnos().printTo(out, indent, indent, verbose, hash);
		if (getAnnoHides() != null)
			getAnnoHides().printTo(out, indent, indent, verbose, hash);
		out.print("end of ");
		printIdentityLn(out, 0, hash);
		out.flush();
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
		if (head != null)
			n += head.normalizeByteN() - head.byteN0();
		if (fields != null)
			n += fields.normalizeByteN() - fields.byteN0();
		if (procs != null)
			n += procs.normalizeByteN() - procs.byteN0();
		if (annos != null)
			n += annos.normalizeByteN() - annos.byteN0();
		if (annoHides != null)
			n += annoHides.normalizeByteN() - annoHides.byteN0();
		return n;
	}

	@Override
	public int normalizeTo(byte[] bs, int begin)
	{
		System.arraycopy(bytes, 0, bs, begin, 8);
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

		if (attrBi <= 0)
		{
			System.arraycopy(bytes, bi, bs, begin, end1Bi - bi);
			return begin + end1Bi - bi;
		}
		writeU2(bs, begin, attrN);
		bi = attrBi;
		begin += 2;
		for (int an = attrN; an > 0; an--)
		{
			int bn = 6 + read0u4(bi + 2);
			if (bi == annosBi && annos != null)
				begin = annos.normalizeTo(bs, begin);
			else if (bi == annoHidesBi && annoHides != null)
				begin = annoHides.normalizeTo(bs, begin);
			else
			{
				System.arraycopy(bytes, bi, bs, begin, bn);
				begin += bn;
			}
			bi += bn;
		}
		return begin;
	}
}
