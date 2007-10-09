//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import objot.aspect.Aspect;
import objot.aspect.Weaver;
import objot.container.Bind;
import objot.container.Container;
import objot.container.Factory;
import objot.util.Class2;
import objot.util.Mod2;

import org.hibernate.SessionFactory;

import chat.service.Do;
import chat.service.Session;
import chat.service.Do.Service;


public class Services
{
	/** @return container for service inside container for session */
	public static Container build(final SessionFactory d) throws Exception
	{
		final Weaver w = new Weaver(Sign.As.class, Transac.As.class)
		{
			@Override
			protected Object doWeave(Class<? extends Aspect> ac, Method m) throws Exception
			{
				if ( !m.isAnnotationPresent(Service.class))
					return this;
				if (ac == Sign.As.class)
					return m.isAnnotationPresent(Sign.Any.class) ? this : null;
				Annotation t = Class2.annoExclusive(m, Transac.class);
				Transac.Config c = Transac.Config.config(t);
				return c != null ? c : this;
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
		Factory f = new Factory()
		{
			@Override
			protected Object doBind(Class<?> c, Bind b) throws Exception
			{
				if (sess.bound(c))
					return b.mode(null);
				return b.cla(c.isSynthetic() ? b.cla : w.weave(c));
			}
		};
		for (Class<?> c: Class2.packageClasses(Do.class))
			if ( !Mod2.match(c, Mod2.ABSTRACT) && !Session.class.isAssignableFrom(c))
				f.bind(c);
		return f.create(sess);
	}
}
