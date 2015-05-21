package objot.util;

import java.lang.reflect.Field;
import java.security.ProtectionDomain;
import java.util.concurrent.atomic.AtomicBoolean;


/** only for SUN Java, {@link #throwException} and {@link #defineClass} are useful */
@SuppressWarnings("all")
public final class Unsafe
{
	public static final sun.misc.Unsafe o;

	static
	{
		try
		{
			o = (sun.misc.Unsafe)Class2.accessible(
				Class2.declaredField(AtomicBoolean.class, "unsafe")).get(null);
		}
		catch (Exception e)
		{
			throw new ExceptionInInitializerError(e);
		}
	}

	private Unsafe()
	{
	}

	public static int addressSize()
	{
		return o.addressSize();
	}

	public static Object allocateInstance(Class arg0) throws InstantiationException
	{
		return o.allocateInstance(arg0);
	}

	public static long allocateMemory(long arg0)
	{
		return o.allocateMemory(arg0);
	}

	public static int arrayBaseOffset(Class arg0)
	{
		return o.arrayBaseOffset(arg0);
	}

	public static int arrayIndexScale(Class arg0)
	{
		return o.arrayIndexScale(arg0);
	}

	public static boolean compareAndSwapInt(Object arg0, long arg1, int arg2, int arg3)
	{
		return o.compareAndSwapInt(arg0, arg1, arg2, arg3);
	}

	public static boolean compareAndSwapLong(Object arg0, long arg1, long arg2, long arg3)
	{
		return o.compareAndSwapLong(arg0, arg1, arg2, arg3);
	}

	public static boolean compareAndSwapObject(Object arg0, long arg1, Object arg2,
		Object arg3)
	{
		return o.compareAndSwapObject(arg0, arg1, arg2, arg3);
	}

	public static void copyMemory(long arg0, long arg1, long arg2)
	{
		o.copyMemory(arg0, arg1, arg2);
	}

