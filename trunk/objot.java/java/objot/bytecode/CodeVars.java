package objot.bytecode;

import java.io.PrintStream;

import objot.util.Array2;
import objot.util.InvalidValueException;


public class CodeVars
	extends Element
{
	protected boolean signature;
	protected int varN;
	protected int[] beginAds;
	protected int[] adNs;
	protected int[] nameCis;
	protected int[] descCis;
	protected int[] locals;

	public CodeVars(byte[] bs, int beginBi_, boolean signature_)
	{
		super(bs, beginBi_);
		signature = signature_;
		varN = read0u2(beginBi + 6);
		end1Bi = beginBi + 8 + varN * 10;
		if (end1Bi - beginBi - 6 != read0u4(beginBi + 2))
			throw new ClassFormatError("inconsistent attribute length");
	}

	public boolean isSignature()
	{
		return signature;
	}

	public int getVarN()
	{
		return varN;
	}

	protected void reads()
	{
		if (beginAds != null)
			return;
		beginAds = new int[allocN(varN)];
		adNs = new int[allocN(varN)];
		nameCis = new int[allocN(varN)];
		descCis = new int[allocN(varN)];
		locals = new int[allocN(varN)];
		int bi = beginBi + 8;
		for (int i = 0; i < varN; i++)
		{
			beginAds[i] = read0u2(bi);
			adNs[i] = read0u2(bi + 2);
			nameCis[i] = read0u2(bi + 4);
			descCis[i] = read0u2(bi + 6);
			locals[i] = read0u2(bi + 8);
			bi += 10;
		}
	}

	protected void checkIndex(int vi)
	{
		if (vi < 0 || vi >= varN)
			throw new InvalidValueException(vi);
	}

	public int getBeginAd(int vi)
	{
		checkIndex(vi);
		reads();
		return beginAds[vi];
	}

	public int getAdN(int vi)
	{
		checkIndex(vi);
		reads();
		return adNs[vi];
	}

	public int getEnd1Ad(int vi)
	{
		checkIndex(vi);
		reads();
		return beginAds[vi] + adNs[vi];
	}

	public int getNameCi(int vi)
	{
		checkIndex(vi);
		reads();
		return nameCis[vi];
	}

	public int getDescCi(int vi)
	{
		checkIndex(vi);
		reads();
		return descCis[vi];
	}

	public int getLocal(int vi)
	{
		checkIndex(vi);
		reads();
		return locals[vi];
	}

	@Override
	protected void printContents(PrintStream out, int indent1st, int indent, int verbose,
		boolean hash)
	{
		out.println();
		for (int i = 0; i < getVarN(); i++)
		{
			printIndent(out, indent);
			out.print(i);
			out.print(". beginAd ");
			out.print(getBeginAd(i));
			out.print(" adN ");
			out.print(getAdN(i));
			out.print(" end1Ad ");
			out.print(getEnd1Ad(i));
			out.print(" nameCi ");
			out.print(getNameCi(i));
			out.print(" descCi ");
			out.print(getDescCi(i));
			out.print(" local ");
			out.println(getLocal(i));
		}
	}

	public void ensureVarN(int n)
	{
		reads();
		beginAds = Array2.ensureN(beginAds, n);
		adNs = Array2.ensureN(adNs, n);
		nameCis = Array2.ensureN(nameCis, n);
		descCis = Array2.ensureN(descCis, n);
		locals = Array2.ensureN(locals, n);
	}

	public int appendInfo(int beginAd, int adN, int nameCi, int descCi, int local)
	{
		reads();
		ensureVarN(varN + 1);
		beginAds[varN] = beginAd;
		adNs[varN] = adN;
		nameCis[varN] = nameCi;
		descCis[varN] = descCi;
		locals[varN] = local;
		return varN++;
	}

	public int putInfo(int beginAd, int adN, int nameCi, int descCi, int local)
	{
		reads();
		for (int i = 0; i < varN; i++)
			if (beginAds[i] == beginAd && adNs[i] == adN && nameCis[i] == nameCi
				&& descCis[i] == descCi && locals[i] == local)
				return i;
		return appendInfo(beginAd, adN, nameCi, descCi, local);
	}

	public void setInfo(int vi, int beginAd, int adN, int nameCi, int descCi, int local)
	{
		checkIndex(vi);
		reads();
		beginAds[vi] = beginAd;
		adNs[vi] = adN;
		nameCis[vi] = nameCi;
		descCis[vi] = descCi;
		locals[vi] = local;
	}

	@Override
	public int generateByteN()
	{
		return 8 + varN * 10;
	}

	@Override
	public int generateTo(byte[] bs, int begin)
	{
		writeU2(bs, begin, read0u2(beginBi));
		writeS4(bs, begin + 2, generateByteN() - 6);
		writeU2(bs, begin + 6, varN);
		if (beginAds == null)
		{
			System.arraycopy(bytes, beginBi + 8, bs, begin + 8, varN * 10);
			return begin + generateByteN();
		}
		begin += 8;
		for (int i = 0; i < varN; i++, begin += 10)
		{
			writeU2(bs, begin, beginAds[i]);
			writeU2(bs, begin + 2, adNs[i]);
			writeU2(bs, begin + 4, nameCis[i]);
			writeU2(bs, begin + 6, descCis[i]);
			writeU2(bs, begin + 8, locals[i]);
		}
		return begin;
	}
}
