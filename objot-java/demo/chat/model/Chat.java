//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Library General Public License version 2
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

import objot.codec.Enc;
import objot.codec.EncDec;
import objot.codec.Name;
import objot.codec.Dec;

import org.hibernate.annotations.Proxy;
import org.hibernate.validator.NotNull;

import chat.BeText;
import chat.service.DoChat;


@Entity
@Proxy(lazy = false /* prevent from Hibernate proxy and unexpected behavior */)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "out", "in_", "datime" }))
@EncDec(DoChat.class)
public class Chat
	extends IdAuto<Chat>
{
	@NotNull
	@EncDec
	@ManyToOne
	public User out;

	@NotNull
	@EncDec
	@Name("In")
	@ManyToOne
	@JoinColumn(name = "in_")
	public User in;

	@NotNull
	@EncDec
	@Temporal(TemporalType.TIMESTAMP)
	public Date datime;

	@BeText(max = 1024, simple = false)
	public Clob text;

	@Enc
	public Clob getText()
	{
		return text;
	}

	@Dec
	public void text(Clob _)
	{
		text = _;
	}
}