	public static Class defineClass(String arg0, byte[] arg1, int arg2, int arg3,
		ClassLoader arg4, ProtectionDomain arg5)
	{
		return o.defineClass(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	public static void ensureClassInitialized(Class arg0)
	{
		o.ensureClassInitialized(arg0);
	}

	public static void freeMemory(long arg0)
	{
		o.freeMemory(arg0);
	}

	public static long getAddress(long arg0)
	{
		return o.getAddress(arg0);
	}

	public static boolean getBoolean(Object arg0, long arg1)
	{
		return o.getBoolean(arg0, arg1);
	}

	public static boolean getBooleanVolatile(Object arg0, long arg1)
	{
		return o.getBooleanVolatile(arg0, arg1);
	}

	public static byte getByte(long arg0)
	{
		return o.getByte(arg0);
	}

	public static byte getByte(Object arg0, long arg1)
	{
		return o.getByte(arg0, arg1);
	}

	public static byte getByteVolatile(Object arg0, long arg1)
	{
		return o.getByteVolatile(arg0, arg1);
	}

	public static char getChar(long arg0)
	{
		return o.getChar(arg0);
	}

	public static char getChar(Object arg0, long arg1)
	{
		return o.getChar(arg0, arg1);
	}

	public static char getCharVolatile(Object arg0, long arg1)
	{
		return o.getCharVolatile(arg0, arg1);
	}

	public static double getDouble(long arg0)
	{
		return o.getDouble(arg0);
	}

	public static double getDouble(Object arg0, long arg1)
	{
		return o.getDouble(arg0, arg1);
	}

	public static double getDoubleVolatile(Object arg0, long arg1)
	{
		return o.getDoubleVolatile(arg0, arg1);
	}

	public static float getFloat(long arg0)
	{
		return o.getFloat(arg0);
	}

	public static float getFloat(Object arg0, long arg1)
	{
		return o.getFloat(arg0, arg1);
	}

	public static float getFloatVolatile(Object arg0, long arg1)
	{
		return o.getFloatVolatile(arg0, arg1);
	}

	public static int getInt(long arg0)
	{
		return o.getInt(arg0);
	}

	public static int getInt(Object arg0, long arg1)
	{
		return o.getInt(arg0, arg1);
	}

	public static int getIntVolatile(Object arg0, long arg1)
	{
		return o.getIntVolatile(arg0, arg1);
	}

	public static int getLoadAverage(double[] arg0, int arg1)
	{
		return o.getLoadAverage(arg0, arg1);
	}

	public static long getLong(long arg0)
	{
		return o.getLong(arg0);
	}

	public static long getLong(Object arg0, long arg1)
	{
		return o.getLong(arg0, arg1);
	}

	public static long getLongVolatile(Object arg0, long arg1)
	{
		return o.getLongVolatile(arg0, arg1);
	}

	public static Object getObject(Object arg0, long arg1)
	{
		return o.getObject(arg0, arg1);
	}

	public static Object getObjectVolatile(Object arg0, long arg1)
	{
		return o.getObjectVolatile(arg0, arg1);
	}

	public static short getShort(long arg0)
	{
		return o.getShort(arg0);
	}

	public static short getShort(Object arg0, long arg1)
	{
		return o.getShort(arg0, arg1);
	}

	public static short getShortVolatile(Object arg0, long arg1)
	{
		return o.getShortVolatile(arg0, arg1);
	}

	public static void monitorEnter(Object arg0)
	{
		o.monitorEnter(arg0);
	}

	public static void monitorExit(Object arg0)
	{
		o.monitorExit(arg0);
	}

	public static long objectFieldOffset(Field arg0)
	{
		return o.objectFieldOffset(arg0);
	}

	public static int pageSize()
	{
		return o.pageSize();
	}

	public static void park(boolean arg0, long arg1)
	{
		o.park(arg0, arg1);
	}

	public static void putAddress(long arg0, long arg1)
	{
		o.putAddress(arg0, arg1);
	}

	public static void putBoolean(Object arg0, long arg1, boolean arg2)
	{
		o.putBoolean(arg0, arg1, arg2);
	}

	public static void putBooleanVolatile(Object arg0, long arg1, boolean arg2)
	{
		o.putBooleanVolatile(arg0, arg1, arg2);
	}

	public static void putByte(long arg0, byte arg1)
	{
		o.putByte(arg0, arg1);
	}

	public static void putByte(Object arg0, long arg1, byte arg2)
	{
		o.putByte(arg0, arg1, arg2);
	}

	public static void putByteVolatile(Object arg0, long arg1, byte arg2)
	{
		o.putByteVolatile(arg0, arg1, arg2);
	}

	public static void putChar(long arg0, char arg1)
	{
		o.putChar(arg0, arg1);
	}

	public static void putChar(Object arg0, long arg1, char arg2)
	{
		o.putChar(arg0, arg1, arg2);
	}

	public static void putCharVolatile(Object arg0, long arg1, char arg2)
	{
		o.putCharVolatile(arg0, arg1, arg2);
	}

	public static void putDouble(long arg0, double arg1)
	{
		o.putDouble(arg0, arg1);
	}

	public static void putDouble(Object arg0, long arg1, double arg2)
	{
		o.putDouble(arg0, arg1, arg2);
	}

	public static void putDoubleVolatile(Object arg0, long arg1, double arg2)
	{
		o.putDoubleVolatile(arg0, arg1, arg2);
	}

	public static void putFloat(long arg0, float arg1)
	{
		o.putFloat(arg0, arg1);
	}

	public static void putFloat(Object arg0, long arg1, float arg2)
	{
		o.putFloat(arg0, arg1, arg2);
	}

	public static void putFloatVolatile(Object arg0, long arg1, float arg2)
	{
		o.putFloatVolatile(arg0, arg1, arg2);
	}

	public static void putInt(long arg0, int arg1)
	{
		o.putInt(arg0, arg1);
	}

	public static void putInt(Object arg0, long arg1, int arg2)
	{
		o.putInt(arg0, arg1, arg2);
	}

	public static void putIntVolatile(Object arg0, long arg1, int arg2)
	{
		o.putIntVolatile(arg0, arg1, arg2);
	}

	public static void putLong(long arg0, long arg1)
	{
		o.putLong(arg0, arg1);
	}

	public static void putLong(Object arg0, long arg1, long arg2)
	{
		o.putLong(arg0, arg1, arg2);
	}

	public static void putLongVolatile(Object arg0, long arg1, long arg2)
	{
		o.putLongVolatile(arg0, arg1, arg2);
	}

	public static void putObject(Object arg0, long arg1, Object arg2)
	{
		o.putObject(arg0, arg1, arg2);
	}

	public static void putObjectVolatile(Object arg0, long arg1, Object arg2)
	{
		o.putObjectVolatile(arg0, arg1, arg2);
	}

	public static void putOrderedInt(Object arg0, long arg1, int arg2)
	{
		o.putOrderedInt(arg0, arg1, arg2);
	}

	public static void putOrderedLong(Object arg0, long arg1, long arg2)
	{
		o.putOrderedLong(arg0, arg1, arg2);
	}

	public static void putOrderedObject(Object arg0, long arg1, Object arg2)
	{
		o.putOrderedObject(arg0, arg1, arg2);
	}

	public static void putShort(long arg0, short arg1)
	{
		o.putShort(arg0, arg1);
	}

	public static void putShort(Object arg0, long arg1, short arg2)
	{
		o.putShort(arg0, arg1, arg2);
	}

	public static void putShortVolatile(Object arg0, long arg1, short arg2)
	{
		o.putShortVolatile(arg0, arg1, arg2);
	}

	public static long reallocateMemory(long arg0, long arg1)
	{
		return o.reallocateMemory(arg0, arg1);
	}

	public static void setMemory(long arg0, long arg1, byte arg2)
	{
		o.setMemory(arg0, arg1, arg2);
	}

	public static Object staticFieldBase(Field arg0)
	{
		return o.staticFieldBase(arg0);
	}

	public static long staticFieldOffset(Field arg0)
	{
		return o.staticFieldOffset(arg0);
	}

	public static void throwException(Throwable arg0)
	{
		o.throwException(arg0);
	}

	public static boolean tryMonitorEnter(Object arg0)
	{
		return o.tryMonitorEnter(arg0);
	}

	public static void unpark(Object arg0)
	{
		o.unpark(arg0);
	}
}
