//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat.model;

import java.sql.Clob;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import objot.codec.Get;
import objot.codec.GetSet;
import objot.codec.Name;
import objot.codec.Set;

import org.hibernate.annotations.Proxy;
import org.hibernate.validator.NotNull;

import chat.BeText;
import chat.service.DoChat;


@Entity
@Proxy(lazy = false /* prevent from Hibernate proxy and unexpected behavior */)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "out", "in_", "datime" }))
@GetSet(DoChat.class)
public class Chat
	extends IdAuto<Chat>
{
	@NotNull
	@GetSet
	@ManyToOne
	public User out;

	@NotNull
	@GetSet
	@Name("In")
	@ManyToOne
	@JoinColumn(name = "in_")
	public User in;

	@NotNull
	@GetSet
	@Temporal(TemporalType.TIMESTAMP)
	public Date datime;

	@BeText(max = 1024, simple = false)
	public Clob text;

	@Get
	public Clob getText()
	{
		return text;
	}

	@Set
	public void setText(Clob _)
	{
		text = _;
	}
}
