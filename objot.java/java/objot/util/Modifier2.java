package objot.util;

import java.lang.reflect.Modifier;


public class Modifier2
	extends Modifier
{
	public Modifier2()
	{
		throw new AbstractMethodError();
	}

	public static final int SUPER = 0x0020;
	public static final int SYNTHETIC = 0x1000;
	public static final int ANNOTATION = 0x2000;
	public static final int ENUM = 0x4000;

	public static final int BRIDGE = 0x0040;
	public static final int VARARGS = 0x0080;
}
