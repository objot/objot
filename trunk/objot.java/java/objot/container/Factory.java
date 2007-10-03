//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.container;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import objot.bytecode.Bytecode;
import objot.bytecode.Constants;
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
		y.head.setClassCi(y.cons.addClass(name));
		y.head.setSuperCi(superCi);
		y.getProcs().addProc(Procedure.addCtor0(y.cons, superCi, 0));

		int get0Ci = y.cons.addProc(Container.M_get0);
		int create0Ci = y.cons.addProc(Container.M_create0);
		int outCi = y.cons.addField(Container.F_outer);
		int ossCi = y.cons.addField(Container.F_objss);
		int[] fCis = new int[bs.length];
		makeFields(y, fCis);
		makeIndex(y);
		makeGet0(y, get0Ci, create0Ci, outCi, ossCi, fCis);
		makeCreate0(y, get0Ci, create0Ci, outCi, ossCi, fCis);

		con = Class2.<Container>load(Container.class.getClassLoader(), name, y.normalize())
			.newInstance();
		con.objss = oss;
		bs = null;
		oss = null;
	}

	public Container container()
	{
		try
		{
			Container c = con.create(Container.class);
			c.outer = null;
			return c;
		}
		catch (Exception e)
		{
			throw new AssertionError(e);
		}
	}

	/** @return index of a {@link Bind} which {@link Bind#b} is self or object */
	private int bind(int bx)
	{
		Bind b = bs[bx];
		if (b.b != b && b.b instanceof Bind)
			for (int i = bs.length - 1; i >= 0; i--)
				if (bs[i] == b.b)
					return i;
		return bx;
	}

	/** @return index of a {@link Bind} which {@link Bind#b} is self or object */
	private int bind(Bind b)
	{
		if (b.b != b && b.b instanceof Bind)
			for (int i = bs.length - 1; i >= 0; i--)
				if (bs[i] == b.b)
					return i;
		for (int i = bs.length - 1; i >= 0; i--)
			if (bs[i] == b)
				return i;
		throw new AssertionError();
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
				fCis[i] = y.cons.addField(y.head.getClassCi(), y.cons.addNameDesc(f
					.getNameCi(), f.getDescCi()));
			}
	}

	private static final Method M_hashCode = Class2.declaredMethod1(Object.class, "hashCode");

	private void makeIndex(Bytecode y)
	{
		Procedure p = new Procedure(y.cons);
		p.setModifier(Modifier.FINAL);
		p.setNameCi(p.cons.addUtf(Container.NAME_index));
		p.setDescCi(p.cons.addUtf(Container.DESC_index));
		Instruction s = new Instruction(500);
		s.ins0(Opcode.ALOAD1); // class
		s.ins0(Opcode.DUP); // class
		s.insU2(Opcode.INVOKEVIRTUAL, p.cons.addProc(M_hashCode)); // hash code
		s.insS1(Opcode.BIPUSH, 15);
		s.ins0(Opcode.IREM);
		long sw = s.insSwitchTable(0, 14);
		for (int i = 0; i <= 14; i++)
		{
			s.switchTableFrom(sw, i);
			for (int j = 0; j < bs.length; j++)
				if (bs[j].c.hashCode() % 15 == i)
				{
					s.ins0(Opcode.DUP); // class
					s.insU2(Opcode.LDCW, p.cons.addClass(bs[j].c));
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

	private void makeGet0(Bytecode y, int get0Ci, int create0Ci, int outCi, int ossCi,
		int[] fCis)
	{
		Procedure p = new Procedure(y.cons);
		p.setModifier(Modifier.FINAL);
		p.setNameCi(p.cons.readCprocName(get0Ci));
		p.setDescCi(p.cons.readCprocDesc(get0Ci));
		Instruction s = new Instruction(1000);
		s.ins0(Opcode.ALOAD0); // this
		s.ins0(Opcode.ILOAD1);
		long sw = s.insSwitchTable(0, bs.length - 1);
		int sw0 = s.addr;
		s.switchTableFrom(sw, -1);
		s.ins0(Opcode.ARETURN);
		int swO = s.addr;
		s.insU2(Opcode.GETFIELD, ossCi);
		s.ins0(Opcode.ILOAD1);
		s.ins0(Opcode.AALOAD);
		s.ins0(Opcode.ICONST0);
		s.ins0(Opcode.AALOAD); // oss[i][0]
		s.ins0(Opcode.ARETURN);
		int swN = s.addr;
		s.ins0(Opcode.ILOAD1);
		s.ins0(Opcode.ICONST0);
		s.insU2(Opcode.INVOKEVIRTUAL, create0Ci);
		s.ins0(Opcode.ARETURN);
		for (int i = 0; i < bs.length; i++)
		{
			Bind b = bs[i];
			if (b.c == Container.class)
				s.switchTable(sw, i, sw0); // return this
			else if (b.b == b)
				if (b.scope == Scope.None.class)
					s.switchTable(sw, i, swN);
				else if (b.scope == Scope.Private.class)
				{
					s.switchTableFrom(sw, i);
					s.insU2(Opcode.GETFIELD, fCis[i]);
					s.ins0(Opcode.DUP);
					int j = s.insJump(Opcode.IFNOTNULL);
					s.ins0(Opcode.POP);
					s.ins0(Opcode.ALOAD0);
					s.ins0(Opcode.ILOAD1);
					s.ins0(Opcode.ICONST1);
					s.insU2(Opcode.INVOKEVIRTUAL, create0Ci);
					s.jumpFrom(j);
					s.ins0(Opcode.ARETURN);
				}
				else
				{
					s.switchTableFrom(sw, i);
					int loop = s.addr;
					s.ins0(Opcode.DUP);
					s.insU2(Opcode.GETFIELD, fCis[i]); // c, c.field
					s.ins0(Opcode.DUP);
					int j1 = s.insJump(Opcode.IFNULL);
					s.ins0(Opcode.DUP);
					s.ins0(Opcode.ALOAD0);
					s.ins0(Opcode.SWAP); // c, c.field, this, c.field
					s.insU2(Opcode.PUTFIELD, fCis[i]);
					s.ins0(Opcode.ARETURN);
					s.jumpFrom(j1);
					s.ins0(Opcode.POP);
					s.ins0(Opcode.DUP);
					s.insU2(Opcode.GETFIELD, outCi); // c, c.outer
					int j2 = s.insJump(Opcode.IFNULL);
					s.insU2(Opcode.GETFIELD, outCi);
					s.insU2(Opcode.CHECKCAST, y.head.getClassCi());
					s.jump(s.insJump(Opcode.GOTO), loop);
					s.jumpFrom(j2);
					if (b.scope != Scope.SpreadCreate.class)
						s.ins0(Opcode.ALOAD0);
					s.ins0(Opcode.ILOAD1);
					s.ins0(Opcode.ICONST1);
					s.insU2(Opcode.INVOKEVIRTUAL, create0Ci);
					if (b.scope == Scope.SpreadCreate.class)
					{
						s.ins0(Opcode.DUP);
						s.ins0(Opcode.ALOAD0);
						s.ins0(Opcode.SWAP); // o, this, o
						s.insU2(Opcode.CHECKCAST, p.cons.putClass(b.c));
						s.insU2(Opcode.PUTFIELD, fCis[i]);
					}
					s.ins0(Opcode.ARETURN);
				}
			else if (b.b instanceof Bind)
				s.switchTable(sw, i, sw0); // never happen
			else
				s.switchTable(sw, i, swO);
		}
		p.getCode().setIns(s, false);
		p.getCode().setLocalN(3);
		p.getCode().setStackN(30);
		y.getProcs().addProc(p);
	}

	private void makeCreate0(Bytecode y, int get0Ci, int create0Ci, int outCi, int ossCi,
		int[] fCis)
	{
		Procedure p = new Procedure(y.cons);
		p.setModifier(Modifier.FINAL);
		p.setNameCi(p.cons.readCprocName(create0Ci));
		p.setDescCi(p.cons.readCprocDesc(create0Ci));
		Instruction s = new Instruction(250);
		s.ins0(Opcode.ILOAD1); // index
		long sw = s.insSwitchTable(0, bs.length - 1);
		int sw0 = s.addr;
		s.switchTableFrom(sw, -1);
		s.ins0(Opcode.ACONSTNULL);
		s.ins0(Opcode.ARETURN);
		int maxParamN = 0;
		for (int i = 0; i < bs.length; i++)
		{
			Bind b = bs[i];
			maxParamN = Math.max(maxParamN, b.maxParamN);
			int o = 1;
			if (b.c == Container.class)
			{
				s.switchTableFrom(sw, i);
				s.insU2(Opcode.NEW, y.head.getClassCi());
				s.ins0(Opcode.DUP);
				s.insU2(Opcode.INVOKESPECIAL, p.cons.addCproc(y.head.getClassCi(), //
					p.cons.addNameDesc(y.getProcs().getProc(0).getNameCi(), //
						y.getProcs().getProc(0).getDescCi())));
				s.ins0(Opcode.DUP);
				s.ins0(Opcode.ALOAD0);
				s.insU2(Opcode.PUTFIELD, outCi);
				s.ins0(Opcode.DUP);
				s.ins0(Opcode.ALOAD0);
				s.insU2(Opcode.GETFIELD, ossCi);
				s.insU2(Opcode.PUTFIELD, ossCi);
				s.ins0(Opcode.ARETURN);
			}
			else if (b.b == b)
			{
				s.switchTableFrom(sw, i);
				s.insU2(Opcode.NEW, p.cons.putClass(b.c));
				s.ins0(Opcode.DUP);
				Class<?>[] ps = b.ct.getParameterTypes();
				for (int cb = 0; cb < b.cbs.length; cb++)
					o = makeCreate0_bind(p.cons, s, get0Ci, ossCi, b.cbs[cb], ps[cb], o);
				s.insU2(Opcode.INVOKESPECIAL, p.cons.putProc(b.ct));
				s.ins0(Opcode.ILOAD2); // save
				int j = s.insJump(Opcode.IFIE0);
				s.ins0(Opcode.DUP);
				s.ins0(Opcode.ALOAD0);
				s.ins0(Opcode.SWAP);
				s.insU2(Opcode.PUTFIELD, fCis[i]);
				s.jumpFrom(j);
				for (int f = 0; f < b.fs.length; f++)
				{
					s.ins0(Opcode.DUP);
					o = makeCreate0_bind(p.cons, s, get0Ci, ossCi, b.fbs[f], b.fs[f]
						.getType(), o);
					s.insU2(Opcode.PUTFIELD, p.cons.addField(b.fs[f]));
				}
				for (int m = 0; m < b.ms.length; m++)
				{
					ps = b.ms[m].getParameterTypes();
					s.ins0(Opcode.DUP);
					for (int mb = 0; mb < b.mbs[m].length; mb++)
						o = makeCreate0_bind(p.cons, s, get0Ci, ossCi, b.mbs[m][mb], ps[mb],
							o);
					s.insU2(Opcode.INVOKEVIRTUAL, p.cons.addProc(b.ms[m]));
				}
				s.ins0(Opcode.ARETURN);
			}
			else
				s.switchTable(sw, i, sw0); // never happen
		}
		p.getCode().setIns(s, false);
		p.getCode().setLocalN(3);
		p.getCode().setStackN(3 + maxParamN);
		y.getProcs().addProc(p);
	}

	private int makeCreate0_bind(Constants cons, Instruction s, int get0Ci, int ossCi,
		Object b, Class<?> c, int o)
	{
		if (b instanceof Bind)
		{
			s.ins0(Opcode.ALOAD0);
			s.insU2(Opcode.SIPUSH, bind((Bind)b));
			s.insU2(Opcode.INVOKEVIRTUAL, get0Ci);
		}
		else
		{
			s.ins0(Opcode.ALOAD0);
			s.insU2(Opcode.GETFIELD, ossCi);
			s.ins0(Opcode.ILOAD1);
			s.ins0(Opcode.AALOAD);
			s.insU2(Opcode.SIPUSH, o++);
			s.ins0(Opcode.AALOAD); // oss[i][o]
		}
		s.insUnboxNarrow(cons, c);
		return o;
	}
}
