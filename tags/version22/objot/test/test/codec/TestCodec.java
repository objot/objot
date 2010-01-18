package test.codec;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import objot.codec.Codec;

import org.junit.Assert;
import org.junit.Test;


public class TestCodec
	extends Assert
{
	Codec codec = new Codec();
	A x;
	B y;
	Object[] z;

	{
		x = new A();
		x.a2 = "\n\\20sss\tasd$@#%$%3423qwerty";
		x.d = new Date(54321);
		y = new B();
		y.a1 = Integer.MIN_VALUE / 2000;
		y.a2 = true;
		y.a3 = 34e-5f;
		y.a4 = null;
		y.a5 = Integer.MAX_VALUE / 2000 + 1L;
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
		y.a10 = 23e+100;
		z = new Object[] { y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y,
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
			y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y, y };
		z[0] = z;
	}

	@Test
	public void single() throws Exception
	{
		Date d = new Date();
		CharSequence s = codec.enc(new Date(), null);
		assertEquals(d, codec.dec(s.toString().toCharArray(), null, null));
	}

	@Test
	public void normal() throws Exception
	{
		CharSequence s = codec.enc(z, null);
		Object o = codec.dec(s.toString().toCharArray(), null, null);
		CharSequence s2 = codec.enc(o, Object.class);
		if (s.length() != s2.length())
			fail("length error: " + s.length() + " " + s2.length());
		for (int i = 0; i < s.length(); i++)
			if (s.charAt(i) != s2.charAt(i))
				fail("data error at " + i + ": " + s.charAt(i) + " " + s2.charAt(i));
	}

	@Test
	public void fast() throws Exception
	{
		CharSequence s = codec.encFast(z, null);
		Object o = codec.decFast(s.toString().toCharArray(), null, null);
		CharSequence s2 = codec.encFast(o, Object.class);
		if (s.length() != s2.length())
			fail("length error: " + s.length() + " " + s2.length());
		for (int i = 0; i < s.length(); i++)
			if (s.charAt(i) != s2.charAt(i))
				fail("data error at " + i + ": " + s.charAt(i) + " " + s2.charAt(i));
	}
}
