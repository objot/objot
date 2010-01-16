//
// Copyright 2007-2010 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package chat.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import objot.util.Bytes;
import objot.util.Input;
import objot.util.String2;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import chat.Transac;
import chat.model.Chat;
import chat.model.Smiley;
import chat.model.User;


public class DoChat
	extends Do
{
	/**
	 * read chats according to SO' {@link Chat#out} and {@link Chat#in} from
	 * {@link Chat#datime}(excluded, or oldest if null), order by {@link Chat#datime} asc
	 */
	@Service
	@Transac.Readonly
	public List<Chat> read(Chat c) throws Exception
	{
		Criteria<Chat> _ = data.criteria(Chat.class);
		User me = new User().id(sess.me);

		Criterion out = Restrictions.eq("out", me);
		if (c.in != null)
			out = Restrictions.and(out, // chats from me
				Restrictions.eq("in", c.in));
		Criterion in = Restrictions.eq("in", me);
		if (c.out != null)
			in = Restrictions.and(in, // chats to me
				Restrictions.eq("out", c.out));
		_.add(Restrictions.or(out, in));
		if (c.datime != null)
			_.add(Restrictions.gt("datime", c.datime));
		return _.addOrder(Order.asc("datime")).list();
	}

	/**
	 * also persist SO
	 * 
	 * @return with {@link Chat#datime}
	 */
	@Service
	public Chat post(Chat c, Input.Upload smiley) throws Exception
	{
		c.out = new User().id(sess.me);
		validate(c);

		Criteria<?> _ = data.criteria(User.class).setProjection(Projections.rowCount());
		_.add(Restrictions.idEq(c.in.id));
		_.createCriteria("friends").add(Restrictions.idEq(c.out.id));
		if ((Integer)_.uniqueResult() == 0)
			throw err("You must be his/her friend");
		c.datime = new Date();
		while (smiley != null && smiley.available() > 0)
		{
			final Bytes b = new Bytes(smiley, false);
			final ByteArrayInputStream in = new ByteArrayInputStream(b.bytes, b.beginBi,
				b.byteN());
			Smiley s = new Smiley();
			s.in = c.in;
			s.image = new Blob()
			{
				public long length()
				{
					return b.byteN();
				}

				public void truncate(long pos)
				{
					throw new UnsupportedOperationException();
				}

				public byte[] getBytes(long pos, int len)
				{
					throw new UnsupportedOperationException();
				}

				public int setBytes(long pos, byte[] bytes)
				{
					throw new UnsupportedOperationException();
				}

				public int setBytes(long pos, byte[] bytes, int i, int j)
				{
					throw new UnsupportedOperationException();
				}

				public long position(byte[] bytes, long pos)
				{
					throw new UnsupportedOperationException();
				}

				public InputStream getBinaryStream()
				{
					in.reset();
					return in;
				}

				public OutputStream setBinaryStream(long pos)
				{
					throw new UnsupportedOperationException();
				}

				public long position(Blob blob, long pos)
				{
					throw new UnsupportedOperationException();
				}

				public void free()
				{
				}

				public InputStream getBinaryStream(long pos, long length)
				{
					throw new UnsupportedOperationException();
				}
			};
			s.type = smiley.type();
			if (c.smileys == null)
				c.smileys = new ArrayList<Smiley>();
			c.smileys.add(s);
			smiley.next();
		}
		data.save(c);
		return c;
	}

	@Service
	@Transac.Readonly
	public InputStream smiley(int id) throws Exception
	{
		Smiley s = data.criteria(Smiley.class).add(Restrictions.idEq(id)).add(
			Restrictions.eq("in.id", sess.me)).uniqueResult();
		if (s == null)
			throw err("smiley not found");
		respType = String2.maskEmpty(s.type, "image");
		return s.image.getBinaryStream();
	}
}
