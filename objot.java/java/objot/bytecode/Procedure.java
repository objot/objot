package objot.bytecode;

import java.io.PrintStream;

import objot.util.Bytes;
import objot.util.Mod2;


public class Procedure
	extends Element
{
	/** No signature and attribute except exceptions and code. */
	public static final Procedure EMPTY;

	public static final Bytes CTOR_NAMEUTF = Element.utf("<init>");
	public static final Bytes CINIT_NAMEUTF = Element.utf("<cinit>");
	public static final Bytes VOID_DESC = Element.utf("()V");

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

	public final Constants cons;
	protected int modifier;
	protected int nameCi;
	protected int descCi;
	protected int paramN;
	protected int paramLocalN;
	protected char returnType;
	protected int attrN;
	protected int attrBi;
	protected int signatureBi;
	protected int signatureCi;
	protected int annosBi;
	protected Annotations annos;
	protected int annoHidesBi;
	protected Annotations annoHides;
	protected int annoParamsBi;
	protected AnnoParams annoParams;
	protected int annoHideParamsBi;
	protected AnnoParams annoHideParams;
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
		super(bs, beginBi_);
		cons = c;
		modifier = read0u2(beginBi);
		setNameCi(read0u2(beginBi + 2));
		descCi = read0u2(beginBi + 4);
		paramN = -1;
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
			else if (annoParamsBi <= 0 && cons.equalsUtf(name, Bytecode.ANNO_PARAMS))
				annoParamsBi = bi;
			else if (annoHideParamsBi <= 0 && cons.equalsUtf(name, Bytecode.ANNOHIDE_PARAMS))
				annoHideParamsBi = bi;
			else if (exceptionsBi <= 0 && cons.equalsUtf(name, Bytecode.EXCEPTIONS))
				exceptionsBi = bi;
			else if (codeBi <= 0 && cons.equalsUtf(name, Bytecode.CODE))
				codeBi = bi;
			bi += 6 + read0u4(bi + 2);
		}
		end1Bi = bi;
	}

	public int getModifier()
	{
		return modifier;
	}

	/** @return whether a sythetic or bridge procedure except ctor and cinit. */
	public boolean isSpecial()
	{
		return (modifier & (Mod2.SYNTHETIC | Mod2.BRIDGE)) != 0
			&& (modifier & Mod2.P.INITER) == 0;
	}

	public int getNameCi()
	{
		return nameCi;
	}

	public int getDescCi()
	{
		return descCi;
	}

	public int getParamN()
	{
		if (paramN < 0)
		{
			Bytes desc = cons.readUtf(descCi);
			paramN = getParamN(desc);
			paramLocalN = getParamLocalN(desc);
			returnType = getReturnTypeChar(desc);
		}
		return paramN;
	}

	public int getParamLocalN()
	{
		getParamN();
		return paramLocalN;
	}

	public char getReturnTypeChar()
	{
		getParamN();
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
		if (annoParams == null && annoParamsBi > 0)
		{
			annoParams = new AnnoParams(cons, bytes, annoParamsBi, false);
			if (annoParams.getParamN() != getParamN())
				throw new ClassFormatError("inconsistant parameterN in parameter-annotations");
		}
		return annoParams;
	}

	public AnnoParams getAnnoHideParams()
	{
		if (annoHideParams == null && annoHideParamsBi > 0)
		{
			annoHideParams = new AnnoParams(cons, bytes, annoHideParamsBi, true);
			if (annoHideParams.getParamN() != getParamN())
				throw new ClassFormatError("inconsistant parameterN in parameter-annotations");
		}
		return annoHideParams;
	}

	public Exceptions getExceptions()
	{
		if (exceptions == null && exceptionsBi > 0)
			exceptions = new Exceptions(bytes, exceptionsBi);
		return exceptions;
	}

	public Code getCode()
	{
		if (code == null && codeBi > 0)
			code = new Code(cons, bytes, codeBi);
		return code;
	}

	@Override
	protected void printContents(PrintStream out, int indent1st, int indent, int verbose,
		boolean hash)
	{
		out.println();
		cons.printIdentityLn(out, indent, hash);
		printIndent(out, indent);
		out.print("modifier 0x");
		out.print(Integer.toHexString(modifier));
		out.print(' ');
		out.println(Mod2.toString(modifier));
		printIndent(out, indent);
		out.print("nameCi ");
		out.print(getNameCi());
		cons.printUtf(out, getNameCi(), verbose);
		out.print(" descCi ");
		out.print(getDescCi());
		cons.printUtf(out, getDescCi(), verbose);
		out.println();
		printIndent(out, indent);
		out.print("attrN ");
		out.println(getAttrN());
		printIndent(out, indent);
		out.print("signatureCi ");
		out.print(getSignatureCi());
		cons.printUtf(out, getSignatureCi(), verbose);
		out.println();
		if (getAnnos() != null)
			getAnnos().printTo(out, indent, indent, verbose, hash);
		if (getAnnoHides() != null)
			getAnnoHides().printTo(out, indent, indent, verbose, hash);
		if (getAnnoParams() != null)
			getAnnoParams().printTo(out, indent, indent, verbose, hash);
		if (getAnnoHideParams() != null)
			getAnnoHideParams().printTo(out, indent, indent, verbose, hash);
		if (getExceptions() != null)
			getExceptions().printTo(out, indent, indent, verbose, hash);
		if (getCode() != null)
			getCode().printTo(out, indent, indent, verbose, hash);
	}

	/** @return the annotation found, null for not found. */
	public Annotation searchAnnoParam(Class<? extends java.lang.annotation.Annotation> anno)
	{
		long i = AnnoParams.searchAnno(getAnnoParams(), anno);
		if (i >= 0)
			return getAnnoParams().getAnno((int)(i >>> 32), (int)i);
		i = AnnoParams.searchAnno(getAnnoHideParams(), anno);
		if (i >= 0)
			return getAnnoHideParams().getAnno((int)(i >>> 32), (int)i);
		return null;
	}

	/** @return the annotated annotation found, null for not found. */
	public Annotation searchAnnoAnnoParam(ClassLoader cl,
		Class<? extends java.lang.annotation.Annotation> anno) throws ClassNotFoundException
	{
		long i = AnnoParams.searchAnnoAnno(cl, getAnnoParams(), anno);
		if (i >= 0)
			return getAnnoParams().getAnno((int)(i >>> 32), (int)i);
		i = AnnoParams.searchAnnoAnno(cl, getAnnoHideParams(), anno);
		if (i >= 0)
			return getAnnoHideParams().getAnno((int)(i >>> 32), (int)i);
		return null;
	}

	public static int getParamN(Bytes descUtf)
	{
		if (descUtf.readS1(0) != '(')
			throw new ClassFormatError("invalid descriptor " + Element.unicode(descUtf));
		for (int i = descUtf.beginBi + 1, n = 0; i < descUtf.end1Bi; //
		i += typeDescByteN(descUtf, i - descUtf.beginBi), n++)
			if (descUtf.bytes[i] == ')')
				return n;
		throw new ClassFormatError("invalid descriptor " + Element.unicode(descUtf));
	}

	public static int getParamLocalN(Bytes descUtf)
	{
		if (descUtf.readS1(0) != '(')
			throw new ClassFormatError("invalid descriptor " + Element.unicode(descUtf));
		for (int i = descUtf.beginBi + 1, n = 0; i < descUtf.end1Bi; //
		i += typeDescByteN(descUtf, i - descUtf.beginBi), n++)
		{
			if (descUtf.bytes[i] == ')')
				return n;
			n += Opcode.getLocalStackN((char)descUtf.bytes[i]);
		}
		throw new ClassFormatError("invalid descriptor " + Element.unicode(descUtf));
	}

	/** @return char of return type */
	public static char getReturnTypeChar(Bytes descUtf)
	{
		for (int i = descUtf.end1Bi - 1; i >= descUtf.beginBi; i--)
			if (descUtf.bytes[i] == ')')
				switch (descUtf.bytes[i + 1])
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
					return (char)descUtf.bytes[i + 1];
				default:
					throw new ClassFormatError("invalid return type "
						+ (char)descUtf.bytes[i + 1]);
				}
		throw new ClassFormatError("invalid descriptor " + Element.unicode(descUtf));
	}

	public void setModifier(int v)
	{
		modifier = v;
		setNameCi(nameCi);
	}

	/** may change {@link #modifier} */
	public void setNameCi(int v)
	{
		nameCi = v;
		modifier = Mod2.get(modifier, cons.read0s1(cons.readUtfBegin(nameCi)));
	}

	public void setDescCi(int v)
	{
		descCi = v;
		paramN = -1;
	}

	public void setSignatureCi(int v)
	{
		if (signatureBi <= 0)
			throw new RuntimeException("no signature attribute found");
		signatureCi = v;
	}

	@Override
	public int generateByteN()
	{
		int n = byteN();
		if (annos != null)
			n += annos.generateByteN() - annos.byteN();
		if (annoHides != null)
			n += annoHides.generateByteN() - annoHides.byteN();
		if (annoParams != null)
			n += annoParams.generateByteN() - annoParams.byteN();
		if (annoHideParams != null)
			n += annoHideParams.generateByteN() - annoHideParams.byteN();
		if (exceptions != null)
			n += exceptions.generateByteN() - exceptions.byteN();
		if (code != null)
			n += code.generateByteN() - code.byteN();
		return n;
	}

	@Override
	public int generateTo(byte[] bs, int begin)
	{
		writeU2(bs, begin, modifier & 0xFFFF);
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
				begin = annos.generateTo(bs, begin);
			else if (bi == annoHidesBi && annoHides != null)
				begin = annoHides.generateTo(bs, begin);
			else if (bi == annoParamsBi && annoParams != null)
				begin = annoParams.generateTo(bs, begin);
			else if (bi == annoHideParamsBi && annoHideParams != null)
				begin = annoHideParams.generateTo(bs, begin);
			else if (bi == exceptionsBi && exceptions != null)
				begin = exceptions.generateTo(bs, begin);
			else if (bi == codeBi && code != null)
				begin = code.generateTo(bs, begin);
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
