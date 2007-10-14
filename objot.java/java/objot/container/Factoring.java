//
// Copyright 2007 Qianyan Cai
// Under the terms of the GNU General Public License version 2
//
package objot.container;

import java.lang.reflect.Method;

import objot.bytecode.Bytecode;
import objot.bytecode.Constants;
import objot.bytecode.Field;
import objot.bytecode.Instruction;
import objot.bytecode.Procedure;
import objot.util.Class2;
import objot.util.Mod2;

import static objot.bytecode.Opcode.*;


final class Factoring
{
	private static final String NAME_oss = "oss";

	/** [0] == null */
	Bind.Clazz[] cs;
	Object[][] oss;
	boolean lazy;
	Bytecode y;
	Constants cons;
	int indexCi;
	int get0Ci;
	int create0Ci;
	int outCi;
	int ossCi;
	int[] fCis;
	int[] cCis;

	Container create(Bind.Clazz[] cs_, boolean lazy_) throws Exception
	{
		cs = cs_;
		oss = new Object[cs.length][];
		for (int i = 1; i < cs.length; i++)
			oss[i] = cs[i].os;
		lazy = lazy_;

		String name = Container.class.getName() + "$$" + hashCode();
		y = new Bytecode();
		cons = y.cons;
		int superCi = cons.addClass(Container.class);
		y.head.setModifier(Mod2.FINAL | Mod2.SYNTHETIC);
		y.head.setClassCi(cons.addClass(name));
		y.head.setSuperCi(superCi);
		y.getProcs().addProc(Procedure.addCtor0(cons, superCi, 0));

		get0Ci = cons.addProc(Container.M_get0);
		create0Ci = cons.addProc(Container.M_create0);
		outCi = cons.addField(Container.F_parent);
		fCis = new int[cs.length];
		cCis = new int[cs.length];
		makeFields();
		makeIndex();
		makeGet0();
		makeCreate0();

		Container c = Class2.<Container>load(Container.class.getClassLoader(), name,
			y.normalize()).newInstance();
		Class2.declaredField(c.getClass(), NAME_oss).set(null, oss);
		return c;
	}

	/** @return index or -index of actual bind or object */
	private int bind(int i0)
	{
		Bind.Clazz c = cs[i0];
		if (c.b != c && c.b != null)
			for (int i = cs.length - 1; i > 0; i--)
				if (cs[i] == c.b)
					return cs[i].mode == Inject.New.class ? -i : i;
		return c.mode == Inject.New.class ? -i0 : i0;
	}

	/** @return index or -index of actual bind or object */
	private int bind(Bind.Clazz c)
	{
		if (c.b != c && c.b != null)
			for (int i = cs.length - 1; i > 0; i--)
				if (cs[i] == c.b)
					return cs[i].mode == Inject.New.class ? -i : i;
		for (int i = cs.length - 1; i > 0; i--)
			if (cs[i] == c)
				return cs[i].mode == Inject.New.class ? -i : i;
		throw new AssertionError();
	}

	private void makeFields()
	{
		Field f = new Field(cons);
		f.setModifier(Mod2.STATIC);
		f.setNameCi(cons.addUcs(NAME_oss));
		f.setDescCi(cons.addUcs(Class2.descript(Object[][].class)));
		y.getFields().addField(f);
		ossCi = cons.addField(y.head.getClassCi(), cons.addNameDesc(f.getNameCi(), f
			.getDescCi()));
		int descCi = cons.addUcs(Class2.descript(Object.class));
		for (int i = 1; i < cs.length; i++)
			if (cs[i].b == cs[i] && cs[i].mode != Inject.New.class)
			{
				f = new Field(cons);
				f.setModifier(Mod2.PRIVATE);
				f.setNameCi(cons.addUcs("o" + i));
				f.setDescCi(descCi);
				y.getFields().addField(f);
				fCis[i] = cons.addField(y.head.getClassCi(), cons.addNameDesc(f.getNameCi(),
					descCi));
			}
	}

	private static final Method M_hashCode = Class2.declaredMethod1(Object.class, "hashCode");

