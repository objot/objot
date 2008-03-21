//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU General Public License version 2
//
package chat.model;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import objot.codec.Enc;
import objot.codec.EncDec;
import objot.codec.NameEnc;
import objot.codec.Dec;

import org.hibernate.annotations.Proxy;

import chat.BeText;
import chat.service.DoChat;
import chat.service.DoSign;
import chat.service.DoUser;


@Entity
@Proxy(lazy = false /* prevent from Hibernate proxy and unexpected behavior */)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class User
	extends IdAuto<User>
{
	@BeText(max = 20)
	@Enc( { DoUser.class, DoChat.class })
	@Dec( { DoSign.class, DoUser.class })
	public String name;

	@BeText(max = 20)
	@Dec(DoSign.class)
	public String password;

	@ManyToMany
	public java.util.Set<User> friends;

	@EncDec(DoUser.class)
	@NameEnc("friends")
	@Transient
	public java.util.Set<User> friends_;
}
