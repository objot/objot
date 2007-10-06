//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.aspect;

import java.lang.reflect.Method;

import objot.aspect.Aspect.Target;
import objot.bytecode.Bytecode;
import objot.bytecode.Code;
import objot.bytecode.CodeCatchs;
import objot.bytecode.CodeLines;
import objot.bytecode.CodeVars;
import objot.bytecode.Constants;
import objot.bytecode.Element;
import objot.bytecode.Instruction;
import objot.bytecode.Opcode;
import objot.bytecode.Procedure;
import objot.util.Bytes;
import objot.util.Class2;
import objot.util.Mod2;

import static objot.bytecode.Opcode.*;


final class WeaveProc
{
	private Bytecode y;
	private Constants cons;
	private Procedure ap;
	private Code ac;
	private Procedure wp;
	private Code wc;
	private Instruction ws;
	private int nameStrCi;
	private int descStrCi;
	private int nameDescStrCi;

	WeaveProc(Bytecode y_, Procedure ap_)
	{
		y = y_;
		cons = y.cons;
		ap = ap_;
		ac = ap.getCode();
	}

	void method(Method m, int pi, int ossCi)
	{
		wp = new Procedure(cons, ap.bytes, ap.beginBi);
		wp.setModifier(Mod2.PUBLIC);
		wp.setNameCi(cons.addUcs(m.getName()));
		wp.setDescCi(cons.addUcs(Class2.descript(m)));
		y.getProcs().addProc(wp);
		wc = wp.getCode();

		int[] ads = new int[ac.getAddrN() + 1];
		ws = new Instruction(ac.getAddrN() + 200);
		for (int ad = 0, adn; ad < ac.getAddrN(); ad += adn)
		{
			ads[ad] = ws.addr;
			adn = ac.getInsAddrN(ad);
			if ( !opLocal(ad) && !opReturn(ad) && !opAspect(ad, pi, ossCi))
				opCopy(ad, adn);
		}
		ads[ac.getAddrN()] = ws.addr;
		for (int ad = 0; ad < ac.getAddrN(); ad += ac.getInsAddrN(ad))
			opJump(ad, ads);
		wc.setIns(ws, false);
		wc.setLocalN(wc.getLocalN() + wp.getParamLocalN() + 2 /* return value */);

		CodeCatchs cc = wc.getCatchs();
		for (int i = 0; cc != null && i < cc.getCatchN(); i++)
			cc.setInfo(i, ads[cc.getBeginAd(i)], ads[cc.getEnd1Ad(i)], ads[cc.getCatchAd(i)],
				cc.getTypeCi(i));
		CodeLines cl = wc.getLines();
		for (int i = 0; cl != null && i < cl.getLineN(); i++)
			cl.setInfo(i, ads[cl.getBeginAd(i)], cl.getLine(i));
		CodeVars cv = wc.getVars();
		for (int i = 0, b; cv != null && i < cv.getVarN(); i++)
			cv.setInfo(i, b = ads[cv.getBeginAd(i)], ads[cv.getEnd1Ad(i)] - b, //
				cv.getNameCi(i), cv.getDescCi(i), //
				cv.getLocal(i) == 0 ? 0 : cv.getLocal(i) + wp.getParamLocalN());
		cv = wc.getVarSigns();
		for (int i = 0, b; cv != null && i < cv.getVarN(); i++)
			cv.setInfo(i, b = ads[cv.getBeginAd(i)], ads[cv.getEnd1Ad(i)] - b, //
				cv.getNameCi(i), cv.getDescCi(i), //
				cv.getLocal(i) == 0 ? 0 : cv.getLocal(i) + wp.getParamLocalN());
	}

