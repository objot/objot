//
// Copyright 2007 Qianyan Cai
// Under the terms of the GNU General Public License version 2
//
package objot.bytecode;

import objot.util.Array2;
import objot.util.Math2;

import java.io.PrintStream;


public final class Code
	extends Element
{
	public final Constants cons;
	int attrNameCi;
	int stackN;
	int localN;
	int addrN0;
	int addrN;
	int addrBi;
	byte[] ins;
	int catchBi;
	CodeCatchs catchs;
	int catchBn;
	int attrN;
	int attrBi;
	int linesBi;
	CodeLines lines;
	int varsBi;
	CodeVars vars;
	int varSignsBi;
	CodeVars varSigns;

	public Code(Constants c, byte[] bs, int beginBi_)
	{
		super(bs, beginBi_);
		cons = c;
		attrNameCi = read0u2(beginBi);
		stackN = read0u2(beginBi + 6);
		localN = read0u2(beginBi + 8);
		addrN0 = addrN = read0u4(beginBi + 10);
		if (addrN > 65535)
			throw new ClassFormatError("too large code");
		addrBi = beginBi + 14;
		ins = bytes;
		catchBi = addrBi + addrN;
		catchBn = CodeCatchs.readByteN(bytes, catchBi);
		attrBi = catchBi + catchBn + 2;
		attrN = read0u2(attrBi - 2);
		int bi = attrBi;
		for (int an = attrN; an > 0; an--)
		{
			int name = read0u2(bi);
			if (linesBi == 0 && cons.equalsUtf(name, Bytecode.CODE_LINES))
				linesBi = bi;
			else if (varsBi == 0 && cons.equalsUtf(name, Bytecode.CODE_VARS))
				varsBi = bi;
			else if (varSignsBi == 0 && cons.equalsUtf(name, Bytecode.CODE_VARSIGNS))
				varSignsBi = bi;
			bi += 6 + read0u4(bi + 2);
		}
		if (bi - beginBi - 6 != read0u4(beginBi + 2))
			throw new ClassFormatError("inconsistent attribute length");
		end1Bi = bi;
	}

	public int getStackN()
	{
		return stackN;
	}

	public int getLocalN()
	{
		return localN;
	}

	public int getAddrN()
	{
		return addrN;
	}

	public int getAddrBi()
	{
		return addrBi;
	}

	public byte getInsS1(int addr)
	{
		return ins[addr + addrBi];
	}

	public int getInsU1(int addr)
	{
		return ins[addr + addrBi] & 0xFF;
	}

	public short getInsS2(int addr)
	{
		return readS2(ins, addr + addrBi);
	}

	public int getInsU2(int addr)
	{
		return readU2(ins, addr + addrBi);
	}

	public int getInsS4(int addr)
	{
		return readS4(ins, addr + addrBi);
	}

	public int getInsU4(int addr)
	{
		return readU4(ins, addr + addrBi);
	}

	public long getInsS8(int addr)
	{
		return readS8(ins, addr + addrBi);
	}

	public int getInsAddrN(int addr)
	{
		return Opcode.getInsAddrN(ins, addr + addrBi, addrBi);
	}

	public void copyInsTo(int addr, byte[] dest, int destBi, int n)
	{
		System.arraycopy(ins, addr + addrBi, dest, destBi, n);
	}

	byte[] insBytes()
	{
		return ins;
	}

	public CodeCatchs getCatchs()
	{
		if (catchs == null)
			catchs = new CodeCatchs(cons, bytes, catchBi);
		return catchs;
	}

	public int getAttrN()
	{
		return attrN;
	}

	public int getAttrBi()
	{
		return attrBi;
	}

	public CodeLines getLines()
	{
		if (lines == null && linesBi > 0)
			lines = new CodeLines(bytes, linesBi);
		return lines;
	}

	public CodeVars getVars()
	{
		if (vars == null && varsBi > 0)
			vars = new CodeVars(cons, bytes, varsBi, false);
		return vars;
	}

	public CodeVars getVarSigns()
	{
		if (varSigns == null && varSignsBi > 0)
			varSigns = new CodeVars(cons, bytes, varSignsBi, true);
		return varSigns;
	}

	@Override
	void printContents(PrintStream out, int indent1st, int indent, int verbose)
	{
		out.println();
		printIndent(out, indent);
		out.print("stackN ");
		out.print(stackN);
		out.print(" localN ");
		out.print(localN);
		out.print(" addrN ");
		out.print(addrN);
		if (verbose > 1)
		{
			out.print(' ');
			out.print(insBytes());
		}
		out.println();
		if (verbose > 0)
			for (int i = 0; i < addrN; i += getInsAddrN(i))
			{
				printIndent(out, indent);
				out.print(i);
				out.print(". ");
				Opcode.println(this, i, out, 0, indent + 2, verbose);
			}
		getCatchs().printTo(out, indent, indent, verbose);
		printIndent(out, indent);
		out.print("attrN ");
		out.println(attrN);
		if (getLines() != null)
			getLines().printTo(out, indent, indent, verbose);
		if (getVars() != null)
			getVars().printTo(out, indent, indent, verbose);
		if (getVarSigns() != null)
		{
			printIndent(out, indent);
			out.print("varSigns ");
			getVarSigns().printTo(out, 0, indent, verbose);
		}
	}

	public void setStackN(int v)
	{
		stackN = v;
	}

	public void setLocalN(int v)
	{
		localN = v;
	}

	public void setIns(byte[] bs, int addrBegin, int addrEnd1)
	{
		if (bs == null)
			throw null;
		Math2.checkRange(addrBegin, addrEnd1, bs.length);
		if (addrEnd1 - addrBegin > 65535)
			throw new ClassFormatError("too large code");
		ins = bs;
		addrN = addrEnd1 - addrBegin;
		addrBi = addrBegin;
	}

	public void setIns(Instruction i, boolean clone)
	{
		if (i.cons != null && i.cons != cons)
			throw new IllegalArgumentException("inconsistent constants");
		if (i.addr > 65535)
			throw new ClassFormatError("too large code");
		ins = clone ? Array2.subClone(i.bytes, 0, i.end1Bi) : i.bytes;
		addrN = i.addr;
		addrBi = 0;
	}

	@Override
	public int normalizeByteN()
	{
		int n = byteN0() + addrN - addrN0;
		if (catchs != null)
			n += catchs.normalizeByteN() - catchs.byteN0();
		if (lines != null)
			n += lines.normalizeByteN() - lines.byteN0();
		if (vars != null)
			n += vars.normalizeByteN() - vars.byteN0();
		if (varSigns != null)
			n += varSigns.normalizeByteN() - varSigns.byteN0();
		return n;
	}

	@Override
	public int normalizeTo(byte[] bs, int begin)
	{
		writeU2(bs, begin, attrNameCi);
		writeU2(bs, begin + 6, stackN);
		writeU2(bs, begin + 8, localN);

		int bi = beginBi + 10;
		int bbi = begin + 10;
		writeU4(bs, bbi, addrN);
		copyInsTo(0, bs, bbi + 4, addrN);
		bi += 4 + addrN0;
		bbi += 4 + addrN;

		if (catchs == null)
		{
			System.arraycopy(bytes, bi, bs, bbi, catchBn);
			bbi += catchBn;
		}
		else
			bbi = catchs.normalizeTo(bs, bbi);

		writeU2(bs, bbi, attrN);
		bi = attrBi;
		bbi += 2;
		for (int an = attrN; an > 0; an--)
		{
			int bn = 6 + read0u4(bi + 2);
			if (bi == linesBi && lines != null)
				bbi = lines.normalizeTo(bs, bbi);
			else if (bi == varsBi && vars != null)
				bbi = vars.normalizeTo(bs, bbi);
			else if (bi == varSignsBi && varSigns != null)
				bbi = varSigns.normalizeTo(bs, bbi);
			else
			{
				System.arraycopy(bytes, bi, bs, bbi, bn);
				bbi += bn;
			}
			bi += bn;
		}

		writeS4(bs, begin + 2, bbi - begin - 6);
		return bbi;
	}
}
