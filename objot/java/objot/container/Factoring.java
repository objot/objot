//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Library General Public License version 2
//
package objot.container;

import java.lang.reflect.Method;

import objot.bytecode.Bytecode;
import objot.bytecode.Constants;
import objot.bytecode.Field;
import objot.bytecode.Instruction;
import objot.bytecode.Procedure;
import objot.container.Inject.New;
import objot.container.Inject.Parent;
import objot.container.Inject.Set;
import objot.container.Inject.Single;
import objot.util.Class2;
import objot.util.Mod2;

import static objot.bytecode.Opcode.*;


final class Factoring
{
	private static final String NAME_oss = "oss";

	/** [0] == null */
	Bind.Clazz[] cs;
	/** number of actual classes and static objects, for optimization */
	int csn;
	Object[][] oss;
	boolean lazy;
	Bytecode y;
	Constants cons;
	int createCi;
	int parentCi;
	int indexCi;
	int get0Ci;
	int set0Ci;
	int ossCi;
	int[] fCis;
	int[] cCis;

	Container create(Bind.Clazz[] cs_, boolean lazy_) throws Exception
	{
		cs = cs_;
		for (int i = csn = 1; i < cs.length; i++)
			if (cs[i].b == cs[i])
			{
				Bind.Clazz c = cs[i];
				cs[i] = cs[csn];
				cs[csn++] = c;
			}
		oss = new Object[csn][];
		for (int i = 1; i < csn; i++)
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

		createCi = cons.addProc(Container.M_create);
		parentCi = cons.addField(Container.F_parent);
		indexCi = cons.addProc(Container.M_index);
		get0Ci = cons.addProc(Container.M_get0);
		set0Ci = cons.addProc(Container.M_set0);
		fCis = new int[csn];
		cCis = new int[cs.length];
		makeFields();
		makeCreate();
		makeIndex();
		makeGet0();
		makeSet0();

		Class<Container> c = Class2.<Container>load(Container.class.getClassLoader(), name,
			y.normalize());
		Class2.declaredField(c, NAME_oss).set(null, oss);
		return c.newInstance(); // nothing inited
	}

	private void makeFields()
	{
		Field f = new Field(cons);
		f.setModifier(Mod2.STATIC);
		f.setNameCi(cons.addUcs(NAME_oss));
		f.setDescCi(cons.addUcs(Class2.descript(Object[][].class)));
		y.getFields().addField(f);
		ossCi = cons.addField(y.head.getClassCi(), cons.addNameDesc(f.getNameCi(),
			f.getDescCi()));
		int descCi = cons.addUcs(Class2.descript(Object.class));
		for (int i = 1; i < csn; i++)
			if (cs[i].mode != null && cs[i].mode != New.class)
			{
				f = new Field(cons);
				f.setModifier(Mod2.PRIVATE | (cs[i].mode == Set.class ? Mod2.VOLATILE : 0));
				f.setNameCi(cons.addUcs("o" + i));
				f.setDescCi(descCi);
				y.getFields().addField(f);
				fCis[i] = cons.addField(y.head.getClassCi(), //
					cons.addNameDesc(f.getNameCi(), descCi));
			}
	}

	private void makeCreate()
	{
		Procedure p = new Procedure(cons);
		p.setModifier(Mod2.FINAL);
		p.setNameCi(cons.getCprocName(createCi));
		p.setDescCi(cons.getCprocDesc(createCi));
		Instruction s = new Instruction(cons, 250);
		s.insU2(NEW, y.head.getClassCi());
		s.ins0(DUP);
		s.insU2(INVOKESPECIAL, cons.addCproc(y.head.getClassCi(), //
			cons.addNameDesc(y.getProcs().getProc(0).getNameCi(), //
				y.getProcs().getProc(0).getDescCi())));
		s.ins0(DUP);
		s.ins0(DUP); // new, new
		s.ins0(ALOAD1);
		s.ins0(DUP);
		int j = s.insJump(IFNOTNULL);
		s.ins0(POP);
		s.insU2(GETSTATIC, cons.addField(Container.F_null));
		s.jumpHere(j);
		s.insU2(PUTFIELD, parentCi);
		if ( !lazy)
			for (int i = 1; i < csn; i++)
				if (cs[i].mode == Single.class && cs[i].cla != Container.class)
				{
					s.ins0(DUP);
					s.insS2(SIPUSH, i);
					s.insU2(INVOKEVIRTUAL, get0Ci);
					s.ins0(POP);
				}

		s.ins0(ARETURN);
		p.getCode().setIns(s, false);
		p.getCode().setLocalN(2);
		p.getCode().setStackN(5);
		y.getProcs().addProc(p);
	}

