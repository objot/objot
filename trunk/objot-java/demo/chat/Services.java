//
// Copyright 2007 Qianyan Cai
// Under the terms of the GNU General Public License version 2
//
package chat;

import java.lang.reflect.Method;

import objot.aspect.Aspect;
import objot.aspect.Weaver;
import objot.codec.Codec;
import objot.container.Bind;
import objot.container.Container;
import objot.container.Factory;
import objot.container.Inject;
import objot.util.Class2;
import objot.util.Mod2;

import org.hibernate.SessionFactory;

import chat.service.Data;
import chat.service.Do;
import chat.service.Session;
import chat.service.Do.Service;


public class Services
{
	/** @return container of services which parent is for session */
	public static Container build(final Codec codec, final SessionFactory hib)
		throws Exception
	{
		final Weaver w = new Weaver(Sign.As.class, Transac.As.class, EncAs.class)
		{
			@Override
			protected Object doWeave(Class<? extends Aspect> ac, Method m) throws Exception
			{
				if ( !m.isAnnotationPresent(Service.class))
					return this;
				if (ac == Sign.As.class)
					return m.isAnnotationPresent(Sign.Any.class) ? this : null;
				if (ac == Transac.As.class)
					return new Transac.Config(m);
				return codec == null ? this : codec;
			}
		};
		final Container sess = new Factory()
		{
			{
				for (Class<?> c: Class2.packageClasses(Do.class))
					if (Session.class.isAssignableFrom(c))
						bind(c);
				bind(SessionFactory.class);
			}

			@Override
			protected Object doBind(Class<?> c, Bind b) throws Exception
			{
				return c == SessionFactory.class ? b.obj(hib) : b;
			}
		}.create(null);
		Factory f = new Factory(Codec.class)
		{
			@Override
			protected Object doBind(Class<?> c, Bind b) throws Exception
			{
				if (sess.bound(c))
					return b.mode(null);
				if (c == Codec.class)
					return b.obj(codec);
				return c.isSynthetic() ? b : b.cla(w.weave(c));
			}
		};
		for (Class<?> c: Class2.packageClasses(Do.class))
			if ( !Mod2.match(c, Mod2.ABSTRACT))
				f.bind(c);
		return f.create(sess, false);
	}

	static final class EncAs
		extends Aspect
	{
		@Inject
		public Codec codec;
		@Inject
		public Data data;

		@Override
		protected void aspect() throws Throwable
		{
			Target.invoke();
			if (Target.<Do>getThis().data.depth == 1
				&& Target.<Void>getReturnClass() != void.class)
				data.enc = codec.enc(Target.getReturn(), Target.getClazz());
		}
	}
}
