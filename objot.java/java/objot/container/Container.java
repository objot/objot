//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.container;

public abstract class Container
{
	public abstract <T>T get(Class<T> c);

	public static Container create(Binder b) throws Exception
	{

	}
}
