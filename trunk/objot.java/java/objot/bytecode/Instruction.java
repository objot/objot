//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.bytecode;

import objot.util.Bytes;
import objot.util.Class2;

import static objot.bytecode.Opcode.*;


public class Instruction
	extends Bytes
{
	private static final Bytes VALUEOF_NAME = Element.utf("valueOf");
	private static final Bytes TYPE_NAME = Element.utf("TYPE");

	public int addr;

	public Instruction()
	{
		super(null);
	}

	public Instruction(int ensureAddrN)
	{
		super(null);
		ensureByteN(ensureAddrN);
	}

	public final void copyFrom(byte[] ins, int ad, int adN)
	{
		ensureByteN(addr + adN);
		System.arraycopy(ins, ad, bytes, addr, adN);
		addr += adN;
	}

	public final void copyFrom(Bytes ins, int ad, int adN)
	{
		ensureByteN(addr + adN);
		ins.copyTo(ad, bytes, addr, adN);
		addr += adN;
	}

	public final void copyFrom(Code code, int ad, int adN)
	{
		ensureByteN(addr + adN);
		code.copyInsTo(ad, bytes, addr, adN);
		addr += adN;
	}

	public final void ins0(byte op)
	{
		ensureByteN(addr + 1);
		write0s1(addr++, op);
	}

	public final void insS1(byte op, byte s1)
	{
		ensureByteN(addr + 2);
		write0s1(addr++, op);
		write0s1(addr++, s1);
	}

	public final void insS1(byte op, int s1)
	{
		ensureByteN(addr + 2);
		write0s1(addr++, op);
		write0s1(addr++, s1);
	}

	public final void insU1(byte op, int u1)
	{
		ensureByteN(addr + 2);
		write0s1(addr++, op);
		write0u1(addr++, u1);
	}

	public final void insU2(byte op, int u2)
	{
		ensureByteN(addr + 3);
		write0s1(addr, op);
		write0u2(addr + 1, u2);
		addr += 3;
	}

	public final void insS2(byte op, short s2)
	{
		ensureByteN(addr + 3);
		write0s1(addr, op);
		write0s2(addr + 1, s2);
		addr += 3;
	}

	public final void insS2(byte op, int s2)
	{
		ensureByteN(addr + 3);
		write0s1(addr, op);
		write0s2(addr + 1, s2);
		addr += 3;
	}

	public final void insS4(byte op, int s4)
	{
		ensureByteN(addr + 5);
		write0s1(addr, op);
		write0s4(addr + 1, s4);
		addr += 5;
	}

	public final void insS1U2(byte op, byte s1, int u2)
	{
		ensureByteN(addr + 4);
		write0s1(addr, op);
		write0s1(addr + 1, s1);
		write0u2(addr + 2, u2);
		addr += 4;
	}

	public final void insS1U2(byte op, int s1, int u2)
	{
		ensureByteN(addr + 4);
		write0s1(addr, op);
		write0s1(addr + 1, s1);
		write0u2(addr + 2, u2);
		addr += 4;
	}

	public final void insU1wU2(byte op, int u2)
	{
		if (u2 >> 8 == 0)
			insU1(op, u2);
		else
			insWideU2(op, u2);
	}

	public final void insU1S1(byte op, int u1, byte s1)
	{
		ensureByteN(addr + 3);
		write0s1(addr, op);
		write0u1(addr + 1, u1);
		write0s1(addr + 2, s1);
		addr += 3;
	}

	public final void insU1S1(byte op, int u1, int s1)
	{
		ensureByteN(addr + 3);
		write0s1(addr, op);
		write0u1(addr + 1, u1);
		write0s1(addr + 2, s1);
		addr += 3;
	}

	public final void insIproc(int iProcCi, int needStackN)
	{
		ensureByteN(addr + 5);
		write0s1(addr, INVOKEINTERFACE);
		write0u2(addr + 1, iProcCi);
		write0u1(addr + 3, needStackN);
		write0s1(addr + 4, (byte)0);
		addr += 5;
	}

	public final void insWideU2(byte op, int u2)
	{
		ensureByteN(addr + 4);
		write0s1(addr, WIDE);
		write0s1(addr + 1, op);
		write0u2(addr + 2, u2);
		addr += 4;
	}

	public final void insWideInc(int localI, int incValue)
	{
		ensureByteN(addr + 6);
		write0s1(addr, WIDE);
		write0s1(addr + 1, IINC);
		write0u2(addr + 2, localI);
		write0s2(addr + 4, incValue);
		addr += 6;
	}

	/** @return jump tag: <code>addr-op</code> or <code>-addr-op - 1</code> */
	public final int insJump(byte op)
	{
		if (op == Opcode.GOTO4 || op == Opcode.JSR4)
		{
			ensureByteN(addr + 5);
			write0s1(addr, op);
			write0s4(addr + 1, 0);
			return -((addr += 5) - 5) - 1;
		}
		ensureByteN(addr + 3);
		write0s1(addr, op);
		write0s2(addr + 1, 0);
		return (addr += 3) - 3;
	}

	public final void jumpFrom(int jumpTag)
	{
		jump(jumpTag, addr);
	}

	public final void jump(int jumpTag, int addrTo)
	{
		if (jumpTag >= 0)
			write0s2(jumpTag + 1, addrTo - jumpTag);
		else
		{
			jumpTag = -jumpTag - 1;
			write0s4(jumpTag + 1, addrTo - jumpTag);
		}
	}

	/**
	 * @return switch-table tag:
	 *         <code>(long)(high0 - low + 1) << 32 | addr-op & 0xFFFFFFFFL</code>
	 */
	public final long insSwitchTable(int low, int high0)
	{
		int n = high0 - low + 1;
		if (n >> 16 != 0)
			throw new ClassFormatError("invalid table switch low/high value " + low + ' '
				+ high0);
		int h = 4 - (addr & 3);
		int bn = h + 12 + (n << 2);
		ensureByteN(addr + bn);
		write0s4(addr, 0);
		write0s1(addr, TABLESWITCH);
		write0s4(addr + h + 4, low);
		write0s4(addr + h + 8, high0);
		addr += bn;
		return (long)n << 32 | addr - bn & 0xFFFFFFFFL;
	}

	/** @param index negative for default, 0 for low-value ... */
	public final void switchTableFrom(long tableTag, int index)
	{
		switchTable(tableTag, index, addr);
	}

	/** @param index negative for default, 0 for low-value ... */
	public final void switchTable(long tableTag, int index, int addrTo)
	{
		int n = (int)(tableTag >> 32);
		int adOp = (int)tableTag;
		if (index >= n)
			throw new IndexOutOfBoundsException(String.valueOf(index));
		int h = 4 - (adOp & 3);
		if (index < 0)
			write0s4(adOp + h, addrTo - adOp);
		else
			write0s4(adOp + h + 12 + (index << 2), addrTo - adOp);
	}

	/**
	 * @return switch-lookup tag:<code>(long)n << 32 | addr-op & 0xFFFFFFFFL</code>
	 */
	public final long insSwitchLookup(int n)
	{
		if (n >> 16 != 0)
			throw new ClassFormatError("invalid lookup switch valueN " + n);
		int h = 4 - (addr & 3);
		int bn = h + 8 + (n << 3);
		ensureByteN(addr + bn);
		write0s4(addr, 0);
		write0s1(addr, LOOKUPSWITCH);
		write0s4(addr + h + 4, n);
		addr += bn;
		return (long)n << 32 | addr - bn & 0xFFFFFFFFL;
	}

	/** @param index negative for default, 0 for first value ... */
	public final void switchLookupFrom(long lookupTag, int index)
	{
		switchTable(lookupTag, index, addr);
	}

	/** @param index negative for default, 0 for low-value ... */
	public final void switchLookup(long lookupTag, int index, int addrTo)
	{
		int n = (int)(lookupTag >> 32);
		int adOp = (int)lookupTag;
		if (index >= n)
			throw new IndexOutOfBoundsException(String.valueOf(index));
		int h = 4 - (adOp & 3);
		if (index < 0)
			write0s4(adOp + h, addrTo - adOp);
		else
			write0s4(adOp + h + 8 + (index << 3) + 4, addrTo - adOp);
	}

	public final void insLoad(Constants cons, Class<?> c)
	{
		if (c.isPrimitive())
			insU2(GETSTATIC, cons.putField(cons.putClass(Class2.box(c, false)), //
				cons.putNameDesc(cons.putUtf(TYPE_NAME), //
					cons.putUcs(Class2.descript(Class.class)))));
		else
			insU2(LDCW, cons.putClass(c));
	}

	/** Stack: primitive value(s) or object --> object */
	public final void insBox(Constants cons, Class<?> c)
	{
		if ( !c.isPrimitive())
			return;
		Class<?> b = Class2.box(c, false);
		insU2(INVOKESTATIC, cons.putCproc(cons.putClass(b), //
			cons.putNameDesc(cons.putUtf(VALUEOF_NAME), //
				cons.putUcs('(' + Class2.descript(c) + ')' + Class2.descript(b)))));
	}

	/** Stack: object --> primitive value(s) or narrowed object */
	public final void insUnboxNarrow(Constants cons, Class<?> c)
	{
		if (c.isPrimitive())
		{
			Class<?> b = Class2.box(c, false);
			int cla = cons.putClass(b);
			insU2(CHECKCAST, cla);
			insU2(INVOKEVIRTUAL, cons.putCproc(cla, //
				cons.putNameDesc(cons.putUcs(c.getName() + "Value"), //
					cons.putUcs("()" + Class2.descript(c)))));
		}
		else if (c != Object.class)
			insU2(CHECKCAST, cons.putClass(c));
	}

	/** Stack: length --> elem[length] */
	public final void insNews(Constants cons, Class<?> elem)
	{
		if ( !elem.isPrimitive())
			insU2(ANEWARRAY, cons.putClass(elem));
		else if (elem == int.class)
			insU1(NEWARRAY, NEWARRAY_INT);
		else if (elem == long.class)
			insU1(NEWARRAY, NEWARRAY_LONG);
		else if (elem == boolean.class)
			insU1(NEWARRAY, NEWARRAY_BOOL);
		else if (elem == byte.class)
			insU1(NEWARRAY, NEWARRAY_BYTE);
		else if (elem == char.class)
			insU1(NEWARRAY, NEWARRAY_CHAR);
		else if (elem == short.class)
			insU1(NEWARRAY, NEWARRAY_SHORT);
		else if (elem == float.class)
			insU1(NEWARRAY, NEWARRAY_FLOAT);
		else if (elem == double.class)
			insU1(NEWARRAY, NEWARRAY_DOUBLE);
		else
			throw new ClassCastException("invalid array element class " + elem);
	}
}
