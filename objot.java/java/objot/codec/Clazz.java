//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.codec;

import static objot.bytecode.Element.utf;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import objot.bytecode.Bytecode;
import objot.bytecode.Instruction;
import objot.bytecode.Opcode;
import objot.bytecode.Procedure;
import objot.util.Bytes;
import objot.util.Class2;


public abstract class Clazz
{
	private static final Field F_encs = Class2.declaredField(Clazz.class, "encs");
	private Property[] encs;

	private HashMap<String, Property> decs = new HashMap<String, Property>();

	static Clazz create(Class<?> c) throws Exception
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
		HashMap<String, Property> ds = new HashMap<String, Property>();
		for (Field f: c.getFields())
			if ((f.getModifiers() & Modifier.STATIC) == 0)
			{
				Dec d = f.getAnnotation(Dec.class);
				EncDec gs = f.getAnnotation(EncDec.class);
				if (d != null || gs != null)
					new Property(f, null, d, gs, false).into(ds);
			}
		for (Method m: c.getMethods())
			if ((m.getModifiers() & Modifier.STATIC) == 0)
			{
				Dec d = m.getAnnotation(Dec.class);
				if (d != null)
					new Property(m, null, d, false).into(ds);
			}
		return make(c, es, ds);
	}

	private static Clazz make(Class<?> c, Property[] es, HashMap<String, Property> ds)
		throws Exception
	{
		String name = Clazz.class.getName() + "$$" + c.getName().replace('.', '$');
		Bytecode b = new Bytecode();
		int superCi = b.cons.appendClass(Clazz.class);
		b.head.setModifier(Modifier.PUBLIC);
		b.head.setClassCi(b.cons.appendClass(b.cons.appendUcs(Class2.pathName(name))));
		b.head.setSuperCi(superCi);
		b.getProcs().appendProc(Procedure.appendEmptyCtor(b.cons, superCi, Modifier.PUBLIC));
		int classCi = b.cons.appendClass(c);
		int encsCi = b.cons.appendField(F_encs);
		int allowCi = b.cons.appendIproc(Property.M_allow);
		int nameCi = b.cons.appendField(Property.F_name);

		makeEncodeRefs(b, es, classCi, encsCi, allowCi);
		makeEncode(b, es, classCi, encsCi, allowCi, nameCi);
		// makeDecode(zb, ds);

		b.printTo(System.out, 0, 0, 1000, false);
		Clazz z = Bytecode.LOADER.<Clazz>loadClass(name, b.normalize()).newInstance();
		z.encs = es;
		z.decs = ds;
		return z;
	}

	private static final Bytes NAME_encodeRefs = utf("encodeRefs");
	private static final Bytes DESC_encodeRefs = utf(Class2.descript //
		(Class2.declaredMethod1(Clazz.class, "encodeRefs")));

	/**
	 * Example:
	 * 
	 * <pre>
	 * {@link Property}[] es = {@link #encs};
	 * A a = (A)o;
	 * // no property 0
	 * if (es[1].{@link Property#allow}(forClass) e.{@link Encoder#refs}(a.f1);
	 * // no property 1
	 * if (es[3].{@link Property#allow}(forClass)) e.{@link Encoder#refs}(a.m3());
	 * ...
	 * </pre>
	 */
	abstract void encodeRefs(Encoder e, Object o, Class<?> forClass) throws Exception;

	private static final Bytes NAME_encode = utf("encode");
	private static final Bytes DESC_encode = utf(Class2.descript //
		(Class2.declaredMethod1(Clazz.class, "encode")));

	/**
	 * Example:
	 * 
	 * <pre>
	 * {@link Property}[] es = {@link #encs};
	 * A a = (A)o;
	 * if (es[0].{@link Property#allow}(forClass))
	 *   e.{@link Encoder#value(String, int)}(es[0].{@link Property#name}, a.f0);
	 * if (es[1].{@link Property#allow}(forClass))
	 *   e.{@link Encoder#value(String, Object)}(es[1].{@link Property#name}, a.m1());
	 * ...
	 * </pre>
	 */
	abstract void encode(Encoder e, Object o, Class<?> forClass) throws Exception;

	private static void makeEncodeRefs(Bytecode b, Property[] es, int classCi, int encsCi,
		int allowCi)
	{
		Procedure p = new Procedure(b.cons);
		p.setNameCi(p.cons.appendUtf(NAME_encodeRefs));
		p.setDescCi(p.cons.appendUtf(DESC_encodeRefs));
		Instruction s = new Instruction(256);
		s.ins0(Opcode.ALOAD0);
		s.insU2(Opcode.GETFIELD, encsCi); // properties
		s.ins0(Opcode.ALOAD2);
		s.insU2(Opcode.CHECKCAST, classCi); // object
		int refsCi = p.cons.appendIproc(Encoder.M_refs);
		Property e;
		for (int i = 0; i < es.length; i++)
			if ( !(e = es[i]).cla.isPrimitive() && !Number.class.isAssignableFrom(e.cla)
				&& e.cla != Boolean.class)
			{
				s.ins0(Opcode.DUPI); // properties
				s.insS2(Opcode.SIPUSH, i);
				s.ins0(Opcode.AALOAD); // property
				s.ins0(Opcode.ALOAD3);
				s.insU2(Opcode.INVOKEVIRTUAL, allowCi);
				int adIf = s.addr;
				s.insS2(Opcode.IFIE0, 0);
				s.ins0(Opcode.ALOAD1);
				s.ins0(Opcode.DUPI); // object
				if (e.field != null)
					s.insU2(Opcode.GETFIELD, p.cons.appendField(e.field));
				else
					s.insU2(Opcode.INVOKEVIRTUAL, p.cons.appendIproc(e.method));
				s.insU2(Opcode.INVOKEVIRTUAL, refsCi);
				s.write0s2(adIf + 1, s.addr - adIf);
			}
		s.ins0(Opcode.RETURN);
		p.getCode().setIns(s, false);
		p.getCode().setLocalN(4);
		p.getCode().setStackN(4);
		b.getProcs().appendProc(p);
	}

	private static void makeEncode(Bytecode b, Property[] es, int classCi, int encsCi,
		int allowCi, int nameCi)
	{
		Procedure p = new Procedure(b.cons);
		p.setNameCi(p.cons.appendUtf(NAME_encode));
		p.setDescCi(p.cons.appendUtf(DESC_encode));
		Instruction s = new Instruction(256);
		s.ins0(Opcode.ALOAD0);
		s.insU2(Opcode.GETFIELD, encsCi); // properties
		s.ins0(Opcode.ALOAD2);
		s.insU2(Opcode.CHECKCAST, classCi); // object
		int intCi = p.cons.appendIproc(Encoder.M_valueInt);
		int longCi = p.cons.appendIproc(Encoder.M_valueLong);
		int boolCi = p.cons.appendIproc(Encoder.M_valueBool);
		int floatCi = p.cons.appendIproc(Encoder.M_valueFloat);
		int doubleCi = p.cons.appendIproc(Encoder.M_valueDouble);
		int objCi = p.cons.appendIproc(Encoder.M_valueObject);
		Property e;
		for (int i = 0; i < es.length; i++)
		{
			s.ins0(Opcode.DUPI); // properties
			s.insS2(Opcode.SIPUSH, i);
			s.ins0(Opcode.AALOAD); // property
			s.ins0(Opcode.DUP); // property
			s.ins0(Opcode.ALOAD3);
			s.insU2(Opcode.INVOKEVIRTUAL, allowCi);
			int adIf = s.addr;
			s.insS2(Opcode.IFIE0, 0);
			s.ins0(Opcode.ALOAD1);
			s.ins0(Opcode.SWAP); // property
			s.insU2(Opcode.GETFIELD, nameCi);
			s.ins0(Opcode.DUPII); // object
			if ((e = es[i]).field != null)
				s.insU2(Opcode.GETFIELD, p.cons.appendField(e.field));
			else
				s.insU2(Opcode.INVOKEVIRTUAL, p.cons.appendIproc(e.method));
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
			s.write0s2(adIf + 1, s.addr - adIf);
		}
		s.ins0(Opcode.RETURN);
		p.getCode().setIns(s, false);
		p.getCode().setLocalN(4);
		p.getCode().setStackN(5);
		b.getProcs().appendProc(p);
	}

	int decIndex(String name, Class<?> forClass)
	{
		Property p = decs.get(name);
		return p != null && p.allow(forClass) ? p.index : -1;
	}
}
