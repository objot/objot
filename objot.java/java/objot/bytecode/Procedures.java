package objot.bytecode;

import java.io.PrintStream;

import objot.util.Array2;
import objot.util.InvalidValueException;


public class Procedures
	extends Element
{
	protected Constants cons;
	protected int procN;
	protected Procedure[] procs;

	public Procedures(Constants c, byte[] bs, int beginBi_, boolean forExtension_)
	{
		super(bs, beginBi_, forExtension_);
		cons = c;
		procN = read0u2(beginBi);
		int bi = beginBi + 2;
		for (int i = 0; i < procN; i++)
			bi = Procedure.readEnd1Bi(bytes, bi);
		end1Bi = bi;
	}

	public Constants getCons()
	{
		return cons;
	}

	public int getProcN()
	{
		return procN;
	}

	protected void checkIndex(int fi)
	{
		if (fi < 0 || fi >= procN)
			throw new InvalidValueException(fi);
	}

	protected void readProcs()
	{
		if (procs != null)
			return;
		procs = new Procedure[allocN(procN, 31)];
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

	@Override
	protected void printContents(PrintStream out, String indent1st, String indent,
		int verbose, boolean hash)
	{
		out.println();
		for (int i = 0; i < getProcN(); i++)
		{
			out.print(indent);
			out.print(i);
			out.print('.');
			getProc(i).printTo(out, "", indent, verbose, hash);
		}
	}

	public void ensureProcN(int n)
	{
		readProcs();
		procs = Array2.ensureN(procs, n);
	}

	/** @return procedure index(not Ci) */
	public int appendProc(Procedure p)
	{
		readProcs();
		ensureProcN(procN + 1);
		procs[procN] = p;
		return procN++;
	}

	public void setProc(int pi, Procedure p)
	{
		checkIndex(pi);
		readProcs();
		procs[pi] = p;
	}

	@Override
	public int normalizeByteN()
	{
		if (procs == null)
			return byteN();
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
			System.arraycopy(bytes, beginBi, bs, begin, byteN());
			return begin + byteN();
		}
		writeU2(bs, begin, procN);
		begin += 2;
		for (int i = 0; i < procN; i++)
			begin = procs[i].normalizeTo(bs, begin);
		return begin;
	}
}
