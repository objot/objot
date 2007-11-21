//
// Copyright 2007 Qianyan Cai
// Under the terms of the GNU General Public License version 2
//
package objot.aspect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import objot.aspect.Aspect.Targ;
import objot.bytecode.Annotation;
import objot.bytecode.Bytecode;
import objot.bytecode.Code;
import objot.bytecode.CodeCatchs;
import objot.bytecode.CodeLines;
import objot.bytecode.CodeVars;
import objot.bytecode.Constants;
import objot.bytecode.Instruction;
import objot.bytecode.Opcode;
import objot.bytecode.Procedure;
import objot.container.Inject;
import objot.util.Bytes;
import objot.util.Class2;
import objot.util.Mod2;

import static objot.bytecode.Opcode.*;


final class WeaveProc
{
	private Bytes targetName;
	private Bytecode y;
	private Constants cons;
	private int clazzCi;
	private Code ao;
	private Procedure wp;
	private Class<?> returnCla;
	private Code wo;
	private Instruction ws;
	private int nameStrCi;
	private int descStrCi;
	private int targetStrCi;

	WeaveProc(Class<?> target, Bytecode y_, Code ao_)
	{
		targetName = Bytecode.utf(target.getName() + ".");
		y = y_;
		cons = y.cons;
		clazzCi = cons.addClass(target);
		ao = ao_;
		wp = new Procedure(cons);
		wo = new Code(cons, ao.bytes, ao.beginBi);
		wp.setCode(wo);
	}

	void method(Method m, int pi, int datasCi)
	{
		wp.setModifier(m.getModifiers() & Mod2.PUBLIC_PROTECT);
		wp.setNameCi(cons.addUcs(m.getName()));
		wp.setDescCi(cons.addUcs(Class2.descript(m)));
		returnCla = m.getReturnType();

		int[] ads = new int[ao.getAddrN() + 1];
		ws = new Instruction(cons, ao.getAddrN() + 200);
		for (int ad = 0, adn; ad < ao.getAddrN(); ad += adn)
		{
			ads[ad] = ws.addr;
			adn = ao.getInsAddrN(ad);
			if ( !opLocal(ad) && !opReturn(ad) && !opAspect(ad, pi, datasCi))
				opCopy(ad, adn);
		}
		ads[ao.getAddrN()] = ws.addr;
		wo.setLocalN(wo.getLocalN() + wp.getParamLocalN() + 2 /* return value */);
		post(ads);
	}

	void ctor(Constructor<?> t)
	{
		wp.setModifier(t.getModifiers() & Mod2.PUBLIC_PROTECT);
		wp.setNameCi(cons.addUtf(Weaver.CTOR_NAME));
		wp.setDescCi(cons.addUcs(Class2.descript(t)));
		returnCla = void.class;

		int[] ads = new int[ao.getAddrN() + 1];
		ws = new Instruction(cons, ao.getAddrN() + 200);
		for (int ad = 0, adn; ad < ao.getAddrN(); ad += adn)
		{
			ads[ad] = ws.addr;
			adn = ao.getInsAddrN(ad);
			if ( !opLocal(ad) && !opCtor(ad))
				opCopy(ad, adn);
		}
		ads[ao.getAddrN()] = ws.addr;
		wo.setLocalN(wo.getLocalN() + wp.getParamLocalN());
		post(ads);
		if (t.isAnnotationPresent(Inject.class))
		{
			byte[] inject = new byte[4];
			Bytes.writeU2(inject, 0, cons.addClass(Inject.class));
			wp.getAnnos().addAnno(new Annotation(cons, inject, 0));
		}
	}

	void post(int[] ads)
	{
		for (int ad = 0; ad < ao.getAddrN(); ad += ao.getInsAddrN(ad))
			opJump(ad, ads);
		wo.setIns(ws, false);
		y.getProcs().addProc(wp);

		CodeCatchs cc = wo.getCatchs();
		for (int i = 0; cc != null && i < cc.getCatchN(); i++)
			cc.setInfo(i, ads[cc.getBeginAd(i)], ads[cc.getEnd1Ad(i)], ads[cc.getCatchAd(i)],
				cc.getTypeCi(i));
		CodeLines cl = wo.getLines();
		for (int i = 0; cl != null && i < cl.getLineN(); i++)
			cl.setInfo(i, ads[cl.getBeginAd(i)], cl.getLine(i));
		CodeVars cv = wo.getVars();
		for (int i = 0, b; cv != null && i < cv.getVarN(); i++)
			cv.setInfo(i, b = ads[cv.getBeginAd(i)], ads[cv.getEnd1Ad(i)] - b, //
				cv.getNameCi(i), cv.getDescCi(i), //
				cv.getLocal(i) == 0 ? 0 : cv.getLocal(i) + wp.getParamLocalN());
		cv = wo.getVarSigns();
		for (int i = 0, b; cv != null && i < cv.getVarN(); i++)
			cv.setInfo(i, b = ads[cv.getBeginAd(i)], ads[cv.getEnd1Ad(i)] - b, //
				cv.getNameCi(i), cv.getDescCi(i), //
				cv.getLocal(i) == 0 ? 0 : cv.getLocal(i) + wp.getParamLocalN());
	}

