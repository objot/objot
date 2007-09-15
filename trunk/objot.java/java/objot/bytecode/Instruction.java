package objot.bytecode;

import static objot.bytecode.Opcode.CHECKCAST;
import static objot.bytecode.Opcode.GETSTATIC;
import static objot.bytecode.Opcode.IINC;
import static objot.bytecode.Opcode.INVOKEINTERFACE;
import static objot.bytecode.Opcode.INVOKESTATIC;
import static objot.bytecode.Opcode.INVOKEVIRTUAL;
import static objot.bytecode.Opcode.LDCW;
import static objot.bytecode.Opcode.LOOKUPSWITCH;
import static objot.bytecode.Opcode.TABLESWITCH;
import static objot.bytecode.Opcode.WIDE;
import java.util.Arrays;

import objot.util.Bytes;
import objot.util.Class2;
import objot.util.Math2;


public class Instruction
	extends Bytes
{
	private static final byte[] VALUEOF_NAME = Element.chars2Utf("valueOf");
	private static final byte[] TYPE_NAME = Element.chars2Utf("TYPE");

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

	public final void copyFrom(byte[] code, int bi, int bn)
	{
		reserveN(bn);
		if (bn == 1)
			write0s1(addr, code[bi]);
		else
			System.arraycopy(code, bi, bytes, addr, bn);
		addr += bn;
	}

	public final void copyFrom(Bytes code, int bi, int bn)
	{
		reserveN(bn);
		if (bn == 1)
			write0s1(addr, code.read0s1(bi));
		else
			code.copyTo(bi, bytes, addr, bn);
		addr += bn;
	}

	public final void copyFrom(Code code, int ad, int adN)
	{
		reserveN(adN);
		if (adN == 1)
			write0s1(addr, code.readInsS1(ad));
		else
			code.copyInsTo(ad, bytes, addr, adN);
		addr += adN;
	}

	public final void reserveN(int n)
	{
		ensureByteN(addr + n);
	}

	public final void ins0(byte op)
	{
		reserveN(1);
		write0s1(addr++, op);
	}

	public final void insS1(byte op, byte s1)
	{
		reserveN(2);
		write0s1(addr, op);
		write0s1(addr + 1, s1);
		addr += 2;
	}

	public final void insS1(byte op, int s1)
	{
		reserveN(2);
		write0s1(addr, op);
		write0s1(addr + 1, s1);
		addr += 2;
	}

	public final void insU1(byte op, int u1)
	{
		reserveN(2);
		write0s1(addr, op);
		write0u1(addr + 1, u1);
		addr += 2;
	}

	public final void insU2(byte op, int u2)
	{
		reserveN(3);
		write0s1(addr, op);
		write0u2(addr + 1, u2);
		addr += 3;
	}

	public final void insS2(byte op, short s2)
	{
		reserveN(3);
		write0s1(addr, op);
		write0s2(addr + 1, s2);
		addr += 3;
	}

	public final void insS2(byte op, int s2)
	{
		reserveN(3);
		write0s1(addr, op);
		write0s2(addr + 1, s2);
		addr += 3;
	}

	public final void insS4(byte op, int s4)
	{
		reserveN(5);
		write0s1(addr, op);
		write0s4(addr + 1, s4);
		addr += 5;
	}

	public final void insS1U2(byte op, byte s1, int u2)
	{
		reserveN(4);
		write0s1(addr, op);
		write0s1(addr + 1, s1);
		write0u2(addr + 2, u2);
		addr += 4;
	}

	public final void insS1U2(byte op, int s1, int u2)
	{
		reserveN(4);
		write0s1(addr, op);
		write0s1(addr + 1, s1);
		write0u2(addr + 2, u2);
		addr += 4;
	}

	public final void insU1WU2(byte op, int u2)
	{
		if (u2 >> 8 == 0)
			insS1(op, (byte)u2);
		else
			insWideU2(op, u2);
	}

	public final void insU1S1(byte op, int u1, byte s1)
	{
		reserveN(3);
		write0s1(addr, op);
		write0u1(addr + 1, u1);
		write0s1(addr + 2, s1);
		addr += 3;
	}

	public final void insU1S1(byte op, int u1, int s1)
	{
		reserveN(3);
		write0s1(addr, op);
		write0u1(addr + 1, u1);
		write0s1(addr + 2, s1);
		addr += 3;
	}

	public final void insIproc(int iProcCi, int needStackN)
	{
		reserveN(5);
		write0s1(addr, INVOKEINTERFACE);
		write0u2(addr + 1, iProcCi);
		write0u1(addr + 3, needStackN);
		write0s1(addr + 4, (byte)0);
		addr += 5;
	}

	public final void insWideU2(byte op, int u2)
	{
		reserveN(4);
		write0s1(addr, WIDE);
		write0s1(addr + 1, op);
		write0u2(addr + 2, u2);
		addr += 4;
	}

	public final void insWideInc(int localI, int incValue)
	{
		reserveN(6);
		write0s1(addr, WIDE);
		write0s1(addr + 1, IINC);
		write0u2(addr + 2, localI);
		write0s2(addr + 4, incValue);
		addr += 6;
	}

	/** @return jump tag: <code>addr-op</code> */
	public final int insJump2(byte op)
	{
		reserveN(3);
		write0s1(addr, op);
		write0s2(addr + 1, 0);
		addr += 3;
		return addr - 3;
	}

	/** @return jump tag: <code>-addr-op - 1</code> */
	public final int insJump4(byte op)
	{
		reserveN(5);
		write0s1(addr, op);
		write0s4(addr + 1, 0);
		addr += 5;
		return -(addr - 5) - 1;
	}

	public final void jumpTo(int jumpTag)
	{
		jumpTo(jumpTag, addr);
	}

	public final void jumpTo(int jumpTag, int addrTo)
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
		reserveN(bn);
		write0s1(addr, TABLESWITCH);
		Arrays.fill(bytes, addr + 1, addr + bn, (byte)0);
		write0s4(addr + h + 4, low);
		write0s4(addr + h + 8, high0);
		addr += bn;
		return (long)n << 32 | addr - bn & 0xFFFFFFFFL;
	}

	/** @param index negative for default, 0 for low-value ... */
	public final void switchTableTo(long tableTag, int index)
	{
		switchTableTo(tableTag, index, addr);
	}

	/** @param index negative for default, 0 for low-value ... */
	public final void switchTableTo(long tableTag, int index, int addrTo)
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
		reserveN(bn);
		write0s1(addr, LOOKUPSWITCH);
		Arrays.fill(bytes, addr + 1, addr + bn, (byte)0);
		write0s4(addr + h + 4, n);
		addr += bn;
		return (long)n << 32 | addr - bn & 0xFFFFFFFFL;
	}

	public final void switchLookupValue(long lookupTag, int index, int value)
	{
		int n = (int)(lookupTag >> 32);
		int adOp = (int)lookupTag;
		Math2.checkIndex(index, n);
		write0s4(adOp + 4 - (adOp & 3) + 8 + (index << 3), value);
	}

	/** @param index negative for default, 0 for first value ... */
	public final void switchLookupTo(long lookupTag, int index)
	{
		switchTableTo(lookupTag, index, addr);
	}

	/** @param index negative for default, 0 for low-value ... */
	public final void switchLookupTo(long lookupTag, int index, int addrTo)
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
			insU2(GETSTATIC, cons.putField( //
				cons.putClass(cons.putUtf(Element.utf(Class2.internalName(Class2
					.box(c, false))))), //
				cons.putNameDesc(cons.putUtf(TYPE_NAME), //
					cons.putUtf(Element.utf(Class2.descriptor(Class.class))))));
		else
			insU2(LDCW, cons.putClass(cons.putUtf(Element.utf(Class2.internalName(c)))));
	}

	/** Stack: primitive value(s) or object --> object */
	public final void insBoxing(Constants cons, Class<?> c)
	{
		if ( !c.isPrimitive())
			return;
		Class<?> b = Class2.box(c, false);
		insU2(INVOKESTATIC, cons.putCproc( //
			cons.putClass(cons.putUtf(Element.utf(Class2.internalName(b)))), //
			cons.putNameDesc(cons.putUtf(VALUEOF_NAME), //
				cons.putUtf(Element.chars2Utf('(' + Class2.descriptor(c) + ')'
					+ Class2.descriptor(b))))));
	}

	/** Stack: object --> primitive value(s) or narrowed object */
	public final void insUnboxingNarrow(Constants cons, Class<?> c)
	{
		if (c.isPrimitive())
		{
			Class<?> b = Class2.box(c, false);
			Bytes bUtf = Element.utf(Class2.internalName(b));
			insU2(CHECKCAST, cons.putClass(cons.putUtf(bUtf)));
			insU2(INVOKEVIRTUAL, cons.putCproc( //
				cons.putClass(cons.putUtf(bUtf)), //
				cons.putNameDesc(cons.putUtf(Element.chars2Utf(c.getName() + "Value")), //
					cons.putUtf(Element.chars2Utf("()" + Class2.descriptor(c))))));
		}
		else if (c != Object.class)
			insU2(CHECKCAST, cons.putClass(cons.putUtf(Element.utf(Class2.internalName(c)))));
	}
}
