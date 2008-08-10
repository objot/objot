//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.bytecode;

import java.io.PrintStream;

import objot.util.Mod2;


public final class Field
	extends Element
{
	public final Constants cons;
	int modifier;
	int nameCi;
	int descCi;
	int attrN;
	int attrBi;
	int signatureBi;
	int signatureCi;
	int constantBi;
	int constantCi;
	int annosBi;
	Annotations annos;
	int annoHidesBi;
	Annotations annoHides;

	static int readEnd1Bi(byte[] bs, int begin)
	{
		begin += 8;
		for (int an = readU2(bs, begin - 2); an > 0; an--)
			begin += 6 + readU4(bs, begin + 2);
		return begin;
	}

	public Field(Constants c, byte[] bs, int beginBi_)
	{
		super(bs, beginBi_);
		cons = c;
		setModifier(read0u2(beginBi));
		nameCi = read0u2(beginBi + 2);
		descCi = read0u2(beginBi + 4);
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
			else if (constantBi == 0 && cons.equalsUtf(name, Bytecode.CONSTANT_VALUE))
			{
				constantBi = bi;
				constantCi = read0u2(bi + 6);
			}
			else if (annosBi == 0 && cons.equalsUtf(name, Bytecode.ANNOS))
				annosBi = bi;
			else if (annoHidesBi == 0 && cons.equalsUtf(name, Bytecode.ANNOHIDES))
				annoHidesBi = bi;
			bi += 6 + read0u4(bi + 2);
		}
		end1Bi = bi;
	}

	/** empty field, without value signature and attribute. */
	public Field(Constants c)
	{
		super(null, 0);
		cons = c;
		ensureByteN(8);
		write0u2(0, 0); // modifier
		write0u2(2, 0); // nameCi
		write0u2(4, 0); // descCi
		write0u2(6, 0); // attrN
		end1Bi = 8;
	}

	public int getModifier()
	{
		return modifier;
	}

	public int getNameCi()
	{
		return nameCi;
	}

	public int getDescCi()
	{
		return descCi;
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

	public int getConstantCi()
	{
		return constantCi;
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
		if (getConstantCi() > 0)
		{
			printIndent(out, indent);
			out.print("constant ");
			cons.print(out, getConstantCi(), verbose).println();
		}
		if (getAnnos() != null)
			getAnnos().printTo(out, indent, indent, verbose);
		if (getAnnoHides() != null)
			getAnnoHides().printTo(out, indent, indent, verbose);
	}

	public void setModifier(int v)
	{
		modifier = Mod2.get(v, 0);
	}

	public void setNameCi(int v)
	{
		nameCi = v;
	}

	public void setDescCi(int v)
	{
		descCi = v;
	}

	public void setSignatureCi(int v)
	{
		if (signatureBi == 0)
			throw new RuntimeException("no signature attribute found");
		signatureCi = v;
	}

	public void setConstantCi(int v)
	{
		if (constantBi == 0)
			throw new RuntimeException("no constant attribute found");
		constantCi = v;
	}

	@Override
	public int normalizeByteN()
	{
		int n = byteN0();
		if (annos != null)
			n += annos.normalizeByteN() - annos.byteN0();
		if (annoHides != null)
			n += annoHides.normalizeByteN() - annoHides.byteN0();
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
			else if (bi == constantBi)
			{
				System.arraycopy(bytes, bi, bs, begin, 6);
				writeU2(bs, begin + 6, constantCi);
				begin += 8;
			}
			else if (bi == annosBi && annos != null)
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
