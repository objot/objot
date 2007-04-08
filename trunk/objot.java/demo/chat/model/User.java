//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import objot.Get;
import objot.GetSet;
import objot.NameGet;
import objot.Set;

import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import chat.service.DoChat;
import chat.service.DoSign;
import chat.service.DoUser;


/** be final to prevent from Hibernate proxy and unexpected behavior */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public final class User
{
	public static final ClassValidator<User> V = new ClassValidator<User>(User.class);

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@GetSet(Object.class)
	public Integer id; // use Integer just for less object creation

	@NotNull
	@Length(max = 20)
	@Get( { DoUser.class, DoChat.class })
	@Set( { DoSign.class, DoUser.class })
	public String name;

	@NotNull
	@Length(max = 20)
	@Set(DoSign.class)
	public String password;

	@ManyToMany
	public java.util.Set<User> friends;

	@Transient
	@GetSet(DoUser.class)
	@NameGet("friends")
	public java.util.Set<User> myFriends;
}
