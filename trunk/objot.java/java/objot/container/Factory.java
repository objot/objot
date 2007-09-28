//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.container;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import objot.bytecode.Bytecode;
import objot.bytecode.Instruction;
import objot.bytecode.Opcode;
import objot.bytecode.Procedure;
import objot.util.Class2;
import objot.util.Mod2;


public final class Factory
{
	final Container con;

	public Factory(Binder ber) throws Exception
	{
		Bind[] bs = ber.toArray();
		String name = Container.class.getName() + "$$" + hashCode();
		Bytecode y = new Bytecode();
		int superCi = y.cons.addClass(Container.class);
		y.head.setModifier(Modifier.FINAL | Mod2.SYNTHETIC);
		y.head.setClassCi(y.cons.addClass(y.cons.addUcs(Class2.pathName(name))));
		y.head.setSuperCi(superCi);
		y.getProcs().addProc(Procedure.addCtor(y.cons, superCi, 0, Class2.descript(name)));

		makeCreateTop(y);
		makeIndex(y, bs);
		makeGet0(y, bs);

		con = (Container)Class2.load(Container.class.getClassLoader(), name, y.normalize())
			.getDeclaredConstructors()[0].newInstance((Object)null);
	}

	public Container container()
	{
		return con.createTop();
	}

	private void makeCreateTop(Bytecode y)
	{
		Procedure ctor = y.getProcs().getProc(0);
		Procedure p = new Procedure(y.cons);
		p.setModifier(Modifier.FINAL);
		p.setNameCi(p.cons.addUtf(Container.NAME_createTop));
		p.setDescCi(p.cons.addUtf(Container.DESC_createTop));
		Instruction s = new Instruction(6);
		s.insU2(Opcode.NEW, y.head.getClassCi());
		s.ins0(Opcode.DUP);
		s.ins0(Opcode.ACONSTNULL);
		s.insU2(Opcode.INVOKESPECIAL, p.cons.addCproc(y.head.getClassCi(), p.cons
			.addNameDesc(ctor.getNameCi(), ctor.getDescCi())));
		s.ins0(Opcode.ARETURN);
		p.getCode().setIns(s, false);
		p.getCode().setLocalN(1);
		p.getCode().setStackN(3);
		y.getProcs().addProc(p);
	}

	private static final Method M_hashCode = Class2.declaredMethod1(Object.class, "hashCode");

	private void makeIndex(Bytecode y, Bind[] bs)
	{
		Procedure p = new Procedure(y.cons);
		p.setModifier(Modifier.FINAL);
		p.setNameCi(p.cons.addUtf(Container.NAME_index));
		p.setDescCi(p.cons.addUtf(Container.DESC_index));
		Instruction s = new Instruction(250);
		s.ins0(Opcode.ALOAD1); // class
		s.ins0(Opcode.DUP); // class
		s.insU2(Opcode.INVOKEVIRTUAL, p.cons.addProc(M_hashCode)); // hash code
		long sw = s.insSwitchTable(0, 14);
		for (int i = 0; i <= 14; i++)
		{
			s.switchTableFrom(sw, i);
			Bind b;
			for (int j = 0; j < bs.length; j++)
				if ((b = bs[j]).c.hashCode() % 15 == i)
				{
					s.ins0(Opcode.DUP); // class
					s.insU2(Opcode.LDC, p.cons.addClass(b.c));
					s.insS2(Opcode.IFAE, 7);
					s.insS2(Opcode.SIPUSH, j);
					s.ins0(Opcode.IRETURN);
				}
			s.ins0(Opcode.ICONSTm1);
			s.ins0(Opcode.IRETURN);
		}
		s.switchTableFrom(sw, -1);
		s.ins0(Opcode.ICONSTm1);
		s.ins0(Opcode.IRETURN);
		p.getCode().setIns(s, false);
		p.getCode().setLocalN(2);
		p.getCode().setStackN(3);
		y.getProcs().addProc(p);
	}

	private void makeGet0(Bytecode y, Bind[] bs)
	{
		Procedure p = new Procedure(y.cons);
		p.setModifier(Modifier.FINAL);
		p.setNameCi(p.cons.addUtf(Container.NAME_get0));
		p.setDescCi(p.cons.addUtf(Container.DESC_get0));
		Instruction s = new Instruction(250);
		s.ins0(Opcode.ALOAD1); // class
		s.ins0(Opcode.DUP); // class
		s.insU2(Opcode.INVOKEVIRTUAL, p.cons.addProc(M_hashCode)); // hash code
		long sw = s.insSwitchTable(0, 14);
		for (int i = 0; i <= 14; i++)
		{
			s.switchTableFrom(sw, i);
			Bind b;
			for (int j = 0; j < bs.length; j++)
				if ((b = bs[j]).c.hashCode() % 15 == i)
				{
					s.ins0(Opcode.DUP); // class
					s.insU2(Opcode.LDC, p.cons.addClass(b.c));
					s.insS2(Opcode.IFAE, 7);
					s.insS2(Opcode.SIPUSH, j);
					s.ins0(Opcode.IRETURN);
				}
			s.ins0(Opcode.ICONSTm1);
			s.ins0(Opcode.IRETURN);
		}
		s.switchTableFrom(sw, -1);
		s.ins0(Opcode.ICONSTm1);
		s.ins0(Opcode.IRETURN);
		p.getCode().setIns(s, false);
		p.getCode().setLocalN(2);
		p.getCode().setStackN(3);
		y.getProcs().addProc(p);
	}
}
