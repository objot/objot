package test.codec;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import objot.codec.Codec;


public class Test1
{
	public static void main(String[] args) throws Exception
	{
		A x = new A();
		x.a2 = "x";
		x.d = new Date(54321);
		B y = new B();
		y.a1 = "\tasdasdf";
		y.a2 = true;
		y.a3 = 34e-5f;
		y.a4 = null;
		y.a5 = "\n\\20";
		y.a6 = new ArrayList<Object[]>();
		y.a6.add(new Object[] { y, y.a1, y.a2, y.a3, y.a4, y.a5, null, null });
		y.a6.get(0)[6] = y.a6;
		y.a6.get(0)[7] = y.a6.get(0);
		y.a7 = new HashMap<String, A>();
		y.a7.put("x", x);
		y.a7.put("xx", (A)y.a6.get(0)[0]);
		y.a8 = (Boolean)y.a2;
		y.a9 = new HashSet<String>();
		y.a9.add("unique1");
		y.a9.add("unique2");
		Object[] z = new Object[] { y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y,
			y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y,
			y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y,
			y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y,
			y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y,
			y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y,
			y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y,
			y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y,
			y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y,
			y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y,
			y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y,
			y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y,
			y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y,
			y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y,
			y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y,
			y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y,
			y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y,
			y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y,
			y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y };
		z[0] = z;

		Codec objot = new Codec();
		CharSequence s = objot.enc(z, Object.class);
		Object o = objot.dec(s.toString().toCharArray(), Object.class, Object.class);
		CharSequence s2 = objot.enc(o, Object.class);
		if (s.length() != s2.length())
			throw new Exception("length error: " + s.length() + " " + s2.length());
		for (int i = 0; i < s.length(); i++)
			if (s.charAt(i) != s2.charAt(i))
				throw new Exception("data error at " + i + ": " + s.charAt(i) + " "
					+ s2.charAt(i));
		System.out.println("ok");
	}
}
