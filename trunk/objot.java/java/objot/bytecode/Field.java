package objot.bytecode;

import java.io.PrintStream;


public class Field
	extends Element
{
	/** No constant value, signature and attribute. */
	public static final Field EMPTY = new Field(Bytecode.EMPTY.cons, new byte[] //
		{ 0, 0, // modifier
			0, 0, // nameCi
			0, 0, // descCi
			0, 0, // attrN
		}, 0);

	protected Constants cons;
	protected int modifier;
	protected Visible visible;
	protected int nameCi;
	protected int descCi;
	protected int attrN;
	protected int attrBi;
	protected int signatureBi;
	protected int signatureCi;
	protected int constantBi;
	protected int constantCi;
	protected int annosBi;
	protected Annotations annos;
	protected int annoHidesBi;
	protected Annotations annoHides;

	protected static int readEnd1Bi(byte[] bs, int begin)
	{
		begin += 8;
		for (int an = readU2(bs, begin - 2); an > 0; an--)
			begin += 6 + readU4(bs, begin + 2);
		return begin;
	}

	public Field(Constants c, byte[] bs, int beginBi_)
	{
		super(bs, beginBi_, true);
		cons = c;
		modifier = read0u2(beginBi);
		visible = Visible.get(modifier);
		nameCi = read0u2(beginBi + 2);
		descCi = read0u2(beginBi + 4);
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
			else if (constantBi <= 0 && cons.equalsUtf(name, Bytecode.CONSTANTVALUE))
			{
				constantBi = bi;
				constantCi = read0u2(bi + 6);
			}
			else if (annosBi <= 0 && cons.equalsUtf(name, Bytecode.ANNOS))
				annosBi = bi;
			else if (annoHidesBi <= 0 && cons.equalsUtf(name, Bytecode.ANNOHIDES))
				annoHidesBi = bi;
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
	protected void printContents(PrintStream out, String indent1st, String indent,
		int verbose, boolean hash)
	{
		out.println();
		getCons().printIdentityLn(out, indent, hash);
		out.print(indent);
		out.print("modifier 0x");
		out.print(Integer.toHexString(modifier));
		out.print(" visible ");
		out.println(getVisible());
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
		out.print(" constantCi ");
		out.print(getConstantCi());
		if (getConstantCi() != 0 && verbose > 0)
			getCons().printConColon(getConstantCi(), out);
		out.println();
		if (getAnnos() != null)
			getAnnos().printTo(out, indent, indent, verbose, hash);
		if (getAnnoHides() != null)
			getAnnoHides().printTo(out, indent, indent, verbose, hash);
	}

	public void setModifier(int v)
	{
		modifier = v;
		visible = Visible.get(modifier);
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
		if (signatureBi <= 0)
			throw new RuntimeException("no signature attribute found");
		signatureCi = v;
	}

	public void setConstantCi(int v)
	{
		if (constantBi <= 0)
			throw new RuntimeException("no constant attribute found");
		constantCi = v;
	}

	@Override
	public int normalizeByteN()
	{
		int n = byteN();
		if (annos != null)
			n += annos.normalizeByteN() - annos.byteN();
		if (annoHides != null)
			n += annoHides.normalizeByteN() - annoHides.byteN();
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