	private void makeIndex()
	{
		Procedure p = new Procedure(cons);
		p.setModifier(Mod2.FINAL);
		p.setNameCi(cons.addUtf(Container.NAME_index));
		p.setDescCi(cons.addUtf(Container.DESC_index));
		Instruction s = new Instruction(cons, 500);
		s.ins0(ALOAD1); // class
		s.ins0(DUP); // class
		s.insU2(INVOKEVIRTUAL, cons.addProc(M_hashCode)); // hash code
		s.insS1(BIPUSH, 31);
		s.ins0(IREM);
		long sw = s.insSwitchTable(0, 30);
		for (int i = 0; i < 31; i++)
		{
			s.switchTableFrom(sw, i);
			for (int j = 1; j < cs.length; j++)
				if (cs[j].cla.hashCode() % 31 == i)
				{
					s.ins0(DUP); // class
					s.insU2(LDCW, cCis[j] = cons.addClass(cs[j].cla));
					s.insS2(IFAN, 7);
					s.insS2(SIPUSH, bind(j));
					s.ins0(IRETURN);
				}
			s.ins0(ICONST0);
			s.ins0(IRETURN);
		}
		s.switchTableFrom(sw, -1);
		s.ins0(ICONST0);
		s.ins0(IRETURN);
		p.getCode().setIns(s, false);
		p.getCode().setLocalN(2);
		p.getCode().setStackN(3);
		y.getProcs().addProc(p);
		indexCi = cons.addCproc(y.head.getSuperCi(), cons.addNameDesc(p.getNameCi(), p
			.getDescCi()));
	}

	private void makeGet0()
	{
		Procedure p = new Procedure(cons);
		p.setModifier(Mod2.FINAL);
		p.setNameCi(cons.getCprocName(get0Ci));
		p.setDescCi(cons.getCprocDesc(get0Ci));
		Instruction s = new Instruction(cons, 1000);
		s.ins0(ALOAD0); // this
		s.ins0(ILOAD1);
		long sw = s.insSwitchTable(0, cs.length - 1);
		s.switchTableFrom(sw, -1);
		s.switchTableFrom(sw, 0);
		int sw0 = s.addr;
		s.ins0(ACONSTNULL);
		s.ins0(ARETURN);
		int swO = s.addr;
		s.insU2(GETSTATIC, ossCi);
		s.ins0(ILOAD1);
		s.ins0(AALOAD);
		s.ins0(ICONST0);
		s.ins0(AALOAD); // oss[i][0]
		s.ins0(ARETURN);
		for (int i = 1; i < cs.length; i++)
		{
			Bind.Clazz c = cs[i];
			if (c.cla == Container.class)
			{
				s.switchTableFrom(sw, i);
				s.ins0(ARETURN); // return this;
			}
			else if (c.b == null)
				if (oss[i][0] != null)
					s.switchTable(sw, i, swO);
				else
					s.switchTable(sw, i, sw0);
			else if (c.b == c && c.mode == Inject.Single.class)
			{
				s.switchTableFrom(sw, i);
				s.insU2(GETFIELD, fCis[i]);
				s.ins0(DUP);
				int j = s.insJump(IFNOTNULL);
				s.ins0(POP);
				s.ins0(ALOAD0);
				s.ins0(ILOAD1);
				s.ins0(ACONSTNULL);
				s.insU2(INVOKEVIRTUAL, create0Ci);
				s.jumpFrom(j);
				s.ins0(ARETURN);
			}
			else if (c.b == c && c.mode == null)
			{
				s.switchTableFrom(sw, i);
				s.ins0(DUP);
				s.insU2(GETFIELD, fCis[i]); // this, field
				s.ins0(DUP);
				int cache = s.insJump(IFNULL);
				s.ins0(ARETURN);
				s.jumpFrom(cache);
				s.ins0(POP);
				int out = s.addr; // c
				s.insU2(GETFIELD, outCi); // c
				s.ins0(DUP);
				s.insU2(LDCW, cCis[i]);
				s.insU2(INVOKEVIRTUAL, indexCi); // c, i
				s.ins0(DUP);
				int ge0 = s.insJump(IFIGE0);
				s.ins0(ALOAD0);
				s.insU2(INVOKEVIRTUAL, create0Ci); // o
				s.ins0(ARETURN);
				s.jumpFrom(ge0);
				s.ins0(DUP);
				int e0 = s.insJump(IFIE0);
				s.insU2(INVOKEVIRTUAL, get0Ci); // o
				s.ins0(DUP);
				s.ins0(ALOAD0);
				s.ins0(SWAP); // o, this, o
				s.insU2(PUTFIELD, fCis[i]);
				s.ins0(ARETURN);
				s.jumpFrom(e0);
				s.ins0(POP);
				s.jump(s.insJump(GOTO), out);
			}
			else
				s.switchTable(sw, i, sw0);
		}
		p.getCode().setIns(s, false);
		p.getCode().setLocalN(3);
		p.getCode().setStackN(4);
		y.getProcs().addProc(p);
	}

