package objot.bytecode;

import java.io.PrintStream;

import objot.util.Array2;
import objot.util.InvalidValueException;


public class Head
	extends Element
{
	public final Constants cons;
	protected int modifier;
	protected Visible visible;
	protected int classCi;
	protected int superCi;
	protected int interfaceN;
	protected int[] interfaceCis;

	public Head(Constants c, byte[] bs, int beginBi_)
	{
		super(bs, beginBi_, true);
		cons = c;
		modifier = read0u2(beginBi);
		visible = Visible.get(modifier);
		classCi = read0u2(beginBi + 2);
		superCi = read0u2(beginBi + 4);
		interfaceN = read0u2(beginBi + 6);
		end1Bi = beginBi + 8 + (interfaceN << 1);
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

	protected void readInterfaceCis()
	{
		if (interfaceCis != null)
			return;
		interfaceCis = new int[allocN(interfaceN, 15)];
		for (int i = 0; i < interfaceN; i++)
			interfaceCis[i] = read0u2(beginBi + 8 + (i << 1));
	}

	protected void checkIndex(int ii)
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
	protected void printContents(PrintStream out, String indent1st, String indent,
		int verbose, boolean hash)
	{
		out.println();
		out.print(indent);
		out.print("modifier 0x");
		out.print(Integer.toHexString(modifier));
		out.print(" visible ");
		out.println(getVisible());
		out.print(indent);
		out.print("classCi ");
		out.print(getClassCi());
		getCons().printClassChars(out, getClassCi(), verbose);
		out.print(" superCi ");
		out.print(getSuperCi());
		getCons().printClassChars(out, getSuperCi(), verbose);
		out.println();
		for (int i = 0; i < getInterfaceN(); i++)
		{
			out.print(indent);
			out.print(i);
			out.print(". interfaceCi ");
			out.print(getInterfaceCi(i));
			getCons().printClassChars(out, getInterfaceCi(i), verbose);
			out.println();
		}
	}

	public void setModifier(int v)
	{
		modifier = v;
		visible = Visible.get(modifier);
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
	public int appendInterface(int interfaceCi)
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
		return appendInterface(interfaceCi);
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
		writeU2(bs, begin, modifier);
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
