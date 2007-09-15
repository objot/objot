package objot.bytecode;

import java.lang.reflect.Modifier;


public enum ProcKind
{
	/** object instance method (excluding constructor) */
	OBJECT(1 << 23),
	/** constructor */
	CTOR(1 << 22),
	/** static class method (excluding class initializer) */
	CLASS(1 << 21),
	/** static initializer */
	CINIT(1 << 20),
	//
	/** object or ctor */
	INSTANCE(OBJECT.value | CTOR.value),
	/** class or cinit */
	STATIC(CLASS.value | CINIT.value),
	/** object or class */
	NORMAL(OBJECT.value | CLASS.value),
	/** ctor or cinit */
	INITIAL(CTOR.value | CINIT.value),
	//
	/** ctor or class or cinit */
	NOTOBJECT(CTOR.value | CLASS.value | CINIT.value),
	/** object or class or cinit */
	NOTCTOR(OBJECT.value | CLASS.value | CINIT.value),
	/** object or ctor or cinit */
	NOTCLASS(OBJECT.value | CTOR.value | CINIT.value),
	/** object or ctor or class */
	NOTCINIT(OBJECT.value | CTOR.value | CLASS.value),
	//
	/** object or ctor or class or cinit */
	ALL(OBJECT.value | CTOR.value | CLASS.value | CINIT.value);

	public final int value;

	private ProcKind(int v)
	{
		value = v;
	}

	/**
	 * No multi-kind checked.
	 * 
	 * @return a single-kind
	 */
	public static ProcKind get(int modifier, char nameFirstChar)
	{
		boolean isInit = nameFirstChar == '<';
		boolean isStatic = (modifier & Modifier.STATIC) != 0;
		if ( !isStatic && !isInit)
			return OBJECT;
		if ( !isStatic && isInit)
			return CTOR;
		if (isStatic && !isInit)
			return CLASS;
		// isStatic && isInit
		return CINIT;
	}

	public boolean isSingle()
	{
		switch (this)
		{
		case OBJECT:
		case CTOR:
		case CLASS:
		case CINIT:
			return true;
		default:
			return false;
		}
	}

	public static boolean isSingle(int kind)
	{
		return kind == OBJECT.value || kind == CTOR.value || kind == CLASS.value
			|| kind == CINIT.value;
	}

	public boolean isMatching(ProcKind kind)
	{
		return kind != null && (value & kind.value) != 0;
	}

	public boolean isMatching(int kind)
	{
		return isMatching(value, kind);
	}

	public static boolean isMatching(int kindA, int kindB)
	{
		return (kindA & kindB & ALL.value) != 0;
	}
}
