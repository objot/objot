//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;
import static com.google.inject.matcher.Matchers.not;
import org.hibernate.SessionFactory;

import chat.service.Do;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;


public class Services
{
	/** @param subRequest sequent sub requests in a request */
	public static Injector init(final SessionFactory d, final boolean subRequest,
		final int verbose) throws Exception
	{
		return Guice.createInjector(new AbstractModule()
		{
			@Override
			protected void configure()
			{
				bindScope(Scope.Session.class, Scope.SESSION);
				bindScope(Scope.Request.class, Scope.REQUEST);

				bindInterceptor(any(), annotatedWith(Do.Service.class).and(
					not(annotatedWith(Sign.Any.class))), //
					new Sign.Aspect());

				bindInterceptor(any(), annotatedWith(Do.Service.class).and(
					annotatedWith(Transac.Readonly.class)).and(
					annotatedWith(Transac.Serial.class)), //
					new Transac.Aspect(true, false, true, subRequest, d, verbose));
				bindInterceptor(any(), annotatedWith(Do.Service.class).and(
					annotatedWith(Transac.Serial.class)), //
					new Transac.Aspect(false, false, true, subRequest, d, verbose));

				bindInterceptor(any(), annotatedWith(Do.Service.class).and(
					annotatedWith(Transac.Readonly.class)).and(
					annotatedWith(Transac.Commit.class)).and(
					not(annotatedWith(Transac.Repeat.class))).and(
					not(annotatedWith(Transac.Serial.class))), //
					new Transac.Aspect(true, true, false, subRequest, d, verbose));
				bindInterceptor(any(), annotatedWith(Do.Service.class).and(
					annotatedWith(Transac.Commit.class)).and(
					not(annotatedWith(Transac.Repeat.class))).and(
					not(annotatedWith(Transac.Serial.class))), //
					new Transac.Aspect(false, true, false, subRequest, d, verbose));

				bindInterceptor(any(), annotatedWith(Do.Service.class).and(
					annotatedWith(Transac.Readonly.class)).and(
					not(annotatedWith(Transac.Commit.class))).and(
					not(annotatedWith(Transac.Serial.class))), //
					new Transac.Aspect(true, false, false, subRequest, d, verbose));
				bindInterceptor(any(), annotatedWith(Do.Service.class).and(
					not(annotatedWith(Transac.Any.class))).and(
					not(annotatedWith(Transac.Readonly.class))).and(
					not(annotatedWith(Transac.Commit.class))).and(
					not(annotatedWith(Transac.Serial.class))), //
					new Transac.Aspect(false, false, false, subRequest, d, verbose));

				try
				{
					for (Class<?> c: Models.getPackageClasses(Do.class))
						bind(c);
				}
				catch (RuntimeException e)
				{
					throw e;
				}
				catch (Exception e)
				{
					throw new RuntimeException(e);
				}
			}
		});
	}
}