	private void makeCreate0()
	{
		Procedure p = new Procedure(cons);
		p.setModifier(Mod2.FINAL);
		p.setNameCi(cons.getCprocName(create0Ci));
		p.setDescCi(cons.getCprocDesc(create0Ci));
		int max = cs.length - 1;
		Instruction s = new Instruction(cons, 250);
		s.ins0(ILOAD1); // index
		long sw = s.insSwitchTable( -max, max);
		s.switchTableFrom(sw, -1);
		s.switchTableFrom(sw, max);
		int sw0 = s.addr;
		s.ins0(ACONSTNULL);
		s.ins0(ARETURN);
		int maxParamN = 0;
		for (int i = 1; i < cs.length; i++)
		{
			Bind.Clazz c = cs[i];
			maxParamN = Math.max(maxParamN, c.maxParamN);
			if (c.cla == Container.class)
			{
				s.switchTableFrom(sw, max + i);
				s.switchTableFrom(sw, max - i);
				s.insU2(NEW, y.head.getClassCi());
				s.ins0(DUP);
				s.insU2(INVOKESPECIAL, cons.addCproc(y.head.getClassCi(), //
					cons.addNameDesc(y.getProcs().getProc(0).getNameCi(), //
						y.getProcs().getProc(0).getDescCi())));
				s.ins0(DUP);
				s.ins0(ALOAD2);
				s.insU2(PUTFIELD, outCi);
				if ( !lazy)
					for (int j = 1; j < cs.length; j++)
						if (cs[j].cla != Container.class && cs[j].b == cs[j]
							&& cs[j].mode == Inject.Single.class)
						{
							s.ins0(DUP);
							s.insU2(GETFIELD, fCis[j]);
							int k = s.insJump(IFNOTNULL);
							s.ins0(DUP);
							s.insS2(SIPUSH, j);
							s.ins0(ACONSTNULL);
							s.insU2(INVOKEVIRTUAL, create0Ci);
							s.ins0(POP);
							s.jumpFrom(k);
						}
				s.ins0(ARETURN);
			}
			else if (c.t != null) // never bind to parent
			{
				s.switchTableFrom(sw, max + i);
				s.switchTableFrom(sw, max - i);
				s.insU2(NEW, cons.putClass(c.cla));
				s.ins0(DUP);
				int o = 1;
				for (int cb = 0; cb < c.t.ps.length; cb++)
					o = makeCreate0_bind(i, s, c.t.ps[cb], oss[i], o);
				s.insU2(INVOKESPECIAL, cons.putProc(c.t.t));
				if (c.mode == Inject.Single.class)
				{
					s.ins0(ALOAD2); // save
					int j = s.insJump(IFNOTNULL);
					s.ins0(DUP);
					s.ins0(ALOAD0);
					s.ins0(SWAP);
					s.insU2(PUTFIELD, fCis[i]);
					s.jumpFrom(j);
				}
				for (Bind.FM fm: c.fms)
					if (fm.f != null)
					{
						s.ins0(DUP);
						o = makeCreate0_bind(i, s, fm, oss[i], o);
						s.insU2(PUTFIELD, cons.addField(fm.f));
					}
					else
					{
						s.ins0(DUP);
						for (Bind mp: fm.ps)
							o = makeCreate0_bind(i, s, mp, oss[i], o);
						s.insU2(INVOKEVIRTUAL, cons.addProc(fm.m));
					}
				s.ins0(ARETURN);
			}
			else
			{ // never happen
				s.switchTable(sw, max + i, sw0);
				s.switchTable(sw, max - i, sw0);
			}
		}
		p.getCode().setIns(s, false);
		p.getCode().setLocalN(3);
		p.getCode().setStackN(5 + maxParamN * 2);
		y.getProcs().addProc(p);
	}

	private int makeCreate0_bind(int i0, Instruction s, Bind b, Object[] os, int o)
	{
		if (b.b != null)
		{
			s.ins0(ALOAD0);
			int i = bind(b.b);
			s.insS2(SIPUSH, i);
			if (i < 0)
				s.ins0(ALOAD0);
			s.insU2(INVOKEVIRTUAL, i > 0 ? get0Ci : create0Ci);
			s.insUnboxNarrow(b.cla);
			return o;
		}
		if (b.cla.isArray() && os[o] instanceof Integer)
		{
			int n = (Integer)os[o];
			if (n << 16 >> 16 == n)
				s.insS2(SIPUSH, n);
			else
				s.insU2(LDCW, cons.addInt(n));
			s.insNews(b.cla.getComponentType());
		}
		else if (os[o] != null)
		{
			s.insU2(GETSTATIC, ossCi);
			s.insS2(SIPUSH, i0);
			s.ins0(AALOAD);
			s.insS2(SIPUSH, o);
			s.ins0(AALOAD); // oss[i0][o]
			s.insUnboxNarrow(b.cla);
		}
		else
		{
			s.ins0(ACONSTNULL);
			if (b.cla.isPrimitive())
				s.insUnboxNarrow(b.cla);
		}
		return ++o;
	}
}