	private static final Method M_hashCode = Class2.declaredMethod1(Object.class, "hashCode");

	/** @return index or -index of actual bind */
	private int index(int i0)
	{
		Bind.Clazz c = cs[i0];
		if (c.b != c)
			for (int i = csn; i > 0; i--)
				if (cs[i] == c.b)
					return c.b.mode == null || c.b.mode == Single.class ? i : -i;
		return c.mode == null || c.mode == Single.class ? i0 : -i0;
	}

	/** @return index or -index of actual bind */
	private int index(Bind c)
	{
		for (int i = csn - 1; i > 0; i--)
			if (cs[i] == c || cs[i] == c.b)
				return cs[i].mode == null || cs[i].mode == Single.class ? i : -i;
		throw new AssertionError();
	}

	private void makeIndex()
	{
		Procedure p = new Procedure(cons);
		p.setModifier(Mod2.FINAL);
		p.setNameCi(cons.getCprocName(indexCi));
		p.setDescCi(cons.getCprocDesc(indexCi));
		Instruction s = new Instruction(cons, 500);
		s.ins0(ALOAD1); // class
		s.ins0(DUP); // class
		s.insU2(INVOKEVIRTUAL, cons.addProc(M_hashCode)); // hash code
		s.insS1(BIPUSH, 31);
		s.ins0(IREM);
		long sw = s.insSwitchTable(0, 30);
		for (int i = 0; i < 31; i++)
		{
			s.switchTableHere(sw, i);
			for (int j = 1; j < cs.length; j++)
				if (cs[j].cla.hashCode() % 31 == i)
				{
					s.ins0(DUP); // class
					s.insU2(LDCW, cCis[j] = cons.addClass(cs[j].cla));
					s.insS2(IFAN, 7);
					s.insS2(SIPUSH, index(j));
					s.ins0(IRETURN);
				}
			s.ins0(ICONST0);
			s.ins0(IRETURN);
		}
		s.switchTableHere(sw, -1);
		s.ins0(ICONST0);
		s.ins0(IRETURN);
		p.getCode().setIns(s, false);
		p.getCode().setLocalN(2);
		p.getCode().setStackN(3);
		y.getProcs().addProc(p);
	}

