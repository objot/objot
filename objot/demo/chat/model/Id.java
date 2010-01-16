//
// Copyright 2007-2010 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package chat.model;

public abstract class Id<T extends Id<T>>
{
	/** get identifier, the identifier property name must be "id" */
	public abstract int id();

	/**
	 * set identifier
	 * 
	 * @return self
	 */
	public T id(int id_)
	{
		throw new UnsupportedOperationException();
	}
}
