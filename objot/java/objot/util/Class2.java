//
// Copyright 2007-2010 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.util;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.JarURLConnection;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;


public class Class2
{
	protected Class2()
	{
		throw new AbstractMethodError();
	}

	public static String systemIdentity(Object o)
	{
		return o == null ? "null" : o.getClass().getName() + '@' + System.identityHashCode(o);
	}

	@SuppressWarnings("unchecked")
	public static <T>T cast(Object o, Class<T> c)
	{
		if (o == null || c.isInstance(o))
			return (T)o;
		throw new ClassCastException(o.getClass().getName() + " forbidden for " + c.getName());
	}

	@SuppressWarnings("unchecked")
	public static <T>T cast(Object o, Class<T> c, boolean nullable)
	{
		if (o == null ? nullable : c.isInstance(o))
			return (T)o;
		throw new ClassCastException((o != null ? o.getClass().getName() : "null")
			+ " forbidden for " + c.getName());
	}

	public static boolean castable(Object o, Class<?> c)
	{
		return !c.isPrimitive() && (o == null || c.isInstance(o));
	}

	public static boolean castableBox(Object o, Class<?> c)
	{
		if (c.isPrimitive())
			if (o == null)
				return false;
			else if (c == int.class)
				c = Integer.class;
			else if (c == boolean.class)
				c = Boolean.class;
			else if (c == long.class)
				c = Long.class;
			else if (c == byte.class)
				c = Byte.class;
			else if (c == char.class)
				c = Character.class;
			else if (c == short.class)
				c = Short.class;
			else if (c == float.class)
				c = Float.class;
			else if (c == double.class)
				c = Double.class;
		return o == null || c.isInstance(o);
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
		if (c.isPrimitive())
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
		c.getModifiers();
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
		throw new ClassCastException();
	}

	/**
	 * Try to convert primitive class to box class.
	 *
	 * @param boxVoid whether box void.class to Void.class
	 * @return the box class, or original class if not primitive
	 */
	public static Class<?> boxTry(Class<?> c, boolean boxVoid)
	{
		if (c.isPrimitive())
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
		return c;
	}

	/**
	 * Try to conver box class to primitive class.
	 *
	 * @param unboxVoid whether unbox Void.class to void.class
	 * @return the primitive class, or original class if not box
	 */
	public static Class<?> unboxTry(Class<?> c, boolean unboxVoid)
	{
		c.getModifiers();
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
		return c;
	}

	// ********************************************************************************

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

	public static String pathName(Class<?> c)
	{
		return pathName(c.getName());
	}

	public static String pathName(String className)
	{
		return className.replace('.', '/');
	}

	public static String resourceName(Class<?> c)
	{
		return resourceName(c.getName());
	}

	public static String resourceName(String className)
	{
		return '/' + pathName(className) + ".class";
	}

	public static String descript(Class<?> c)
	{
		return descript(c.getName());
	}

