//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package chat.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.MappedSuperclass;

import objot.codec.Dec;
import objot.codec.Enc;


@MappedSuperclass
public abstract class IdAutoBean<T extends IdAutoBean<T>>
	extends Id<T>
{
	private int id; // use Integer just for less object creation

	@Enc
	@javax.persistence.Id
	@GeneratedValue(strategy = GenerationType.AUTO /* start from 1 */)
	public int getId()
	{
		return id;
	}

	@Dec
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
