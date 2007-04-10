//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import objot.GetSet;
import objot.Name;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

import chat.service.DoChat;


/** a chat message. I prefer "Chat" to "Message" just for less letters, am I lazy ? */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "out", "in_", "datime" }))
@GetSet(DoChat.class)
public final class Chat
{
	@Id
	@GeneratedValue
	protected Integer id; // just for simple identity strategy

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

	@NotEmpty
	@Length(max = 1024)
	@GetSet
	public String text;
}
