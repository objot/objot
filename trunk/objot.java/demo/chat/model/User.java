//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat.model;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import objot.Get;
import objot.GetSet;
import objot.NameGet;
import objot.Set;
import chat.service.DoChat;
import chat.service.DoSign;
import chat.service.DoUser;


/** be final to prevent from Hibernate proxy and unexpected behavior */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public final class User
	extends IdAuto<User>
{
	@BeSimple(max = 20)
	@Get( { DoUser.class, DoChat.class })
	@Set( { DoSign.class, DoUser.class })
	public String name;

	@BeSimple(max = 20)
	@Set(DoSign.class)
	public String password;

	@ManyToMany
	public java.util.Set<User> friends;

	@GetSet(DoUser.class)
	@NameGet("friends")
	@Transient
	public java.util.Set<User> friends_;
}