	private boolean opLocal(int ad)
	{
		int inc = Integer.MAX_VALUE;
		byte op = Opcode.getNormalLocalOp(ac, ad);
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
			break;
		case IINC:
			inc = ac.getInsS1(ad) != WIDE ? ac.getInsS1(ad + 2) : ac.getInsS2(ad + 4);
			break;
		default:
			return false;
		}
		int i = Opcode.getLocalIndex(ac, ad);
		if (i > 0)
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
		byte op = ac.getInsS1(ad);
		if (op != RETURN)
			return false;
		if (wp.getReturnTypeChar() != 'V')
			ws.insU1wU2(Opcode.getLoadOp(wp.getReturnTypeChar()), ac.getLocalN()
				+ wp.getParamLocalN());
		ws.ins0(Opcode.getReturnOp(wp.getReturnTypeChar()));
		return true;
	}

	private boolean opAspect(int ad, int pi, int ossCi)
	{
		if (ac.getInsS1(ad) != INVOKESTATIC)
			return false;
		int ci = ac.getInsU2(ad + 1);
		if ( !cons.equalsUtf(cons.getCprocClass(ci), Weaver.TARGET_NAME))
			return false;
		Bytes name = cons.getUtf(cons.getCprocName(ci));
		if (Target.getData.utf.equals(name))
			opGetData(pi, ossCi);
		else if (Target.getName.utf.equals(name))
			opGetName();
		else if (Target.getDescript.utf.equals(name))
			opGetDescript();
		else if (Target.getNameDescript.utf.equals(name))
			opGetNameDescript();
		else if (Target.getClazz.utf.equals(name))
			opGetClazz();
		else if (Target.invoke.utf.equals(name))
			opInvoke();
		else
			throw new AssertionError();
		return true;
	}

	private void opCopy(int ad, int adn)
	{
		byte op = ac.getInsS1(ad);
		if (op == LOOKUPSWITCH || op == TABLESWITCH)
		{
			ws.ins0(op);
			ws.insU2((byte)0, 0);
			int h = 4 - (ad & 3);
			ws.addr = ws.addr - (ws.addr & 3);
			ws.copyFrom(ac, ad + h, ac.getInsAddrN(ad) - h);
		}
		else
			ws.copyFrom(ac, ad, adn);
	}

	private void opGetData(int pi, int ossCi)
	{
		ws.insU2(GETSTATIC, ossCi);
		ws.insS2(SIPUSH, pi);
		ws.ins0(AALOAD);
		wc.setStackN(wc.getStackN() + 1);
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

	private void opGetNameDescript()
	{
		if (nameDescStrCi <= 0)
		{
			int n = wp.getNameCi();
			int d = wp.getDescCi();
			Bytes b = new Bytes(new byte[cons.getUtfByteN(n) + cons.getUtfByteN(d)]);
			cons.getUtfTo(n, b, 0);
			cons.getUtfTo(d, b, cons.getUtfByteN(n));
			nameDescStrCi = cons.addString(cons.addUtf(b));
		}
		ws.insU2(LDCW, nameDescStrCi);
	}

	private void opGetClazz()
	{
		ws.insU2(LDCW, y.head.getSuperCi());
	}

	private void opInvoke()
	{
		ws.ins0(ALOAD0);
		Bytes desc = cons.getUtf(wp.getDescCi());
		for (int i = 1, b = desc.beginBi + 1; desc.bytes[b] != ')';)
		{
			ws.insU1wU2(Opcode.getLoadOp((char)desc.bytes[b]), i);
			i += Opcode.getLocalStackN((char)desc.bytes[b]);
			b += Element.typeDescByteN(desc, b - desc.beginBi);
		}
		ws.insU2(INVOKESPECIAL, y.cons.addCproc(y.head.getSuperCi(), y.cons.addNameDesc(wp
			.getNameCi(), wp.getDescCi())));
		if (wp.getReturnTypeChar() != 'V')
			ws.insU1wU2(Opcode.getStoreOp(wp.getReturnTypeChar()), ac.getLocalN()
				+ wp.getParamLocalN());
		wc.setStackN(wc.getStackN() + 2 /* e.g. return long */+ wp.getParamLocalN());
	}

	private void opJump(int ad, int[] ads)
	{
		switch (ac.getInsS1(ad))
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
			ws.writeS2(ads[ad] + 1, ads[ad + ac.getInsS2(ad + 1)] - ads[ad]);
			break;
		case GOTO4:
		case JSR4:
			ws.writeS4(ads[ad] + 1, ads[ad + ac.getInsS4(ad + 1)] - ads[ad]);
			break;
		case LOOKUPSWITCH:
		{
			int a = ad + 4 - (ad & 3);
			int wa = ads[ad] + 4 - (ads[ad] & 3);
			ws.writeS4(wa, ads[ad + ac.getInsS4(a)] - ads[ad]);
			int n = ac.getInsU4(a + 4);
			a += 12;
			wa += 12;
			for (; n > 0; n--, a += 8, wa += 8)
				ws.writeS4(wa, ads[ad + ac.getInsS4(a)] - ads[ad]);
			break;
		}
		case TABLESWITCH:
		{
			int a = ad + 4 - (ad & 3);
			int wa = ads[ad] + 4 - (ads[ad] & 3);
			ws.writeS4(wa, ads[ad + ac.getInsS4(a)] - ads[ad]);
			int n = ac.getInsS4(a + 8) - ac.getInsS4(a + 4) + 1;
			a += 12;
			wa += 12;
			for (; n > 0; n--, a += 4, wa += 4)
				ws.writeS4(wa, ads[ad + ac.getInsS4(a)] - ads[ad]);
			break;
		}
		}
	}
}
