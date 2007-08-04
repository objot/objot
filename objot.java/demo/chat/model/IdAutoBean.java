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
	private Integer id; // use Integer just for less object creation

	@Get
	@javax.persistence.Id
	@GeneratedValue(strategy = GenerationType.IDENTITY /* start from 1 */)
	public Integer getId()
	{
		return id;
	}

	@Set
	public void setId(Integer v)
	{
		id = v;
	}

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
