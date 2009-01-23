//
// Copyright 2007-2009 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package chat;

import java.io.InputStream;
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
	public static Container build(final Codec codec, final SessionFactory hib,
		final boolean test) throws Exception
	{
		final Weaver w = new Weaver(Sign.As.class, Transac.As.class, EncAs.class,
			ResultAs.class)
		{
			CharSequence v = codec != null ? codec.enc(true, null) : null;

			@Override
			protected Object forWeave(Class<? extends Aspect> ac, Method m) throws Exception
			{
				if ( !m.isAnnotationPresent(Service.class))
					return this;
				if (ac == Sign.As.class)
					return m.isAnnotationPresent(Sign.Any.class) ? this : null;
				if (ac == Transac.As.class)
					return m.isAnnotationPresent(Transac.Any.class) ? this
						: new Transac.Config(m);
				if (codec == null || m.getReturnType() == byte[].class
					|| InputStream.class.isAssignableFrom(m.getReturnType()))
					return ac == ResultAs.class ? null : this;
				if (ac == EncAs.class)
					return m.getReturnType() == void.class ? v : null;
				return this;
			}
		};
		final Weaver testW = new Weaver(TestAs.class)
		{
			@Override
			protected Object forWeave(Class<? extends Aspect> ac, Method m) throws Exception
			{
				if (m.getDeclaringClass().isSynthetic()
					&& Mod2.match(m, Mod2.P.OBJECT, Mod2.PUBLIC_PROTECT, Mod2.FINAL))
					return null;
				return this;
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
			protected Object forBind(Class<?> c, Bind b) throws Exception
			{
				return c == SessionFactory.class ? b.obj(hib) : b;
			}
		}.create(null);
		Factory req = new Factory()
		{
			@Override
			protected Object forBind(Class<?> c, Bind b) throws Exception
			{
				if (sess.bound(c))
					return b.mode(Inject.Parent.class);
				if (c == Codec.class)
					return b.obj(codec);
				if (c.isSynthetic())
					return b;
				return b.cla(test ? testW.weave(w.weave(c)) : w.weave(c));
			}
		}.bind(Codec.class);
		for (Class<?> c: Class2.packageClasses(Do.class))
			if (Mod2.match(c, Mod2.PUBLIC, Mod2.ABSTRACT))
				req.bind(c);
		return req.create(sess, true);
	}

	static final class EncAs
		extends Aspect
	{
		@Inject
		public Data data;
		@Inject
		public Codec codec;

		@Override
		protected void aspect() throws Throwable
		{
			boolean in = data.result != null;
			data.result = this;
			Target.invoke();
			if (in)
				return;
			CharSequence v = Target.data();
			data.result = v != null ? v : codec.enc(Target.getReturn(), Target.clazz());
		}
	}

	static final class ResultAs
		extends Aspect
	{
		@Inject
		public Data data;

		@Override
		protected void aspect() throws Throwable
		{
			boolean in = data.result != null;
			data.result = this;
			Target.invoke();
			if (in)
				return;
			data.result = Target.getReturn();
		}
	}

	static final class TestAs
		extends Aspect
	{
		@Inject
		public Data data;

		@Override
		protected void aspect() throws Throwable
		{
			data.result = null;
			data.rollbackOnly = false;
			Target.invoke();
		}
	}
}