	private boolean opLocal(int ad)
	{
		int inc = Integer.MAX_VALUE;
		byte op = Opcode.getNormalLocalOp(ao, ad);
		switch (op)
		{
		case LLOAD:
		case DLOAD:
		case ALOAD:
		case ILOAD:
		case FLOAD:
		case LSTORE:
		case DSTORE:
		case ASTORE:
		case ISTORE:
		case FSTORE:
		case RET:
			break;
		case IINC:
			inc = ao.getInsS1(ad) != WIDE ? ao.getInsS1(ad + 2) : ao.getInsS2(ad + 4);
			break;
		default:
			return false;
		}
		int i = Opcode.getLocalIndex(ao, ad);
		if (i == 0)
			ws.ins0(ALOAD0);
		else
		{
			i += wp.getParamLocalN();
			if (inc != Integer.MAX_VALUE)
				ws.insWideInc(i, inc);
			else
				ws.insU1wU2(op, i);
		}
		return true;
	}

	private boolean opReturn(int ad)
	{
		byte op = ao.getInsS1(ad);
		if (op != RETURN)
			return false;
		if (wp.getReturnTypeChar() != 'V')
			ws.insU1wU2(Opcode.getLoadOp(wp.getReturnTypeChar()), ao.getLocalN()
				+ wp.getParamLocalN());
		ws.ins0(Opcode.getReturnOp(wp.getReturnTypeChar()));
		return true;
	}

	private boolean opAspect(int ad, int pi, int datasCi)
	{
		if (ao.getInsS1(ad) != INVOKESTATIC)
			return false;
		int ci = ao.getInsU2(ad + 1);
		if ( !cons.equalsUtf(cons.getCprocClass(ci), Weaver.TARGET_NAME))
			return false;
		Bytes name = cons.getUtf(cons.getCprocName(ci));
		if (Targ.getData.utf.equals(name))
			opGetData(pi, datasCi);
		else if (Targ.getName.utf.equals(name))
			opGetName();
		else if (Targ.getDescript.utf.equals(name))
			opGetDescript();
		else if (Targ.getTarget.utf.equals(name))
			opGetTarget();
		else if (Targ.getThis.utf.equals(name))
			opGetThis();
		else if (Targ.getClazz.utf.equals(name))
			opGetClazz();
		else if (Targ.invoke.utf.equals(name))
			opInvoke();
		else if (Targ.getReturnClass.utf.equals(name))
			opGetReturnClass();
		else if (Targ.getReturn.utf.equals(name))
			opGetReturn();
		else if (Targ.setReturn.utf.equals(name))
			opSetReturn();
		else
			throw new AssertionError();
		return true;
	}

	private void opCopy(int ad, int adn)
	{
		byte op = ao.getInsS1(ad);
		if (op == LOOKUPSWITCH || op == TABLESWITCH)
		{
			ws.ins0(op);
			ws.insU2((byte)0, 0);
			int h = 4 - (ad & 3);
			ws.addr = ws.addr - (ws.addr & 3);
			ws.copyFrom(ao, ad + h, ao.getInsAddrN(ad) - h);
		}
		else
			ws.copyFrom(ao, ad, adn);
	}

	private void opGetData(int pi, int datasCi)
	{
		ws.insU2(GETSTATIC, datasCi);
		ws.insS2(SIPUSH, pi);
		ws.ins0(AALOAD);
		wo.setStackN(wo.getStackN() + 1);
	}

	private void opGetName()
	{
		if (nameStrCi <= 0)
			nameStrCi = cons.addString(wp.getNameCi());
		ws.insU2(LDCW, nameStrCi);
	}

	private void opGetDescript()
	{
		if (descStrCi <= 0)
			descStrCi = cons.addString(wp.getDescCi());
		ws.insU2(LDCW, descStrCi);
	}

	private void opGetTarget()
	{
		if (targetStrCi <= 0)
		{
			Bytes b = new Bytes(targetName);
			int n = wp.getNameCi();
			int d = wp.getDescCi();
			b.addByteN(cons.getUtfByteN(n) + cons.getUtfByteN(d));
			cons.getUtfTo(n, b, targetName.byteN());
			cons.getUtfTo(d, b, targetName.byteN() + cons.getUtfByteN(n));
			targetStrCi = cons.addString(cons.addUtf(b));
		}
		ws.insU2(LDCW, targetStrCi);
	}

