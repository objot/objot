package objot.bytecode;

import java.io.PrintStream;

import objot.util.Bytes;


public class Procedure
	extends Element
{
	/** No signature and attribute except exceptions and code. */
	public static final Procedure EMPTY;

	public static final Bytes CTOR_NAMEUTF = new Bytes(Element.chars2Utf("<init>"));
	public static final Bytes CINIT_NAMEUTF = new Bytes(Element.chars2Utf("<cinit>"));
	public static final Bytes VOID_DESC = new Bytes(Element.chars2Utf("()V"));

	static
	{
		Constants cons = Bytecode.EMPTY.cons;
		Bytes bs = new Bytes(null).ensureByteN(128);
		bs.write0u2(0, 0); // modifier
		bs.write0u2(2, 0); // nameCi
		bs.write0u2(4, 0); // descCi
		// attrs
		bs.write0u2(6, 2); // attrN
		int i = 8;
		// code
		bs.write0u2(i, cons.searchUtf(Bytecode.CODE)); // attr name ci
		bs.write0u4(i + 2, 12); // attr length
		i += 6;
		bs.write0u2(i, 0); // stackN
		bs.write0u2(i + 2, 0); // localN
		bs.write0u4(i + 4, 0); // addrN and ins
		bs.write0u2(i + 8, 0); // catchN
		bs.write0u2(i + 10, 0); // attrN
		i += 12;
		// exceptions
		bs.write0u2(i, cons.searchUtf(Bytecode.EXCEPTIONS)); // attr name ci
		bs.write0u4(i + 2, 2); // attr length
		i += 6;
		bs.write0u2(i, 0); // exceptionN
		i += 2;
		// end
		EMPTY = new Procedure(cons, bs.bytes, bs.beginBi);
	}

	protected Constants cons;
	protected int modifier;
	protected Visible visible;
	protected ProcKind kind;
	protected int nameCi;
	protected int descCi;
	protected int argN;
	protected int argLocalN;
	protected char returnType;
	protected int attrN;
	protected int attrBi;
	protected int signatureBi;
	protected int signatureCi;
	protected int annosBi;
	protected Annotations annos;
	protected int annoHidesBi;
	protected Annotations annoHides;
	protected int annoArgsBi;
	protected AnnoParams annoParams;
	protected int annoHideArgsBi;
	protected AnnoParams annoHideArgs;
	protected int exceptionsBi;
	protected Exceptions exceptions;
	protected int codeBi;
	protected Code code;

	protected static int readEnd1Bi(byte[] bs, int begin)
	{
		begin += 8;
		for (int an = readU2(bs, begin - 2); an > 0; an--)
			begin += 6 + readU4(bs, begin + 2);
		return begin;
	}

	public Procedure(Constants c, byte[] bs, int beginBi_)
	{
		super(bs, beginBi_, true);
		cons = c;
		modifier = read0u2(beginBi);
		visible = Visible.get(modifier);
		nameCi = read0u2(beginBi + 2);
		descCi = read0u2(beginBi + 4);
		argN = -1;
		argLocalN = -1;
		attrN = read0u2(beginBi + 6);
		attrBi = beginBi + 8;
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
			else if (annoArgsBi <= 0 && cons.equalsUtf(name, Bytecode.ANNOS_ARG))
				annoArgsBi = bi;
			else if (annoHideArgsBi <= 0 && cons.equalsUtf(name, Bytecode.ANNOHIDES_ARG))
				annoHideArgsBi = bi;
			else if (exceptionsBi <= 0 && cons.equalsUtf(name, Bytecode.EXCEPTIONS))
				exceptionsBi = bi;
			else if (codeBi <= 0 && cons.equalsUtf(name, Bytecode.CODE))
				codeBi = bi;
			bi += 6 + read0u4(bi + 2);
		}
		end1Bi = bi;
	}

	public Constants getCons()
	{
		return cons;
	}

	public int getModifier()
	{
		return modifier;
	}

	public Visible getVisible()
	{
		return visible;
	}

	public ProcKind getKind()
	{
		if (kind == null)
			kind = ProcKind.get(modifier, nameCi > 0 ? (char)cons.read0s1(cons
				.readUtfBegin(nameCi)) : ' ');
		return kind;
	}

	/** @return whether a sythetic or bridge procedure except ctor and cinit. */
	public boolean isSpecial()
	{
		return (modifier & 0x1040) != 0 && !getKind().isMatching(ProcKind.INITIAL);
	}

	public int getNameCi()
	{
		return nameCi;
	}

	public int getDescCi()
	{
		return descCi;
	}

	public int getArgN()
	{
		if (argN < 0)
			argN = getArgN(cons.bytes, cons.readUtfBegin(descCi), cons.readUtfEnd1(descCi));
		return argN;
	}

	public int getArgLocalN()
	{
		if (argLocalN < 0)
			argLocalN = getArgLocalN(cons.bytes, cons.readUtfBegin(descCi), cons
				.readUtfEnd1(descCi));
		return argLocalN;
	}

	public char getReturnTypeChar()
	{
		if (returnType == 0)
			returnType = getReturnTypeChar(cons.bytes, cons.readUtfBegin(descCi), cons
				.readUtfEnd1(descCi));
		return returnType;
	}

	public int getAttrN()
	{
		return attrN;
	}

	public int getAttrBi()
	{
		return attrBi;
	}

	public int getSignatureCi()
	{
		return signatureCi;
	}

	@Override
	public Annotations getAnnos()
	{
		if (annos == null && annosBi > 0)
			annos = new Annotations(cons, bytes, annosBi, false);
		return annos;
	}

	@Override
	public Annotations getAnnoHides()
	{
		if (annoHides == null && annoHidesBi > 0)
			annoHides = new Annotations(cons, bytes, annoHidesBi, true);
		return annoHides;
	}

	public AnnoParams getAnnoParams()
	{
		if (annoParams == null && annoArgsBi > 0)
		{
			annoParams = new AnnoParams(cons, bytes, annoArgsBi, false);
			if (annoParams.getParamN() != getArgN())
				throw new ClassFormatError("inconsistant argumentN in argument-annotations");
		}
		return annoParams;
	}

	public AnnoParams getAnnoHideArgs()
	{
		if (annoHideArgs == null && annoHideArgsBi > 0)
		{
			annoHideArgs = new AnnoParams(cons, bytes, annoHideArgsBi, true);
			if (annoHideArgs.getParamN() != getArgN())
				throw new ClassFormatError("inconsistant argumentN in argument-annotations");
		}
		return annoHideArgs;
	}

	public Exceptions getExceptions()
	{
		if (exceptions == null && exceptionsBi > 0)
			exceptions = new Exceptions(bytes, exceptionsBi, forExtension);
		return exceptions;
	}

	public Code getCode()
	{
		if (code == null && codeBi > 0)
			code = new Code(cons, bytes, codeBi, forExtension);
		return code;
	}

	@Override
	protected void printContents(PrintStream out, String indent1st, String indent,
		int verbose, boolean hash)
	{
		out.println();
		getCons().printIdentityLn(out, indent, hash);
		out.print(indent);
		out.print("modifier 0x");
		out.print(Integer.toHexString(modifier));
		out.print(" visible ");
		out.print(getVisible());
		out.print(" kind ");
		out.println(getKind());
		out.print(indent);
		out.print("nameCi ");
		out.print(getNameCi());
		getCons().printUtfChars(out, getNameCi(), verbose);
		out.print(" descCi ");
		out.print(getDescCi());
		getCons().printUtfChars(out, getDescCi(), verbose);
		out.println();
		out.print(indent);
		out.print("attrN ");
		out.println(getAttrN());
		out.print(indent);
		out.print("signatureCi ");
		out.print(getSignatureCi());
		getCons().printUtfChars(out, getSignatureCi(), verbose);
		out.println();
		if (getAnnos() != null)
			getAnnos().printTo(out, indent, indent, verbose, hash);
		if (getAnnoHides() != null)
			getAnnoHides().printTo(out, indent, indent, verbose, hash);
		if (getAnnoParams() != null)
			getAnnoParams().printTo(out, indent, indent, verbose, hash);
		if (getAnnoHideArgs() != null)
			getAnnoHideArgs().printTo(out, indent, indent, verbose, hash);
		if (getExceptions() != null)
			getExceptions().printTo(out, indent, indent, verbose, hash);
		if (getCode() != null)
			getCode().printTo(out, indent, indent, verbose, hash);
	}

	/** @return the annotation found, null for not found. */
	public Annotation searchAnnoArg(Class<? extends java.lang.annotation.Annotation> anno)
	{
		long i = AnnoParams.searchAnno(cons, getAnnoParams(), anno);
		if (i >= 0)
			return getAnnoParams().getAnno((int)(i >>> 32), (int)i);
		i = AnnoParams.searchAnno(cons, getAnnoHideArgs(), anno);
		if (i >= 0)
			return getAnnoHideArgs().getAnno((int)(i >>> 32), (int)i);
		return null;
	}

	/** @return the annotated annotation found, null for not found. */
	public Annotation searchAnnoAnnoArg(ClassLoader cl,
		Class<? extends java.lang.annotation.Annotation> anno) throws ClassNotFoundException
	{
		long i = AnnoParams.searchAnnoAnno(cl, cons, getAnnoParams(), anno);
		if (i >= 0)
			return getAnnoParams().getAnno((int)(i >>> 32), (int)i);
		i = AnnoParams.searchAnnoAnno(cl, cons, getAnnoHideArgs(), anno);
		if (i >= 0)
			return getAnnoHideArgs().getAnno((int)(i >>> 32), (int)i);
		return null;
	}

	public static int getArgN(byte[] descUtf, int begin, int end1)
	{
		if (descUtf[begin] != '(')
			throw new ClassFormatError("invalid procedure descriptor "
				+ Element.utf2chars(descUtf, begin, end1));
		begin++;
		for (int n = 0; begin < end1; n++, begin += typeDescByteN(descUtf, begin, end1))
			if (descUtf[begin] == ')')
				return n;
		throw new ClassFormatError("invalid procedure descriptor "
			+ Element.utf2chars(descUtf, begin, end1));
	}

	public static int getArgLocalN(byte[] descUtf, int begin, int end1)
	{
		if (descUtf[begin] != '(')
			throw new ClassFormatError("invalid procedure descriptor "
				+ Element.utf2chars(descUtf, begin, end1));
		begin++;
		for (int n = 0; begin < end1; begin += typeDescByteN(descUtf, begin, end1))
		{
			if (descUtf[begin] == ')')
				return n;
			n += Opcode.getLocalStackN((char)descUtf[begin]);
		}
		throw new ClassFormatError("invalid procedure descriptor "
			+ Element.utf2chars(descUtf, begin, end1));
	}

	/** @return char of return type */
	public static char getReturnTypeChar(byte[] descUtf)
	{
		return getReturnTypeChar(descUtf, 0, descUtf.length);
	}

	/** @return char of return type */
	public static char getReturnTypeChar(byte[] descUtf, int begin, int end1)
	{
		for (int bi = end1 - 1; bi >= begin; bi--)
			if (descUtf[bi] == ')')
				switch (descUtf[bi + 1])
				{
				case 'L':
				case '[':
				case 'V':
				case 'Z':
				case 'B':
				case 'C':
				case 'S':
				case 'I':
				case 'J':
				case 'F':
				case 'D':
					return (char)descUtf[bi + 1];
				default:
					throw new ClassFormatError("invalid procedure return type "
						+ (char)descUtf[bi + 1]);
				}
		throw new ClassFormatError("invalid procedure descriptor "
			+ Element.utf2chars(descUtf, begin, end1));
	}

	public void setModifier(int v)
	{
		modifier = v;
		visible = Visible.get(modifier);
		kind = null;
	}

	public void setNameCi(int v)
	{
		nameCi = v;
		kind = null;
	}

	public void setDescCi(int v)
	{
		descCi = v;
		argN = -1;
		argLocalN = -1;
		returnType = 0;
	}

	public void setSignatureCi(int v)
	{
		if (signatureBi <= 0)
			throw new RuntimeException("no signature attribute found");
		signatureCi = v;
	}

	@Override
	public int normalizeByteN()
	{
		int n = byteN();
		if (annos != null)
			n += annos.normalizeByteN() - annos.byteN();
		if (annoHides != null)
			n += annoHides.normalizeByteN() - annoHides.byteN();
		if (annoParams != null)
			n += annoParams.normalizeByteN() - annoParams.byteN();
		if (annoHideArgs != null)
			n += annoHideArgs.normalizeByteN() - annoHideArgs.byteN();
		if (exceptions != null)
			n += exceptions.normalizeByteN() - exceptions.byteN();
		if (code != null)
			n += code.normalizeByteN() - code.byteN();
		return n;
	}

	@Override
	public int normalizeTo(byte[] bs, int begin)
	{
		writeU2(bs, begin, modifier);
		writeU2(bs, begin + 2, nameCi);
		writeU2(bs, begin + 4, descCi);
		writeU2(bs, begin + 6, attrN);
		int bi = attrBi;
		begin += 8;
		for (int an = attrN; an > 0; an--)
		{
			int bn = 6 + read0u4(bi + 2);
			if (bi == signatureBi)
			{
				System.arraycopy(bytes, bi, bs, begin, 6);
				writeU2(bs, begin + 6, signatureCi);
				begin += 8;
			}
			else if (bi == annosBi && annos != null)
				begin = annos.normalizeTo(bs, begin);
			else if (bi == annoHidesBi && annoHides != null)
				begin = annoHides.normalizeTo(bs, begin);
			else if (bi == annoArgsBi && annoParams != null)
				begin = annoParams.normalizeTo(bs, begin);
			else if (bi == annoHideArgsBi && annoHideArgs != null)
				begin = annoHideArgs.normalizeTo(bs, begin);
			else if (bi == exceptionsBi && exceptions != null)
				begin = exceptions.normalizeTo(bs, begin);
			else if (bi == codeBi && code != null)
				begin = code.normalizeTo(bs, begin);
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
