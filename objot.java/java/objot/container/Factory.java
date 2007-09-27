package objot.container;

import java.util.HashMap;


public class Factory
{
	final HashMap<Class<?>, Bind> bs;

	@SuppressWarnings("unchecked")
	public Factory(Binds b) throws Exception
	{
		synchronized (b)
		{
			bs = (HashMap<Class<?>, Bind>)b.binds.clone();
		}
	}

	public Container container()
	{
		return new Container(null, bs);
	}
}
