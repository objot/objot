//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.MappedSuperclass;

import objot.GetSet;


@MappedSuperclass
public abstract class IdAuto<T extends IdAuto<T>>
	extends Id<T>
{
	@GetSet(Object.class)
	@javax.persistence.Id
	@GeneratedValue(strategy = GenerationType.IDENTITY /* start from 1 */)
	public Integer id; // use Integer just for less object creation

	/** @return 0 if {@link #id} == null, or (int){@link #id} */
	@Override
	public int id()
	{
		return id == null ? 0 : id;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T id(int id_)
	{
		id = id_ == 0 ? null : id_;
		return (T)this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T id(Integer id_)
	{
		id = id_ == null || (int)id_ == 0 ? null : id_;
		return (T)this;
	}
}
