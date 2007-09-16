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

	public Fields(Constants c, byte[] bs, int beginBi_, boolean forExtension_)
	{
		super(bs, beginBi_, forExtension_);
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
		fields = new Field[allocN(fieldN, 31)];
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
	protected void printContents(PrintStream out, String indent1st, String indent,
		int verbose, boolean hash)
	{
		out.println();
		for (int i = 0; i < getFieldN(); i++)
		{
			out.print(indent);
			out.print(i);
			out.print('.');
			getField(i).printTo(out, "", indent, verbose, hash);
		}
	}

	public void ensureFieldN(int n)
	{
		readFields();
		fields = Array2.ensureN(fields, n);
	}

	/** @return field index(not Ci) */
	public int appendField(Field f)
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
	public int generateByteN()
	{
		if (fields == null)
			return byteN();
		int n = 2;
		for (int i = 0; i < fieldN; i++)
			n += fields[i].generateByteN();
		return n;
	}

	@Override
	public int generateTo(byte[] bs, int begin)
	{
		if (fields == null)
		{
			System.arraycopy(bytes, beginBi, bs, begin, byteN());
			return begin + byteN();
		}
		writeU2(bs, begin, fieldN);
		begin += 2;
		for (int i = 0; i < fieldN; i++)
			begin = fields[i].generateTo(bs, begin);
		return begin;
	}
}