	private void opGetThis()
	{
		ws.ins0(ALOAD0);
	}

	private void opGetClazz()
	{
		ws.insU2(LDCW, clazzCi);
	}

	private void opInvoke()
	{
		ws.ins0(ALOAD0);
		Bytes desc = cons.getUtf(wp.getDescCi());
		for (int i = 1, b = desc.beginBi + 1; desc.bytes[b] != ')';)
		{
			ws.insU1wU2(Opcode.getLoadOp((char)desc.bytes[b]), i);
			i += Opcode.getLocalStackN((char)desc.bytes[b]);
			b += Bytecode.typeDescByteN(desc, b - desc.beginBi);
		}
		ws.insU2(INVOKESPECIAL, cons.addCproc(y.head.getSuperCi(), cons.addNameDesc(
			wp.getNameCi(), wp.getDescCi())));
		if (wp.getReturnTypeChar() != 'V')
			ws.insU1wU2(Opcode.getStoreOp(wp.getReturnTypeChar()), ao.getLocalN()
				+ wp.getParamLocalN());
		wo.setStackN(wo.getStackN() + 2 /* e.g. return long */+ wp.getParamLocalN());
	}

	private void opGetReturnClass()
	{
		ws.insLoad(returnCla);
	}

	private void opGetReturn()
	{
		if (returnCla == void.class)
			ws.ins0(ACONSTNULL);
		else
			ws.insU1wU2(Opcode.getLoadOp(wp.getReturnTypeChar()), ao.getLocalN()
				+ wp.getParamLocalN());
		ws.insBox(returnCla);
	}

	private void opSetReturn()
	{
		ws.insUnboxNarrow(returnCla);
		if (returnCla == void.class)
			ws.ins0(POP);
		else
			ws.insU1wU2(Opcode.getStoreOp(wp.getReturnTypeChar()), ao.getLocalN()
				+ wp.getParamLocalN());
	}

	private boolean opCtor(int ad)
	{
		if (ao.getInsS1(ad) != INVOKESPECIAL)
			return false;
		int ci = ao.getInsU2(ad + 1);
		if ( !cons.equalsUtf(cons.getCprocClass(ci), Weaver.ASPECT_NAME))
			return false;
		Bytes desc = cons.getUtf(wp.getDescCi());
		for (int i = 1, b = desc.beginBi + 1; desc.bytes[b] != ')';)
		{
			ws.insU1wU2(Opcode.getLoadOp((char)desc.bytes[b]), i);
			i += Opcode.getLocalStackN((char)desc.bytes[b]);
			b += Bytecode.typeDescByteN(desc, b - desc.beginBi);
		}
		ws.insU2(INVOKESPECIAL, cons.addCproc(y.head.getSuperCi(), cons.addNameDesc(
			wp.getNameCi(), wp.getDescCi())));
		wo.setStackN(wo.getStackN() + wp.getParamLocalN());
		return true;
	}

	private void opJump(int ad, int[] ads)
	{
		switch (ao.getInsS1(ad))
		{
		case GOTO:
		case IFAE:
		case IFAN:
		case IFIE:
		case IFIE0:
		case IFIG:
		case IFIG0:
		case IFIGE:
		case IFIGE0:
		case IFIL:
		case IFIL0:
		case IFILE:
		case IFILE0:
		case IFIN:
		case IFIN0:
		case IFNOTNULL:
		case IFNULL:
		case JSR:
			ws.writeS2(ads[ad] + 1, ads[ad + ao.getInsS2(ad + 1)] - ads[ad]);
			break;
		case GOTO4:
		case JSR4:
			ws.writeS4(ads[ad] + 1, ads[ad + ao.getInsS4(ad + 1)] - ads[ad]);
			break;
		case LOOKUPSWITCH:
		{
			int a = ad + 4 - (ad & 3);
			int wa = ads[ad] + 4 - (ads[ad] & 3);
			ws.writeS4(wa, ads[ad + ao.getInsS4(a)] - ads[ad]);
			int n = ao.getInsU4(a + 4);
			a += 12;
			wa += 12;
			for (; n > 0; n--, a += 8, wa += 8)
				ws.writeS4(wa, ads[ad + ao.getInsS4(a)] - ads[ad]);
			break;
		}
		case TABLESWITCH:
		{
			int a = ad + 4 - (ad & 3);
			int wa = ads[ad] + 4 - (ads[ad] & 3);
			ws.writeS4(wa, ads[ad + ao.getInsS4(a)] - ads[ad]);
			int n = ao.getInsS4(a + 8) - ao.getInsS4(a + 4) + 1;
			a += 12;
			wa += 12;
			for (; n > 0; n--, a += 4, wa += 4)
				ws.writeS4(wa, ads[ad + ao.getInsS4(a)] - ads[ad]);
			break;
		}
		}
	}
}
