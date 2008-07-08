//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Library General Public License version 2
//
package chat.model;

import java.sql.Blob;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import objot.codec.Name;

import org.hibernate.annotations.Proxy;
import org.hibernate.validator.NotNull;

import chat.BeText;


@Entity
@Proxy(lazy = false /* prevent from Hibernate proxy and unexpected behavior */)
public class Smiley
	extends IdAuto<Smiley>
{
	@NotNull
	@Name("In")
	@ManyToOne
	@JoinColumn(name = "in_")
	public User in;

	public Blob image;

	@BeText(min = 0, max = 30)
	public String type;
}
