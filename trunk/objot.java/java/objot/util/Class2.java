package objot.util;

public class Class2
{
	protected Class2()
	{
		throw new AbstractMethodError();
	}

	/**
	 * Primitive class to box class.
	 * 
	 * @param boxVoid whether box void.class to Void.class
	 * @return the box class
	 * @throws ClassCastException if this class is not primitive, or if this class is void
	 *             and boxVoid is false
	 */
	public static Class<?> box(Class<?> c, boolean boxVoid)
	{
		if (c == int.class)
			return Integer.class;
		else if (c == boolean.class)
			return Boolean.class;
		else if (c == long.class)
			return Long.class;
		else if (c == byte.class)
			return Byte.class;
		else if (c == char.class)
			return Character.class;
		else if (c == short.class)
			return Short.class;
		else if (c == float.class)
			return Float.class;
		else if (c == double.class)
			return Double.class;
		else if (c == void.class && boxVoid)
			return Void.class;
		else
			throw new ClassCastException();
	}

	/**
	 * Box class to primitive class.
	 * 
	 * @param unboxVoid whether unbox Void.class to void.class
	 * @return the primitive class
	 * @throws ClassCastException if this is not primitive box class, or if this is Void
	 *             class and unboxVoid is false
	 */
	public static Class<?> unbox(Class<?> c, boolean unboxVoid)
	{
		if (c == Integer.class)
			return int.class;
		else if (c == Boolean.class)
			return boolean.class;
		else if (c == Long.class)
			return long.class;
		else if (c == Byte.class)
			return byte.class;
		else if (c == Character.class)
			return char.class;
		else if (c == Short.class)
			return short.class;
		else if (c == Float.class)
			return float.class;
		else if (c == Double.class)
			return double.class;
		else if (c == Void.class && unboxVoid)
			return void.class;
		else
			throw new ClassCastException();
	}

	/** @return class name without package. */
	public static String selfName(Class<?> c)
	{
		return selfName(c.getName());
	}

	/** @return class name without package. */
	public static String selfName(String className)
	{
		return className.substring(className.lastIndexOf('.') + 1);
	}

	public static String packageName(Class<?> c)
	{
		return packageName(c.getName());
	}

	public static String packageName(String className)
	{
		int dot = className.lastIndexOf('.');
		return dot > 0 ? className.substring(0, dot) : "";
	}

	public static String internalName(Class<?> c)
	{
		return internalName(c.getName());
	}

	public static String internalName(String className)
	{
		return className.replace('.', '/');
	}

	public static String resourceName(Class<?> c)
	{
		return resourceName(c.getName());
	}

	public static String resourceName(String className)
	{
		return internalName(className).concat(".class");
	}

	public static String descriptor(Class<?> c)
	{
		return descriptor(c.getName());
	}

	public static String descriptor(String className)
	{
		if (className.equals("int"))
			return "I";
		if (className.equals("boolean"))
			return "Z";
		if (className.equals("byte"))
			return "B";
		if (className.equals("short"))
			return "S";
		if (className.equals("char"))
			return "C";
		if (className.equals("long"))
			return "J";
		if (className.equals("float"))
			return "F";
		if (className.equals("double"))
			return "D";
		if (className.equals("void"))
			return "V";
		if (className.charAt(0) == '[')
			return internalName(className);
		return 'L' + internalName(className) + ';';
	}
}
