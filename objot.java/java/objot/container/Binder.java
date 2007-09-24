//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.container;

import java.util.HashMap;


public class Binder
{
	protected HashMap<Class<?>, Bound<?>> bs = new HashMap<Class<?>, Bound<?>>();

	@SuppressWarnings("unchecked")
	public <T>Bound<T> bind(Class<T> c) throws Exception
	{
		Bound<T> b = (Bound<T>)bs.get(c);
		if (b == null)
			bs.put(c, b = doBind(c));
		return b;
	}

	public void bind(Bound<?> b) throws Exception
	{
		if (bs.get(b.cla) != null)
			throw new Exception(b.cla + " already bound");
		bs.put(b.cla, b);
	}

	protected <T>Bound<T> doBind(Class<T> c) throws Exception
	{
		Bound<T> b = new Bound<T>();
		b.implement = b.cla = c;
		return b;
	}
}
