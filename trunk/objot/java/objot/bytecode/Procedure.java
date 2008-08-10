//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.bytecode;

import java.io.PrintStream;

import objot.util.Array2;
import objot.util.Bytes;
import objot.util.Class2;
import objot.util.Mod2;


public final class Procedure
	extends Element
{
	public static final String CTOR_NAME = "<init>";
	public static final String CINIT_NAME = "<cinit>";
	public static final String VOID_DESC = "()V";
	static final Bytes CTOR_NAME_ = utf(CTOR_NAME);
	static final Bytes CINIT_NAME_ = utf(CINIT_NAME);
	static final Bytes VOID_DESC_ = utf(VOID_DESC);

	public final Constants cons;
	int modifier;
	int nameCi;
	int descCi;
	int paramN;
	int paramLocalN;
	char returnType;
	int attrN;
	int attrBi;
	int signatureBi;
	int signatureCi;
	int annosBi;
	Annotations annos;
	int annoHidesBi;
	Annotations annoHides;
	int annoParamsBi;
	AnnoParams annoParams;
	int annoHideParamsBi;
	AnnoParams annoHideParams;
	int exceptionsBi;
	Exceptions exceptions;
	int codeBi;
	Code code;
	int codeByteN0;

	static int readEnd1Bi(byte[] bs, int begin)
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
			if (signatureBi == 0 && cons.equalsUtf(name, Bytecode.SIGNATURE))
			{
				signatureBi = bi;
				signatureCi = read0u2(bi + 6);
			}
			else if (annosBi == 0 && cons.equalsUtf(name, Bytecode.ANNOS))
				annosBi = bi;
			else if (annoHidesBi == 0 && cons.equalsUtf(name, Bytecode.ANNOHIDES))
				annoHidesBi = bi;
			else if (annoParamsBi == 0 && cons.equalsUtf(name, Bytecode.ANNO_PARAMS))
				annoParamsBi = bi;
			else if (annoHideParamsBi == 0 && cons.equalsUtf(name, Bytecode.ANNOHIDE_PARAMS))
				annoHideParamsBi = bi;
			else if (exceptionsBi == 0 && cons.equalsUtf(name, Bytecode.EXCEPTIONS))
				exceptionsBi = bi;
			else if (codeBi == 0 && cons.equalsUtf(name, Bytecode.CODE))
				codeBi = bi;
			bi += 6 + read0u4(bi + 2);
		}
		end1Bi = bi;
	}

	/** empty procedure, with exceptions and code, without signature and attribute */
	public Procedure(Constants c)
	{
		super(null, 0);
		cons = c;
		ensureByteN(60);
		write0u2(0, 0); // modifier
		write0u2(2, 0); // nameCi
		write0u2(4, 0); // descCi
		paramN = -1;
		attrN = 4;
		write0u2(6, attrN);
		attrBi = 8;
		int i = attrBi;
		exceptionsBi = i;
		write0u2(i, cons.putUtf(Bytecode.EXCEPTIONS)); // attr name ci
		write0u4(i + 2, 2); // attr length
		i += 6;
		write0u2(i, 0); // exceptionN
		i += 2;
		annosBi = i;
		write0u2(i, cons.putUtf(Bytecode.ANNOS)); // attr name ci
		write0u4(i + 2, 2); // attr length
		i += 6;
		write0u2(i, 0); // annoN
		i += 2;
		annoHidesBi = i;
		write0u2(i, cons.putUtf(Bytecode.ANNOHIDES)); // attr name ci
		write0u4(i + 2, 2); // attr length
		i += 6;
		write0u2(i, 0); // annoHideN
		i += 2;
		codeBi = i;
		write0u2(i, cons.putUtf(Bytecode.CODE)); // attr name ci
		write0u4(i + 2, 12); // attr length
		i += 6;
		write0u2(i, 0); // stackN
		write0u2(i + 2, 0); // localN
		write0u4(i + 4, 0); // addrN and ins
		write0u2(i + 8, 0); // catchN
		write0u2(i + 10, 0); // attrN
		i += 12;
		end1Bi = i;
	}

	/** <code>&lt;init&gt;() { super(); }</code> */
	public static Procedure addCtor0(Constants c, int superCi, int modifier)
	{
		return addCtor(c, superCi, modifier, (Object[])null);
	}

	/** <code>&lt;init&gt;() { super(); }</code> */
	public static Procedure putCtor0(Constants c, int superCi, int modifier)
	{
		return putCtor(c, superCi, modifier, (Object[])null);
	}

	/** <code>&lt;init&gt;(a, b, ...) { super(a, b, ...); }</code> */
	public static Procedure addCtor(Constants c, int superCi, int modifier, Object... params)
	{
		if (params == null)
			params = Array2.CLASSES0;
		Procedure p = new Procedure(c);
		int nameCi = c.addUtf(CTOR_NAME_);
		int descCi = params.length == 0 ? c.addUtf(VOID_DESC_) : c.addUcs(Class2.descript(
			params, void.class));
		p.setModifier(modifier);
		p.setNameCi(nameCi);
		p.setDescCi(descCi);
		Instruction s = new Instruction(c, 5 + params.length * 2);
		s.ins0(Opcode.ALOAD0);
		int local = 1;
		for (Object a: params)
		{
			char d = a instanceof Class ? Class2.descriptChar((Class<?>)a)
				: ((String)a).charAt(0);
			s.insU1(Opcode.getLoadOp(d), local);
			local += Opcode.getLocalStackN(d);
		}
		s.insU2(Opcode.INVOKESPECIAL, c.addCproc(superCi, c.addNameDesc(nameCi, descCi)));
		s.ins0(Opcode.RETURN);
		p.getCode().setLocalN(local);
		p.getCode().setStackN(local);
		p.getCode().setIns(s, false);
		return p;
	}

	/** <code>&lt;init&gt;(a, b, ...) { super(a, b, ...); }</code> */
	public static Procedure putCtor(Constants c, int superCi, int modifier, Object... params)
	{
		if (params == null)
			params = Array2.CLASSES0;
		Procedure p = new Procedure(c);
		int nameCi = c.putUtf(CTOR_NAME_);
		int descCi = params.length == 0 ? c.putUtf(VOID_DESC_) : c.putUcs(Class2.descript(
			params, void.class));
		p.setModifier(modifier);
		p.setNameCi(nameCi);
		p.setDescCi(descCi);
		Instruction s = new Instruction(c, 5 + params.length * 2);
		s.ins0(Opcode.ALOAD0);
		int local = 1;
		for (Object a: params)
		{
			char d = a instanceof Class ? Class2.descriptChar((Class<?>)a) //
				: ((String)a).charAt(0);
			s.insU1(Opcode.getLoadOp(d), local);
			local += Opcode.getLocalStackN(d);
		}
		s.insU2(Opcode.INVOKESPECIAL, c.putCproc(superCi, c.putNameDesc(nameCi, descCi)));
		s.ins0(Opcode.RETURN);
		p.getCode().setLocalN(local);
		p.getCode().setStackN(local);
		p.getCode().setIns(s, false);
		return p;
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
			Bytes desc = cons.getUtf(descCi);
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
			exceptions = new Exceptions(cons, bytes, exceptionsBi);
		return exceptions;
	}

	public Code getCode()
	{
		if (code == null && codeBi > 0)
		{
			code = new Code(cons, bytes, codeBi);
			codeByteN0 = code.byteN0();
		}
		return code;
	}

	@Override
	void printContents(PrintStream out, int indent1st, int indent, int verbose)
	{
		out.println();
		printIndent(out, indent);
		out.print("modifier 0x");
		out.print(Integer.toHexString(modifier));
		out.print(' ');
		out.println(Mod2.toString(modifier));
		printIndent(out, indent);
		out.print("name ");
		cons.print(out, getNameCi(), verbose);
		out.print(" desc ");
		cons.print(out, getDescCi(), verbose);
		out.println();
		printIndent(out, indent);
		out.print("attrN ");
		out.println(attrN);
		if (getSignatureCi() > 0)
		{
			printIndent(out, indent);
			out.print("signature ");
			cons.print(out, getSignatureCi(), verbose).println();
		}
		if (getAnnos() != null)
			getAnnos().printTo(out, indent, indent, verbose);
		if (getAnnoHides() != null)
			getAnnoHides().printTo(out, indent, indent, verbose);
		if (getAnnoParams() != null)
			getAnnoParams().printTo(out, indent, indent, verbose);
		if (getAnnoHideParams() != null)
			getAnnoHideParams().printTo(out, indent, indent, verbose);
		if (getExceptions() != null)
			getExceptions().printTo(out, indent, indent, verbose);
		if (getCode() != null)
			getCode().printTo(out, indent, indent, verbose);
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
			throw new ClassFormatError("invalid descriptor " + Element.ucs(descUtf));
		for (int i = descUtf.beginBi + 1, n = 0; i < descUtf.end1Bi; //
		i += typeDescByteN(descUtf, i - descUtf.beginBi), n++)
			if (descUtf.bytes[i] == ')')
				return n;
		throw new ClassFormatError("invalid descriptor " + Element.ucs(descUtf));
	}

	public static int getParamLocalN(Bytes descUtf)
	{
		if (descUtf.readS1(0) != '(')
			throw new ClassFormatError("invalid descriptor " + Element.ucs(descUtf));
		for (int i = descUtf.beginBi + 1, n = 0; i < descUtf.end1Bi; //
		i += typeDescByteN(descUtf, i - descUtf.beginBi), n++)
		{
			if (descUtf.bytes[i] == ')')
				return n;
			n += Opcode.getLocalStackN((char)descUtf.bytes[i]);
		}
		throw new ClassFormatError("invalid descriptor " + Element.ucs(descUtf));
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
		throw new ClassFormatError("invalid descriptor " + Element.ucs(descUtf));
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
		modifier = Mod2.get(modifier, v > 0 ? cons.read0s1(cons.getUtfBegin(v)) : '\0');
	}

	public void setDescCi(int v)
	{
		descCi = v;
		paramN = -1;
	}

	public void setSignatureCi(int v)
	{
		if (signatureBi == 0)
			throw new RuntimeException("no signature attribute found");
		signatureCi = v;
	}

	public void setCode(Code c)
	{
		if (c.cons != cons)
			throw new IllegalArgumentException("inconsistent constants");
		getCode();
		code = c;
	}

	@Override
	public int normalizeByteN()
	{
		int n = byteN0();
		if (annos != null)
			n += annos.normalizeByteN() - annos.byteN0();
		if (annoHides != null)
			n += annoHides.normalizeByteN() - annoHides.byteN0();
		if (annoParams != null)
			n += annoParams.normalizeByteN() - annoParams.byteN0();
		if (annoHideParams != null)
			n += annoHideParams.normalizeByteN() - annoHideParams.byteN0();
		if (exceptions != null)
			n += exceptions.normalizeByteN() - exceptions.byteN0();
		if (code != null)
			n += code.normalizeByteN() - codeByteN0;
		return n;
	}

	@Override
	public int normalizeTo(byte[] bs, int begin)
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
				begin = annos.normalizeTo(bs, begin);
			else if (bi == annoHidesBi && annoHides != null)
				begin = annoHides.normalizeTo(bs, begin);
			else if (bi == annoParamsBi && annoParams != null)
				begin = annoParams.normalizeTo(bs, begin);
			else if (bi == annoHideParamsBi && annoHideParams != null)
				begin = annoHideParams.normalizeTo(bs, begin);
			else if (bi == exceptionsBi && exceptions != null)
				begin = exceptions.normalizeTo(bs, begin);
			else if (bi != codeBi || code == null)
			{
				System.arraycopy(bytes, bi, bs, begin, bn);
				begin += bn;
			}
			bi += bn;
		}
		if (code != null)
			begin = code.normalizeTo(bs, begin);
		return begin;
	}
}
