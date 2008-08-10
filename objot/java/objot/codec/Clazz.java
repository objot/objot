//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.codec;

import java.lang.reflect.Field;
import java.util.Map;

import objot.bytecode.Bytecode;
import objot.bytecode.Instruction;
import objot.bytecode.Procedure;
import objot.util.Array2;
import objot.util.Bytes;
import objot.util.Class2;
import objot.util.Mod2;

import static objot.bytecode.Opcode.*;


public abstract class Clazz
{
	static final Field F_encs = Class2.declaredField(Clazz.class, "encs");
	protected Property[] encs;
	Map<String, Property> decs;

	static Clazz make(Class<?> c, Property[] es, Property[] ds, Map<String, Property> dNames)
		throws Exception
	{
		String name = c.getName().startsWith("java.") //
			? Clazz.class.getName() + "$$" + c.getName().replace('.', '$') //
			: c.getName() + "$$Codec" + (c.hashCode() ^ Clazz.class.hashCode());
		Bytecode y = new Bytecode();
		y.head.setModifier(Mod2.PUBLIC | Mod2.FINAL | Mod2.SYNTHETIC);
		y.head.setClassCi(y.cons.addClass(y.cons.addUcs(Class2.pathName(name))));
		y.head.setSuperCi(y.cons.addClass(Clazz.class));
		y.getProcs().addProc(Procedure.addCtor0(y.cons, y.head.getSuperCi(), Mod2.PUBLIC));

		int classCi = y.cons.addClass(c);
		if (es.length > 0)
		{
			int encsCi = y.cons.addField(F_encs);
			int allowCi = y.cons.addProc(Property.M_allowEnc);
			int nameCi = y.cons.addField(Property.F_name);
			makeEncode(y, es, classCi, encsCi, allowCi, nameCi);
			makeEncodeRefs(y, es, classCi, encsCi, allowCi);
		}
		try
		{
			c.getDeclaredConstructor(Array2.CLASSES0);
			makeObject(y, classCi);
		}
		catch (NoSuchMethodException e)
		{
		}
		if (ds.length > 0)
		{
			makeDecode(y, ds, classCi, 0);
			makeDecode(y, ds, classCi, 1);
			makeDecode(y, ds, classCi, 2);
		}

		Clazz z = Class2.<Clazz>load(Clazz.class.getClassLoader(), name, y.normalize()) //
		.newInstance();
		z.encs = es;
		z.decs = dNames;
		return z;
	}

	protected Clazz()
	{
	}

	/**
	 * Example:
	 * 
	 * <pre>
	 * {@link Property}[] es = {@link #encs};
	 * A a = (A)o; // shouldn't cause ClassCastException
	 * // no property 0
	 * if (es[1].{@link Property#allowEnc}(ruleKey) e.{@link Encoder#refs}(a.f1);
	 * // no property 1
	 * if (es[3].{@link Property#allowEnc}(ruleKey)) e.{@link Encoder#refs}(a.m3());
	 * ...
	 * </pre>
	 */
	protected void encodeRefs(Encoder e, Object o, Object ruleKey)
	{
	}

	static final Bytes NAME_encodeRefs = Bytecode.utf("encodeRefs");
	static final Bytes DESC_encodeRefs = Bytecode.utf(Class2.descript(Class2.declaredMethod1(
		Clazz.class, "encodeRefs")));

	/**
	 * Example:
	 * 
	 * <pre>
	 * {@link Property}[] es = {@link #encs};
	 * A a = (A)o; // shouldn't cause ClassCastException
	 * if (es[0].{@link Property#allowEnc}(ruleKey))
	 *   e.{@link Encoder#value(String, int)}(es[0].{@link Property#name}, a.f0);
	 * if (es[1].{@link Property#allowEnc}(ruleKey))
	 *   e.{@link Encoder#value(String, Object)}(es[1].{@link Property#name}, a.m1());
	 * ...
	 * </pre>
	 */
	protected void encode(Encoder e, Object o, Object ruleKey)
	{
	}

	static final Bytes NAME_encode = Bytecode.utf("encode");
	static final Bytes DESC_encode = Bytecode.utf(Class2.descript(Class2.declaredMethod1(
		Clazz.class, "encode")));

