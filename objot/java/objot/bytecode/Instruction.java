//
// Copyright 2007-2015 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
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

	public final Constants cons;
	public int addr;

	public Instruction(Constants c)
	{
		cons = c;
	}

	public Instruction(Constants c, int ensureCapacity)
	{
		cons = c;
		ensureByteN(ensureCapacity);
	}

	public final void reserveAddr(int add)
	{
		ensureByteN(add += addr);
		if (add > end1Bi)
			end1Bi = add;
	}

	public final void copyFrom(byte[] ins, int ad, int adN)
	{
		reserveAddr(adN);
		System.arraycopy(ins, ad, bytes, addr, adN);
		addr += adN;
	}

	public final void copyFrom(Bytes ins, int ad, int adN)
	{
		reserveAddr(adN);
		ins.copyTo(ad, bytes, addr, adN);
		addr += adN;
	}

	public final void copyFrom(Code code, int ad, int adN)
	{
		reserveAddr(adN);
		code.copyInsTo(ad, bytes, addr, adN);
		addr += adN;
	}

	public final void ins0(byte op)
	{
		reserveAddr(1);
		addr = writeS1(bytes, addr, op);
	}

	public final void insS1(byte op, byte s1)
	{
		reserveAddr(2);
		addr = writeS1(bytes, addr, op);
		addr = writeS1(bytes, addr, s1);
	}

	public final void insS1(byte op, int s1)
	{
		reserveAddr(2);
		addr = writeS1(bytes, addr, op);
		addr = writeS1(bytes, addr, s1);
	}

	public final void insU1(byte op, int u1)
	{
		reserveAddr(2);
		addr = writeS1(bytes, addr, op);
		addr = writeU1(bytes, addr, u1);
	}

	public final void insU2(byte op, int u2)
	{
		reserveAddr(3);
		addr = writeS1(bytes, addr, op);
		addr = writeU2(bytes, addr, u2);
	}

	public final void insS2(byte op, short s2)
	{
		reserveAddr(3);
		addr = writeS1(bytes, addr, op);
		addr = writeS2(bytes, addr, s2);
	}

	public final void insS2(byte op, int s2)
	{
		reserveAddr(3);
		addr = writeS1(bytes, addr, op);
		addr = writeS2(bytes, addr, s2);
	}

	public final void insS4(byte op, int s4)
	{
		reserveAddr(5);
		addr = writeS1(bytes, addr, op);
		addr = writeS4(bytes, addr, s4);
	}

	public final void insS1U2(byte op, byte s1, int u2)
	{
		reserveAddr(4);
		addr = writeS1(bytes, addr, op);
		addr = writeS1(bytes, addr, s1);
		addr = writeU2(bytes, addr, u2);
	}

	public final void insS1U2(byte op, int s1, int u2)
	{
		reserveAddr(4);
		addr = writeS1(bytes, addr, op);
		addr = writeS1(bytes, addr, s1);
		addr = writeU2(bytes, addr, u2);
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
		reserveAddr(3);
		addr = writeS1(bytes, addr, op);
		addr = writeU1(bytes, addr, u1);
		addr = writeS1(bytes, addr, s1);
	}

	public final void insU1S1(byte op, int u1, int s1)
	{
		reserveAddr(3);
		addr = writeS1(bytes, addr, op);
		addr = writeU1(bytes, addr, u1);
		addr = writeS1(bytes, addr, s1);
	}

	public final void insIproc(int iProcCi, int needStackN)
	{
		reserveAddr(5);
		addr = writeS1(bytes, addr, INVOKEINTERFACE);
		addr = writeU2(bytes, addr, iProcCi);
		addr = writeU1(bytes, addr, needStackN);
		addr = writeS1(bytes, addr, (byte)0);
	}

	public final void insWideU2(byte op, int u2)
	{
		reserveAddr(4);
		addr = writeS1(bytes, addr, WIDE);
		addr = writeS1(bytes, addr, op);
		addr = writeU2(bytes, addr, u2);
	}

	public final void insWideInc(int localI, int incValue)
	{
		reserveAddr(6);
		addr = writeS1(bytes, addr, WIDE);
		addr = writeS1(bytes, addr, IINC);
		addr = writeU2(bytes, addr, localI);
		addr = writeS2(bytes, addr, incValue);
	}

	/** @return jump tag: <code>addr.op</code> or <code>~addr.op</code> */
	public final int insJump(byte op)
	{
		if (op == Opcode.GOTO4 || op == Opcode.JSR4)
		{
			reserveAddr(5);
			addr = writeS1(bytes, addr, op);
			addr = writeS4(bytes, addr, 0);
			return ~addr + 5;
		}
		reserveAddr(3);
		addr = writeS1(bytes, addr, op);
		addr = writeS2(bytes, addr, 0);
		return addr - 3;
	}

	public final void jumpHere(int jumpTag)
	{
		jump(jumpTag, addr);
	}

	public final void jump(int jumpTag, int addrTo)
	{
		if (jumpTag >= 0)
			writeS2(bytes, jumpTag + 1, addrTo - jumpTag);
		else
		{
			jumpTag = -jumpTag - 1;
			writeS4(bytes, jumpTag + 1, addrTo - jumpTag);
		}
	}

	/**
	 * @return switch-table tag:
	 *         <code>(long)(high0 - low + 1) << 32 | addr.op & 0xFFFFFFFFL</code>
	 */
	public final long insSwitchTable(int low, int high0)
	{
		int n = high0 - low + 1;
		if (n >> 16 != 0)
			throw new ClassFormatError("invalid table switch low/high value " + low + ' '
				+ high0);
		int h = 4 - (addr & 3);
		int bn = h + 12 + (n << 2);
		reserveAddr(bn);
		writeS4(bytes, addr, 0); // for align
		writeS1(bytes, addr, TABLESWITCH);
		writeS4(bytes, addr + h + 4, low);
		writeS4(bytes, addr + h + 8, high0);
		addr += bn;
		return (long)n << 32 | addr - bn & 0xFFFFFFFFL;
	}

	/** @param index negative for default, 0 for low-value ... */
	public final void switchTableHere(long tableTag, int index)
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
			writeS4(bytes, adOp + h, addrTo - adOp);
		else
			writeS4(bytes, adOp + h + 12 + (index << 2), addrTo - adOp);
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
		reserveAddr(bn);
		writeS4(bytes, addr, 0); // for align
		writeS1(bytes, addr, LOOKUPSWITCH);
		writeS4(bytes, addr + h + 4, n);
		addr += bn;
		return (long)n << 32 | addr - bn & 0xFFFFFFFFL;
	}

	/** @param index negative for default, 0 for first value ... */
	public final void switchLookupHere(long lookupTag, int index)
	{
		switchLookup(lookupTag, index, addr);
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
			writeS4(bytes, adOp + h, addrTo - adOp);
		else
			writeS4(bytes, adOp + h + 8 + (index << 3) + 4, addrTo - adOp);
	}

	public final void insLoad(Class<?> c)
	{
		if (c.isPrimitive())
			insU2(GETSTATIC, cons.putField(cons.putClass(Class2.box(c, true)), //
				cons.putNameDesc(cons.putUtf(TYPE_NAME), //
					cons.putUcs(Class2.descript(Class.class)))));
		else
			insU2(LDCW, cons.putClass(c));
	}

	/** Stack: object --> narrowed object */
	public final void insNarrow(Class<?> c)
	{
		if (c != Object.class)
			insU2(CHECKCAST, cons.putClass(c));
	}

	/** Stack: object --> narrowed object */
	public final void insNarrowBox(Class<?> c)
	{
		if (c != Object.class)
			insU2(CHECKCAST, cons.putClass(Class2.boxTry(c, true)));
	}

	/** Stack: primitive value(s) or object --> object */
	public final void insBox(Class<?> c)
	{
		if ( !c.isPrimitive() || c == void.class)
			return;
		Class<?> b = Class2.box(c, false);
		insU2(INVOKESTATIC, cons.putCproc(cons.putClass(b), //
			cons.putNameDesc(cons.putUtf(VALUEOF_NAME), //
				cons.putUcs('(' + Class2.descript(c) + ')' + Class2.descript(b)))));
	}

	/** Stack: object --> primitive value(s) or narrowed object */
	public final void insUnboxNarrow(Class<?> c)
	{
		if (c == void.class)
			c = Void.class;
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
	public final void insNews(Class<?> elem)
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