	private void makeGet0()
	{
		Procedure p = new Procedure(cons);
		p.setModifier(Mod2.FINAL);
		p.setNameCi(cons.getCprocName(get0Ci));
		p.setDescCi(cons.getCprocDesc(get0Ci));
		int i0 = csn - 1;
		Instruction s = new Instruction(cons, 250);
		s.ins0(ALOAD0); // this
		s.ins0(ILOAD1);
		long sw = s.insSwitchTable( -i0, i0);
		s.switchTableHere(sw, -1);
		s.switchTableHere(sw, i0);
		int sw0 = s.addr;
		s.ins0(ACONSTNULL);
		s.ins0(ARETURN);

		int swObj = s.addr;
		s.insU2(GETSTATIC, ossCi);
		s.ins0(ILOAD1);
		s.ins0(AALOAD);
		s.ins0(ICONST0);
		s.ins0(AALOAD); // oss[i][0]
		s.ins0(ARETURN);
		int swObj_ = s.addr;
		s.insU2(GETSTATIC, ossCi);
		s.ins0(ILOAD1);
		s.ins0(INEG);
		s.ins0(AALOAD);
		s.ins0(ICONST0);
		s.ins0(AALOAD); // oss[-i][0]
		s.ins0(ARETURN);
		int circle = s.addr;
		int circleCi = cons.addClass(ClassCircularityError.class);
		s.insU2(NEW, circleCi);
		s.ins0(DUP);
		s.insU2(INVOKESPECIAL, cons.addCtor0(circleCi));
		s.ins0(ATHROW);

		int maxParamN = 0;
		for (int i = 1; i < csn; i++) // always actual bind
		{
			Bind.Clazz c = cs[i];
			maxParamN = Math.max(maxParamN, c.maxParamN);
			if (c.cla == Container.class)
			{
				s.switchTableHere(sw, i0 + i);
				s.switchTable(sw, i0 - i, sw0); // never happen
				s.ins0(ARETURN);
			}
			else if (c.mode == null) // static object
			{
				s.switchTable(sw, i0 + i, oss[i][0] == null ? sw0 : swObj);
				s.switchTable(sw, i0 - i, oss[i][0] == null ? sw0 : swObj_);
			}
			else if (c.t != null) // New and Single
			{
				s.switchTableHere(sw, i0 + i);
				if (c.mode == Single.class)
				{
					s.ins0(DUP);
					s.insU2(GETFIELD, fCis[i]);
					s.ins0(DUP);
					int j = s.insJump(IFNULL);
					s.ins0(ARETURN);
					s.jumpHere(j);
					s.ins0(POP);
				}
				s.switchTableHere(sw, i0 - i);
				s.insU2(NEW, cons.putClass(c.cla));
				s.ins0(DUP);
				int x = 1;
				for (int cb = 0; cb < c.t.ps.length; cb++)
					x = makeGet0_fp(i, s, c.t.ps[cb], oss[i], x);
				s.insU2(INVOKESPECIAL, cons.putProc(c.t.t));
				if (c.mode == Single.class)
				{
					s.ins0(ILOAD1);
					int j = s.insJump(IFIL0);
					if (c.t.ps.length > 0)
					{ // never happen if ctor no parameter
						s.ins0(ALOAD0);
						s.insU2(GETFIELD, fCis[i]);
						s.jump(s.insJump(IFNOTNULL), circle);
					}
					s.ins0(DUP);
					s.ins0(ALOAD0);
					s.ins0(SWAP); // o, this, o
					s.insU2(PUTFIELD, fCis[i]); // single
					s.jumpHere(j);
				}
				for (Bind.FM fm: c.fms)
					if (fm.f != null)
					{
						s.ins0(DUP);
						x = makeGet0_fp(i, s, fm, oss[i], x);
						s.insU2(PUTFIELD, cons.addField(fm.f));
					}
					else
					{
						s.ins0(DUP);
						for (Bind mp: fm.ps)
							x = makeGet0_fp(i, s, mp, oss[i], x);
						s.insU2(INVOKEVIRTUAL, cons.addProc(fm.m));
					}
				s.ins0(ARETURN);
			}
			else if (c.mode == Set.class)
			{
				s.switchTableHere(sw, i0 + i);
				s.switchTableHere(sw, i0 - i);
				s.insU2(GETFIELD, fCis[i]);
				s.ins0(ARETURN);
			}
			else if (c.mode == Parent.class)
			{
				s.switchTableHere(sw, i0 - i);
				s.ins0(DUP);
				s.insU2(GETFIELD, fCis[i]);
				s.ins0(DUP);
				int j = s.insJump(IFNULL);
				s.ins0(ARETURN);
				s.jumpHere(j);
				s.ins0(POP);

				s.switchTableHere(sw, i0 + i);
				int loop = s.addr; // n
				s.insU2(GETFIELD, parentCi); // n
				s.ins0(DUP);
				s.insU2(LDCW, cCis[i]);
				s.insU2(INVOKEVIRTUAL, indexCi); // n, j
				s.ins0(DUP);
				s.ins0(ISTORE2);
				s.jump(s.insJump(IFIE0), loop);
				s.ins0(ILOAD1);
				j = s.insJump(IFIL0);
				s.ins0(ILOAD2);
				s.ins0(INEG); // -j
				s.insU2(INVOKEVIRTUAL, get0Ci); // get0
				s.ins0(ARETURN);
				s.jumpHere(j);

				s.ins0(ILOAD2); // j
				s.insU2(INVOKEVIRTUAL, get0Ci); // get0
				s.ins0(DUP);
				s.ins0(ILOAD2);
				j = s.insJump(IFIL0);
				s.ins0(ALOAD0);
				s.ins0(SWAP); // o, this, o
				s.insU2(PUTFIELD, fCis[i]); // cache
				s.ins0(ARETURN);
				s.jumpHere(j);
				s.ins0(ARETURN);
			}
			else
				throw new AssertionError();
		}
		p.getCode().setIns(s, false);
		p.getCode().setLocalN(3);
		p.getCode().setStackN(5 + maxParamN * 2);
		y.getProcs().addProc(p);
	}

