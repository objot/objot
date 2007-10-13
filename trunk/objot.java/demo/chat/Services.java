//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat;

import java.lang.reflect.Method;

import objot.aspect.Aspect;
import objot.aspect.Weaver;
import objot.codec.Codec;
import objot.codec.Err;
import objot.codec.Errs;
import objot.container.Bind;
import objot.container.Container;
import objot.container.Factory;
import objot.container.Inject;
import objot.util.Class2;
import objot.util.Mod2;

import org.hibernate.SessionFactory;

import chat.model.Id;
import chat.service.Do;
import chat.service.Session;
import chat.service.Do.Service;


public class Services
{
	public static final Codec CODEC = new Codec()
	{
		String modelPrefix = Class2.packageName(Id.class).concat(".");

		@Override
		protected Class<?> classByName(String name) throws Exception
		{
			return Class.forName(modelPrefix.concat(name));
		}

		/** include {@link Err} and {@link Errs} */
		@Override
		protected String className(Object o, Class<?> c) throws Exception
		{
			return c.getName().substring(c.getName().lastIndexOf('.') + 1);
		}
	};

	/** @return container of services which parent is for session */
	public static Container build(final SessionFactory d, final Codec codec) throws Exception
	{
		final Weaver w = new Weaver(Sign.As.class, Transac.As.class, As.class)
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
				return c == SessionFactory.class ? b.obj(d) : b;
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
			if ( !Mod2.match(c, Mod2.ABSTRACT) && !Session.class.isAssignableFrom(c))
				f.bind(c);
		return f.create(sess, false);
	}

	static final class As
		extends Aspect
	{
		@Inject
		public Codec codec;

		@Override
		protected void aspect() throws Throwable
		{
			Target.invoke();
			if (Target.<Do>getThis().data.deep == 1)
				codec.enc(Target.getReturn(), Target.getClazz());
		}
	}
}