	private static void makeEncodeRefs(Bytecode y, Property[] es, int classCi, int encsCi,
		int allowCi)
	{
		Procedure p = new Procedure(y.cons);
		p.setModifier(Mod2.PROTECTED | Mod2.FINAL);
		p.setNameCi(p.cons.addUtf(NAME_encodeRefs));
		p.setDescCi(p.cons.addUtf(DESC_encodeRefs));
		Instruction s = new Instruction(p.cons, 250);
		s.ins0(ALOAD2);
		s.insU2(CHECKCAST, classCi);
		s.insU1(ASTORE, 4); // object
		s.ins0(ALOAD0);
		s.insU2(GETFIELD, encsCi); // properties
		int refsCi = p.cons.addProc(Encoder.M_refs);
		Property e;
		for (int i = 0; i < es.length; i++)
			if ( !(e = es[i]).cla.isPrimitive() && !Number.class.isAssignableFrom(e.cla)
				&& e.cla != Boolean.class)
			{
				s.ins0(DUP);
				s.insS2(SIPUSH, i);
				s.ins0(AALOAD); // property
				s.ins0(ALOAD3);
				s.insU2(INVOKEVIRTUAL, allowCi);
				int if0 = s.insJump(IFIE0);
				s.ins0(ALOAD1);
				s.insU1(ALOAD, 4); // object
				if (e.field != null)
					s.insU2(GETFIELD, p.cons.addField(e.field));
				else
					s.insU2(INVOKEVIRTUAL, p.cons.addProc(e.method));
				s.insU2(INVOKEVIRTUAL, refsCi);
				s.jumpHere(if0);
			}
		s.ins0(RETURN);
		p.getCode().setIns(s, false);
		p.getCode().setLocalN(5);
		p.getCode().setStackN(4);
		y.getProcs().addProc(p);
	}

	private static void makeEncode(Bytecode y, Property[] es, int classCi, int encsCi,
		int allowCi, int nameCi)
	{
		Procedure p = new Procedure(y.cons);
		p.setModifier(Mod2.PROTECTED | Mod2.FINAL);
		p.setNameCi(p.cons.addUtf(NAME_encode));
		p.setDescCi(p.cons.addUtf(DESC_encode));
		Instruction s = new Instruction(y.cons, 250);
		s.ins0(ALOAD2);
		s.insU2(CHECKCAST, classCi);
		s.insU1(ASTORE, 4); // object
		s.ins0(ALOAD0);
		s.insU2(GETFIELD, encsCi); // properties
		int intCi = p.cons.addProc(Encoder.M_valueInt);
		int longCi = p.cons.addProc(Encoder.M_valueLong);
		int boolCi = p.cons.addProc(Encoder.M_valueBool);
		int floatCi = p.cons.addProc(Encoder.M_valueFloat);
		int doubleCi = p.cons.addProc(Encoder.M_valueDouble);
		int objCi = p.cons.addProc(Encoder.M_valueObject);
		Property e;
		for (int i = 0; i < es.length; i++)
		{
			s.ins0(DUP);
			s.insS2(SIPUSH, i);
			s.ins0(AALOAD); // property
			s.ins0(DUP); // property
			s.insU1(ASTORE, 5); // property
			s.ins0(ALOAD3);
			s.insU2(INVOKEVIRTUAL, allowCi);
			int if0 = s.insJump(IFIE0);
			s.ins0(ALOAD1);
			s.insU1(ALOAD, 5); // property
			s.insU2(GETFIELD, nameCi);
			s.insU1(ALOAD, 4); // object
			if ((e = es[i]).field != null)
				s.insU2(GETFIELD, p.cons.addField(e.field));
			else
				s.insU2(INVOKEVIRTUAL, p.cons.addProc(e.method));
			if (e.cla == int.class)
				s.insU2(INVOKEVIRTUAL, intCi);
			else if (e.cla == long.class)
				s.insU2(INVOKEVIRTUAL, longCi);
			else if (e.cla == boolean.class)
				s.insU2(INVOKEVIRTUAL, boolCi);
			else if (e.cla == float.class)
				s.insU2(INVOKEVIRTUAL, floatCi);
			else if (e.cla == double.class)
				s.insU2(INVOKEVIRTUAL, doubleCi);
			else
			{
				s.insBox(e.cla);
				s.insU2(INVOKEVIRTUAL, objCi);
			}
			s.jumpHere(if0);
		}
		s.ins0(RETURN);
		p.getCode().setIns(s, false);
		p.getCode().setLocalN(6);
		p.getCode().setStackN(5);
		y.getProcs().addProc(p);
	}

