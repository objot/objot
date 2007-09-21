package objot.bytecode;

import java.io.PrintStream;

import objot.util.Array2;
import objot.util.InvalidValueException;


public class Fields
	extends Element
{
	public final Constants cons;
	protected int fieldN;
	protected Field[] fields;

	public Fields(Constants c, byte[] bs, int beginBi_)
	{
		super(bs, beginBi_);
		cons = c;
		fieldN = read0u2(beginBi);
		int bi = beginBi + 2;
		for (int i = 0; i < fieldN; i++)
			bi = Field.readEnd1Bi(bytes, bi);
		end1Bi = bi;
	}

	public int getFieldN()
	{
		return fieldN;
	}

	protected void checkIndex(int fi)
	{
		if (fi < 0 || fi >= fieldN)
			throw new InvalidValueException(fi);
	}

	protected void readFields()
	{
		if (fields != null)
			return;
		fields = new Field[allocN(fieldN)];
		int bi = beginBi + 2;
		for (int i = 0; i < fieldN; i++)
		{
			fields[i] = new Field(cons, bytes, bi);
			bi = fields[i].end1Bi;
		}
	}

	public Field getField(int fi)
	{
		checkIndex(fi);
		readFields();
		return fields[fi];
	}

	@Override
	protected void printContents(PrintStream out, int indent1st, int indent, int verbose,
		boolean hash)
	{
		if (verbose > 0)
		{
			printIndent(out, indent1st);
			out.print(" fieldN ");
			out.print(fieldN);
		}
		out.println();
		for (int i = 0; i < fieldN; i++)
		{
			printIndent(out, indent);
			out.print(i);
			out.print('.');
			getField(i).printTo(out, 0, indent, verbose, hash);
		}
	}

	public void ensureFieldN(int n)
	{
		readFields();
		fields = Array2.ensureN(fields, n);
	}

	/** @return field index(not Ci) */
	public int addField(Field f)
	{
		readFields();
		ensureFieldN(fieldN + 1);
		fields[fieldN] = f;
		return fieldN++;
	}

	public void setField(int fi, Field f)
	{
		checkIndex(fi);
		readFields();
		fields[fi] = f;
	}

	@Override
	public int normalizeByteN()
	{
		if (fields == null)
			return byteN0();
		int n = 2;
		for (int i = 0; i < fieldN; i++)
			n += fields[i].normalizeByteN();
		return n;
	}

	@Override
	public int normalizeTo(byte[] bs, int begin)
	{
		if (fields == null)
		{
			System.arraycopy(bytes, beginBi, bs, begin, byteN0());
			return begin + byteN0();
		}
		writeU2(bs, begin, fieldN);
		begin += 2;
		for (int i = 0; i < fieldN; i++)
			begin = fields[i].normalizeTo(bs, begin);
		return begin;
	}
}
