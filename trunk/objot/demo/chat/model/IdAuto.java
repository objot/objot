//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package chat.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.MappedSuperclass;

import objot.codec.EncDec;


@MappedSuperclass
public abstract class IdAuto<T extends IdAuto<T>>
	extends Id<T>
{
	@EncDec
	@javax.persistence.Id
	@GeneratedValue(strategy = GenerationType.AUTO /* start from 1 */)
	public int id;

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
