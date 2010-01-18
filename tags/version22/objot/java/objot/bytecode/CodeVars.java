//
// Copyright 2007-2010 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.bytecode;

import java.io.PrintStream;

import objot.util.Array2;
import objot.util.InvalidValueException;


public final class CodeVars
	extends Element
{
	public final Constants cons;
	boolean signature;
	int varN;
	int[] beginAds;
	int[] adNs;
	int[] nameCis;
	int[] descCis;
	int[] locals;

	public CodeVars(Constants c, byte[] bs, int beginBi_, boolean signature_)
	{
		super(bs, beginBi_);
		cons = c;
		signature = signature_;
		varN = readU2(bytes, beginBi + 6);
		end1Bi = beginBi + 8 + varN * 10;
		if (end1Bi - beginBi - 6 != readU4(bytes, beginBi + 2))
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

	void reads()
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
			beginAds[i] = readU2(bytes, bi);
			adNs[i] = readU2(bytes, bi + 2);
			nameCis[i] = readU2(bytes, bi + 4);
			descCis[i] = readU2(bytes, bi + 6);
			locals[i] = readU2(bytes, bi + 8);
			bi += 10;
		}
	}

	void checkIndex(int vi)
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
	void printContents(PrintStream out, int indent1st, int indent, int verbose)
	{
		out.println();
		for (int i = 0; i < varN; i++)
		{
			printIndent(out, indent);
			out.print(i);
			out.print(". beginAd ");
			out.print(getBeginAd(i));
			out.print(" adN ");
			out.print(getAdN(i));
			out.print(" end1Ad ");
			out.print(getEnd1Ad(i));
			out.print(" local ");
			out.print(getLocal(i));
			out.print(" name ");
			cons.print(out, getNameCi(i), verbose);
			out.print(" desc ");
			cons.print(out, getDescCi(i), verbose).println();
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

	public int addInfo(int beginAd, int adN, int nameCi, int descCi, int local)
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
		return addInfo(beginAd, adN, nameCi, descCi, local);
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
	public int normalizeByteN()
	{
		return 8 + varN * 10;
	}

	@Override
	public int normalizeTo(byte[] bs, int begin)
	{
		writeU2(bs, begin, readU2(bytes, beginBi));
		writeS4(bs, begin + 2, normalizeByteN() - 6);
		writeU2(bs, begin + 6, varN);
		if (beginAds == null)
		{
			System.arraycopy(bytes, beginBi + 8, bs, begin + 8, varN * 10);
			return begin + normalizeByteN();
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
