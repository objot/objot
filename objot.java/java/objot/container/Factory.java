//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.container;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import objot.bytecode.Bytecode;
import objot.bytecode.Field;
import objot.bytecode.Instruction;
import objot.bytecode.Opcode;
import objot.bytecode.Procedure;
import objot.util.Class2;
import objot.util.Mod2;


public final class Factory
{
	final Container con;
	Bind[] bs;
	Object[][] oss;

	public Factory(Binder ber) throws Exception
	{
		bs = ber.toArray();
		oss = new Object[bs.length][];
		for (int i = 0; i < bs.length; i++)
			oss[i] = bs[i].os;

		String name = Container.class.getName() + "$$" + hashCode();
		Bytecode y = new Bytecode();
		int superCi = y.cons.addClass(Container.class);
		y.head.setModifier(Modifier.FINAL | Mod2.SYNTHETIC);
		y.head.setClassCi(y.cons.addClass(y.cons.addUcs(Class2.pathName(name))));
		y.head.setSuperCi(superCi);
		y.getProcs().addProc(Procedure.addCtor(y.cons, superCi, 0, Class2.descript(name)));

		int[] fCis = new int[bs.length];
		int ossCi = y.cons.addField(Container.F_objss);
		makeFields(y, fCis);
		makeCreateTop(y, ossCi);
		makeIndex(y);
		makeGet0(y, fCis, ossCi);

		con = (Container)Class2.load(Container.class.getClassLoader(), name, y.normalize())
			.getDeclaredConstructors()[0].newInstance((Object)null, oss);
		bs = null;
		oss = null;
	}

	public Container container()
	{
		return con.createTop();
	}

	/** @return index of a {@link Bind} which {@link Bind#b} is self or object */
	private int bind(int bx)
	{
		Bind b = bs[bx];
		if (b.b != b && b.b instanceof Bind)
			for (int i = bs.length - 1; i >= 0; i--)
				if (bs[i] == b)
					return i;
		return bx;
	}

	private void makeFields(Bytecode y, int[] fCis)
	{
		for (int i = 0; i < bs.length; i++)
			if (bs[i].b == bs[i])
			{
				Field f = new Field(y.cons);
				f.setNameCi(y.cons.addUcs("o" + i));
				f.setDescCi(y.cons.addUcs(Class2.descript(bs[i].c)));
				y.getFields().addField(f);
				fCis[i] = y.cons.addField(y.head.getClassCi(), y.cons.addField(f.getNameCi(),
					f.getDescCi()));
			}
	}

	private void makeCreateTop(Bytecode y, int ossCi)
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
		s.ins0(Opcode.ALOAD0);
		s.insU2(Opcode.GETFIELD, ossCi);
		s.insU2(Opcode.INVOKESPECIAL, p.cons.addCproc(y.head.getClassCi(), p.cons
			.addNameDesc(ctor.getNameCi(), ctor.getDescCi())));
		s.ins0(Opcode.ARETURN);
		p.getCode().setIns(s, false);
		p.getCode().setLocalN(1);
		p.getCode().setStackN(4);
		y.getProcs().addProc(p);
	}

	private static final Method M_hashCode = Class2.declaredMethod1(Object.class, "hashCode");

	private void makeIndex(Bytecode y)
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
			for (int j = 0; j < bs.length; j++)
				if (bs[j].c.hashCode() % 15 == i)
				{
					s.ins0(Opcode.DUP); // class
					s.insU2(Opcode.LDC, p.cons.addClass(bs[j].c));
					s.insS2(Opcode.IFAN, 7);
					s.insS2(Opcode.SIPUSH, bind(j));
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

	private void makeGet0(Bytecode y, int[] fCis, int ossCi)
	{
		Procedure p = new Procedure(y.cons);
		p.setModifier(Modifier.FINAL);
		p.setNameCi(p.cons.addUtf(Container.NAME_get0));
		p.setDescCi(p.cons.addUtf(Container.DESC_get0));
		Instruction s = new Instruction(250);
		s.ins0(Opcode.ALOAD0);
		s.ins0(Opcode.ALOAD1); // index
		s.ins0(Opcode.DUP); // index
		long sw = s.insSwitchTable(0, bs.length - 1);
		for (int i = 0; i < bs.length; i++)
		{
			s.switchTableFrom(sw, i);
			Bind b = bs[i];
			if (b.c == Container.class)
				;
			else if (b.b instanceof Bind)
				if (b.b == b)
				{

				}
				else
				{

				}
			else
			{
				s.insU2(Opcode.GETFIELD, ossCi);
				s.insS2(Opcode.SIPUSH, i);
				s.ins0(Opcode.AALOAD);
				s.ins0(Opcode.ICONST0);
				s.ins0(Opcode.AALOAD); // oss[i][0]
			}
			s.ins0(Opcode.ARETURN);
		}
		s.switchTableFrom(sw, -1);
		s.ins0(Opcode.ARETURN); // never happen
		p.getCode().setIns(s, false);
		p.getCode().setLocalN(2);
		p.getCode().setStackN(3);
		y.getProcs().addProc(p);
	}

	private void makeCreate0(Bytecode y)
	{
		Procedure p = new Procedure(y.cons);
		p.setModifier(Modifier.FINAL);
		p.setNameCi(p.cons.addUtf(Container.NAME_create0));
		p.setDescCi(p.cons.addUtf(Container.DESC_create0));
		Instruction s = new Instruction(250);
		s.ins0(Opcode.ALOAD1); // index
		s.ins0(Opcode.DUP); // index
		long sw = s.insSwitchTable(0, bs.length - 1);
		p.getCode().setIns(s, false);
		p.getCode().setLocalN(2);
		p.getCode().setStackN(3);
		y.getProcs().addProc(p);
	}
}
