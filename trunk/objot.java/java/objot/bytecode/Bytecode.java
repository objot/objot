package objot.bytecode;

import java.io.PrintStream;

import objot.util.Bytes;


public class Bytecode
	extends Element
{
	public static final Bytecode EMPTY;

	protected static final byte[] SIGNATURE = chars2Utf("Signature");
	protected static final byte[] CONSTANTVALUE = chars2Utf("ConstantValue");
	protected static final byte[] ANNOS = chars2Utf("RuntimeVisibleAnnotations");
	protected static final byte[] ANNOHIDES = chars2Utf("RuntimeInvisibleAnnotations");
	protected static final byte[] EXCEPTIONS = chars2Utf("Exceptions");
	protected static final byte[] ANNOS_ARG = chars2Utf("RuntimeVisibleParameterAnnotations");
	protected static final byte[] ANNOHIDES_ARG = chars2Utf("RuntimeInvisibleParameterAnnotations");
	protected static final byte[] CODE = chars2Utf("Code");
	protected static final byte[] CODE_LINES = chars2Utf("LineNumberTable");
	protected static final byte[] CODE_VARS = chars2Utf("LocalVariableTable");
	protected static final byte[] CODE_VARSIGNS = chars2Utf("LocalVariableTypeTable");

	static
	{
		Bytes bs = new Bytes(null).ensureByteN(256);
		bs.write0s4(0, 0xcafebabe); // magic number
		bs.write0u2(4, 0); // minor verson
		bs.write0u2(6, 49); // major version
		int i = 8;
		// constants
		bs.write0u2(i, 3); // constantN (index from 1)
		i += 2;
		bs.write0s1(i, Constants.TAG_UTF);
		bs.write0u2(i + 1, CODE.length);
		bs.copyFrom(i + 3, CODE, 0, CODE.length);
		i += 3 + CODE.length;
		bs.write0s1(i, Constants.TAG_UTF);
		bs.write0u2(i + 1, EXCEPTIONS.length);
		bs.copyFrom(i + 3, EXCEPTIONS, 0, EXCEPTIONS.length);
		i += 3 + EXCEPTIONS.length;
		// head
		bs.write0u2(i, 0); // modifier
		bs.write0u2(i + 2, 0); // classCi
		bs.write0u2(i + 4, 0); // superCi
		bs.write0u2(i + 6, 0); // interfaceN
		i += 8;
		// fields
		bs.write0u2(i, 0); // fieldN
		i += 2;
		// procs
		bs.write0u2(i, 0); // procN
		i += 2;
		// attrs
		bs.write0u2(i, 0); // attrN
		i += 2;
		// end
		EMPTY = new Bytecode(bs.bytes, bs.beginBi, bs.beginBi + i);
	}

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

	public Bytecode(byte[] bs, int beginBi_, int end1Bi_)
	{
		super(bs, beginBi_);
		if (read0s4(beginBi) != 0xcafebabe)
			throw new ClassFormatError("invalid magic number");
		if ((read0u2(beginBi + 4) | read0u2(beginBi + 6) << 16) > 50 << 16)
			throw new ClassFormatError("unsupported bytecode version");
		end1Bi = end1Bi_;
		if (end1Bi <= beginBi)
			throw new ClassFormatError("invalid bytecode index");
		cons = new Constants(bytes, beginBi + 8);
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
		byte[] bs = new byte[generateByteN()];
		generateTo(bs, 0);
		return bs;
	}

	@Override
	public int generateByteN()
	{
		int n = byteN();
		n += cons.generateByteN() - cons.byteN();
		if (head != null)
			n += head.generateByteN() - head.byteN();
		if (fields != null)
			n += fields.generateByteN() - fields.byteN();
		if (procs != null)
			n += procs.generateByteN() - procs.byteN();
		if (annos != null)
			n += annos.generateByteN() - annos.byteN();
		if (annoHides != null)
			n += annoHides.generateByteN() - annoHides.byteN();
		return n;
	}

	@Override
	public int generateTo(byte[] bs, int begin)
	{
		System.arraycopy(bytes, 0, bs, begin, 8);
		int bi = cons.end1Bi;
		begin = cons.generateTo(bs, begin + 8);

		bi = head.end1Bi;
		begin = head.generateTo(bs, begin);

		if (fields == null)
		{
			System.arraycopy(bytes, bi, bs, begin, end1Bi - bi);
			return begin + end1Bi - bi;
		}
		bi = fields.end1Bi;
		begin = fields.generateTo(bs, begin);

		if (procs == null)
		{
			System.arraycopy(bytes, bi, bs, begin, end1Bi - bi);
			return begin + end1Bi - bi;
		}
		bi = procs.end1Bi;
		begin = procs.generateTo(bs, begin);

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
				begin = annos.generateTo(bs, begin);
			else if (bi == annoHidesBi && annoHides != null)
				begin = annoHides.generateTo(bs, begin);
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