	/** Example: <code>return new A();</code> if nullary constructor found */
	protected Object object(Codec codec) throws Exception
	{
		throw new InstantiationException("no nullary constructor");
	}

	static final Bytes NAME_object = Bytecode.utf("object");
	static final Bytes DESC_object = Bytecode.utf(Class2.descript(Class2.declaredMethod1(
		Clazz.class, "object")));

	private static void makeObject(Bytecode y, int classCi)
	{
		Procedure p = new Procedure(y.cons);
		p.setModifier(Mod2.PROTECTED | Mod2.FINAL);
		p.setNameCi(p.cons.addUtf(NAME_object));
		p.setDescCi(p.cons.addUtf(DESC_object));
		Instruction s = new Instruction(p.cons, 250);
		s.insU2(NEW, classCi);
		s.ins0(DUP);
		s.insU2(INVOKESPECIAL, p.cons.addCtor0(classCi));
		s.ins0(ARETURN);
		p.getCode().setIns(s, false);
		p.getCode().setLocalN(2);
		p.getCode().setStackN(2);
		y.getProcs().addProc(p);
	}

	/**
	 * Example:
	 * 
	 * <pre>
	 * A a = (A)o;
	 * switch(x) {
	 *   case 2: a.f2 = (Class2)v; return;
	 *   case 5: a.m5((Class5)v); return;
	 *   ...
	 *   default: throw new ClassCastException();
	 * }</pre>
	 */
	protected abstract void decode(Object o, int x, Object v);

	protected abstract void decode(Object o, int x, long v);

	/** includes floats */
	protected abstract void decode(Object o, int x, double v);

	static final Bytes NAME_decode = Bytecode.utf("decode");
	static final Bytes DESC_decode = Bytecode.utf(Class2.descript( //
	Class2.declaredMethod(Clazz.class, "decode", Object.class, int.class, Object.class)));
	static final Bytes DESC_decodeL = Bytecode.utf(Class2.descript( //
	Class2.declaredMethod(Clazz.class, "decode", Object.class, int.class, long.class)));
	static final Bytes DESC_decodeD = Bytecode.utf(Class2.descript( //
	Class2.declaredMethod(Clazz.class, "decode", Object.class, int.class, double.class)));

	private static void makeDecode(Bytecode y, Property[] ds, int classCi, int type)
	{
		Procedure p = new Procedure(y.cons);
		p.setModifier(Mod2.PROTECTED | Mod2.FINAL);
		p.setNameCi(p.cons.addUtf(NAME_decode));
		p.setDescCi(p.cons.addUtf(type == 0 ? DESC_decode : type == 1 ? DESC_decodeL
			: DESC_decodeD));
		Instruction s = new Instruction(p.cons, 250);
		s.ins0(ALOAD1);
		s.insU2(CHECKCAST, classCi); // object
		s.ins0(type == 0 ? ALOAD3 : type == 1 ? LLOAD3 : DLOAD3);
		s.ins0(ILOAD2);
		long sw = s.insSwitchTable(0, ds.length - 1);
		int sw0 = s.addr; // default
		s.switchTableHere(sw, -1);
		int exCi = p.cons.addClass(ClassCastException.class);
		s.insU2(NEW, exCi);
		s.ins0(DUP);
		s.insU2(INVOKESPECIAL, p.cons.addCtor0(exCi));
		s.ins0(ATHROW);
		Property d;
		for (int i = 0; i < ds.length && (d = ds[i]) != null; i++)
			if (type == 0 ? d.cla.isPrimitive() && d.cla != boolean.class //
			: type == 1 ? d.cla != int.class && d.cla != long.class //
			: d.cla != double.class && d.cla != float.class)
				s.switchTable(sw, i, sw0);
			else
			{
				s.switchTableHere(sw, i);
				if (type == 0)
					if (d.cla == boolean.class)
						s.insUnboxNarrow(d.cla);
					else
						s.insU2(CHECKCAST, p.cons.addClass(d.cla));
				else if (type == 1 && d.cla == int.class)
					s.ins0(L2I);
				else if (type == 2 && d.cla == float.class)
					s.ins0(D2F);
				if (d.field != null)
					s.insU2(PUTFIELD, p.cons.addField(d.field));
				else
					s.insU2(INVOKEVIRTUAL, p.cons.addProc(d.method));
				s.ins0(RETURN);
			}
		p.getCode().setIns(s, false);
		p.getCode().setLocalN(5);
		p.getCode().setStackN(5);
		y.getProcs().addProc(p);
	}
}
