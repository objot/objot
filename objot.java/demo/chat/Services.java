//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import objot.aspect.Aspect;
import objot.aspect.Weaver;
import objot.container.Container;
import objot.container.Factory;
import objot.util.Class2;
import objot.util.Mod2;

import org.hibernate.SessionFactory;

import chat.service.Do;
import chat.service.Do.Service;


public class Services
{
	/** @param subRequest sequent sub requests in a request */
	public static Container build(final SessionFactory d, final boolean subRequest)
		throws Exception
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
				Transac.Config c = Transac.Config.config(t, subRequest, d);
				return c != null ? c : this;
			}
		};
		Factory f = new Factory()
		{
			@Override
			protected Object doBind(Class<?> c) throws Exception
			{
				return bind(c.isSynthetic() ? c : w.weave(c));
			}
		};
		for (Class<?> c: Class2.packageClasses(Do.class))
			if ( !Mod2.match(c, Mod2.ABSTRACT))
				f.bind(c);
		return f.createOutest(null);
	}
}