	private int makeGet0_fp(int i0, Instruction s, Bind b, Object[] os, int x)
	{
		if (b.b == b) // static object
			// if (b.cla.isArray() && os[x] instanceof Integer)
			// {
			// int n = (Integer)os[x];
			// if (n << 16 >> 16 == n)
			// s.insS2(SIPUSH, n);
			// else
			// s.insU2(LDCW, cons.addInt(n));
			// s.insNews(b.cla.getComponentType());
			// } else
			if (os[x] != null)
			{
				s.insU2(GETSTATIC, ossCi);
				s.insS2(SIPUSH, i0);
				s.ins0(AALOAD);
				s.insS2(SIPUSH, x);
				s.ins0(AALOAD); // oss[i0][o]
				s.insUnboxNarrow(b.cla);
				return ++x;
			}
			else
			{
				s.ins0(ACONSTNULL);
				return ++x;
			}
		else if (b.b.cla == Container.class)
			s.ins0(ALOAD0);
		else if (b.b.mode == Set.class)
		{
			s.ins0(ALOAD0);
			s.insU2(GETFIELD, fCis[Math.abs(index(b.b))]);
			s.insUnboxNarrow(b.cla);
		}
		else
		{
			s.ins0(ALOAD0);
			s.insS2(SIPUSH, index(b.b));
			s.insU2(INVOKEVIRTUAL, get0Ci);
			s.insUnboxNarrow(b.cla);
		}
		return x;
	}

	private void makeSet0()
	{
		Procedure p = new Procedure(cons);
		p.setModifier(Mod2.FINAL);
		p.setNameCi(cons.getCprocName(set0Ci));
		p.setDescCi(cons.getCprocDesc(set0Ci));
		int i0 = csn - 1;
		Instruction s = new Instruction(cons, 250);
		s.ins0(ILOAD1); // index
		long sw = s.insSwitchTable( -i0, -1);
		s.switchTableHere(sw, -1);
		int sw0 = s.addr;
		s.ins0(ICONST0);
		s.ins0(IRETURN);
		for (int i = 1; i < csn; i++) // always actual bind
		{
			Bind.Clazz c = cs[i];
			if (c.mode == Set.class)
			{
				s.switchTableHere(sw, i0 - i);
				s.ins0(ALOAD0);
				s.ins0(ALOAD2);
				s.insNarrow(c.box);
				s.insU2(PUTFIELD, fCis[i]);
				s.ins0(ICONST1);
				s.ins0(IRETURN);
			}
			else if (c.mode == Parent.class)
			{
				s.switchTableHere(sw, i0 - i);
				s.ins0(ALOAD0);
				int loop = s.addr; // n
				s.insU2(GETFIELD, parentCi); // n
				s.ins0(DUP);
				s.insU2(LDCW, cCis[i]);
				s.insU2(INVOKEVIRTUAL, indexCi); // n, j
				s.ins0(DUP);
				s.ins0(ISTORE3);
				s.jump(s.insJump(IFIE0), loop);
				s.ins0(ILOAD3); // j
				s.ins0(ALOAD2);
				s.insU2(INVOKEVIRTUAL, set0Ci); // set
				s.ins0(IRETURN);
			}
			else
				s.switchTable(sw, i0 - i, sw0); // other
		}
		p.getCode().setIns(s, false);
		p.getCode().setLocalN(4);
		p.getCode().setStackN(5);
		y.getProcs().addProc(p);
	}
}
