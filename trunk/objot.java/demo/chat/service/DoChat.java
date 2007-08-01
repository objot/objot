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
	public List<Chat> read(Chat c) throws Exception
	{
		Criteria<Chat> _ = data.criteria(Chat.class);

		User me = new User().id(sess.me);
		Criterion out = Restrictions.eq("out", me);
		if (c.in != null)
			out = Restrictions.and(out, Restrictions.eq("in", c.in));
		Criterion in = Restrictions.eq("in", me);
		if (c.out != null)
			in = Restrictions.and(in, Restrictions.eq("out", c.out));

		_.add(Restrictions.or(out, in)).add(Restrictions.gt("datime", c.datime));
		return _.addOrder(Order.asc("datime")).list();
	}

	/** @return with {@link Chat#datime} */
	@Service
	public Chat post(Chat c) throws Exception
	{
		c.out = new User().id(sess.me);
		validator(c);

		// List/Set.contains causes fetch rows
		// // if (! c.in.friends.contains($.load(c.out)))
		// so count(*)
		Criteria<?> _ = data.criteria(User.class).setProjection(Projections.rowCount());
		_.add(Restrictions.idEq(c.in.id));
		_.createCriteria("friends").add(Restrictions.idEq(c.out.id));
		if ((Integer)_.uniqueResult() == 0)
			throw err("You must be his/her friend");
		c.datime = new Date();
		data.save(c);
		return c;
	}
}
