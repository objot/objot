//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat.service;

import java.util.Date;
import java.util.List;

import objot.servlet.Service;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import chat.model.Chat;
import chat.model.User;


public class DoChat
	extends Do
{
	/**
	 * read chats according to SO' {@link Chat#out} and {@link Chat#in} from
	 * {@link Chat#datime}(excluded), order by {@link Chat#datime} asc
	 */
	@Service
	public static List<Chat> read(Chat c, Do $) throws Exception
	{
		Criteria<Chat> _ = $.criteria(Chat.class);

		Criterion out = Restrictions.eq("out", $.me);
		if (c.in != null)
			out = Restrictions.and(out, Restrictions.eq("in", c.in));
		Criterion in = Restrictions.eq("in", $.me);
		if (c.out != null)
			in = Restrictions.and(in, Restrictions.eq("out", c.out));

		_.add(Restrictions.or(out, in)).add(Restrictions.gt("datime", c.datime));
		return _.addOrder(Order.asc("datime")).list();
	}

	/** @return with {@link Chat#datime} */
	@Service
	public static Chat post(Chat c, Do $) throws Exception
	{
		c.out = $.me;
		c.in = $.get(User.class, c.in.id);
		validator(c);

		// List/Set.contains causes fetch rows
		// if (! c.in.friends.contains($.get(User.class, c.out.id)))
		// so count(*)
		Criteria<?> _ = $.criteria(User.class);
		_.setProjection(Projections.rowCount());
		_.add(Restrictions.idEq(c.in.id));
		_.createCriteria("friends").add(Restrictions.idEq(c.out.id));
		if ((Integer)_.uniqueResult() == 0)
			throw err("You must be his/her friend");
		c.datime = new Date();
		$.save(c);
		return c;
	}
}
