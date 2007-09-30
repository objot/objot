//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.codec;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import objot.bytecode.Bytecode;
import objot.bytecode.Element;
import objot.bytecode.Instruction;
import objot.bytecode.Opcode;
import objot.bytecode.Procedure;
import objot.util.Bytes;
import objot.util.Class2;
import objot.util.Mod2;


abstract class Clazz
{
	static final Field F_encs = Class2.declaredField(Clazz.class, "encs");
	Property[] encs;
	HashMap<String, Property> decs = new HashMap<String, Property>();

	static Clazz clazz(Class<?> c) throws Exception
	{
		HashMap<String, Property> es_ = new HashMap<String, Property>();
		for (Field f: c.getFields())
			if ((f.getModifiers() & Modifier.STATIC) == 0)
			{
				Enc e = f.getAnnotation(Enc.class);
				EncDec gs = f.getAnnotation(EncDec.class);
				if (e != null || gs != null)
					new Property(f, e, null, gs, true).into(es_);
			}
		for (Method m: c.getMethods())
			if ((m.getModifiers() & Modifier.STATIC) == 0)
			{
				Enc e = m.getAnnotation(Enc.class);
				if (e != null)
					new Property(m, e, null, true).into(es_);
			}
		Property[] es = es_.values().toArray(new Property[es_.size()]);

		HashMap<String, Property> ds_ = new HashMap<String, Property>();
		for (Field f: c.getFields())
			if ((f.getModifiers() & Modifier.STATIC) == 0)
			{
				Dec d = f.getAnnotation(Dec.class);
				EncDec gs = f.getAnnotation(EncDec.class);
				if (d != null || gs != null)
					new Property(f, null, d, gs, false).into(ds_);
			}
		for (Method m: c.getMethods())
			if ((m.getModifiers() & Modifier.STATIC) == 0)
			{
				Dec d = m.getAnnotation(Dec.class);
				if (d != null)
					new Property(m, null, d, false).into(ds_);
			}
		Property[] ds = ds_.values().toArray(new Property[ds_.size()]);
		for (int i = 0; i < ds.length; i++)
			ds[i].index = i;

		Clazz z = make(c, es, ds);
		z.encs = es;
		z.decs = ds_;
		return z;
	}

	private static Clazz make(Class<?> c, Property[] es, Property[] ds) throws Exception
	{
		String name = Clazz.class.getName() + "$$" + c.getName().replace('.', '$');
		Bytecode b = new Bytecode();
		int superCi = b.cons.addClass(Clazz.class);
		b.head.setModifier(Modifier.FINAL | Mod2.SYNTHETIC);
		b.head.setClassCi(b.cons.addClass(b.cons.addUcs(Class2.pathName(name))));
		b.head.setSuperCi(superCi);
		b.getProcs().addProc(Procedure.addCtor0(b.cons, superCi, 0));

		int classCi = b.cons.addClass(c);
		if (es.length > 0)
		{
			int encsCi = b.cons.addField(F_encs);
			int allowCi = b.cons.addProc(Property.M_allow);
			int nameCi = b.cons.addField(Property.F_name);
			makeEncode(b, es, classCi, encsCi, allowCi, nameCi);
			makeEncodeRefs(b, es, classCi, encsCi, allowCi);
		}
		makeObject(b, classCi);
		if (ds.length > 0)
		{
			makeDecode(b, ds, classCi, 0);
			makeDecode(b, ds, classCi, 1);
			makeDecode(b, ds, classCi, 2);
		}

		return Class2.<Clazz>load(Clazz.class.getClassLoader(), name, b.normalize())
			.newInstance();
	}

	static final Bytes NAME_encodeRefs = Element.utf("encodeRefs");
	static final Bytes DESC_encodeRefs = Element.utf(Class2.descript( //
		Class2.declaredMethod1(Clazz.class, "encodeRefs")));

	/**
	 * Example:
	 * 
	 * <pre>
	 * {@link Property}[] es = {@link #encs};
	 * A a = (A)o; // shouldn't cause ClassCastException
	 * // no property 0
	 * if (es[1].{@link Property#allow}(forClass) e.{@link Encoder#refs}(a.f1);
	 * // no property 1
	 * if (es[3].{@link Property#allow}(forClass)) e.{@link Encoder#refs}(a.m3());
	 * ...
	 * </pre>
	 */
	void encodeRefs(Encoder e, Object o, Class<?> forClass) throws Exception
	{
	}

	static final Bytes NAME_encode = Element.utf("encode");
	static final Bytes DESC_encode = Element.utf(Class2.descript( //
		Class2.declaredMethod1(Clazz.class, "encode")));

