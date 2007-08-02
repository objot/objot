//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;
import static com.google.inject.matcher.Matchers.not;

import objot.servlet.Service;

import org.hibernate.SessionFactory;

import chat.service.Do;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.ServletScopes;


public class Services
{
	public static Injector init(final SessionFactory data) throws Exception
	{
		return Guice.createInjector(new AbstractModule()
		{
			@Override
			protected void configure()
			{
				bindScope(ScopeSession.class, ServletScopes.SESSION);
				bindScope(ScopeRequest.class, ServletScopes.REQUEST);

				bindInterceptor(any(), annotatedWith(Service.class).and(
					not(annotatedWith(Sign.Any.class))), //
					new Sign.Aspect());

				bindInterceptor(any(), annotatedWith(Service.class).and(
					annotatedWith(Transac.Readonly.class)).and(
					annotatedWith(Transac.Serial.class)), //
					new Transac.Aspect(data, true, false, true));
				bindInterceptor(any(), annotatedWith(Service.class).and(
					annotatedWith(Transac.Serial.class)), //
					new Transac.Aspect(data, false, false, true));

				bindInterceptor(any(), annotatedWith(Service.class).and(
					annotatedWith(Transac.Readonly.class)).and(
					annotatedWith(Transac.Commit.class)).and(
					not(annotatedWith(Transac.Repeat.class))).and(
					not(annotatedWith(Transac.Serial.class))), //
					new Transac.Aspect(data, true, true, false));
				bindInterceptor(any(), annotatedWith(Service.class).and(
					annotatedWith(Transac.Commit.class)).and(
					not(annotatedWith(Transac.Repeat.class))).and(
					not(annotatedWith(Transac.Serial.class))), //
					new Transac.Aspect(data, false, true, false));

				bindInterceptor(any(), annotatedWith(Service.class).and(
					annotatedWith(Transac.Readonly.class)).and(
					not(annotatedWith(Transac.Commit.class))).and(
					not(annotatedWith(Transac.Serial.class))), //
					new Transac.Aspect(data, true, false, false));
				bindInterceptor(any(), annotatedWith(Service.class).and(
					not(annotatedWith(Transac.Any.class))).and(
					not(annotatedWith(Transac.Readonly.class))).and(
					not(annotatedWith(Transac.Commit.class))).and(
					not(annotatedWith(Transac.Serial.class))), //
					new Transac.Aspect(data, false, false, false));
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
