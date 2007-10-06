//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.bytecode;

import java.io.PrintStream;

import objot.util.Array2;
import objot.util.Bytes;
import objot.util.InvalidValueException;


public final class Procedures
	extends Element
{
	public final Constants cons;
	int procN;
	Procedure[] procs;

	public Procedures(Constants c, byte[] bs, int beginBi_)
	{
		super(bs, beginBi_);
		cons = c;
		procN = read0u2(beginBi);
		int bi = beginBi + 2;
		for (int i = 0; i < procN; i++)
			bi = Procedure.readEnd1Bi(bytes, bi);
		end1Bi = bi;
	}

	public int getProcN()
	{
		return procN;
	}

	void checkIndex(int fi)
	{
		if (fi < 0 || fi >= procN)
			throw new InvalidValueException(fi);
	}

	void readProcs()
	{
		if (procs != null)
			return;
		procs = new Procedure[allocN(procN)];
		int bi = beginBi + 2;
		for (int i = 0; i < procN; i++)
		{
			procs[i] = new Procedure(cons, bytes, bi);
			bi = procs[i].end1Bi;
		}
	}

	public Procedure getProc(int pi)
	{
		checkIndex(pi);
		readProcs();
		return procs[pi];
	}

	/**
	 * @param name ignored if null
	 * @param desc ignored if null
	 * @return the index (not Ci) of found procedure, or negative if not found
	 */
	public int searchProc(Bytes name, Bytes desc)
	{
		readProcs();
		for (int i = 0; i < procN; i++)
			if ((name == null || cons.equalsUtf(procs[i].getNameCi(), name))
				&& (desc == null || cons.equalsUtf(procs[i].getDescCi(), desc)))
				return i;
		return -1;
	}

	@Override
	void printContents(PrintStream out, int indent1st, int indent, int verbose)
	{
		if (verbose > 0)
		{
			printIndent(out, indent1st);
			out.print(" procN ");
			out.print(procN);
		}
		out.println();
		for (int i = 0; i < procN; i++)
		{
			printIndent(out, indent);
			out.print(i);
			out.print('.');
			getProc(i).printTo(out, 0, indent, verbose);
		}
	}

	public void ensureProcN(int n)
	{
		readProcs();
		procs = Array2.ensureN(procs, n);
	}

	/** @return procedure index(not Ci) */
	public int addProc(Procedure p)
	{
		if (cons != p.cons)
			throw new IllegalArgumentException("inconsistent constants");
		readProcs();
		ensureProcN(procN + 1);
		procs[procN] = p;
		return procN++;
	}

	public void setProc(int pi, Procedure p)
	{
		if (cons != p.cons)
			throw new IllegalArgumentException("inconsistent constants");
		checkIndex(pi);
		readProcs();
		procs[pi] = p;
	}

	public Procedure removeProc(int pi)
	{
		checkIndex(pi);
		readProcs();
		Procedure p = procs[pi];
		System.arraycopy(procs, pi + 1, procs, pi, procN - pi - 1);
		procs[--procN] = null;
		return p;
	}

	@Override
	public int normalizeByteN()
	{
		if (procs == null)
			return byteN0();
		int n = 2;
		for (int i = 0; i < procN; i++)
			n += procs[i].normalizeByteN();
		return n;
	}

	@Override
	public int normalizeTo(byte[] bs, int begin)
	{
		if (procs == null)
		{
			System.arraycopy(bytes, beginBi, bs, begin, byteN0());
			return begin + byteN0();
		}
		writeU2(bs, begin, procN);
		begin += 2;
		for (int i = 0; i < procN; i++)
			begin = procs[i].normalizeTo(bs, begin);
		return begin;
	}
}