	/**
	 * Example:
	 * 
	 * <pre>
	 * {@link Property}[] es = {@link #encs};
	 * A a = (A)o; // shouldn't cause ClassCastException
	 * if (es[0].{@link Property#allow}(forClass))
	 *   e.{@link Encoder#value(String, int)}(es[0].{@link Property#name}, a.f0);
	 * if (es[1].{@link Property#allow}(forClass))
	 *   e.{@link Encoder#value(String, Object)}(es[1].{@link Property#name}, a.m1());
	 * ...
	 * </pre>
	 */
	void encode(Encoder e, Object o, Class<?> forClass) throws Exception
	{
	}

	private static void makeEncodeRefs(Bytecode b, Property[] es, int classCi, int encsCi,
		int allowCi)
	{
		Procedure p = new Procedure(b.cons);
		p.setModifier(Modifier.FINAL);
		p.setNameCi(p.cons.addUtf(NAME_encodeRefs));
		p.setDescCi(p.cons.addUtf(DESC_encodeRefs));
		Instruction s = new Instruction(250);
		s.ins0(Opcode.ALOAD2);
		s.insU2(Opcode.CHECKCAST, classCi);
		s.insU1(Opcode.ASTORE, 4); // object
		s.ins0(Opcode.ALOAD0);
		s.insU2(Opcode.GETFIELD, encsCi); // properties
		int refsCi = p.cons.addProc(Encoder.M_refs);
		Property e;
		for (int i = 0; i < es.length; i++)
			if ( !(e = es[i]).cla.isPrimitive() && !Number.class.isAssignableFrom(e.cla)
				&& e.cla != Boolean.class)
			{
				s.ins0(Opcode.DUP);
				s.insS2(Opcode.SIPUSH, i);
				s.ins0(Opcode.AALOAD); // property
				s.ins0(Opcode.ALOAD3);
				s.insU2(Opcode.INVOKEVIRTUAL, allowCi);
				int if0 = s.insJump(Opcode.IFIE0);
				s.ins0(Opcode.ALOAD1);
				s.insU1(Opcode.ALOAD, 4); // object
				if (e.field != null)
					s.insU2(Opcode.GETFIELD, p.cons.addField(e.field));
				else
					s.insU2(Opcode.INVOKEVIRTUAL, p.cons.addProc(e.method));
				s.insU2(Opcode.INVOKEVIRTUAL, refsCi);
				s.jumpFrom(if0);
			}
		s.ins0(Opcode.RETURN);
		p.getCode().setIns(s, false);
		p.getCode().setLocalN(5);
		p.getCode().setStackN(4);
		b.getProcs().addProc(p);
	}

	private static void makeEncode(Bytecode b, Property[] es, int classCi, int encsCi,
		int allowCi, int nameCi)
	{
		Procedure p = new Procedure(b.cons);
		p.setModifier(Modifier.FINAL);
		p.setNameCi(p.cons.addUtf(NAME_encode));
		p.setDescCi(p.cons.addUtf(DESC_encode));
		Instruction s = new Instruction(250);
		s.ins0(Opcode.ALOAD2);
		s.insU2(Opcode.CHECKCAST, classCi);
		s.insU1(Opcode.ASTORE, 4); // object
		s.ins0(Opcode.ALOAD0);
		s.insU2(Opcode.GETFIELD, encsCi); // properties
		int intCi = p.cons.addProc(Encoder.M_valueInt);
		int longCi = p.cons.addProc(Encoder.M_valueLong);
		int boolCi = p.cons.addProc(Encoder.M_valueBool);
		int floatCi = p.cons.addProc(Encoder.M_valueFloat);
		int doubleCi = p.cons.addProc(Encoder.M_valueDouble);
		int objCi = p.cons.addProc(Encoder.M_valueObject);
		Property e;
		for (int i = 0; i < es.length; i++)
		{
			s.ins0(Opcode.DUP);
			s.insS2(Opcode.SIPUSH, i);
			s.ins0(Opcode.AALOAD); // property
			s.ins0(Opcode.DUP); // property
			s.insU1(Opcode.ASTORE, 5); // property
			s.ins0(Opcode.ALOAD3);
			s.insU2(Opcode.INVOKEVIRTUAL, allowCi);
			int if0 = s.insJump(Opcode.IFIE0);
			s.ins0(Opcode.ALOAD1);
			s.insU1(Opcode.ALOAD, 5); // property
			s.insU2(Opcode.GETFIELD, nameCi);
			s.insU1(Opcode.ALOAD, 4); // object
			if ((e = es[i]).field != null)
				s.insU2(Opcode.GETFIELD, p.cons.addField(e.field));
			else
				s.insU2(Opcode.INVOKEVIRTUAL, p.cons.addProc(e.method));
			if (e.cla == int.class)
				s.insU2(Opcode.INVOKEVIRTUAL, intCi);
			else if (e.cla == long.class)
				s.insU2(Opcode.INVOKEVIRTUAL, longCi);
			else if (e.cla == boolean.class)
				s.insU2(Opcode.INVOKEVIRTUAL, boolCi);
			else if (e.cla == float.class)
				s.insU2(Opcode.INVOKEVIRTUAL, floatCi);
			else if (e.cla == double.class)
				s.insU2(Opcode.INVOKEVIRTUAL, doubleCi);
			else
			{
				s.insBox(p.cons, e.cla);
				s.insU2(Opcode.INVOKEVIRTUAL, objCi);
			}
			s.jumpFrom(if0);
		}
		s.ins0(Opcode.RETURN);
		p.getCode().setIns(s, false);
		p.getCode().setLocalN(6);
		p.getCode().setStackN(5);
		b.getProcs().addProc(p);
	}

