//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.MappedSuperclass;

import objot.Get;
import objot.Set;


@MappedSuperclass
public abstract class IdAutoBean<T extends IdAutoBean<T>>
	extends Id<T>
{
	private int id; // use Integer just for less object creation

	@Get
	@javax.persistence.Id
	@GeneratedValue(strategy = GenerationType.IDENTITY /* start from 1 */)
	public int getId()
	{
		return id;
	}

	@Set
	public void setId(int v)
	{
		id = v;
	}

	/** {@inheritDoc} */
	@Override
	public int id()
	{
		return id;
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	public T id(int id_)
	{
		id = id_;
		return (T)this;
	}
}
