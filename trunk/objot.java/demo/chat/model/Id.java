//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat.model;

public abstract class Id<T extends Id<T>>
{
	/** @return 0 if id == null, or id */
	public abstract int id();

	public abstract T id(int id_);

	public abstract T id(Integer id_);
}
