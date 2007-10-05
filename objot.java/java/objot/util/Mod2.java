//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.util;

import java.lang.reflect.Modifier;


public class Mod2
	extends Modifier
{
	public Mod2()
	{
		throw new AbstractMethodError();
	}

	/** reuse {@link #SYNCHRONIZED} */
	public static final int SUPER = 0x0020;
	public static final int SYNTHETIC = 0x1000;
	public static final int ANNOTATION = 0x2000;
	public static final int ENUM = 0x4000;
	/** reuse {@link #VOLATILE} */
	public static final int BRIDGE = 0x0040;
	/** reuse {@link #TRANSIENT} */
	public static final int VARARGS = 0x0080;

	/** default visible */
	public static final int FRIEND = 0x00010000;
	public static final int PUBLIC_PROTECT = PUBLIC | PROTECTED;
	public static final int FRIEND_PRIVATE = FRIEND | PRIVATE;
	public static final int NOT_PRIVATE = PUBLIC | PROTECTED | FRIEND;
	public static final int NOT_PUBLIC = PROTECTED | FRIEND | PRIVATE;
	/** all visible */
	public static final int VISIBLE = PUBLIC | PROTECTED | FRIEND | PRIVATE;

	/** about procedures */
	public static class P
	{
		public P()
		{
			throw new AbstractMethodError();
		}

		/** object instance method (excluding constructor) */
		public static final int OBJECT = 0x00100000;
		/** constructor */
		public static final int CTOR = 0x00200000;
		/** static class method (excluding class initializer) */
		public static final int CLASS = 0x00400000;
		/** static initializer */
		public static final int CINIT = 0x00800000;
		/** object or ctor */
		public static final int INSTANCE = OBJECT | CTOR;
		/** class or cinit */
		@SuppressWarnings("hiding")
		public static final int STATIC = CLASS | CINIT;
		/** object or class */
		public static final int NORMAL = OBJECT | CLASS;
		/** ctor or cinit */
		public static final int INITER = CTOR | CINIT;
		/** ctor or class or cinit */
		public static final int NOTOBJECT = CTOR | CLASS | CINIT;
		/** object or class or cinit */
		public static final int NOTCTOR = OBJECT | CLASS | CINIT;
		/** object or ctor or cinit */
		public static final int NOTCLASS = OBJECT | CTOR | CINIT;
		/** object or ctor or class */
		public static final int NOTCINIT = OBJECT | CTOR | CLASS;
		/** object or ctor or class or cinit */
		public static final int PROC = OBJECT | CTOR | CLASS | CINIT;
	}

	public static int get(int mod, int procNameFirstChar)
	{
		if ((mod & VISIBLE) == 0)
			mod |= FRIEND;
		if (procNameFirstChar != 0)
			if ((mod & STATIC) == 0)
				mod |= procNameFirstChar != '<' ? P.OBJECT : P.CTOR;
			else
				mod |= procNameFirstChar != '<' ? P.CLASS : P.CINIT;
		return mod;
	}

	public static String toString(int mod)
	{
		StringBuilder s = new StringBuilder();
		if ((mod & P.STATIC) != 0)
			mod |= STATIC;

		if ((mod & PUBLIC) != 0)
			s.append("public ");
		if ((mod & PROTECTED) != 0)
			s.append("protected ");
		if ((mod & FRIEND) != 0)
			s.append("friend ");
		if ((mod & PRIVATE) != 0)
			s.append("private ");

		if ((mod & ABSTRACT) != 0)
			s.append("abstract ");
		if ((mod & STATIC) != 0)
			s.append("static ");
		if ((mod & FINAL) != 0)
			s.append("final ");
		if ((mod & TRANSIENT) != 0)
			s.append("transient ");
		if ((mod & VOLATILE) != 0)
			s.append("volatile ");
		if ((mod & SYNCHRONIZED) != 0)
			s.append("synchronized ");
		if ((mod & NATIVE) != 0)
			s.append("native ");
		if ((mod & STRICT) != 0)
			s.append("strictfp ");
		if ((mod & INTERFACE) != 0)
			s.append("interface ");

		if ((mod & P.NORMAL) != 0)
			s.append("() ");
		if ((mod & P.CTOR) != 0)
			s.append("<init>() ");
		if ((mod & P.CINIT) != 0)
			s.append("<cinit>() ");

		return s.length() == 0 ? "" : s.deleteCharAt(s.length() - 1).toString();
	}
}
