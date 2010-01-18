//
// Copyright 2007-2010 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.bytecode;

import java.io.PrintStream;

import objot.util.Array2;
import objot.util.InvalidValueException;


public final class CodeCatchs
	extends Element
{
	public final Constants cons;
	int catchN;
	int[] beginAds;
	int[] end1Ads;
	int[] catchAds;
	int[] typeCis;

	static int readByteN(byte[] bs, int bi)
	{
		return 2 + (readU2(bs, bi) << 3);
	}

	public CodeCatchs(Constants c, byte[] bs, int beginBi_)
	{
		super(bs, beginBi_);
		cons = c;
		catchN = readU2(bytes, beginBi);
		end1Bi = beginBi + 2 + (catchN << 3);
	}

	public int getCatchN()
	{
		return catchN;
	}

	void reads()
	{
		if (beginAds != null)
			return;
		beginAds = new int[allocN(catchN)];
		end1Ads = new int[allocN(catchN)];
		catchAds = new int[allocN(catchN)];
		typeCis = new int[allocN(catchN)];
		int bi = beginBi + 2;
		for (int i = 0; i < catchN; i++)
		{
			beginAds[i] = readU2(bytes, bi);
			end1Ads[i] = readU2(bytes, bi + 2);
			catchAds[i] = readU2(bytes, bi + 4);
			typeCis[i] = readU2(bytes, bi + 6);
			bi += 8;
		}
	}

	void checkIndex(int ti)
	{
		if (ti < 0 || ti >= catchN)
			throw new InvalidValueException(ti);
	}

	public int getBeginAd(int ti)
	{
		checkIndex(ti);
		reads();
		return beginAds[ti];
	}

	public int getEnd1Ad(int ti)
	{
		checkIndex(ti);
		reads();
		return end1Ads[ti];
	}

	public int getCatchAd(int ti)
	{
		checkIndex(ti);
		reads();
		return catchAds[ti];
	}

	public int getTypeCi(int ti)
	{
		checkIndex(ti);
		reads();
		return typeCis[ti];
	}

	@Override
	void printContents(PrintStream out, int indent1st, int indent, int verbose)
	{
		out.println();
		for (int i = 0; i < catchN; i++)
		{
			printIndent(out, indent);
			out.print(i);
			out.print(". beginAd ");
			out.print(getBeginAd(i));
			out.print(" end1Ad ");
			out.print(getEnd1Ad(i));
			out.print(" catchAd ");
			out.print(getCatchAd(i));
			out.print(" type ");
			cons.print(out, getTypeCi(i), verbose).println();
		}
	}

	public void ensureCatchN(int n)
	{
		reads();
		beginAds = Array2.ensureN(beginAds, n);
		end1Ads = Array2.ensureN(end1Ads, n);
		catchAds = Array2.ensureN(catchAds, n);
		typeCis = Array2.ensureN(typeCis, n);
	}

	public int addInfo(int beginAd, int end1Ad, int catchAd, int typeCi)
	{
		reads();
		ensureCatchN(catchN + 1);
		beginAds[catchN] = beginAd;
		end1Ads[catchN] = end1Ad;
		catchAds[catchN] = catchAd;
		typeCis[catchN] = typeCi;
		return catchN++;
	}

	public int putInfo(int beginAd, int end1Ad, int catchAd, int typeCi)
	{
		reads();
		for (int i = 0; i < catchN; i++)
			if (beginAds[i] == beginAd && end1Ads[i] == end1Ad && catchAds[i] == catchAd
				&& typeCis[i] == typeCi)
				return i;
		return addInfo(beginAd, end1Ad, catchAd, typeCi);
	}

	public void setInfo(int ti, int beginAd, int end1Ad, int catchAd, int typeCi)
	{
		checkIndex(ti);
		reads();
		beginAds[ti] = beginAd;
		end1Ads[ti] = end1Ad;
		catchAds[ti] = catchAd;
		typeCis[ti] = typeCi;
	}

	@Override
	public int normalizeByteN()
	{
		return 2 + (catchN << 3);
	}

	@Override
	public int normalizeTo(byte[] bs, int begin)
	{
		writeU2(bs, begin, catchN);
		if (beginAds == null)
		{
			System.arraycopy(bytes, beginBi + 2, bs, begin + 2, catchN << 3);
			return begin + normalizeByteN();
		}
		begin += 2;
		for (int i = 0; i < catchN; i++, begin += 8)
		{
			writeU2(bs, begin, beginAds[i]);
			writeU2(bs, begin + 2, end1Ads[i]);
			writeU2(bs, begin + 4, catchAds[i]);
			writeU2(bs, begin + 6, typeCis[i]);
		}
		return begin;
	}
}