	public static String descript(String className)
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
			return pathName(className);
		return 'L' + pathName(className) + ';';
	}

	public static char descriptChar(Class<?> c)
	{
		return descriptChar(c.getName());
	}

	public static char descriptChar(String className)
	{
		if (className.equals("int"))
			return 'I';
		if (className.equals("boolean"))
			return 'Z';
		if (className.equals("byte"))
			return 'B';
		if (className.equals("short"))
			return 'S';
		if (className.equals("char"))
			return 'C';
		if (className.equals("long"))
			return 'J';
		if (className.equals("float"))
			return 'F';
		if (className.equals("double"))
			return 'D';
		if (className.equals("void"))
			return 'V';
		if (className.charAt(0) == '[')
			return '[';
		return 'L';
	}

	public static String descript(Field f)
	{
		return descript(f.getType());
	}

	/**
	 * @param params (Class or descriptor String)[]
	 * @param Return Class or descriptor String
	 */
	public static String descript(Object Return, Object... params)
	{
		StringBuilder d = new StringBuilder(31);
		d.append('(');
		if (params != null)
			for (Object p: params)
				d.append(p instanceof Class ? descript((Class<?>)p) : (String)p);
		d.append(')');
		d.append(Return instanceof Class ? descript((Class<?>)Return) : (String)Return);
		return d.toString();
	}

	public static String descript(Constructor<?> c)
	{
		return descript(void.class, (Object[])c.getParameterTypes());
	}

	public static String descript(Method m)
	{
		return descript(m.getReturnType(), (Object[])m.getParameterTypes());
	}

	/**
	 * @param m be checked if follows the getter/setter rules
	 * @return this method's property name
	 */
	public static String propertyName(Method m, boolean get)
	{
		String n = m.getName();
		if (get && (m.getParameterTypes().length > 0 || m.getReturnType() == void.class //
			|| n.length() <= 3 || !n.startsWith("get")))
			throw new RuntimeException("invalid getter: " + m);
		if ( !get && (m.getParameterTypes().length != 1 || m.getReturnType() != void.class //
			|| n.length() <= 3 || !n.startsWith("set")))
			throw new RuntimeException("invalid setter: " + m);
		if (n.length() > 4 && Character.isUpperCase(n.charAt(4)))
			return n.substring(3);
		else
			return Character.toLowerCase(n.charAt(3)) + n.substring(4);
	}

	/** @return the method's property name or original name */
	public static String propertyOrName(Method m, boolean get)
	{
		String n = m.getName();
		if (get && (m.getParameterTypes().length > 0 || m.getReturnType() == void.class))
			throw new RuntimeException("invalid getter: " + m);
		if ( !get && m.getParameterTypes().length != 1)
			throw new RuntimeException("invalid setter: " + m);
		if (n.length() <= 3 || !n.startsWith(get ? "get" : "set"))
			return n;
		if (n.length() > 4 && Character.isUpperCase(n.charAt(4)))
			return n.substring(3);
		else
			return Character.toLowerCase(n.charAt(3)) + n.substring(4);
	}

	// ********************************************************************************

	public static Class<?> byName(String name)
	{
		try
		{
			return Class.forName(name);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static Class<?> byName(String name, boolean init, ClassLoader loader)
	{
		try
		{
			return Class.forName(name, init, loader);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static Class<?> typeParamClass(Type t, int paramIndex, Class<?> Default)
	{
		if (t instanceof ParameterizedType)
		{
			Type[] ts = ((ParameterizedType)t).getActualTypeArguments();
			if (paramIndex < ts.length && ts[paramIndex] instanceof Class)
				Default = (Class<?>)ts[paramIndex];
		}
		return Default;
	}

	public static Field field(Class<?> c, String name)
	{
		try
		{
			return c.getField(name);
		}
		catch (NoSuchFieldException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static Field declaredField(Class<?> c, String name)
	{
		try
		{
			return c.getDeclaredField(name);
		}
		catch (NoSuchFieldException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * find all not-private instance fields including those inherited from super classes.
	 * fields in super class are before those in sub class.
	 */
	public static ArrayList<Field> fields(Class<?> c)
	{
		return fields(c, 0, 0, Mod2.PRIVATE | Mod2.STATIC);
	}

	/**
	 * find all matched fields including those inherited from super classes. fields in
	 * super class are before those in sub class.
	 */
	public static ArrayList<Field> fields(Class<?> c, int andMods, int orMods, int noMods)
	{
		ArrayList<Field> s = c.getSuperclass() == null ? new ArrayList<Field>() //
			: fields(c.getSuperclass(), andMods, orMods, noMods);
		for (Field f: c.getDeclaredFields())
			if (Mod2.match(f, andMods, orMods, noMods))
				s.add(f);
		return s;
	}

	/** excludes {@link Mod2.P#INITER} */
	public static Method method(Class<?> c, String name, Class<?>... params)
	{
		try
		{
			return c.getMethod(name, params != null ? params : Array2.CLASSES0);
		}
		catch (NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
	}

	/** excludes {@link Mod2.P#INITER} */
	public static Method method1(Class<?> c, String name)
	{
		for (Method m: c.getMethods())
			if (m.getName().equals(name))
				return m;
		throw new RuntimeException(new NoSuchMethodException(c.getName() + '.' + name));
	}

	/** excludes {@link Mod2.P#INITER} */
	public static Method declaredMethod(Class<?> c, String name, Class<?>... params)
	{
		try
		{
			return c.getDeclaredMethod(name, params != null ? params : Array2.CLASSES0);
		}
		catch (NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
	}

	/** excludes {@link Mod2.P#INITER} */
	public static Method declaredMethod1(Class<?> c, String name)
	{
		for (Method m: c.getDeclaredMethods())
			if (m.getName().equals(name))
				return m;
		throw new RuntimeException(new NoSuchMethodException(c.getName() + '.' + name));
	}

	/**
	 * find all not-private instance methods including those inherited from super classes.
	 * methods in super class are before those in sub class.
	 */
	public static ArrayList<Method> methods(Class<?> c)
	{
		return methods(c, 0, 0, Mod2.PRIVATE | Mod2.STATIC);
	}

	/**
	 * find all matched methods including those inherited and not-overriden from super
	 * classes. excludes {@link Mod2.P#INITER}. methods in super class are before those in
	 * sub class, the overriden methods are replaced in their original position
	 */
	public static ArrayList<Method> methods(Class<?> c, int andMods, int orMods, int noMods)
	{
		ArrayList<Method> s = c.getSuperclass() == null ? new ArrayList<Method>() //
			: methods(c.getSuperclass(), andMods, orMods, noMods);
		int supN = s.size();
		M: for (Method m: c.getDeclaredMethods())
			if (Mod2.match(m, andMods, orMods, noMods))
			{
				if ( !Mod2.match(m, Mod2.STATIC))
					for (int i = 0; i < supN; i++)
						if (override(s.get(i), m))
						{
							s.set(i, m);
							continue M;
						}
				s.add(m);
			}
		return s;
	}

	public static boolean override(Method a, Method b)
	{
		return a.getName().equals(b.getName())
			&& Arrays.equals(a.getParameterTypes(), b.getParameterTypes());
	}

	/** excludes {@link Mod2.P#CINIT} */
	public static <T>Constructor<T> ctor(Class<T> c, Class<?>... params)
	{
		try
		{
			return c.getConstructor(params != null ? params : Array2.CLASSES0);
		}
		catch (NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
	}

	/** excludes {@link Mod2.P#CINIT} */
	public static <T>Constructor<T> declaredCtor(Class<T> c, Class<?>... params)
	{
		try
		{
			return c.getDeclaredConstructor(params != null ? params : Array2.CLASSES0);
		}
		catch (NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static <T extends AccessibleObject>T accessible(final T o)
	{
		return AccessController.doPrivileged(new PrivilegedAction<T>()
		{
			@Override
			public T run()
			{
				o.setAccessible(true);
				return o;
			}
		});
	}

	// ********************************************************************************

	public static Bytes classFile(Class<?> c) throws IOException
	{
		return new Bytes(c.getResourceAsStream(Class2.resourceName(c)), true);
	}

	public static final Method DEFINE_CLASS = accessible(declaredMethod(ClassLoader.class,
		"defineClass", String.class, byte[].class, int.class, int.class));

	@SuppressWarnings("unchecked")
	public static final <T>Class<T> load(ClassLoader l, String name, final byte[] bytecode,
		final int begin, final int end1) throws Exception
	{
		try
		{
			l.loadClass(name);
			throw new Exception("duplicate class " + name);
		}
		catch (ClassNotFoundException e)
		{
		}
		try
		{
			return (Class<T>)DEFINE_CLASS.invoke(l, name, bytecode, begin, end1 - begin);
		}
		catch (InvocationTargetException e)
		{
			throw exception(e.getCause());
		}
	}

	public static final <T>Class<T> load(ClassLoader l, String name, byte[] bytecode)
		throws Exception
	{
		return load(l, name, bytecode, 0, bytecode.length);
	}

	public static final <T>Class<T> load(ClassLoader l, String name, Bytes bytecode)
		throws Exception
	{
		return load(l, name, bytecode.bytes, bytecode.beginBi, bytecode.end1Bi);
	}

	/**
	 * get all classes in a package
	 *
	 * @param c one of classes in the package
	 */
	public static ArrayList<Class<?>> packageClasses(Class<?> c) throws Exception
	{
		if (c.isPrimitive() || c.isArray())
			throw new Exception("invalid class " + c);
		String p = c.getPackage().getName();
		// find the package directory
		URL url = c.getResource("/" + packageName(c).replace('.', '/') + "/");

		ArrayList<Class<?>> clas = new ArrayList<Class<?>>();

		if (url.getProtocol().equals("file"))
		{
			File path = new File(url.getPath());
			if ( !path.isDirectory())
				throw new Exception(path + " must be directory");
			// iterate on all classes in the package
			for (String _: path.list())
				if (_.endsWith(".class"))
					clas.add(Class.forName(p + "." + _.substring(0, _.lastIndexOf('.'))));
		}
		else if (url.getProtocol().equals("jar"))
		{
			JarURLConnection conn = (JarURLConnection)url.openConnection();
			JarEntry path = conn.getJarEntry();
			if ( !path.isDirectory())
				throw new Exception(path + " must be directory");
			String name;
			// iterate on all classes in the package
			for (Enumeration<JarEntry> es = conn.getJarFile().entries(); es.hasMoreElements();)
			{
				name = es.nextElement().getName();
				if (name.startsWith(path.getName()) && name.endsWith(".class"))
				{
					name = name.substring(path.getName().length(), name.lastIndexOf('.'));
					if (name.indexOf('/') < 0)
						clas.add(Class.forName(p + "." + name));
				}
			}
		}
		else
			throw new Exception(url.getProtocol() + " not supported yet");
		return clas;
	}

	// ********************************************************************************

	public static final Exception exception(Throwable e) throws Error
	{
		if (e instanceof Error)
			throw (Error)e;
		return e instanceof Exception ? (Exception)e : new Exception(e);
	}

	public static final RuntimeException runtimeException(Throwable e) throws Error
	{
		if (e instanceof Error)
			throw (Error)e;
		return e instanceof RuntimeException ? (RuntimeException)e : new RuntimeException(e);
	}

	// ********************************************************************************

	@SuppressWarnings("unchecked")
	public static final Annotation annoExclusive(AnnotatedElement o, Class<?>... cs)
	{
		Annotation a0 = null;
		for (Annotation a: o.getDeclaredAnnotations())
			for (Class<?> c: cs)
				if (a.annotationType() == c)
					if (a0 != null || (a0 = a) == null)
						throw new RuntimeException(o + ": annotation " + c.getName()
							+ " and " + a0.annotationType().getName() + " are exclusive");
		Annotation a;
		if (a0 == null)
			for (Class c: cs)
				if (c.isAnnotation() && (a = o.getAnnotation(c)) != null)
					if (a0 != null || (a0 = a) == null)
						throw new RuntimeException(o + ": annotation " + c.getName()
							+ " and " + a0.annotationType().getName() + " are exclusive");
		return a0;
	}

	public static final Annotation annoExclusive(AnnotatedElement o, Class<?> outC)
	{
		return annoExclusive(o, outC.getDeclaredClasses());
	}

	public static final <O extends AnnotatedElement>O annoExclusive(O o)
	{
		HashMap<Class<?>, Class<?>> cos = new HashMap<Class<?>, Class<?>>();
		for (Annotation a: o.getDeclaredAnnotations())
		{
			Class<?> c = a.annotationType();
			Class<?> co = c.getDeclaringClass();
			if (co != null && (co = cos.put(co, c)) != null)
				throw new RuntimeException(o + ": annotation " + co.getName() + " and "
					+ c.getName() + " are exclusive");
		}
		for (Annotation a: o.getAnnotations())
		{
			Class<?> c = a.annotationType();
			Class<?> co = c.getDeclaringClass();
			if (co != null && !cos.containsKey(co) && (co = cos.put(co, c)) != null)
				throw new RuntimeException(o + ": annotation " + co.getName() + " and "
					+ c.getName() + " are exclusive");
		}
		return o;
	}
}
