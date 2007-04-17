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
public abstract class Id
{
	@GetSet(Object.class)
	@javax.persistence.Id
	@GeneratedValue(strategy = GenerationType.IDENTITY /* start from 1 */)
	public Integer id; // use Integer just for less object creation

	/** @return 0 if {@link #id} == null, or (int){@link #id} */
	public int id()
	{
		return id == null ? 0 : id;
	}
}