	static final Bytes NAME_object = Element.utf("object");
	static final Bytes DESC_object = Element.utf(Class2.descript( //
		Class2.declaredMethod1(Clazz.class, "object")));

	/** Example: <code>return new A();</code> */
	abstract Object object() throws Exception;

	private static void makeObject(Bytecode b, int classCi)
	{
		Procedure p = new Procedure(b.cons);
		p.setModifier(Modifier.FINAL);
		p.setNameCi(p.cons.addUtf(NAME_object));
		p.setDescCi(p.cons.addUtf(DESC_object));
		Instruction s = new Instruction(250);
		s.insU2(Opcode.NEW, classCi);
		s.ins0(Opcode.DUP);
		s.insU2(Opcode.INVOKESPECIAL, p.cons.addCtor0(classCi));
		s.ins0(Opcode.ARETURN);
		p.getCode().setIns(s, false);
		p.getCode().setLocalN(1);
		p.getCode().setStackN(2);
		b.getProcs().addProc(p);
	}

	static final Bytes NAME_decode = Element.utf("decode");
	static final Bytes DESC_decode = Element.utf(Class2.descript( //
		Class2.declaredMethod(Clazz.class, "decode", Object.class, int.class, Object.class)));
	static final Bytes DESC_decodeL = Element.utf(Class2.descript( //
		Class2.declaredMethod(Clazz.class, "decode", Object.class, int.class, long.class)));
	static final Bytes DESC_decodeD = Element.utf(Class2.descript( //
		Class2.declaredMethod(Clazz.class, "decode", Object.class, int.class, double.class)));

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
	abstract void decode(Object o, int x, Object v) throws Exception;

	abstract void decode(Object o, int x, long v) throws Exception;

	/** includes floats */
	abstract void decode(Object o, int x, double v) throws Exception;

	private static void makeDecode(Bytecode b, Property[] ds, int classCi, int type)
	{
		Procedure p = new Procedure(b.cons);
		p.setModifier(Modifier.FINAL);
		p.setNameCi(p.cons.addUtf(NAME_decode));
		p.setDescCi(p.cons.addUtf(type == 0 ? DESC_decode : type == 1 ? DESC_decodeL
			: DESC_decodeD));
		Instruction s = new Instruction(250);
		s.ins0(Opcode.ALOAD1);
		s.insU2(Opcode.CHECKCAST, classCi); // object
		s.ins0(type == 0 ? Opcode.ALOAD3 : type == 1 ? Opcode.LLOAD3 : Opcode.DLOAD3);
		s.ins0(Opcode.ILOAD2);
		long sw = s.insSwitchTable(0, ds.length - 1);
		int sw0 = s.addr; // default
		s.switchTableFrom(sw, -1);
		int exCi = p.cons.addClass(ClassCastException.class);
		s.insU2(Opcode.NEW, exCi);
		s.ins0(Opcode.DUP);
		s.insU2(Opcode.INVOKESPECIAL, p.cons.addCtor0(exCi));
		s.ins0(Opcode.ATHROW);
		Property d;
		for (int i = 0; i < ds.length && (d = ds[i]) != null; i++)
			if (type == 0 ? d.cla.isPrimitive() && d.cla != boolean.class //
			: type == 1 ? d.cla != int.class && d.cla != long.class //
			: d.cla != double.class && d.cla != float.class)
				s.switchTable(sw, i, sw0);
			else
			{
				s.switchTableFrom(sw, i);
				if (type == 0)
					if (d.cla == boolean.class)
						s.insUnboxNarrow(p.cons, d.cla);
					else
						s.insU2(Opcode.CHECKCAST, p.cons.addClass(d.cla));
				else if (type == 1 && d.cla == int.class)
					s.ins0(Opcode.L2I);
				else if (type == 2 && d.cla == float.class)
					s.ins0(Opcode.D2F);
				if (d.field != null)
					s.insU2(Opcode.PUTFIELD, p.cons.addField(d.field));
				else
					s.insU2(Opcode.INVOKEVIRTUAL, p.cons.addProc(d.method));
				s.ins0(Opcode.RETURN);
			}
		p.getCode().setIns(s, false);
		p.getCode().setLocalN(5);
		p.getCode().setStackN(5);
		b.getProcs().addProc(p);
	}
}
