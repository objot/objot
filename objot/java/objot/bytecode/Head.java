//
// Copyright 2007-2015 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.bytecode;

import java.io.PrintStream;

import objot.util.Array2;
import objot.util.InvalidValueException;
import objot.util.Mod2;


public class Head
	extends Element
{
	public final Constants cons;
	int modifier;
	int classCi;
	int superCi;
	int interfaceN;
	int[] interfaceCis;

	public Head(Constants c, byte[] bs, int beginBi_)
	{
		super(bs, beginBi_);
		cons = c;
		setModifier(readU2(bytes, beginBi));
		classCi = readU2(bytes, beginBi + 2);
		superCi = readU2(bytes, beginBi + 4);
		interfaceN = readU2(bytes, beginBi + 6);
		end1Bi = beginBi + 8 + (interfaceN << 1);
	}

	public int getModifier()
	{
		return modifier;
	}

	public int getClassCi()
	{
		return classCi;
	}

	public int getSuperCi()
	{
		return superCi;
	}

	public int getInterfaceN()
	{
		return interfaceN;
	}

	void readInterfaceCis()
	{
		if (interfaceCis != null)
			return;
		interfaceCis = new int[allocN(interfaceN)];
		for (int i = 0; i < interfaceN; i++)
			interfaceCis[i] = readU2(bytes, beginBi + 8 + (i << 1));
	}

	void checkIndex(int ii)
	{
		if (ii < 0 || ii >= interfaceN)
			throw new InvalidValueException(ii);
	}

	public int getInterfaceCi(int ii)
	{
		checkIndex(ii);
		readInterfaceCis();
		return interfaceCis[ii];
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
		out.print("class ");
		cons.print(out, getClassCi(), verbose);
		out.print(" super ");
		cons.print(out, getSuperCi(), verbose);
		out.println();
		for (int i = 0; i < interfaceN; i++)
		{
			printIndent(out, indent);
			out.print(i);
			out.print(". interface ");
			cons.print(out, getInterfaceCi(i), verbose).println();
		}
	}

	public void setModifier(int v)
	{
		modifier = Mod2.get(v, 0);
	}

	public void setClassCi(int v)
	{
		classCi = v;
	}

	public void setSuperCi(int v)
	{
		superCi = v;
	}

	public void ensureInterfaceN(int n)
	{
		readInterfaceCis();
		interfaceCis = Array2.ensureN(interfaceCis, n);
	}

	/** @return interface index(not Ci) */
	public int addInterface(int interfaceCi)
	{
		readInterfaceCis();
		ensureInterfaceN(interfaceN + 1);
		interfaceCis[interfaceN] = interfaceCi;
		return interfaceN++;
	}

	/** @return interface index(not Ci) */
	public int putInterface(int interfaceCi)
	{
		readInterfaceCis();
		for (int i = 0; i < interfaceN; i++)
			if (interfaceCis[i] == interfaceCi)
				return i;
		return addInterface(interfaceCi);
	}

	public void setInterface(int ii, int interfaceCi)
	{
		checkIndex(ii);
		readInterfaceCis();
		interfaceCis[ii] = interfaceCi;
	}

	@Override
	public int normalizeByteN()
	{
		return 8 + (interfaceN << 1);
	}

	@Override
	public int normalizeTo(byte[] bs, int begin)
	{
		writeU2(bs, begin, modifier & 0xFFFF);
		writeU2(bs, begin + 2, classCi);
		writeU2(bs, begin + 4, superCi);
		writeU2(bs, begin + 6, interfaceN);
		if (interfaceCis == null)
		{
			System.arraycopy(bytes, beginBi + 8, bs, begin + 8, interfaceN << 1);
			return begin + normalizeByteN();
		}
		begin += 8;
		for (int i = 0; i < interfaceN; i++, begin += 2)
			writeU2(bs, begin, interfaceCis[i]);
		return begin;
	}
}
