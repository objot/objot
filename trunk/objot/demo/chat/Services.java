//
// Copyright 2007-2008 Qianyan Cai
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
	public static Container build(final Codec codec, final SessionFactory hib)
		throws Exception
	{
		final Weaver w = new Weaver(Sign.As.class, Transac.As.class, EncAs.class,
			ByteAs.class)
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
					return new Transac.Config(m);
				if (m.getReturnType() == byte[].class
					|| InputStream.class.isAssignableFrom(m.getReturnType()))
					return ac == ByteAs.class ? null : this;
				if (codec != null && ac == EncAs.class)
					return m.getReturnType() == void.class ? v : null;
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
				return c.isSynthetic() ? b : b.cla(w.weave(c));
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

	static final class ByteAs
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
}
